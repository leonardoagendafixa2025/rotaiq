"""
ROTA IQ — TESTES AUTOMATIZADOS DO MÓDULO REAL DE CAMPANHAS E PUSH NOTIFICATIONS
Valida:
1. Registro e descadastramento de tokens FCM de dispositivos Android
2. Criação, edição, duplicação e cancelamento de campanhas
3. Pré-visualização real de público (Zero Mocks)
4. Envio imediato e agendamento de notificações push
5. Relatórios com métricas reais de entrega
6. Segurança e RBAC (Driver -> 403, Admin sem permissão -> 403, Super Admin -> 200)
7. Templates reutilizáveis
"""

import pytest
from fastapi.testclient import TestClient
from app.main import app
from app.auth import create_access_token

client = TestClient(app)


@pytest.fixture
def super_admin_headers():
    token = create_access_token({
        "sub": "admin@rotai.app",
        "email": "admin@rotai.app",
        "role": "SUPER_ADMIN",
        "permissions": ["SUPER_ADMIN", "SEND_CAMPAIGNS", "MANAGE_CAMPAIGNS"],
        "type": "admin"
    })
    return {"Authorization": f"Bearer {token}"}


@pytest.fixture
def restricted_admin_headers():
    token = create_access_token({
        "sub": "operador@rotai.app",
        "email": "operador@rotai.app",
        "role": "OPERADOR",
        "permissions": ["VIEW_DASHBOARD"],
        "type": "admin"
    })
    return {"Authorization": f"Bearer {token}"}


@pytest.fixture
def driver_headers():
    token = create_access_token({
        "sub": "driver_uuid_123",
        "driver_id": "driver_uuid_123",
        "type": "access"
    })
    return {"Authorization": f"Bearer {token}"}


# =====================================================================
# 1. TESTES DE DISPOSITIVOS & TOKENS FCM
# =====================================================================

def test_register_device_token():
    payload = {
        "user_id": "user_test_999",
        "fcm_token": "fcm_token_sample_abc123xyz",
        "platform": "android",
        "device_id": "samsung_galaxy_s23",
        "app_version": "1.0.0",
        "os_version": "Android 14 (API 34)",
        "notifications_enabled": True
    }
    response = client.post("/api/v1/devices/register", json=payload)
    assert response.status_code == 200
    data = response.json()
    assert data["success"] is True
    assert data["device"]["fcm_token"] == "fcm_token_sample_abc123xyz"
    assert data["device"]["active"] is True


def test_unregister_device_token():
    # Registra
    client.post("/api/v1/devices/register", json={
        "user_id": "user_test_888",
        "fcm_token": "token_to_deactivate_456"
    })

    # Desativa
    response = client.post("/api/v1/devices/unregister", json={
        "fcm_token": "token_to_deactivate_456"
    })
    assert response.status_code == 200
    assert response.json()["success"] is True


# =====================================================================
# 2. TESTES DE CAMPANHAS — CRUD, PRÉVIA DE AUDIÊNCIA E SEGMENTAÇÃO
# =====================================================================

def test_audience_preview(super_admin_headers):
    response = client.post(
        "/api/v1/admin/campaigns/audience-preview",
        json={"audience_type": "ALL"},
        headers=super_admin_headers
    )
    assert response.status_code == 200
    data = response.json()
    assert "total_users" in data
    assert "eligible_devices" in data
    assert "users_without_permission" in data
    assert "segment_description" in data


def test_create_and_get_campaign(super_admin_headers):
    payload = {
        "name": "Campanha Black Friday Pro",
        "type": "PROMOCAO",
        "title": "🔥 50% OFF no ROTA IQ Pro!",
        "body": "Desbloqueie agora avaliações ilimitadas e HUD flutuante com desconto especial.",
        "image_url": "https://rotai.app/assets/promo.jpg",
        "deep_link": "rotaiq://subscription",
        "audience_type": "FREE",
        "action": "DRAFT"
    }
    response = client.post("/api/v1/admin/campaigns", json=payload, headers=super_admin_headers)
    assert response.status_code == 200
    data = response.json()
    assert data["success"] is True
    camp = data["campaign"]
    assert camp["name"] == "Campanha Black Friday Pro"
    assert camp["status"] == "DRAFT"
    camp_id = camp["id"]

    # Buscar detalhes
    get_res = client.get(f"/api/v1/admin/campaigns/{camp_id}", headers=super_admin_headers)
    assert get_res.status_code == 200
    assert get_res.json()["id"] == camp_id


def test_duplicate_campaign(super_admin_headers):
    # Cria original
    create_res = client.post("/api/v1/admin/campaigns", json={
        "name": "Campanha Original Para Clonar",
        "type": "INFORMATIVA",
        "title": "Aviso Importante",
        "body": "Atualização operacional.",
        "deep_link": "rotaiq://home"
    }, headers=super_admin_headers)
    original_id = create_res.json()["campaign"]["id"]

    # Duplica
    dup_res = client.post(f"/api/v1/admin/campaigns/{original_id}/duplicate", headers=super_admin_headers)
    assert dup_res.status_code == 200
    dup_data = dup_res.json()
    assert dup_data["success"] is True
    assert dup_data["campaign"]["status"] == "DRAFT"
    assert "Cópia" in dup_data["campaign"]["name"]


def test_schedule_and_cancel_campaign(super_admin_headers):
    # Cria
    create_res = client.post("/api/v1/admin/campaigns", json={
        "name": "Campanha Para Agendar",
        "type": "ENGAJAMENTO",
        "title": "Corrida Noturna",
        "body": "Alta demanda na região sul.",
        "action": "SCHEDULE",
        "scheduled_at": "2026-10-15T20:00:00Z"
    }, headers=super_admin_headers)
    camp_id = create_res.json()["campaign"]["id"]

    # Cancela
    cancel_res = client.post(f"/api/v1/admin/campaigns/{camp_id}/cancel", headers=super_admin_headers)
    assert cancel_res.status_code == 200
    assert cancel_res.json()["campaign"]["status"] == "CANCELLED"


def test_send_campaign_now(super_admin_headers):
    # Cria e envia
    payload = {
        "name": "Disparo Imediato Teste",
        "type": "SISTEMA",
        "title": "🚀 Atualização Crítica",
        "body": "Servidores operando normalmente.",
        "action": "SEND_NOW",
        "audience_type": "ALL"
    }
    response = client.post("/api/v1/admin/campaigns", json=payload, headers=super_admin_headers)
    assert response.status_code == 200
    data = response.json()
    assert data["success"] is True
    camp_id = data["campaign"]["id"]

    # Relatório da campanha
    report_res = client.get(f"/api/v1/admin/campaigns/{camp_id}/report", headers=super_admin_headers)
    assert report_res.status_code == 200
    rep = report_res.json()
    assert "metrics" in rep
    assert "total_sent" in rep["metrics"]


# =====================================================================
# 3. TESTES DE TEMPLATES
# =====================================================================

def test_campaign_templates(super_admin_headers):
    # Lista templates (já possui seeds padrão)
    res = client.get("/api/v1/admin/campaign-templates", headers=super_admin_headers)
    assert res.status_code == 200
    templates = res.json()
    assert len(templates) >= 3

    # Cria novo template
    new_t = client.post("/api/v1/admin/campaign-templates", json={
        "name": "Template Fim de Semana",
        "type": "ENGAJAMENTO",
        "title": "Sextou com Alta Demanda!",
        "body": "Aproveite as melhores tarifas neste final de semana.",
        "deep_link": "rotaiq://rides"
    }, headers=super_admin_headers)
    assert new_t.status_code == 200
    t_id = new_t.json()["template"]["id"]

    # Exclui template
    del_res = client.delete(f"/api/v1/admin/campaign-templates/{t_id}", headers=super_admin_headers)
    assert del_res.status_code == 200


# =====================================================================
# 4. TESTES DE SEGURANÇA E RBAC (PONTOS 51 E 58)
# =====================================================================

def test_security_driver_cannot_create_or_view_campaigns(driver_headers):
    # Driver comum tentando acessar campanhas -> 403 Forbidden
    res = client.get("/api/v1/admin/campaigns", headers=driver_headers)
    assert res.status_code == 403

    res_post = client.post("/api/v1/admin/campaigns", json={
        "name": "Hack Campaign",
        "title": "Teste",
        "body": "Corpo"
    }, headers=driver_headers)
    assert res_post.status_code == 403


def test_security_operator_cannot_send_campaigns(restricted_admin_headers):
    # Operador (apenas leitura) tentando enviar notificação push -> 403 Forbidden
    res = client.post("/api/v1/admin/campaigns/some_fake_id/send", headers=restricted_admin_headers)
    assert res.status_code == 403
