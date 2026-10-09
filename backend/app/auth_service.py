"""
ROTA IQ - Auth Persistence & Security Service
Gerenciamento de revogação de tokens (logout real), recuperação de senhas (tokens temporários únicos),
verificação de e-mail e registro de aceite de Termos de Uso e Política de Privacidade.
"""

import os
import sqlite3
import secrets
from datetime import datetime, timezone, timedelta
from typing import Optional, Dict, Any

def get_auth_db_path() -> str:
    if os.environ.get("VERCEL") or os.environ.get("AWS_LAMBDA_FUNCTION_NAME"):
        return os.path.join("/tmp", "rota_iq_auth.db")
    local_dir = os.path.join(os.path.dirname(os.path.dirname(__file__)), "data")
    try:
        os.makedirs(local_dir, exist_ok=True)
        return os.path.join(local_dir, "rota_iq_auth.db")
    except (OSError, PermissionError):
        return os.path.join(os.environ.get("TEMP", "/tmp"), "rota_iq_auth.db")

DB_PATH = get_auth_db_path()

class AuthService:
    def __init__(self, db_path: str = None):
        self.db_path = db_path or get_auth_db_path()
        try:
            os.makedirs(os.path.dirname(self.db_path), exist_ok=True)
        except (OSError, PermissionError):
            pass
        self._init_db()

    def _get_connection(self) -> sqlite3.Connection:
        conn = sqlite3.connect(self.db_path)
        conn.row_factory = sqlite3.Row
        return conn

    def _init_db(self):
        with self._get_connection() as conn:
            cur = conn.cursor()
            # Tabela de tokens revogados (Logout Real)
            cur.execute("""
                CREATE TABLE IF NOT EXISTS revoked_tokens (
                    token_hash TEXT PRIMARY KEY,
                    revoked_at TEXT NOT NULL
                )
            """)
            # Tabela de recuperação de senha (tokens descartáveis de 1 hora)
            cur.execute("""
                CREATE TABLE IF NOT EXISTS password_resets (
                    token TEXT PRIMARY KEY,
                    email TEXT NOT NULL,
                    expires_at TEXT NOT NULL,
                    used INTEGER NOT NULL DEFAULT 0,
                    created_at TEXT NOT NULL
                )
            """)
            # Tabela de verificação de e-mail
            cur.execute("""
                CREATE TABLE IF NOT EXISTS email_verifications (
                    email TEXT PRIMARY KEY,
                    verification_token TEXT NOT NULL,
                    verified INTEGER NOT NULL DEFAULT 0,
                    verified_at TEXT,
                    created_at TEXT NOT NULL
                )
            """)
            # Registro de auditoria de Termos de Uso e Privacidade (LGPD Compliance)
            cur.execute("""
                CREATE TABLE IF NOT EXISTS terms_acceptances (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id TEXT NOT NULL,
                    email TEXT NOT NULL,
                    terms_version TEXT NOT NULL,
                    privacy_version TEXT NOT NULL,
                    accepted_at TEXT NOT NULL,
                    ip_address TEXT
                )
            """)
            conn.commit()

    # -------------------------------------------------------------
    # 1. Revogação de Tokens (Logout Real)
    # -------------------------------------------------------------
    def revoke_token(self, token: str) -> None:
        import hashlib
        thash = hashlib.sha256(token.encode("utf-8")).hexdigest()
        now = datetime.now(timezone.utc).isoformat()
        with self._get_connection() as conn:
            cur = conn.cursor()
            cur.execute("INSERT OR REPLACE INTO revoked_tokens (token_hash, revoked_at) VALUES (?, ?)", (thash, now))
            conn.commit()

    def is_token_revoked(self, token: str) -> bool:
        import hashlib
        thash = hashlib.sha256(token.encode("utf-8")).hexdigest()
        with self._get_connection() as conn:
            cur = conn.cursor()
            cur.execute("SELECT 1 FROM revoked_tokens WHERE token_hash = ?", (thash,))
            return cur.fetchone() is not None

    # -------------------------------------------------------------
    # 2. Recuperação de Senha
    # -------------------------------------------------------------
    def create_password_reset_token(self, email: str, expiry_hours: int = 1) -> str:
        clean_email = email.strip().lower()
        now = datetime.now(timezone.utc)
        expires_at = (now + timedelta(hours=expiry_hours)).isoformat()

        token = None
        try:
            from .auth import jwt, JWT_SECRET, JWT_ALGORITHM
            if jwt:
                payload = {
                    "sub": clean_email,
                    "type": "password_reset",
                    "exp": datetime.now(timezone.utc) + timedelta(hours=expiry_hours)
                }
                token = jwt.encode(payload, JWT_SECRET, algorithm=JWT_ALGORITHM)
        except Exception:
            token = None

        if not token:
            token = secrets.token_urlsafe(32)

        try:
            with self._get_connection() as conn:
                cur = conn.cursor()
                cur.execute(
                    "INSERT INTO password_resets (token, email, expires_at, used, created_at) VALUES (?, ?, ?, 0, ?)",
                    (token, clean_email, expires_at, now.isoformat())
                )
                conn.commit()
        except Exception:
            pass

        return token

    def validate_password_reset_token(self, token: str) -> Optional[str]:
        """Retorna o email vinculado se o token for válido e não expirado/usado."""
        token_str = token.strip()
        # 1. Tenta consulta ao SQLite (se persistido localmente)
        try:
            with self._get_connection() as conn:
                cur = conn.cursor()
                cur.execute("SELECT email, expires_at, used FROM password_resets WHERE token = ?", (token_str,))
                row = cur.fetchone()
                if row:
                    if row["used"] == 1:
                        return None
                    try:
                        exp_dt = datetime.fromisoformat(row["expires_at"])
                        if datetime.now(timezone.utc) > exp_dt:
                            return None
                    except Exception:
                        return None
                    return row["email"]
        except Exception:
            pass

        # 2. Se não estiver no SQLite (ex: instâncias Serverless Vercel distintas), valida via JWT
        try:
            from .auth import jwt, JWT_SECRET, JWT_ALGORITHM
            if jwt:
                payload = jwt.decode(token_str, JWT_SECRET, algorithms=[JWT_ALGORITHM])
                if payload.get("type") == "password_reset":
                    return payload.get("sub")
        except Exception:
            pass

        return None

    def mark_password_reset_used(self, token: str) -> bool:
        try:
            with self._get_connection() as conn:
                cur = conn.cursor()
                cur.execute("UPDATE password_resets SET used = 1 WHERE token = ?", (token.strip(),))
                conn.commit()
                return cur.rowcount > 0
        except Exception:
            return False

    # -------------------------------------------------------------
    # 3. Verificação de E-mail
    # -------------------------------------------------------------
    def register_email_verification(self, email: str) -> str:
        token = secrets.token_hex(16)
        now = datetime.now(timezone.utc).isoformat()
        with self._get_connection() as conn:
            cur = conn.cursor()
            cur.execute(
                "INSERT OR REPLACE INTO email_verifications (email, verification_token, verified, created_at) VALUES (?, ?, 0, ?)",
                (email.strip().lower(), token, now)
            )
            conn.commit()
        return token

    def verify_email(self, token_or_email: str) -> bool:
        now = datetime.now(timezone.utc).isoformat()
        with self._get_connection() as conn:
            cur = conn.cursor()
            cur.execute(
                "UPDATE email_verifications SET verified = 1, verified_at = ? WHERE verification_token = ? OR email = ?",
                (now, token_or_email, token_or_email.strip().lower())
            )
            conn.commit()
            return cur.rowcount > 0

    def is_email_verified(self, email: str) -> bool:
        with self._get_connection() as conn:
            cur = conn.cursor()
            cur.execute("SELECT verified FROM email_verifications WHERE email = ?", (email.strip().lower(),))
            row = cur.fetchone()
            return bool(row and row["verified"] == 1)

    # -------------------------------------------------------------
    # 4. Aceite de Termos e Privacidade
    # -------------------------------------------------------------
    def record_terms_acceptance(
        self,
        user_id: str,
        email: str,
        terms_version: str = "1.0",
        privacy_version: str = "1.0",
        ip_address: Optional[str] = None
    ) -> None:
        now = datetime.now(timezone.utc).isoformat()
        with self._get_connection() as conn:
            cur = conn.cursor()
            cur.execute(
                """INSERT INTO terms_acceptances 
                   (user_id, email, terms_version, privacy_version, accepted_at, ip_address)
                   VALUES (?, ?, ?, ?, ?, ?)""",
                (user_id, email.strip().lower(), terms_version, privacy_version, now, ip_address)
            )
            conn.commit()

auth_service = AuthService()
