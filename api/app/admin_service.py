"""
ROTA IQ ADMIN — SERVIÇO DE BACKOFFICE E REPOSITÓRIO ADMINISTRATIVO
Arquitetura: ADMIN WEB -> API -> SERVICE -> REPOSITORY -> POSTGRESQL (Supabase)

Fornece persistência real, RBAC, auditoria de mutações, gestão de motoristas,
catálogo de planos, controle de feature flags e configurações do sistema.
"""

import os
import json
import sqlite3
import uuid
import hashlib
from datetime import datetime, timezone
from typing import Dict, Any, List, Optional, Tuple

from app.supabase_client import supabase
from app.auth import hash_password, verify_password, create_access_token

# Caminho do banco local persistente para tabelas complementares (sobrevive a restarts e reboots)
def get_admin_db_path() -> str:
    if os.environ.get("VERCEL") or os.environ.get("AWS_LAMBDA_FUNCTION_NAME"):
        return os.path.join("/tmp", "admin_store.db")
    local_dir = os.path.join(os.path.dirname(os.path.dirname(__file__)), "data")
    try:
        os.makedirs(local_dir, exist_ok=True)
        return os.path.join(local_dir, "admin_store.db")
    except (OSError, PermissionError):
        return os.path.join(os.environ.get("TEMP", "/tmp"), "admin_store.db")

LOCAL_DB_PATH = get_admin_db_path()


class AdminStore:
    """
    Camada de persistência relacional para configurações, feature flags e auditoria administrativa.
    Garante integridade e sobrevivência a reinicializações.
    """

    def __init__(self, db_path: str = None):
        self.db_path = db_path or get_admin_db_path()
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

            # 1. Administradores
            cur.execute("""
                CREATE TABLE IF NOT EXISTS admin_users (
                    id TEXT PRIMARY KEY,
                    email TEXT UNIQUE NOT NULL,
                    password_hash TEXT NOT NULL,
                    full_name TEXT NOT NULL,
                    role TEXT NOT NULL DEFAULT 'ADMIN',
                    permissions TEXT NOT NULL,
                    is_active INTEGER NOT NULL DEFAULT 1,
                    last_login_at TEXT,
                    created_at TEXT NOT NULL,
                    updated_at TEXT NOT NULL
                )
            """)

            # 2. Configurações do Sistema
            cur.execute("""
                CREATE TABLE IF NOT EXISTS system_settings (
                    key TEXT PRIMARY KEY,
                    value TEXT NOT NULL,
                    type TEXT NOT NULL DEFAULT 'string',
                    category TEXT NOT NULL DEFAULT 'GERAL',
                    description TEXT,
                    environment TEXT NOT NULL DEFAULT 'production',
                    updated_at TEXT NOT NULL,
                    updated_by TEXT DEFAULT 'system'
                )
            """)

            # 3. Feature Flags
            cur.execute("""
                CREATE TABLE IF NOT EXISTS feature_flags (
                    key TEXT PRIMARY KEY,
                    name TEXT NOT NULL,
                    description TEXT,
                    is_enabled INTEGER NOT NULL DEFAULT 1,
                    environment TEXT NOT NULL DEFAULT 'production',
                    updated_at TEXT NOT NULL,
                    updated_by TEXT DEFAULT 'system'
                )
            """)

            # 4. Auditoria Administrativa
            cur.execute("""
                CREATE TABLE IF NOT EXISTS admin_audit_logs (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    admin_id TEXT,
                    admin_email TEXT NOT NULL,
                    action TEXT NOT NULL,
                    resource TEXT NOT NULL,
                    resource_id TEXT,
                    old_value TEXT,
                    new_value TEXT,
                    ip_address TEXT,
                    created_at TEXT NOT NULL
                )
            """)

            # 5. Tokens de Dispositivos FCM
            cur.execute("""
                CREATE TABLE IF NOT EXISTS device_tokens (
                    id TEXT PRIMARY KEY,
                    user_id TEXT,
                    fcm_token TEXT UNIQUE NOT NULL,
                    platform TEXT NOT NULL DEFAULT 'android',
                    device_id TEXT,
                    app_version TEXT,
                    os_version TEXT,
                    active INTEGER NOT NULL DEFAULT 1,
                    notifications_enabled INTEGER NOT NULL DEFAULT 1,
                    last_seen_at TEXT NOT NULL,
                    created_at TEXT NOT NULL,
                    updated_at TEXT NOT NULL
                )
            """)

            # 6. Campanhas de Notificação
            cur.execute("""
                CREATE TABLE IF NOT EXISTS campaigns (
                    id TEXT PRIMARY KEY,
                    name TEXT NOT NULL,
                    type TEXT NOT NULL DEFAULT 'MARKETING',
                    title TEXT NOT NULL,
                    body TEXT NOT NULL,
                    image_url TEXT,
                    deep_link TEXT DEFAULT 'rotaiq://home',
                    status TEXT NOT NULL DEFAULT 'DRAFT',
                    audience_type TEXT NOT NULL DEFAULT 'ALL',
                    audience_filter TEXT DEFAULT '{}',
                    scheduled_at TEXT,
                    sent_at TEXT,
                    total_recipients INTEGER DEFAULT 0,
                    total_sent INTEGER DEFAULT 0,
                    total_failed INTEGER DEFAULT 0,
                    total_opened INTEGER DEFAULT 0,
                    created_by TEXT NOT NULL,
                    created_at TEXT NOT NULL,
                    updated_at TEXT NOT NULL
                )
            """)

            # 7. Entregas de Notificações
            cur.execute("""
                CREATE TABLE IF NOT EXISTS campaign_deliveries (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    campaign_id TEXT NOT NULL,
                    user_id TEXT,
                    fcm_token TEXT NOT NULL,
                    status TEXT NOT NULL,
                    fcm_message_id TEXT,
                    error_code TEXT,
                    sent_at TEXT NOT NULL,
                    opened_at TEXT,
                    created_at TEXT NOT NULL
                )
            """)

            # 8. Templates de Campanha
            cur.execute("""
                CREATE TABLE IF NOT EXISTS campaign_templates (
                    id TEXT PRIMARY KEY,
                    name TEXT NOT NULL,
                    type TEXT NOT NULL DEFAULT 'MARKETING',
                    title TEXT NOT NULL,
                    body TEXT NOT NULL,
                    deep_link TEXT DEFAULT 'rotaiq://home',
                    created_by TEXT NOT NULL DEFAULT 'system',
                    created_at TEXT NOT NULL,
                    updated_at TEXT NOT NULL
                )
            """)

            conn.commit()

            # Seed de Templates Padrão se tabela vazia
            cur.execute("SELECT COUNT(*) FROM campaign_templates")
            if cur.fetchone()[0] == 0:
                now_tmpl = datetime.now(timezone.utc).isoformat()
                cur.execute("""
                    INSERT INTO campaign_templates (id, name, type, title, body, deep_link, created_by, created_at, updated_at)
                    VALUES 
                    (?, ?, ?, ?, ?, ?, ?, ?, ?),
                    (?, ?, ?, ?, ?, ?, ?, ?, ?),
                    (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, (
                    str(uuid.uuid4()), "Atualização Disponível", "ATUALIZACAO", "🚀 Nova versão do ROTA IQ!", "Atualizamos o copiloto com novas métricas de lucro e suporte aprimorado. Toque para atualizar.", "rotaiq://home", "system", now_tmpl, now_tmpl,
                    str(uuid.uuid4()), "Oferta Especial Pro", "PROMOCAO", "💎 30% OFF no ROTA IQ Pro!", "Desbloqueie avaliações ilimitadas e HUD flutuante com desconto especial para você.", "rotaiq://subscription", "system", now_tmpl, now_tmpl,
                    str(uuid.uuid4()), "Alerta de Alta Demanda", "ENGAJAMENTO", "🔥 Chuva de Corridas na sua Região!", "A demanda está alta agora na sua praça. Abra o app e ative o filtro inteligente de corridas.", "rotaiq://rides", "system", now_tmpl, now_tmpl
                ))
                conn.commit()

            # Seed inicial de Super Admin se não existir
            cur.execute("SELECT id FROM admin_users WHERE email = ?", ("admin@rotai.app",))
            if not cur.fetchone():
                admin_id = str(uuid.uuid4())
                now = datetime.now(timezone.utc).isoformat()
                p_hash = hash_password("Admin@2026Secure")
                perms = json.dumps([
                    "SUPER_ADMIN", "VIEW_DASHBOARD", "MANAGE_USERS", "MANAGE_DRIVERS",
                    "MANAGE_PLANS", "MANAGE_SUBSCRIPTIONS", "MANAGE_PAYMENTS",
                    "MANAGE_FEATURE_FLAGS", "MANAGE_SETTINGS", "VIEW_AUDIT_LOGS"
                ])
                cur.execute("""
                    INSERT INTO admin_users (id, email, password_hash, full_name, role, permissions, is_active, created_at, updated_at)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, (admin_id, "admin@rotai.app", p_hash, "Leonardo (Proprietário ROTA IQ)", "SUPER_ADMIN", perms, 1, now, now))
                conn.commit()

            # Seed inicial de Configurações
            initial_settings = [
                ("FREE_DAILY_EVALUATION_LIMIT", "15", "number", "LIMITES", "Limite diário de avaliações para o plano gratuito"),
                ("PRO_MONTHLY_PRICE", "29.90", "number", "ASSINATURAS", "Preço do plano mensal Pro em reais"),
                ("PRO_ANNUAL_PRICE", "239.90", "number", "ASSINATURAS", "Preço do plano anual Pro em reais"),
                ("MAINTENANCE_MODE", "false", "boolean", "SISTEMA", "Ativa tela de manutenção geral no aplicativo"),
                ("DEFAULT_SCORE_THRESHOLD", "70", "number", "GERAL", "Nota de corte padrão mínima para recomendação de corrida"),
                ("ENABLE_AI", "true", "boolean", "SISTEMA", "Habilita inteligência de cálculo e predições"),
                ("ENABLE_VOICE", "true", "boolean", "SISTEMA", "Permite copiloto por áudio sintetizado TTS"),
                ("ENABLE_OVERLAY", "true", "boolean", "SISTEMA", "Permite HUD flutuante sobre outros aplicativos"),
                ("SESSION_DURATION_HOURS", "24", "number", "SISTEMA", "Duração máxima de sessão de token JWT em horas")
            ]
            for k, val, typ, cat, desc in initial_settings:
                cur.execute("SELECT key FROM system_settings WHERE key = ?", (k,))
                if not cur.fetchone():
                    now = datetime.now(timezone.utc).isoformat()
                    cur.execute("""
                        INSERT INTO system_settings (key, value, type, category, description, environment, updated_at, updated_by)
                        VALUES (?, ?, ?, ?, ?, 'production', ?, 'system')
                    """, (k, val, typ, cat, desc, now))

            # Seed inicial de Feature Flags
            initial_flags = [
                ("ENABLE_OVERLAY", "HUD Flutuante Dinâmico", "Overlay nativo sobre apps Uber/99", 1),
                ("ENABLE_VOICE", "Copiloto por Voz TTS", "Instruções por sintetizador de voz no fone", 1),
                ("ENABLE_ANALYTICS", "Analytics Operacional", "Telemetria e métricas de desempenho em tempo real", 1),
                ("ENABLE_HEATMAP", "Heatmap de Zonas", "Mapa térmico de demanda para motoristas", 1),
                ("ENABLE_AI", "Motor de IA Preditivo", "Cálculos matemáticos de custo e lucratividade", 1),
                ("ENABLE_NEW_SCORE", "Score IQ Aprimorado", "Nova fórmula de pontuação de 0 a 100", 1),
                ("ENABLE_DEADHEAD", "Preditor de Volta Vazia", "Alerta de áreas sem retorno financeiro", 1),
                ("ENABLE_SUBSCRIPTIONS", "Módulo de Assinaturas", "Cobrança via Pix e Google Play Billing", 1),
                ("ENABLE_MAINTENANCE_MODE", "Modo de Manutenção", "Bloqueia aplicativo para atualização técnica", 0)
            ]
            for k, nm, desc, en in initial_flags:
                cur.execute("SELECT key FROM feature_flags WHERE key = ?", (k,))
                if not cur.fetchone():
                    now = datetime.now(timezone.utc).isoformat()
                    cur.execute("""
                        INSERT INTO feature_flags (key, name, description, is_enabled, environment, updated_at, updated_by)
                        VALUES (?, ?, ?, ?, 'production', ?, 'system')
                    """, (k, nm, desc, en, now))

            conn.commit()

    # -------------------------------------------------------------
    # Dispositivos & Tokens FCM
    # -------------------------------------------------------------
    def register_device_token(
        self,
        user_id: Optional[str],
        fcm_token: str,
        platform: str = "android",
        device_id: Optional[str] = None,
        app_version: Optional[str] = None,
        os_version: Optional[str] = None,
        notifications_enabled: bool = True
    ) -> Dict[str, Any]:
        now = datetime.now(timezone.utc).isoformat()
        with self._get_connection() as conn:
            cur = conn.cursor()
            cur.execute("SELECT id, user_id FROM device_tokens WHERE fcm_token = ?", (fcm_token,))
            row = cur.fetchone()
            if row:
                cur.execute("""
                    UPDATE device_tokens SET
                        user_id = COALESCE(?, user_id),
                        platform = ?,
                        device_id = COALESCE(?, device_id),
                        app_version = COALESCE(?, app_version),
                        os_version = COALESCE(?, os_version),
                        active = 1,
                        notifications_enabled = ?,
                        last_seen_at = ?,
                        updated_at = ?
                    WHERE fcm_token = ?
                """, (user_id, platform, device_id, app_version, os_version, 1 if notifications_enabled else 0, now, now, fcm_token))
                token_id = row["id"]
            else:
                token_id = str(uuid.uuid4())
                cur.execute("""
                    INSERT INTO device_tokens (id, user_id, fcm_token, platform, device_id, app_version, os_version, active, notifications_enabled, last_seen_at, created_at, updated_at)
                    VALUES (?, ?, ?, ?, ?, ?, ?, 1, ?, ?, ?, ?)
                """, (token_id, user_id, fcm_token, platform, device_id, app_version, os_version, 1 if notifications_enabled else 0, now, now, now))
            conn.commit()

            return {
                "id": token_id,
                "user_id": user_id,
                "fcm_token": fcm_token,
                "platform": platform,
                "active": True,
                "notifications_enabled": notifications_enabled,
                "last_seen_at": now
            }

    def deactivate_device_token(self, fcm_token: str) -> bool:
        now = datetime.now(timezone.utc).isoformat()
        with self._get_connection() as conn:
            cur = conn.cursor()
            cur.execute("UPDATE device_tokens SET active = 0, updated_at = ? WHERE fcm_token = ?", (now, fcm_token))
            conn.commit()
            return cur.rowcount > 0

    def get_all_device_tokens(self) -> List[Dict[str, Any]]:
        with self._get_connection() as conn:
            cur = conn.cursor()
            cur.execute("SELECT * FROM device_tokens ORDER BY last_seen_at DESC")
            rows = cur.fetchall()
            return [
                {
                    "id": r["id"],
                    "user_id": r["user_id"],
                    "fcm_token": r["fcm_token"],
                    "platform": r["platform"],
                    "device_id": r["device_id"],
                    "app_version": r["app_version"],
                    "os_version": r["os_version"],
                    "active": bool(r["active"]),
                    "notifications_enabled": bool(r["notifications_enabled"]),
                    "last_seen_at": r["last_seen_at"],
                    "created_at": r["created_at"],
                    "updated_at": r["updated_at"]
                }
                for r in rows
            ]

    # -------------------------------------------------------------
    # Campanhas de Notificação
    # -------------------------------------------------------------
    def save_campaign(self, record: Dict[str, Any]) -> None:
        with self._get_connection() as conn:
            cur = conn.cursor()
            cur.execute("""
                INSERT INTO campaigns (id, name, type, title, body, image_url, deep_link, status, audience_type, audience_filter, scheduled_at, sent_at, total_recipients, total_sent, total_failed, total_opened, created_by, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """, (
                record["id"],
                record["name"],
                record["type"],
                record["title"],
                record["body"],
                record.get("image_url"),
                record.get("deep_link") or "rotaiq://home",
                record.get("status", "DRAFT"),
                record.get("audience_type", "ALL"),
                json.dumps(record.get("audience_filter") or {}),
                record.get("scheduled_at"),
                record.get("sent_at"),
                record.get("total_recipients", 0),
                record.get("total_sent", 0),
                record.get("total_failed", 0),
                record.get("total_opened", 0),
                record["created_by"],
                record["created_at"],
                record["updated_at"]
            ))
            conn.commit()

    def get_campaign(self, campaign_id: str) -> Optional[Dict[str, Any]]:
        with self._get_connection() as conn:
            cur = conn.cursor()
            cur.execute("SELECT * FROM campaigns WHERE id = ?", (campaign_id,))
            r = cur.fetchone()
            if not r:
                return None
            return {
                "id": r["id"],
                "name": r["name"],
                "type": r["type"],
                "title": r["title"],
                "body": r["body"],
                "image_url": r["image_url"],
                "deep_link": r["deep_link"],
                "status": r["status"],
                "audience_type": r["audience_type"],
                "audience_filter": json.loads(r["audience_filter"]) if r["audience_filter"] else {},
                "scheduled_at": r["scheduled_at"],
                "sent_at": r["sent_at"],
                "total_recipients": r["total_recipients"],
                "total_sent": r["total_sent"],
                "total_failed": r["total_failed"],
                "total_opened": r["total_opened"],
                "created_by": r["created_by"],
                "created_at": r["created_at"],
                "updated_at": r["updated_at"]
            }

    def list_campaigns(self, status: Optional[str] = None, type: Optional[str] = None) -> List[Dict[str, Any]]:
        with self._get_connection() as conn:
            cur = conn.cursor()
            query = "SELECT * FROM campaigns"
            params = []
            conditions = []
            if status and status.upper() != "ALL":
                conditions.append("status = ?")
                params.append(status.upper())
            if type and type.upper() != "ALL":
                conditions.append("type = ?")
                params.append(type.upper())
            if conditions:
                query += " WHERE " + " AND ".join(conditions)
            query += " ORDER BY created_at DESC"
            cur.execute(query, params)
            rows = cur.fetchall()
            return [
                {
                    "id": r["id"],
                    "name": r["name"],
                    "type": r["type"],
                    "title": r["title"],
                    "body": r["body"],
                    "image_url": r["image_url"],
                    "deep_link": r["deep_link"],
                    "status": r["status"],
                    "audience_type": r["audience_type"],
                    "audience_filter": json.loads(r["audience_filter"]) if r["audience_filter"] else {},
                    "scheduled_at": r["scheduled_at"],
                    "sent_at": r["sent_at"],
                    "total_recipients": r["total_recipients"],
                    "total_sent": r["total_sent"],
                    "total_failed": r["total_failed"],
                    "total_opened": r["total_opened"],
                    "created_by": r["created_by"],
                    "created_at": r["created_at"],
                    "updated_at": r["updated_at"]
                }
                for r in rows
            ]

    def update_campaign(self, campaign_id: str, updates: Dict[str, Any]) -> Optional[Dict[str, Any]]:
        now = datetime.now(timezone.utc).isoformat()
        updates["updated_at"] = now
        with self._get_connection() as conn:
            cur = conn.cursor()
            set_parts = []
            params = []
            for k, v in updates.items():
                set_parts.append(f"{k} = ?")
                if isinstance(v, (dict, list)):
                    params.append(json.dumps(v))
                else:
                    params.append(v)
            params.append(campaign_id)
            cur.execute(f"UPDATE campaigns SET {', '.join(set_parts)} WHERE id = ?", params)
            conn.commit()
            return self.get_campaign(campaign_id)

    def delete_campaign(self, campaign_id: str) -> bool:
        with self._get_connection() as conn:
            cur = conn.cursor()
            cur.execute("DELETE FROM campaigns WHERE id = ?", (campaign_id,))
            cur.execute("DELETE FROM campaign_deliveries WHERE campaign_id = ?", (campaign_id,))
            conn.commit()
            return cur.rowcount > 0

    def record_campaign_delivery(
        self,
        campaign_id: str,
        user_id: Optional[str],
        fcm_token: str,
        status: str,
        fcm_message_id: Optional[str] = None,
        error_code: Optional[str] = None,
        opened_at: Optional[str] = None
    ) -> None:
        now = datetime.now(timezone.utc).isoformat()
        with self._get_connection() as conn:
            cur = conn.cursor()
            cur.execute("""
                INSERT INTO campaign_deliveries (campaign_id, user_id, fcm_token, status, fcm_message_id, error_code, sent_at, opened_at, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """, (campaign_id, user_id, fcm_token, status, fcm_message_id, error_code, now, opened_at, now))
            conn.commit()

    def get_campaign_deliveries(self, campaign_id: str, limit: int = 500) -> List[Dict[str, Any]]:
        with self._get_connection() as conn:
            cur = conn.cursor()
            cur.execute("SELECT * FROM campaign_deliveries WHERE campaign_id = ? ORDER BY id DESC LIMIT ?", (campaign_id, limit))
            rows = cur.fetchall()
            return [
                {
                    "id": r["id"],
                    "campaign_id": r["campaign_id"],
                    "user_id": r["user_id"],
                    "fcm_token": r["fcm_token"],
                    "status": r["status"],
                    "fcm_message_id": r["fcm_message_id"],
                    "error_code": r["error_code"],
                    "sent_at": r["sent_at"],
                    "opened_at": r["opened_at"],
                    "created_at": r["created_at"]
                }
                for r in rows
            ]

    def list_campaign_templates(self) -> List[Dict[str, Any]]:
        with self._get_connection() as conn:
            cur = conn.cursor()
            cur.execute("SELECT * FROM campaign_templates ORDER BY created_at DESC")
            rows = cur.fetchall()
            return [
                {
                    "id": r["id"],
                    "name": r["name"],
                    "type": r["type"],
                    "title": r["title"],
                    "body": r["body"],
                    "deep_link": r["deep_link"],
                    "created_by": r["created_by"],
                    "created_at": r["created_at"],
                    "updated_at": r["updated_at"]
                }
                for r in rows
            ]

    def save_campaign_template(self, record: Dict[str, Any]) -> None:
        with self._get_connection() as conn:
            cur = conn.cursor()
            cur.execute("""
                INSERT INTO campaign_templates (id, name, type, title, body, deep_link, created_by, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """, (
                record["id"],
                record["name"],
                record["type"],
                record["title"],
                record["body"],
                record.get("deep_link") or "rotaiq://home",
                record.get("created_by", "admin"),
                record["created_at"],
                record["updated_at"]
            ))
            conn.commit()

    def delete_campaign_template(self, template_id: str) -> bool:
        with self._get_connection() as conn:
            cur = conn.cursor()
            cur.execute("DELETE FROM campaign_templates WHERE id = ?", (template_id,))
            conn.commit()
            return cur.rowcount > 0

    def get_campaigns_dashboard_stats(self) -> Dict[str, Any]:
        with self._get_connection() as conn:
            cur = conn.cursor()
            cur.execute("SELECT COUNT(*) FROM campaigns")
            total = cur.fetchone()[0]
            cur.execute("SELECT COUNT(*) FROM campaigns WHERE status = 'SENT'")
            sent = cur.fetchone()[0]
            cur.execute("SELECT COUNT(*) FROM campaigns WHERE status = 'SCHEDULED'")
            scheduled = cur.fetchone()[0]
            cur.execute("SELECT COUNT(*) FROM campaigns WHERE status = 'DRAFT'")
            drafts = cur.fetchone()[0]
            cur.execute("SELECT COUNT(*) FROM campaign_deliveries WHERE status = 'SENT'")
            deliv_sent = cur.fetchone()[0]
            cur.execute("SELECT COUNT(*) FROM campaign_deliveries WHERE status = 'FAILED'")
            deliv_failed = cur.fetchone()[0]
            cur.execute("SELECT COUNT(*) FROM device_tokens WHERE active = 1 AND notifications_enabled = 1")
            active_devices = cur.fetchone()[0]
            cur.execute("SELECT sent_at FROM campaigns WHERE status = 'SENT' ORDER BY sent_at DESC LIMIT 1")
            last_sent_row = cur.fetchone()
            last_sent = last_sent_row[0] if last_sent_row else None

            return {
                "total_campaigns": total,
                "sent_campaigns": sent,
                "scheduled_campaigns": scheduled,
                "draft_campaigns": drafts,
                "total_notifications_sent": deliv_sent,
                "total_notifications_failed": deliv_failed,
                "active_eligible_devices": active_devices,
                "eligible_devices": active_devices,
                "last_sent_at": last_sent,
                "average_open_rate": "N/D",
                "open_rate": "N/D"
            }

    def list_drivers(self, status: str = "ALL") -> List[Dict[str, Any]]:
        try:
            res = AdminService.get_drivers_paged(status_filter=status, page=1, limit=1000)
            return res.get("items", [])
        except Exception:
            return []


admin_store = AdminStore()


class AdminService:
    """
    Serviço que orquestra ações do Admin, combinando o PostgreSQL oficial do Supabase
    com persistência relacional protegida e trilhas de auditoria.
    """

    # -------------------------------------------------------------
    # 1. Health Check Real
    # -------------------------------------------------------------
    @staticmethod
    def get_database_health() -> Dict[str, Any]:
        start = datetime.now(timezone.utc)
        try:
            users_count = supabase.count_table("users")
            duration_ms = int((datetime.now(timezone.utc) - start).total_seconds() * 1000)
            return {
                "database": "connected",
                "status": "healthy",
                "provider": "Supabase PostgreSQL",
                "base_url": supabase.base_url,
                "latency_ms": duration_ms,
                "real_users_count": users_count,
                "timestamp": datetime.now(timezone.utc).isoformat()
            }
        except Exception as e:
            return {
                "database": "disconnected",
                "status": "unhealthy",
                "error": str(e),
                "timestamp": datetime.now(timezone.utc).isoformat()
            }

    # -------------------------------------------------------------
    # 2. Autenticação Administrativa & RBAC
    # -------------------------------------------------------------
    @staticmethod
    def authenticate_admin(email: str, password: str, requested_role: Optional[str] = None) -> Optional[Dict[str, Any]]:
        with admin_store._get_connection() as conn:
            cur = conn.cursor()
            cur.execute("SELECT * FROM admin_users WHERE email = ? AND is_active = 1", (email,))
            row = cur.fetchone()
            if not row:
                return None

            if not verify_password(password, row["password_hash"]):
                return None

            now = datetime.now(timezone.utc).isoformat()
            cur.execute("UPDATE admin_users SET last_login_at = ? WHERE id = ?", (now, row["id"]))
            conn.commit()

            role = row["role"]
            perms = json.loads(row["permissions"])

            token_data = {
                "sub": row["id"],
                "email": row["email"],
                "role": role,
                "permissions": perms,
                "type": "admin"
            }
            token = create_access_token(token_data)

            # Gravar auditoria de login
            AdminService.log_audit(
                admin_id=row["id"],
                admin_email=row["email"],
                action="ADMIN_LOGIN",
                resource="auth",
                resource_id=row["id"],
                new_value={"role": role}
            )

            return {
                "access_token": token,
                "token_type": "bearer",
                "admin": {
                    "id": row["id"],
                    "email": row["email"],
                    "full_name": row["full_name"],
                    "role": role,
                    "permissions": perms
                }
            }

    # -------------------------------------------------------------
    # 3. Dashboard Real (PostgreSQL + Supabase)
    # -------------------------------------------------------------
    @staticmethod
    def get_dashboard_metrics() -> Dict[str, Any]:
        # Contagens exatas direto do PostgreSQL
        total_users = supabase.count_table("users")
        total_drivers = supabase.count_table("drivers")
        active_drivers = supabase.count_table("drivers", "status=eq.ACTIVE")
        blocked_drivers = supabase.count_table("drivers", "status=eq.BLOCKED")
        total_evaluations = supabase.count_table("ride_evaluations")
        total_fuel = supabase.count_table("fuel_records")
        total_maintenance = supabase.count_table("maintenance_records")

        # Assinaturas
        active_subs = supabase.count_table("subscriptions", "status=eq.ACTIVE")
        cancelled_subs = supabase.count_table("subscriptions", "status=eq.CANCELLED")

        # MRR apurado com base em assinaturas ativas
        mrr = 0.0
        try:
            sub_rows = supabase._request("subscriptions?status=eq.ACTIVE&select=plan_code") or []
            for s in sub_rows:
                code = s.get("plan_code", "")
                if code == "pro_monthly":
                    mrr += 29.90
                elif code == "pro_annual":
                    mrr += (239.90 / 12.0)
        except Exception:
            mrr = 0.0

        arr = mrr * 12.0

        # Atividade recente (últimos motoristas criados)
        recent_drivers = []
        try:
            recent_drivers = supabase.get_drivers_list(limit=5)
        except Exception:
            pass

        return {
            "snapshot_date": datetime.now(timezone.utc).strftime("%Y-%m-%d %H:%M:%S"),
            "kpis": {
                "total_users": total_users,
                "total_drivers": total_drivers,
                "active_drivers": active_drivers,
                "blocked_drivers": blocked_drivers,
                "active_pro_subscribers": active_subs,
                "cancelled_subscriptions": cancelled_subs,
                "total_evaluations_recorded": total_evaluations,
                "total_fuel_records": total_fuel,
                "total_maintenance_records": total_maintenance,
                "mrr_reais": round(mrr, 2),
                "arr_reais": round(arr, 2),
                "churn_rate_percent": round((cancelled_subs / (active_subs + cancelled_subs) * 100.0) if (active_subs + cancelled_subs) > 0 else 0.0, 1)
            },
            "recent_activity": recent_drivers,
            "database_status": "POSTGRESQL_CONNECTED"
        }

    # -------------------------------------------------------------
    # 4. Motoristas CRUD Completo (PostgreSQL)
    # -------------------------------------------------------------
    @staticmethod
    def get_drivers_paged(
        search: Optional[str] = None,
        status_filter: Optional[str] = None,
        page: int = 1,
        limit: int = 25
    ) -> Dict[str, Any]:
        offset = (page - 1) * limit
        filter_parts = []

        if status_filter and status_filter.upper() != "ALL":
            filter_parts.append(f"status=eq.{status_filter.upper()}")

        if search:
            # Filtro por cidade ou id
            filter_parts.append(f"city=ilike.*{search}*")

        q = "&".join(filter_parts)
        endpoint = f"drivers?select=*&order=created_at.desc&limit={limit}&offset={offset}"
        if q:
            endpoint += f"&{q}"

        drivers_data = supabase._request(endpoint) or []
        total_count = supabase.count_table("drivers", q)

        # Enriquecer com dados do usuário correspondente (email, nome)
        enriched_drivers = []
        for d in drivers_data:
            user_id = d.get("user_id")
            user_info = supabase.get_user_by_id(user_id) if user_id else None
            enriched_drivers.append({
                "id": d.get("id"),
                "user_id": user_id,
                "name": user_info.get("full_name", "Motorista Cadastrado") if user_info else "Motorista Cadastrado",
                "email": user_info.get("email", "—") if user_info else "—",
                "phone": (user_info.get("phone") if user_info else None) or d.get("phone") or "—",
                "city": d.get("city", "São Paulo"),
                "state": d.get("state", "SP"),
                "status": d.get("status", "ACTIVE"),
                "plan_code": d.get("plan_code", "free"),
                "block_reason": d.get("block_reason"),
                "created_at": d.get("created_at"),
                "total_corridas": 0
            })

        return {
            "items": enriched_drivers,
            "total": total_count,
            "page": page,
            "limit": limit,
            "total_pages": max(1, (total_count + limit - 1) // limit)
        }

    @staticmethod
    def create_driver_admin(data: Dict[str, Any], admin_email: str) -> Dict[str, Any]:
        email = data.get("email")
        existing = supabase.get_user_by_email(email)
        if existing:
            raise ValueError(f"Já existe um usuário cadastrado com o e-mail '{email}'.")

        p_hash = hash_password(data.get("password", "Motorista@123"))
        user_record = {
            "email": email,
            "password_hash": p_hash,
            "full_name": data.get("name", "Motorista Novo"),
            "phone": data.get("phone"),
            "is_active": True,
            "created_at": datetime.now(timezone.utc).isoformat(),
            "updated_at": datetime.now(timezone.utc).isoformat()
        }
        try:
            created_user = supabase.insert_user(user_record)
        except Exception as e:
            if "duplicate key" in str(e) or "409" in str(e):
                raise ValueError("Telefone ou e-mail já cadastrado para outro usuário.")
            raise e
        user_id = created_user.get("id")

        driver_record = {
            "user_id": user_id,
            "city": data.get("city", "São Paulo"),
            "state": data.get("state", "SP"),
            "status": data.get("status", "ACTIVE"),
            "created_at": datetime.now(timezone.utc).isoformat()
        }
        created_driver = supabase.insert_driver(driver_record)

        AdminService.log_audit(
            admin_id=None,
            admin_email=admin_email,
            action="CREATE_DRIVER",
            resource="drivers",
            resource_id=created_driver.get("id"),
            new_value={"email": email, "city": data.get("city"), "status": data.get("status")}
        )

        return {**created_driver, "user": created_user}

    @staticmethod
    def update_driver_admin(driver_id: str, updates: Dict[str, Any], admin_email: str) -> Dict[str, Any]:
        # Buscar estado anterior para auditoria
        existing = supabase.get_driver_by_id(driver_id)
        if not existing:
            raise ValueError(f"Motorista ID '{driver_id}' não encontrado.")

        allowed_fields = ["city", "state", "status", "cpf", "cnh_number", "phone", "plan_code"]
        payload = {k: v for k, v in updates.items() if k in allowed_fields}

        updated = supabase._request(
            f"drivers?id=eq.{driver_id}",
            method="PATCH",
            data=payload,
            prefer="return=representation"
        )

        # Se houver atualização de nome ou email, atualiza usuário vinculado
        if ("name" in updates or "email" in updates) and existing.get("user_id"):
            u_payload = {}
            if "name" in updates: u_payload["full_name"] = updates["name"]
            if "email" in updates: u_payload["email"] = updates["email"]
            supabase._request(f"users?id=eq.{existing['user_id']}", method="PATCH", data=u_payload)

        AdminService.log_audit(
            admin_id=None,
            admin_email=admin_email,
            action="UPDATE_DRIVER",
            resource="drivers",
            resource_id=driver_id,
            old_value=existing,
            new_value=payload
        )

        return (updated[0] if updated else existing)

    @staticmethod
    def block_driver_admin(driver_id: str, reason: str, admin_email: str) -> Dict[str, Any]:
        payload = {
            "status": "BLOCKED"
        }
        res = supabase._request(f"drivers?id=eq.{driver_id}", method="PATCH", data=payload, prefer="return=representation")

        AdminService.log_audit(
            admin_id=None,
            admin_email=admin_email,
            action="BLOCK_DRIVER",
            resource="drivers",
            resource_id=driver_id,
            new_value={"reason": reason, "status": "BLOCKED"}
        )
        return res[0] if res else {"id": driver_id, "status": "BLOCKED"}

    @staticmethod
    def unblock_driver_admin(driver_id: str, admin_email: str) -> Dict[str, Any]:
        payload = {"status": "ACTIVE"}
        res = supabase._request(f"drivers?id=eq.{driver_id}", method="PATCH", data=payload, prefer="return=representation")

        AdminService.log_audit(
            admin_id=None,
            admin_email=admin_email,
            action="UNBLOCK_DRIVER",
            resource="drivers",
            resource_id=driver_id,
            new_value={"status": "ACTIVE"}
        )
        return res[0] if res else {"id": driver_id, "status": "ACTIVE"}

    # -------------------------------------------------------------
    # 5. Planos e Assinaturas CRUD (PostgreSQL subscription_plans)
    # -------------------------------------------------------------
    @staticmethod
    def get_plans_admin() -> List[Dict[str, Any]]:
        return supabase.get_subscription_plans()

    @staticmethod
    def create_plan_admin(data: Dict[str, Any], admin_email: str) -> Dict[str, Any]:
        code = data.get("code")
        if not code:
            code = data.get("name", "plano").lower().replace(" ", "_")

        record = {
            "id": str(uuid.uuid4()),
            "code": code,
            "name": data.get("name"),
            "price_cents": int(data.get("price_cents", 0)),
            "interval": data.get("interval", "month"),
            "features": data.get("features", []),
            "is_active": data.get("is_active", True),
            "created_at": datetime.now(timezone.utc).isoformat()
        }

        res = supabase._request("subscription_plans", method="POST", data=record, prefer="return=representation")

        AdminService.log_audit(
            admin_id=None,
            admin_email=admin_email,
            action="CREATE_PLAN",
            resource="subscription_plans",
            resource_id=code,
            new_value=record
        )
        return res[0] if res else record

    @staticmethod
    def update_plan_admin(plan_code: str, updates: Dict[str, Any], admin_email: str) -> Dict[str, Any]:
        # Buscar plano anterior
        current_plans = supabase.get_subscription_plans()
        old_plan = next((p for p in current_plans if p.get("code") == plan_code), None)
        if not old_plan:
            raise ValueError(f"Plano com código '{plan_code}' não encontrado.")

        allowed = ["name", "price_cents", "interval", "features", "is_active"]
        payload = {k: v for k, v in updates.items() if k in allowed}

        res = supabase._request(
            f"subscription_plans?code=eq.{plan_code}",
            method="PATCH",
            data=payload,
            prefer="return=representation"
        )

        AdminService.log_audit(
            admin_id=None,
            admin_email=admin_email,
            action="UPDATE_PLAN",
            resource="subscription_plans",
            resource_id=plan_code,
            old_value=old_plan,
            new_value=payload
        )
        return res[0] if res else {**old_plan, **payload}

    @staticmethod
    def toggle_plan_status(plan_code: str, is_active: bool, admin_email: str) -> Dict[str, Any]:
        res = supabase._request(
            f"subscription_plans?code=eq.{plan_code}",
            method="PATCH",
            data={"is_active": is_active},
            prefer="return=representation"
        )
        AdminService.log_audit(
            admin_id=None,
            admin_email=admin_email,
            action="TOGGLE_PLAN_STATUS",
            resource="subscription_plans",
            resource_id=plan_code,
            new_value={"is_active": is_active}
        )
        return res[0] if res else {"code": plan_code, "is_active": is_active}

    # -------------------------------------------------------------
    # 6. Feature Flags CRUD
    # -------------------------------------------------------------
    @staticmethod
    def get_feature_flags() -> List[Dict[str, Any]]:
        with admin_store._get_connection() as conn:
            cur = conn.cursor()
            cur.execute("SELECT * FROM feature_flags ORDER BY key ASC")
            rows = cur.fetchall()
            return [
                {
                    "key": r["key"],
                    "name": r["name"],
                    "description": r["description"],
                    "is_enabled": bool(r["is_enabled"]),
                    "environment": r["environment"],
                    "updated_at": r["updated_at"],
                    "updated_by": r["updated_by"]
                }
                for r in rows
            ]

    @staticmethod
    def set_feature_flag(key: str, is_enabled: bool, admin_email: str, name: Optional[str] = None, description: Optional[str] = None) -> Dict[str, Any]:
        with admin_store._get_connection() as conn:
            cur = conn.cursor()
            now = datetime.now(timezone.utc).isoformat()
            cur.execute("SELECT * FROM feature_flags WHERE key = ?", (key,))
            existing = cur.fetchone()

            if existing:
                cur.execute("""
                    UPDATE feature_flags 
                    SET is_enabled = ?, updated_at = ?, updated_by = ?
                    WHERE key = ?
                """, (1 if is_enabled else 0, now, admin_email, key))
            else:
                cur.execute("""
                    INSERT INTO feature_flags (key, name, description, is_enabled, environment, updated_at, updated_by)
                    VALUES (?, ?, ?, ?, 'production', ?, ?)
                """, (key, name or key, description or "Feature flag criada pelo Admin", 1 if is_enabled else 0, now, admin_email))

            conn.commit()

        AdminService.log_audit(
            admin_id=None,
            admin_email=admin_email,
            action="UPDATE_FEATURE_FLAG",
            resource="feature_flags",
            resource_id=key,
            new_value={"is_enabled": is_enabled}
        )

        return {"key": key, "is_enabled": is_enabled, "updated_at": now}

    # -------------------------------------------------------------
    # 7. Configurações do Sistema CRUD
    # -------------------------------------------------------------
    @staticmethod
    def get_system_settings() -> List[Dict[str, Any]]:
        with admin_store._get_connection() as conn:
            cur = conn.cursor()
            cur.execute("SELECT * FROM system_settings ORDER BY category, key ASC")
            rows = cur.fetchall()
            return [
                {
                    "key": r["key"],
                    "value": r["value"],
                    "type": r["type"],
                    "category": r["category"],
                    "description": r["description"],
                    "environment": r["environment"],
                    "updated_at": r["updated_at"],
                    "updated_by": r["updated_by"]
                }
                for r in rows
            ]

    @staticmethod
    def update_system_setting(key: str, value: str, admin_email: str) -> Dict[str, Any]:
        with admin_store._get_connection() as conn:
            cur = conn.cursor()
            cur.execute("SELECT * FROM system_settings WHERE key = ?", (key,))
            row = cur.fetchone()
            if not row:
                raise ValueError(f"Configuração '{key}' não encontrada.")

            now = datetime.now(timezone.utc).isoformat()
            cur.execute("UPDATE system_settings SET value = ?, updated_at = ?, updated_by = ? WHERE key = ?",
                        (str(value), now, admin_email, key))
            conn.commit()

        AdminService.log_audit(
            admin_id=None,
            admin_email=admin_email,
            action="UPDATE_SYSTEM_SETTING",
            resource="system_settings",
            resource_id=key,
            old_value={"value": row["value"]},
            new_value={"value": str(value)}
        )

        return {"key": key, "value": value, "updated_at": now}

    # -------------------------------------------------------------
    # 8. Assinaturas e Pagamentos Reais (PostgreSQL)
    # -------------------------------------------------------------
    @staticmethod
    def get_subscriptions_admin() -> List[Dict[str, Any]]:
        try:
            subs = supabase._request("subscriptions?select=*&order=created_at.desc&limit=50") or []
            return subs
        except Exception:
            return []

    @staticmethod
    def get_payments_admin() -> List[Dict[str, Any]]:
        try:
            txs = supabase._request("pix_transactions?select=*&order=created_at.desc&limit=50") or []
            return txs
        except Exception:
            return []

    # -------------------------------------------------------------
    # 9. Auditoria Administrativa
    # -------------------------------------------------------------
    @staticmethod
    def log_audit(
        admin_email: str,
        action: str,
        resource: str,
        resource_id: Optional[str] = None,
        old_value: Optional[Any] = None,
        new_value: Optional[Any] = None,
        admin_id: Optional[str] = None,
        ip_address: Optional[str] = None
    ):
        now = datetime.now(timezone.utc).isoformat()
        with admin_store._get_connection() as conn:
            cur = conn.cursor()
            cur.execute("""
                INSERT INTO admin_audit_logs (admin_id, admin_email, action, resource, resource_id, old_value, new_value, ip_address, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """, (
                admin_id,
                admin_email,
                action,
                resource,
                str(resource_id) if resource_id else None,
                json.dumps(old_value) if old_value else None,
                json.dumps(new_value) if new_value else None,
                ip_address,
                now
            ))
            conn.commit()

    @staticmethod
    def get_audit_logs(limit: int = 50) -> List[Dict[str, Any]]:
        with admin_store._get_connection() as conn:
            cur = conn.cursor()
            cur.execute("SELECT * FROM admin_audit_logs ORDER BY id DESC LIMIT ?", (limit,))
            rows = cur.fetchall()
            return [
                {
                    "id": r["id"],
                    "admin_email": r["admin_email"],
                    "action": r["action"],
                    "resource": r["resource"],
                    "resource_id": r["resource_id"],
                    "old_value": json.loads(r["old_value"]) if r["old_value"] else None,
                    "new_value": json.loads(r["new_value"]) if r["new_value"] else None,
                    "ip_address": r["ip_address"],
                    "created_at": r["created_at"]
                }
                for r in rows
            ]
