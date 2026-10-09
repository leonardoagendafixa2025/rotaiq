"""
ROTA IQ - Testes Completos de Autenticação Real (Fase 1 + Fase 2 Backend)
Validação de cadastro, login, termos, recuperação de senha, verificação de e-mail e logout com revogação.
"""

import pytest
import uuid
from fastapi.testclient import TestClient
from app.main import app

client = TestClient(app)

def test_auth_full_lifecycle():
    unique_suffix = uuid.uuid4().hex[:8]
    test_email = f"driver_{unique_suffix}@rotai.com.br"
    password = "SuperPassword123!"
    full_name = f"Piloto Teste {unique_suffix}"

    # 1. Falha por senha curta (< 8 chars)
    res_short = client.post("/api/v1/auth/register", json={
        "name": full_name,
        "email": test_email,
        "password": "short",
        "terms_accepted": True
    })
    assert res_short.status_code in (400, 422)

    # 2. Falha por não aceitar termos
    res_no_terms = client.post("/api/v1/auth/register", json={
        "name": full_name,
        "email": test_email,
        "password": password,
        "terms_accepted": False
    })
    assert res_no_terms.status_code == 400

    # 3. Cadastro Real com sucesso
    res_reg = client.post("/api/v1/auth/register", json={
        "name": full_name,
        "email": test_email,
        "password": password,
        "terms_accepted": True,
        "terms_version": "1.0",
        "privacy_version": "1.0"
    })
    assert res_reg.status_code == 200, res_reg.text
    data_reg = res_reg.json()
    assert "access_token" in data_reg
    assert "refresh_token" in data_reg
    assert data_reg["email"] == test_email.lower()
    assert data_reg["full_name"] == full_name

    # 4. Falha ao cadastrar email duplicado
    res_dup = client.post("/api/v1/auth/register", json={
        "name": full_name,
        "email": test_email,
        "password": password,
        "terms_accepted": True
    })
    assert res_dup.status_code == 400

    # 5. Login com senha errada -> 401
    res_wrong_pw = client.post("/api/v1/auth/login", json={
        "email": test_email,
        "password": "WrongPassword999!"
    })
    assert res_wrong_pw.status_code == 401

    # 6. Login com sucesso -> 200
    res_login = client.post("/api/v1/auth/login", json={
        "email": test_email,
        "password": password
    })
    assert res_login.status_code == 200
    login_data = res_login.json()
    access_token = login_data["access_token"]
    refresh_token = login_data["refresh_token"]

    # 7. Acesso à rota protegida com token válido
    headers = {"Authorization": f"Bearer {access_token}"}
    res_me = client.get("/api/v1/auth/me", headers=headers)
    assert res_me.status_code == 200
    assert res_me.json()["email"] == test_email.lower()

    # 8. Renovação de token (Refresh)
    res_ref = client.post("/api/v1/auth/refresh", json={"refresh_token": refresh_token})
    assert res_ref.status_code == 200
    new_access_token = res_ref.json()["access_token"]

    # 9. Verificação de e-mail
    res_ver = client.post("/api/v1/auth/verify-email", json={"email": test_email})
    assert res_ver.status_code == 200

    # 10. Esqueci minha senha -> gera token
    res_forgot = client.post("/api/v1/auth/forgot-password", json={"email": test_email})
    assert res_forgot.status_code == 200
    reset_token = res_forgot.json().get("reset_token")
    assert reset_token is not None

    # 11. Redefinir senha com novo hash
    new_password = "NewSuperPassword2026!"
    res_reset = client.post("/api/v1/auth/reset-password", json={
        "token": reset_token,
        "new_password": new_password
    })
    assert res_reset.status_code == 200

    # 12. Login com a nova senha -> Sucesso
    res_login_new = client.post("/api/v1/auth/login", json={
        "email": test_email,
        "password": new_password
    })
    assert res_login_new.status_code == 200
    token_before_change = res_login_new.json()["access_token"]
    auth_headers = {"Authorization": f"Bearer {token_before_change}"}

    # 12.1. Troca de Senha autenticada (Change Password)
    # Erro: Senha atual incorreta
    res_cp_wrong = client.post("/api/v1/auth/change-password", headers=auth_headers, json={
        "current_password": "WrongPassword123!",
        "new_password": "ThirdPassword2026!"
    })
    assert res_cp_wrong.status_code == 400

    # Erro: Nova senha muito curta (< 8 chars)
    res_cp_short = client.post("/api/v1/auth/change-password", headers=auth_headers, json={
        "current_password": new_password,
        "new_password": "short"
    })
    assert res_cp_short.status_code in (400, 422)

    # Erro: Nova senha igual à senha atual
    res_cp_same = client.post("/api/v1/auth/change-password", headers=auth_headers, json={
        "current_password": new_password,
        "new_password": new_password
    })
    assert res_cp_same.status_code == 400

    # Sucesso na troca de senha
    final_password = "FinalStrongPassword2026!"
    res_cp_ok = client.post("/api/v1/auth/change-password", headers=auth_headers, json={
        "current_password": new_password,
        "new_password": final_password
    })
    assert res_cp_ok.status_code == 200
    assert res_cp_ok.json()["success"] is True

    # Login com a senha trocada -> Sucesso
    res_login_final = client.post("/api/v1/auth/login", json={
        "email": test_email,
        "password": final_password
    })
    assert res_login_final.status_code == 200

    # 13. Logout Real -> revoga o token
    latest_token = res_login_final.json()["access_token"]
    res_logout = client.post(
        "/api/v1/auth/logout",
        headers={"Authorization": f"Bearer {latest_token}"},
        json={"refresh_token": res_login_final.json()["refresh_token"]}
    )
    assert res_logout.status_code == 200

    # 14. Tentativa de usar o token revogado -> Deve ser rejeitado com 401 Unauthorized!
    res_revoked = client.get("/api/v1/auth/me", headers={"Authorization": f"Bearer {latest_token}"})
    assert res_revoked.status_code == 401
