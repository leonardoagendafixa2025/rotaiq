"""
ROTA IQ - Módulo de Autenticação Real e Criptografia
Implementa hash seguro com PBKDF2-HMAC-SHA256 (NIST standard) com salt aleatório
e tokens de sessão JWT (HS256) com controle de expiração e revogação.
"""

import os
import hashlib
import secrets
try:
    import jwt
except ImportError:
    try:
        from jose import jwt
    except ImportError:
        jwt = None
from datetime import datetime, timedelta, timezone
from typing import Optional, Dict, Any
from fastapi import HTTPException, Security, status
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials

# Carregar .env se existir localmente
_env_paths = [
    os.path.join(os.path.dirname(os.path.dirname(__file__)), ".env"),
    os.path.join(os.path.dirname(os.path.dirname(os.path.dirname(__file__))), ".env"),
    ".env"
]
for _p in _env_paths:
    if os.path.exists(_p):
        try:
            with open(_p, "r", encoding="utf-8") as _f:
                for _line in _f:
                    _line = _line.strip()
                    if _line and not _line.startswith("#") and "=" in _line:
                        _k, _v = _line.split("=", 1)
                        _k = _k.strip()
                        _v = _v.strip().strip('"').strip("'")
                        if _k not in os.environ:
                            os.environ[_k] = _v
        except Exception:
            pass

_JWT_SECRET_RAW = os.getenv("JWT_SECRET", "")
if not _JWT_SECRET_RAW or len(_JWT_SECRET_RAW) < 32:
    import sys
    _is_vercel = os.getenv("VERCEL") or os.getenv("VERCEL_ENV")
    if _is_vercel or os.getenv("PRODUCTION"):
        print(
            "[ROTA IQ] FATAL: JWT_SECRET não configurado ou muito curto (mínimo 32 chars). "
            "Configure a variável de ambiente JWT_SECRET no painel da Vercel.",
            file=sys.stderr
        )
        # Não abortar em serverless (a função continuaria crashando em loop); 
        # gerar segredo efêmero que invalida todos os tokens existentes ao reiniciar
        import secrets as _sec
        _JWT_SECRET_RAW = _sec.token_hex(32)
    else:
        # Em desenvolvimento local: falhar explicitamente para forçar configuração
        raise RuntimeError(
            "JWT_SECRET não configurado. Defina a variável de ambiente JWT_SECRET "
            "com no mínimo 32 caracteres aleatórios. Exemplo: "
            "export JWT_SECRET=$(python -c 'import secrets; print(secrets.token_hex(32))')"
        )

JWT_SECRET = _JWT_SECRET_RAW
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
    """Decodifica e valida assinatura, expiração e status de revogação do JWT."""
    try:
        from .auth_service import auth_service
        if auth_service.is_token_revoked(token):
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail="Sessão encerrada ou token revogado. Efetue login novamente."
            )
        payload = jwt.decode(token, JWT_SECRET, algorithms=[JWT_ALGORITHM])
        return payload
    except HTTPException:
        raise
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

def require_campaign_send_permission(credentials: HTTPAuthorizationCredentials = Security(security)) -> Dict[str, Any]:
    """Exige privilégios de SUPER_ADMIN ou ADMIN com permissão explícita SEND_CAMPAIGNS."""
    claims = get_current_user_claims(credentials)
    role = claims.get("role")
    permissions = claims.get("permissions") or []
    if role == "SUPER_ADMIN":
        return claims
    if role == "ADMIN" and ("SEND_CAMPAIGNS" in permissions or "SUPER_ADMIN" in permissions or "MANAGE_NOTIFICATIONS" in permissions):
        return claims
    raise HTTPException(
        status_code=status.HTTP_403_FORBIDDEN,
        detail="Acesso negado: Requer permissão SEND_CAMPAIGNS ou SUPER_ADMIN."
    )

def require_campaign_manage_permission(credentials: HTTPAuthorizationCredentials = Security(security)) -> Dict[str, Any]:
    """Exige privilégios para criar/editar campanhas (SUPER_ADMIN ou ADMIN)."""
    claims = get_current_user_claims(credentials)
    role = claims.get("role")
    if role in ["SUPER_ADMIN", "ADMIN"]:
        return claims
    raise HTTPException(
        status_code=status.HTTP_403_FORBIDDEN,
        detail="Acesso negado: Requer privilégios administrativos para gerenciar campanhas."
    )
