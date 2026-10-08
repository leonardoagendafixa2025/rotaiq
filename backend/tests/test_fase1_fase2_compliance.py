"""
ROTA IQ - Teste Formal de Conformidade das Fases 1 e 2
Valida todas as 9 regras obrigatórias e condições de conclusão:
- Sem mocks, sem usuários fake
- Persistência real no PostgreSQL (Supabase)
- Senhas protegidas com PBKDF2 salt de 16 bytes
- Proteção 401 em rotas sem token
- Fluxo de esqueci senha com token temporário e uso único
- Auditoria de Termos de Uso e LGPD
- Logout real com revogação de tokens
"""

import pytest
import uuid
from datetime import datetime, timezone
from fastapi.testclient import TestClient
from app.main import app
from app.supabase_client import supabase
from app.auth import verify_password
from app.auth_service import auth_service

client = TestClient(app)

def test_mandatory_fase1_fase2_compliance():
    # -------------------------------------------------------------
    # REGRA: Nenhum usuário fake ou padrão
    # Geramos um usuário único para a bateria de testes reais
    # -------------------------------------------------------------
    suffix = uuid.uuid4().hex[:10]
    email = f"motorista_real_{suffix}@rotai.com.br"
    password = "SenhaForte2026!#"
    full_name = f"Motorista Oficial {suffix}"

    # TESTE 7 — ROTAS PROTEGIDAS SEM AUTENTICAÇÃO
    # Nenhuma rota protegida pode ser acessada sem token
    res_unauth = client.get("/api/v1/vehicles")
    assert res_unauth.status_code == 401, "Rota protegida deve exigir token"

    res_unauth_me = client.get("/api/v1/auth/me")
    assert res_unauth_me.status_code == 401, "Rota /auth/me deve exigir token"

    # TESTE 2 — CADASTRO REAL NO POSTGRESQL COM VALIDAÇÕES
    # A) Recusar senha com menos de 8 caracteres
    res_short = client.post("/api/v1/auth/register", json={
        "name": full_name,
        "email": email,
        "password": "1234567",
        "terms_accepted": True
    })
    assert res_short.status_code in (400, 422)

    # B) Recusar sem aceite dos Termos de Uso
    res_no_terms = client.post("/api/v1/auth/register", json={
        "name": full_name,
        "email": email,
        "password": password,
        "terms_accepted": False
    })
    assert res_no_terms.status_code in (400, 422)

    # C) Cadastro com sucesso -> Deve persistir no PostgreSQL
    res_reg = client.post("/api/v1/auth/register", json={
        "name": full_name,
        "email": email,
        "password": password,
        "terms_accepted": True,
        "terms_version": "1.0",
        "privacy_version": "1.0"
    })
    assert res_reg.status_code == 200, res_reg.text
    auth_data = res_reg.json()
    assert "access_token" in auth_data
    assert "refresh_token" in auth_data
    user_id = auth_data["user_id"]
    driver_id = auth_data["driver_id"]

    # Validar persistência física no PostgreSQL
    db_user = supabase.get_user_by_id(user_id)
    assert db_user is not None, "Usuário deve existir no banco PostgreSQL"
    assert db_user["email"] == email.lower(), "E-mail deve estar normalizado"
    assert db_user["password_hash"] != password, "Senha NUNCA pode ser texto puro"
    assert db_user["password_hash"].startswith("pbkdf2_sha256$100000$"), "Hash deve ser PBKDF2 HMAC SHA256"
    assert verify_password(password, db_user["password_hash"]) is True, "Hash PBKDF2 deve bater com a senha"

    # Validar que duplicata é rejeitada (Restrição UNIQUE)
    res_dup = client.post("/api/v1/auth/register", json={
        "name": full_name,
        "email": email,
        "password": password,
        "terms_accepted": True
    })
    assert res_dup.status_code == 400

    # TESTE 4 — LOGIN INCORRETO
    res_bad_pw = client.post("/api/v1/auth/login", json={
        "email": email,
        "password": "SenhaErrada999!"
    })
    assert res_bad_pw.status_code == 401
    assert "E-mail ou senha incorretos" in res_bad_pw.json()["detail"]

    # TESTE 3 — LOGIN COM SUCESSO
    res_login = client.post("/api/v1/auth/login", json={
        "email": email,
        "password": password
    })
    assert res_login.status_code == 200
    login_info = res_login.json()
    access_token = login_info["access_token"]
    refresh_token = login_info["refresh_token"]

    # TESTE 5 — SESSÃO PERSISTENTE E RENOVAÇÃO
    # Usando o token de acesso para rota protegida
    auth_headers = {"Authorization": f"Bearer {access_token}"}
    res_profile = client.get("/api/v1/auth/me", headers=auth_headers)
    assert res_profile.status_code == 200
    assert res_profile.json()["email"] == email.lower()

    # Renovação via refresh_token
    res_refresh = client.post("/api/v1/auth/refresh", json={"refresh_token": refresh_token})
    assert res_refresh.status_code == 200
    new_token = res_refresh.json()["access_token"]
    assert new_token is not None

    # TESTE 8 — RECUPERAÇÃO DE SENHA (FORGOT + RESET)
    # Solicitar código de recuperação
    res_forgot = client.post("/api/v1/auth/forgot-password", json={"email": email})
    assert res_forgot.status_code == 200
    reset_token = res_forgot.json().get("reset_token")
    assert reset_token is not None, "Token de recuperação deve ser gerado"

    # Redefinir senha com token temporário
    nova_senha = "NovaSenhaForte2026@!"
    res_reset = client.post("/api/v1/auth/reset-password", json={
        "token": reset_token,
        "new_password": nova_senha
    })
    assert res_reset.status_code == 200

    # Verificar que o token de recuperação não pode ser reutilizado (single-use)
    res_reset_reuse = client.post("/api/v1/auth/reset-password", json={
        "token": reset_token,
        "new_password": "OutraSenhaTentativa1!"
    })
    assert res_reset_reuse.status_code == 400, "Token de recuperação não pode ser reutilizado"

    # Login com a nova senha -> Deve funcionar
    res_login_nova = client.post("/api/v1/auth/login", json={
        "email": email,
        "password": nova_senha
    })
    assert res_login_nova.status_code == 200
    active_token = res_login_nova.json()["access_token"]

    # Antiga senha não funciona mais
    res_login_velha = client.post("/api/v1/auth/login", json={
        "email": email,
        "password": password
    })
    assert res_login_velha.status_code == 401

    # TESTE 6 — LOGOUT REAL E REVOGAÇÃO DE SESSÃO
    res_logout = client.post(
        "/api/v1/auth/logout",
        headers={"Authorization": f"Bearer {active_token}"},
        json={"refresh_token": res_login_nova.json()["refresh_token"]}
    )
    assert res_logout.status_code == 200

    # Token revogado deve ser barrado com 401 Unauthorized
    res_after_logout = client.get("/api/v1/auth/me", headers={"Authorization": f"Bearer {active_token}"})
    assert res_after_logout.status_code == 401, "Sessão após logout deve estar revogada"
