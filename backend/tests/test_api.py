"""
ROTA IQ - Suíte Oficial de Testes Automatizados da Fase 2 (Backend + Financeiro Real)
Testes de ponta a ponta cobrindo:
1. Health & Conectividade
2. Autenticação Real (PBKDF2 + JWT)
3. Veículos e Cálculo de Custo/KM
4. Combustível (Algoritmo dos 2 Tanques Cheios)
5. Manutenção e Despesas
6. Metas e Orientação do Copiloto (Goal Engine)
7. Avaliação Determinística de Corridas (Ride Evaluation Engine)
8. Consolidação Financeira Real
9. Sincronização Offline-First
10. Admin Metrics (Zero Mocks)
"""

import pytest
import uuid
from fastapi.testclient import TestClient
import sys
import os

# Adiciona o diretório backend ao sys.path
sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), "..")))

from app.main import app
from app.auth import hash_password, verify_password

client = TestClient(app)

@pytest.fixture(scope="module")
def authenticated_driver():
    """Cria um usuário real de teste e obtém JWT token."""
    unique_id = uuid.uuid4().hex[:8]
    email = f"driver_{unique_id}@rotai.com.br"
    password = f"SenhaForte_{unique_id}#123"

    reg_resp = client.post("/api/v1/auth/register", json={
        "email": email,
        "password": password,
        "full_name": f"Motorista Teste {unique_id}",
        "phone": f"119{unique_id[:8]}",
        "city": "São Paulo",
        "state": "SP"
    })
    assert reg_resp.status_code == 200, reg_resp.text
    data = reg_resp.json()

    token = data["access_token"]
    driver_id = data["driver_id"]
    headers = {"Authorization": f"Bearer {token}"}

    return {
        "headers": headers,
        "driver_id": driver_id,
        "email": email,
        "password": password,
        "refresh_token": data["refresh_token"]
    }

def test_01_health_check():
    response = client.get("/api/v1/health")
    assert response.status_code == 200
    data = response.json()
    assert data["status"] == "HEALTHY"
    assert data["service"] == "rota-iq-backend"

def test_02_supabase_status():
    response = client.get("/api/v1/supabase/status")
    assert response.status_code == 200
    data = response.json()
    assert data["status"] == "CONNECTED"
    assert "real_users_count" in data

def test_03_auth_hashing():
    raw_pass = "MinhaSenhaSuperSecreta@2026"
    h = hash_password(raw_pass)
    assert h.startswith("pbkdf2_sha256$100000$")
    assert verify_password(raw_pass, h) is True
    assert verify_password("SenhaErrada", h) is False

def test_04_auth_login_and_me(authenticated_driver):
    login_resp = client.post("/api/v1/auth/login", json={
        "email": authenticated_driver["email"],
        "password": authenticated_driver["password"]
    })
    assert login_resp.status_code == 200
    login_data = login_resp.json()
    assert "access_token" in login_data
    assert login_data["driver_id"] == authenticated_driver["driver_id"]

    me_resp = client.get("/api/v1/auth/me", headers=authenticated_driver["headers"])
    assert me_resp.status_code == 200
    me_data = me_resp.json()
    assert me_data["email"] == authenticated_driver["email"]
    assert me_data["driver_id"] == authenticated_driver["driver_id"]

def test_05_auth_refresh(authenticated_driver):
    refresh_resp = client.post("/api/v1/auth/refresh", json={
        "refresh_token": authenticated_driver["refresh_token"]
    })
    assert refresh_resp.status_code == 200
    data = refresh_resp.json()
    assert "access_token" in data
    assert data["token_type"] == "bearer"

def test_06_vehicle_and_costs(authenticated_driver):
    headers = authenticated_driver["headers"]
    unique_plate = "IQ" + uuid.uuid4().hex[:5].upper()

    create_resp = client.post("/api/v1/vehicles", headers=headers, json={
        "name": "Chevrolet Onix Plus",
        "plate": unique_plate,
        "model": "Sedan 1.0 Turbo",
        "year": 2023,
        "fuel_type": "GASOLINE",
        "consumption_km_per_liter": 12.5,
        "fuel_price_per_liter": 5.80,
        "maintenance_cost_per_km": 0.16
    })
    assert create_resp.status_code == 200
    v_data = create_resp.json()
    vehicle_id = v_data.get("id")
    assert vehicle_id is not None

    costs_resp = client.get(f"/api/v1/vehicles/{vehicle_id}/costs", headers=headers)
    assert costs_resp.status_code == 200
    costs_data = costs_resp.json()

    # fuel_cost_per_km = 5.80 / 12.5 = 0.464
    assert costs_data["fuel_cost_per_km"] == pytest.approx(0.464, abs=0.01)
    assert costs_data["maintenance_cost_per_km"] == 0.16
    assert costs_data["total_cost_per_km"] > 0.60
    assert costs_data["cost_per_hour_estimated"] > 15.0

def test_07_fuel_two_tanks_engine(authenticated_driver):
    headers = authenticated_driver["headers"]

    # Tanque 1 (Cheio, odômetro 50.000)
    t1_resp = client.post("/api/v1/fuel", headers=headers, json={
        "date": "2026-10-01",
        "odometer_km": 50000.0,
        "liters": 45.0,
        "price_per_liter": 5.89,
        "fuel_type": "GASOLINE",
        "is_full_tank": True
    })
    assert t1_resp.status_code == 200

    # Tanque 2 (Cheio, odômetro 50.540 km, abasteceu 45L -> dist = 540 km, km/l = 540 / 45 = 12.0)
    t2_resp = client.post("/api/v1/fuel", headers=headers, json={
        "date": "2026-10-05",
        "odometer_km": 50540.0,
        "liters": 45.0,
        "price_per_liter": 5.89,
        "fuel_type": "GASOLINE",
        "is_full_tank": True
    })
    assert t2_resp.status_code == 200
    t2_data = t2_resp.json()

    assert t2_data["calculated_km_per_liter"] == pytest.approx(12.0, abs=0.1)
    assert t2_data["calculated_cost_per_km"] is not None

def test_08_goals_and_coaching(authenticated_driver):
    headers = authenticated_driver["headers"]

    goal_resp = client.post("/api/v1/goals", headers=headers, json={
        "daily_gross_target": 350.0,
        "target_hourly_rate": 45.0,
        "target_km_rate": 2.50,
        "shift_target_hours": 8.0
    })
    assert goal_resp.status_code == 200

    coaching_resp = client.get("/api/v1/goals/coaching", headers=headers)
    assert coaching_resp.status_code == 200
    coaching_data = coaching_resp.json()

    assert coaching_data["daily_gross_target"] == 350.0
    assert coaching_data["remaining_gross"] <= 350.0
    assert "coaching_message" in coaching_data

def test_09_ride_evaluate_engine(authenticated_driver):
    headers = authenticated_driver["headers"]

    # Corrida Excelente
    good_resp = client.post("/api/v1/rides/evaluate", headers=headers, json={
        "platform": "UBER",
        "gross_fare": 32.80,
        "distance_km": 8.2,
        "duration_minutes": 22.0,
        "pickup_distance_km": 1.2,
        "pickup_duration_minutes": 4.0
    })
    assert good_resp.status_code == 200
    good_data = good_resp.json()

    assert good_data["score"] >= 75
    assert good_data["classification"] in ["EXCELLENT", "GOOD"]
    assert good_data["net_profit"] > 20.0
    assert good_data["gross_rate_per_km"] > 3.0

    # Salvar a corrida avaliada
    save_resp = client.post("/api/v1/rides/save", headers=headers, json={
        **good_data,
        "was_accepted": True
    })
    assert save_resp.status_code == 200

    # Corrida Ruim
    bad_resp = client.post("/api/v1/rides/evaluate", headers=headers, json={
        "platform": "NINETY_NINE",
        "gross_fare": 14.00,
        "distance_km": 12.0,
        "duration_minutes": 35.0,
        "pickup_distance_km": 4.5,
        "pickup_duration_minutes": 10.0
    })
    assert bad_resp.status_code == 200
    bad_data = bad_resp.json()

    assert bad_data["score"] <= 45
    assert bad_data["classification"] in ["BAD", "AVOID"]
    assert len(bad_data["alerts"]) > 0

def test_10_financial_summary_and_report(authenticated_driver):
    headers = authenticated_driver["headers"]

    summary_resp = client.get("/api/v1/financial/summary?period=all", headers=headers)
    assert summary_resp.status_code == 200
    summary_data = summary_resp.json()

    assert summary_data["has_activity"] is True
    assert summary_data["gross_revenue"] > 0.0
    assert summary_data["total_costs"] > 0.0

    report_resp = client.get("/api/v1/financial/report?period=daily", headers=headers)
    assert report_resp.status_code == 200
    report_data = report_resp.json()

    assert "cost_breakdown_percentages" in report_data
    assert "fuel" in report_data["cost_breakdown_percentages"]

def test_11_sync_push(authenticated_driver):
    headers = authenticated_driver["headers"]

    sync_resp = client.post("/api/v1/sync/push", headers=headers, json={
        "device_id": "test_pixel_device_01",
        "client_timestamp": 1791399000000,
        "fuel_records": [{
            "date": "2026-10-06",
            "odometer_km": 50800.0,
            "liters": 20.0,
            "price_per_liter": 5.85,
            "total_paid": 117.0,
            "fuel_type": "GASOLINE",
            "is_full_tank": False
        }],
        "maintenance_records": [],
        "expenses": [],
        "evaluations": []
    })
    assert sync_resp.status_code == 200
    sync_data = sync_resp.json()

    assert sync_data["success"] is True
    assert sync_data["acknowledged_fuel_count"] == 1

def test_12_admin_metrics_real_data():
    admin_resp = client.get("/api/v1/admin/metrics")
    assert admin_resp.status_code == 200
    admin_data = admin_resp.json()

    # Confere que os dados vêm de contagens reais do banco de dados (números reais positivos)
    assert isinstance(admin_data["total_registered_users"], int)
    assert admin_data["total_registered_users"] >= 1
    assert isinstance(admin_data["total_registered_drivers"], int)
    assert admin_data["total_registered_drivers"] >= 1
    assert isinstance(admin_data["mrr_reais"], (int, float))
    assert admin_data["source"] == "Supabase PostgreSQL Production (Zero Mocks)"

def test_13_subscription_plans():
    plans_resp = client.get("/api/v1/subscriptions/plans")
    assert plans_resp.status_code == 200
    plans = plans_resp.json()

    codes = [p["code"] for p in plans]
    assert "free" in codes
    assert "pro_monthly" in codes
    assert "pro_annual" in codes

def test_14_admin_drivers_list():
    resp = client.get("/api/v1/admin/drivers")
    assert resp.status_code == 200
    drivers = resp.json()
    assert isinstance(drivers, list)
    if len(drivers) > 0:
        d = drivers[0]
        assert "id" in d
        assert "user_id" in d
        assert "status" in d

def test_15_feature_flags():
    # Atualiza ou cria flag
    update_resp = client.post("/api/v1/admin/feature-flags", json={
        "key": "test_phase5_flag",
        "is_enabled": True,
        "description": "Flag de teste automatizado Fase 5"
    })
    assert update_resp.status_code == 200

    # Consulta flags
    list_resp = client.get("/api/v1/admin/feature-flags")
    assert list_resp.status_code == 200
    flags = list_resp.json()
    assert isinstance(flags, list)

def test_16_lgpd_export(authenticated_driver):
    headers = authenticated_driver["headers"]
    resp = client.post("/api/v1/lgpd/export", headers=headers, json={
        "driver_id": authenticated_driver["driver_id"]
    })
    assert resp.status_code == 200
    data = resp.json()
    assert "exported_at" in data
    assert "user" in data
    assert "driver" in data
    assert "ride_evaluations_count" in data

def test_17_lgpd_anonymize(authenticated_driver):
    headers = authenticated_driver["headers"]
    resp = client.post("/api/v1/lgpd/anonymize", headers=headers, json={
        "reason": "Exercício de Direito ao Esquecimento - LGPD Art. 18"
    })
    assert resp.status_code == 200
    data = resp.json()
    assert data["status"] == "COMPLETED"
    assert "confirmation_id" in data

def test_18_telemetry_sanitized():
    resp = client.post("/api/v1/telemetry/events", json={
        "events": [
            {
                "event_name": "screen_view",
                "app_version": "2.0.0",
                "driver_id_hash": "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
                "properties": {
                    "screen": "cockpit_live",
                    "mode": "HEADS_UP"
                }
            }
        ]
    })
    assert resp.status_code == 200
    data = resp.json()
    assert data["success"] is True
    assert data["ingested_count"] == 1

