"""
Testes automatizados para registro de dispositivos, notificações pendentes e ack.
"""

import pytest
from fastapi.testclient import TestClient
from app.main import app

client = TestClient(app)


def test_device_registration_and_pending_notifications():
    # 1. Registra dispositivo
    reg_resp = client.post("/api/v1/devices/register", json={
        "fcm_token": "test_fcm_token_integration_123",
        "user_id": "test_user_456",
        "platform": "android",
        "device_id": "Samsung S23 (device_test_1)",
        "app_version": "1.0.0",
        "os_version": "Android 14 (API 34)",
        "notifications_enabled": True
    })
    assert reg_resp.status_code == 200
    reg_data = reg_resp.json()
    assert reg_data["success"] is True

    # 2. Testa endpoint de alias /devices/register
    alias_resp = client.post("/devices/register", json={
        "fcm_token": "test_fcm_token_alias_789",
        "platform": "android",
        "device_id": "Pixel 8 (device_test_2)",
        "notifications_enabled": True
    })
    assert alias_resp.status_code == 200
    assert alias_resp.json()["success"] is True

    # 3. Consulta notificações pendentes
    pending_resp = client.get("/api/v1/notifications/pending?device_id=device_test_1")
    assert pending_resp.status_code == 200
    pending_data = pending_resp.json()
    assert "notifications" in pending_data
    assert isinstance(pending_data["notifications"], list)

    # 4. Confirma recebimento (ack)
    ack_resp = client.post("/api/v1/notifications/camp_test_1/ack?device_id=device_test_1")
    assert ack_resp.status_code == 200
    assert ack_resp.json()["success"] is True
