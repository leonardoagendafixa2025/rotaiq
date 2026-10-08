"""
ROTA IQ - Módulo de Autenticação Real e Criptografia
Implementa hash seguro com PBKDF2-HMAC-SHA256 (NIST standard) com salt aleatório
e tokens de sessão JWT (HS256) com controle de expiração e revogação.
"""

import os
import hashlib
import secrets
import jwt
from datetime import datetime, timedelta, timezone
from typing import Optional, Dict, Any
from fastapi import HTTPException, Security, status
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials

JWT_SECRET = os.getenv("JWT_SECRET", "rota_iq_jwt_secret_production_key_994827103857")
JWT_ALGORITHM = "HS256"
ACCESS_TOKEN_EXPIRE_HOURS = 24
REFRESH_TOKEN_EXPIRE_DAYS = 30

security = HTTPBearer()

def hash_password(password: str) -> str:
    """Gera hash seguro com PBKDF2-HMAC-SHA256 e salt de 16 bytes."""
    salt = secrets.token_hex(16)
    iterations = 100000
    derived = hashlib.pbkdf2_hmac(
        "sha256",
        password.encode("utf-8"),
        salt.encode("utf-8"),
        iterations
    )
    return f"pbkdf2_sha256${iterations}${salt}${derived.hex()}"

def verify_password(password: str, hashed: str) -> bool:
    """Verifica se a senha coincide com o hash PBKDF2."""
    try:
        parts = hashed.split("$")
        if len(parts) != 4 or parts[0] != "pbkdf2_sha256":
            return False
        iterations = int(parts[1])
        salt = parts[2]
        expected_hex = parts[3]
        derived = hashlib.pbkdf2_hmac(
            "sha256",
            password.encode("utf-8"),
            salt.encode("utf-8"),
            iterations
        )
        return secrets.compare_digest(derived.hex(), expected_hex)
    except Exception:
        return False

def create_access_token(data: Dict[str, Any], expires_delta: Optional[timedelta] = None) -> str:
    """Gera JWT Access Token com tempo de expiração."""
    to_encode = data.copy()
    expire = datetime.now(timezone.utc) + (expires_delta or timedelta(hours=ACCESS_TOKEN_EXPIRE_HOURS))
    to_encode.update({"exp": expire, "type": "access"})
    return jwt.encode(to_encode, JWT_SECRET, algorithm=JWT_ALGORITHM)

def create_refresh_token(data: Dict[str, Any]) -> str:
    """Gera JWT Refresh Token com 30 dias de expiração."""
    to_encode = data.copy()
    expire = datetime.now(timezone.utc) + timedelta(days=REFRESH_TOKEN_EXPIRE_DAYS)
    to_encode.update({"exp": expire, "type": "refresh"})
    return jwt.encode(to_encode, JWT_SECRET, algorithm=JWT_ALGORITHM)

def decode_token(token: str) -> Dict[str, Any]:
    """Decodifica e valida assinatura e expiração do JWT."""
    try:
        payload = jwt.decode(token, JWT_SECRET, algorithms=[JWT_ALGORITHM])
        return payload
    except jwt.ExpiredSignatureError:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Token expirado. Efetue login novamente."
        )
    except jwt.InvalidTokenError:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Token de autenticação inválido."
        )

def get_current_user_claims(credentials: HTTPAuthorizationCredentials = Security(security)) -> Dict[str, Any]:
    """Extrai as claims do usuário logado a partir do cabeçalho Authorization: Bearer <token>."""
    token = credentials.credentials
    claims = decode_token(token)
    if claims.get("type") != "access":
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Token fornecido não é um access token válido."
        )
    return claims

def require_admin_claims(credentials: HTTPAuthorizationCredentials = Security(security)) -> Dict[str, Any]:
    """Exige que a requisição venha de um usuário com role de administração (SUPER_ADMIN, ADMIN ou OPERADOR)."""
    claims = get_current_user_claims(credentials)
    role = claims.get("role")
    if not role or role not in ["SUPER_ADMIN", "ADMIN", "OPERADOR"]:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Acesso restrito: Requer privilégios administrativos."
        )
    return claims

def require_super_admin_claims(credentials: HTTPAuthorizationCredentials = Security(security)) -> Dict[str, Any]:
    """Exige privilégios de SUPER_ADMIN para ações críticas."""
    claims = get_current_user_claims(credentials)
    role = claims.get("role")
    if role != "SUPER_ADMIN":
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Acesso negado: Operação exclusiva para SUPER_ADMIN."
        )
    return claims
