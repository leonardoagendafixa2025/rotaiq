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
LOCAL_DB_DIR = os.path.join(os.path.dirname(os.path.dirname(__file__)), "data")
os.makedirs(LOCAL_DB_DIR, exist_ok=True)
LOCAL_DB_PATH = os.path.join(LOCAL_DB_DIR, "admin_store.db")


class AdminStore:
    """
    Camada de persistência relacional para configurações, feature flags e auditoria administrativa.
    Garante integridade e sobrevivência a reinicializações.
    """

    def __init__(self, db_path: str = LOCAL_DB_PATH):
        self.db_path = db_path
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
