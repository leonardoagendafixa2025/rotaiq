"""
ROTA IQ - Auth Persistence & Security Service (Supabase PostgreSQL + SQLite Local Fallback)
Gerenciamento de revogação de tokens (logout real), recuperação de senhas (tokens temporários únicos),
verificação de e-mail e registro de aceite de Termos de Uso e Política de Privacidade.

Projetado para funcionar perfeitamente em ambientes Serverless (Vercel) e locais.
"""

import os
import sqlite3
import secrets
import hashlib
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
        self._init_sqlite()

    def _get_connection(self) -> sqlite3.Connection:
        conn = sqlite3.connect(self.db_path)
        conn.row_factory = sqlite3.Row
        return conn

    def _init_sqlite(self):
        try:
            with self._get_connection() as conn:
                cur = conn.cursor()
                cur.execute("""
                    CREATE TABLE IF NOT EXISTS revoked_tokens (
                        token_hash TEXT PRIMARY KEY,
                        revoked_at TEXT NOT NULL
                    )
                """)
                cur.execute("""
                    CREATE TABLE IF NOT EXISTS password_resets (
                        token TEXT PRIMARY KEY,
                        email TEXT NOT NULL,
                        expires_at TEXT NOT NULL,
                        used INTEGER NOT NULL DEFAULT 0,
                        created_at TEXT NOT NULL
                    )
                """)
                cur.execute("""
                    CREATE TABLE IF NOT EXISTS email_verifications (
                        email TEXT PRIMARY KEY,
                        verification_token TEXT NOT NULL,
                        verified INTEGER NOT NULL DEFAULT 0,
                        verified_at TEXT,
                        created_at TEXT NOT NULL
                    )
                """)
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
        except Exception as e:
            print(f"[AuthService] Aviso ao inicializar SQLite local: {e}")

    # -------------------------------------------------------------
    # 1. Revogação de Tokens (Logout Real)
    # -------------------------------------------------------------
    def revoke_token(self, token: str) -> None:
        thash = hashlib.sha256(token.encode("utf-8")).hexdigest()
        now_iso = datetime.now(timezone.utc).isoformat()

        # 1. Tentar persistência no Supabase PostgreSQL (sobrevive a lambdas Vercel)
        try:
            from .supabase_client import supabase
            supabase._request(
                "revoked_tokens",
                method="POST",
                data={"token_hash": thash, "revoked_at": now_iso},
                prefer="resolution=merge-duplicates"
            )
        except Exception:
            pass

        # 2. Persistência local no SQLite
        try:
            with self._get_connection() as conn:
                cur = conn.cursor()
                cur.execute(
                    "INSERT OR REPLACE INTO revoked_tokens (token_hash, revoked_at) VALUES (?, ?)",
                    (thash, now_iso)
                )
                conn.commit()
        except Exception:
            pass

    def is_token_revoked(self, token: str) -> bool:
        thash = hashlib.sha256(token.encode("utf-8")).hexdigest()

        # 1. Consulta ao Supabase PostgreSQL
        try:
            from .supabase_client import supabase
            rows = supabase._request(f"revoked_tokens?token_hash=eq.{thash}&limit=1")
            if rows and len(rows) > 0:
                return True
        except Exception:
            pass

        # 2. Fallback ao SQLite local
        try:
            with self._get_connection() as conn:
                cur = conn.cursor()
                cur.execute("SELECT 1 FROM revoked_tokens WHERE token_hash = ?", (thash,))
                if cur.fetchone() is not None:
                    return True
        except Exception:
            pass

        return False

    # -------------------------------------------------------------
    # 2. Recuperação de Senha (Tokens descartáveis de 1 hora)
    # -------------------------------------------------------------
    def create_password_reset_token(self, email: str, expiry_hours: int = 1) -> str:
        clean_email = email.strip().lower()
        now = datetime.now(timezone.utc)
        expires_at_iso = (now + timedelta(hours=expiry_hours)).isoformat()
        now_iso = now.isoformat()

        # Gera token único e legível
        token = secrets.token_urlsafe(32)

        # 1. Salvar no Supabase PostgreSQL
        try:
            from .supabase_client import supabase
            supabase._request(
                "password_resets",
                method="POST",
                data={
                    "token": token,
                    "email": clean_email,
                    "expires_at": expires_at_iso,
                    "used": False,
                    "created_at": now_iso
                },
                prefer="return=minimal"
            )
        except Exception:
            pass

        # 2. Salvar no SQLite local
        try:
            with self._get_connection() as conn:
                cur = conn.cursor()
                cur.execute(
                    "INSERT OR REPLACE INTO password_resets (token, email, expires_at, used, created_at) VALUES (?, ?, ?, 0, ?)",
                    (token, clean_email, expires_at_iso, now_iso)
                )
                conn.commit()
        except Exception:
            pass

        return token

    def validate_password_reset_token(self, token: str) -> Optional[str]:
        token_str = token.strip()
        now = datetime.now(timezone.utc)

        # 1. Consulta ao Supabase PostgreSQL
        try:
            from .supabase_client import supabase
            rows = supabase._request(f"password_resets?token=eq.{token_str}&limit=1")
            if rows and len(rows) > 0:
                row = rows[0]
                if row.get("used") in (True, 1):
                    return None
                exp_str = row.get("expires_at", "")
                if exp_str:
                    exp_dt = datetime.fromisoformat(exp_str.replace("Z", "+00:00"))
                    if now > exp_dt:
                        return None
                return row.get("email")
        except Exception:
            pass

        # 2. Fallback ao SQLite local
        try:
            with self._get_connection() as conn:
                cur = conn.cursor()
                cur.execute("SELECT email, expires_at, used FROM password_resets WHERE token = ?", (token_str,))
                row = cur.fetchone()
                if row:
                    if row["used"] == 1:
                        return None
                    exp_dt = datetime.fromisoformat(row["expires_at"].replace("Z", "+00:00"))
                    if now > exp_dt:
                        return None
                    return row["email"]
        except Exception:
            pass

        # 3. Fallback JWT decodificável se tiver sido emitido via JWT
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
        token_str = token.strip()

        # 1. Supabase PostgreSQL
        try:
            from .supabase_client import supabase
            supabase._request(
                f"password_resets?token=eq.{token_str}",
                method="PATCH",
                data={"used": True}
            )
        except Exception:
            pass

        # 2. SQLite local
        try:
            with self._get_connection() as conn:
                cur = conn.cursor()
                cur.execute("UPDATE password_resets SET used = 1 WHERE token = ?", (token_str,))
                conn.commit()
                return cur.rowcount > 0
        except Exception:
            return False

    # -------------------------------------------------------------
    # 3. Verificação de E-mail
    # -------------------------------------------------------------
    def register_email_verification(self, email: str) -> str:
        clean_email = email.strip().lower()
        token = secrets.token_hex(16)
        now_iso = datetime.now(timezone.utc).isoformat()

        # 1. Supabase
        try:
            from .supabase_client import supabase
            supabase._request(
                "email_verifications",
                method="POST",
                data={
                    "email": clean_email,
                    "verification_token": token,
                    "verified": False,
                    "created_at": now_iso
                },
                prefer="resolution=merge-duplicates"
            )
        except Exception:
            pass

        # 2. SQLite
        try:
            with self._get_connection() as conn:
                cur = conn.cursor()
                cur.execute(
                    "INSERT OR REPLACE INTO email_verifications (email, verification_token, verified, created_at) VALUES (?, ?, 0, ?)",
                    (clean_email, token, now_iso)
                )
                conn.commit()
        except Exception:
            pass

        return token

    def verify_email(self, token_or_email: str) -> bool:
        target = token_or_email.strip()
        now_iso = datetime.now(timezone.utc).isoformat()

        # 1. Supabase
        try:
            from .supabase_client import supabase
            # Tenta atualizar por token
            rows = supabase._request(
                f"email_verifications?verification_token=eq.{target}",
                method="PATCH",
                data={"verified": True, "verified_at": now_iso},
                prefer="return=representation"
            )
            if rows:
                return True
            # Tenta atualizar por email
            rows_email = supabase._request(
                f"email_verifications?email=eq.{target.lower()}",
                method="PATCH",
                data={"verified": True, "verified_at": now_iso},
                prefer="return=representation"
            )
            if rows_email:
                return True
        except Exception:
            pass

        # 2. SQLite fallback
        try:
            with self._get_connection() as conn:
                cur = conn.cursor()
                cur.execute(
                    "UPDATE email_verifications SET verified = 1, verified_at = ? WHERE verification_token = ? OR email = ?",
                    (now_iso, target, target.lower())
                )
                conn.commit()
                return cur.rowcount > 0
        except Exception:
            return False

    def is_email_verified(self, email: str) -> bool:
        clean_email = email.strip().lower()

        # 1. Supabase
        try:
            from .supabase_client import supabase
            rows = supabase._request(f"email_verifications?email=eq.{clean_email}&limit=1")
            if rows and len(rows) > 0:
                return bool(rows[0].get("verified"))
        except Exception:
            pass

        # 2. SQLite
        try:
            with self._get_connection() as conn:
                cur = conn.cursor()
                cur.execute("SELECT verified FROM email_verifications WHERE email = ?", (clean_email,))
                row = cur.fetchone()
                return bool(row and row["verified"] == 1)
        except Exception:
            return False

    # -------------------------------------------------------------
    # 4. Aceite de Termos e Privacidade (LGPD Compliance)
    # -------------------------------------------------------------
    def record_terms_acceptance(
        self,
        user_id: str,
        email: str,
        terms_version: str = "1.0",
        privacy_version: str = "1.0",
        ip_address: Optional[str] = None
    ) -> None:
        clean_email = email.strip().lower()
        now_iso = datetime.now(timezone.utc).isoformat()

        # 1. Supabase
        try:
            from .supabase_client import supabase
            supabase._request(
                "terms_acceptances",
                method="POST",
                data={
                    "user_id": user_id,
                    "email": clean_email,
                    "terms_version": terms_version,
                    "privacy_version": privacy_version,
                    "accepted_at": now_iso,
                    "ip_address": ip_address
                },
                prefer="return=minimal"
            )
        except Exception:
            pass

        # 2. SQLite
        try:
            with self._get_connection() as conn:
                cur = conn.cursor()
                cur.execute(
                    """INSERT INTO terms_acceptances 
                       (user_id, email, terms_version, privacy_version, accepted_at, ip_address)
                       VALUES (?, ?, ?, ?, ?, ?)""",
                    (user_id, clean_email, terms_version, privacy_version, now_iso, ip_address)
                )
                conn.commit()
        except Exception:
            pass


auth_service = AuthService()
