"""
ROTA IQ — TESTES AUTOMATIZADOS DO PAINEL ADMINISTRATIVO (BACKOFFICE REAL)
Testa Health Check, RBAC, CRUD de Motoristas, Planos, Feature Flags,
Configurações, Auditoria e Bloqueio de Acesso para Motoristas Comuns (403).
"""

import pytest
from fastapi.testclient import TestClient
from app.main import app

client = TestClient(app)

@pytest.fixture(scope="module")
def admin_token():
    """Autentica o super admin e retorna o token Bearer."""
    resp = client.post("/api/v1/admin/auth/login", json={
        "email": "admin@rotai.app",
        "password": "Admin@2026Secure",
        "role": "SUPER_ADMIN"
    })
    assert resp.status_code == 200, f"Falha no login do admin: {resp.text}"
    data = resp.json()
    assert "access_token" in data
    return data["access_token"]

@pytest.fixture(scope="module")
def driver_token():
    """Gera token de motorista comum para testar restrição RBAC (403)."""
    import uuid
    email = f"driver_rbac_{uuid.uuid4().hex[:6]}@teste.com"
    resp = client.post("/api/v1/auth/register", json={
        "email": email,
        "password": "DriverPassword123",
        "full_name": "Motorista Comum RBAC",
        "phone": f"119999{uuid.uuid4().hex[:4]}",
        "city": "São Paulo",
        "state": "SP"
    })
    assert resp.status_code == 200
    return resp.json()["access_token"]


def test_01_health_check_database_connected():
    """Valida o endpoint /health exigido para averiguação da conexão PostgreSQL."""
    resp = client.get("/health")
    assert resp.status_code == 200
    data = resp.json()
    assert data["database"] == "connected"
    assert data["status"] == "healthy"
    assert data["real_users_count"] >= 1
    assert "latency_ms" in data


def test_02_admin_login_invalid_password():
    """Garante que senhas incorretas retornam 401 e nunca criam token de teste."""
    resp = client.post("/api/v1/admin/auth/login", json={
        "email": "admin@rotai.app",
        "password": "WrongPassword999"
    })
    assert resp.status_code == 401


def test_03_rbac_driver_forbidden(driver_token):
    """Garante que motoristas comuns recebem 403 Forbidden ao tentar acessar rotas /admin."""
    headers = {"Authorization": f"Bearer {driver_token}"}
    
    dash_resp = client.get("/api/v1/admin/dashboard", headers=headers)
    assert dash_resp.status_code == 403

    plans_resp = client.get("/api/v1/admin/plans", headers=headers)
    assert plans_resp.status_code == 403

    settings_resp = client.get("/api/v1/admin/settings", headers=headers)
    assert settings_resp.status_code == 403


def test_04_admin_dashboard(admin_token):
    """Verifica dashboard com métricas reais agregadas do banco."""
    headers = {"Authorization": f"Bearer {admin_token}"}
    resp = client.get("/api/v1/admin/dashboard", headers=headers)
    assert resp.status_code == 200
    data = resp.json()
    kpis = data["kpis"]
    assert kpis["total_users"] >= 1
    assert kpis["total_drivers"] >= 1
    assert isinstance(kpis["mrr_reais"], (int, float))
    assert data["database_status"] == "POSTGRESQL_CONNECTED"


def test_05_admin_drivers_paged(admin_token):
    """Testa listagem paginada de motoristas."""
    headers = {"Authorization": f"Bearer {admin_token}"}
    resp = client.get("/api/v1/admin/drivers?paged=true&page=1&limit=10", headers=headers)
    assert resp.status_code == 200
    data = resp.json()
    assert "items" in data
    assert "total" in data
    assert isinstance(data["items"], list)
    assert len(data["items"]) >= 1


def test_06_admin_create_and_block_driver(admin_token):
    """Testa criação, edição, bloqueio e desbloqueio de motorista no banco."""
    import uuid
    headers = {"Authorization": f"Bearer {admin_token}"}
    new_email = f"driver_adm_{uuid.uuid4().hex[:6]}@rotai.com.br"

    # 1. Criar motorista
    dynamic_phone = f"119{uuid.uuid4().int % 100000000:08d}"
    create_resp = client.post("/api/v1/admin/drivers", headers=headers, json={
        "name": "Motorista E2E Backoffice",
        "email": new_email,
        "phone": dynamic_phone,
        "city": "Campinas",
        "state": "SP",
        "status": "ACTIVE"
    })
    assert create_resp.status_code == 200
    driver_data = create_resp.json()
    driver_id = driver_data["id"]

    # 2. Bloquear motorista com motivo
    block_resp = client.post(f"/api/v1/admin/drivers/{driver_id}/block", headers=headers, json={
        "reason": "Comportamento operacional em desacordo com as regras"
    })
    assert block_resp.status_code == 200
    assert block_resp.json()["status"] == "BLOCKED"

    # 3. Desbloquear motorista
    unblock_resp = client.post(f"/api/v1/admin/drivers/{driver_id}/unblock", headers=headers)
    assert unblock_resp.status_code == 200
    assert unblock_resp.json()["status"] == "ACTIVE"


def test_07_admin_plans_crud(admin_token):
    """Testa ciclo completo de planos: criação, edição de preço e status."""
    import uuid
    headers = {"Authorization": f"Bearer {admin_token}"}
    test_code = f"plan_test_{uuid.uuid4().hex[:6]}"

    # 1. Criar Plano
    create_resp = client.post("/api/v1/admin/plans", headers=headers, json={
        "code": test_code,
        "name": "Plano VIP Turbo",
        "price_cents": 4990,
        "interval": "month",
        "features": ["Feature A", "Feature B"],
        "description": "Plano para frotas e motoristas de elite"
    })
    assert create_resp.status_code == 200
    assert create_resp.json()["code"] == test_code

    # 2. Editar Preço do Plano
    update_resp = client.patch(f"/api/v1/admin/plans/{test_code}", headers=headers, json={
        "price_cents": 5990,
        "name": "Plano VIP Turbo Atualizado"
    })
    assert update_resp.status_code == 200
    assert update_resp.json()["price_cents"] == 5990

    # 3. Desativar Plano
    delete_resp = client.delete(f"/api/v1/admin/plans/{test_code}", headers=headers)
    assert delete_resp.status_code == 200


def test_08_admin_feature_flags(admin_token):
    """Testa listagem e alteração de Feature Flags."""
    headers = {"Authorization": f"Bearer {admin_token}"}
    
    # Listar flags
    flags_resp = client.get("/api/v1/admin/feature-flags")
    assert flags_resp.status_code == 200
    flags = flags_resp.json()
    assert isinstance(flags, list)
    assert len(flags) >= 3

    # Alternar flag
    patch_resp = client.patch("/api/v1/admin/feature-flags/ENABLE_OVERLAY", headers=headers, json={
        "key": "ENABLE_OVERLAY",
        "is_enabled": True
    })
    assert patch_resp.status_code == 200
    assert patch_resp.json()["is_enabled"] is True


def test_09_admin_system_settings(admin_token):
    """Testa alteração de configurações do sistema."""
    headers = {"Authorization": f"Bearer {admin_token}"}
    
    # Listar configurações
    settings_resp = client.get("/api/v1/admin/settings", headers=headers)
    assert settings_resp.status_code == 200
    settings = settings_resp.json()
    assert len(settings) >= 5

    # Atualizar configuração
    update_resp = client.patch("/api/v1/admin/settings/FREE_DAILY_EVALUATION_LIMIT", headers=headers, json={
        "value": "20"
    })
    assert update_resp.status_code == 200
    assert update_resp.json()["value"] == "20"


def test_10_admin_audit_logs(admin_token):
    """Verifica que as ações administrativas geraram registros de auditoria."""
    headers = {"Authorization": f"Bearer {admin_token}"}
    logs_resp = client.get("/api/v1/admin/audit-logs", headers=headers)
    assert logs_resp.status_code == 200
    logs = logs_resp.json()
    assert isinstance(logs, list)
    assert len(logs) >= 1
    # Verifica estrutura
    first_log = logs[0]
    assert "action" in first_log
    assert "admin_email" in first_log
    assert "created_at" in first_log


def test_11_admin_telemetry_purge(admin_token):
    """Verifica que o expurgo de telemetria LGPD é executado com sucesso por um admin."""
    headers = {"Authorization": f"Bearer {admin_token}"}
    purge_resp = client.post("/api/v1/admin/telemetry/purge?retention_days=30", headers=headers)
    assert purge_resp.status_code == 200
    data = purge_resp.json()
    assert data["success"] is True
    assert data["retention_days"] == 30
    assert "cutoff_timestamp" in data
    assert "purged_count" in data
    assert "executed_by" in data

