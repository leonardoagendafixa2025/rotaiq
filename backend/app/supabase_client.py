"""
ROTA IQ - Supabase API Client
Conexão assíncrona/direta com PostgREST e Auth do Supabase utilizando urllib padrão.
Suporte a contagens exatas via cabeçalho Prefer: count=exact.
"""

import os
import json
import urllib.request
import urllib.error
from typing import Dict, Any, List, Optional, Tuple

# Carregar .env se existir localmente (sem vazar segredos para o Git)
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

SUPABASE_URL = os.getenv("SUPABASE_URL", "https://jkreduqzekllsmzxiugn.supabase.co")
SUPABASE_KEY = os.getenv("SUPABASE_KEY", "")

class SupabaseClient:

    def __init__(self, base_url: str = SUPABASE_URL, api_key: str = SUPABASE_KEY):
        self.base_url = base_url.rstrip("/")
        self.api_key = api_key
        self.headers = {
            "apikey": self.api_key,
            "Authorization": f"Bearer {self.api_key}",
            "Content-Type": "application/json",
            "User-Agent": "ROTA-IQ-Backend/1.0"
        }

    def _request(
        self,
        endpoint: str,
        method: str = "GET",
        data: Optional[Any] = None,
        prefer: Optional[str] = None,
        extra_headers: Optional[Dict[str, str]] = None
    ) -> Any:
        url = f"{self.base_url}/rest/v1/{endpoint.lstrip('/')}"
        req_headers = dict(self.headers)
        if prefer:
            req_headers["Prefer"] = prefer
        if extra_headers:
            req_headers.update(extra_headers)

        body_bytes = None
        if data is not None:
            body_bytes = json.dumps(data).encode("utf-8")

        req = urllib.request.Request(
            url=url,
            data=body_bytes,
            headers=req_headers,
            method=method
        )

        try:
            with urllib.request.urlopen(req, timeout=12) as response:
                res_body = response.read().decode("utf-8")
                if res_body:
                    return json.loads(res_body)
                return None
        except urllib.error.HTTPError as e:
            err_body = e.read().decode("utf-8")
            print(f"[SupabaseClient ERROR] {method} {url} -> {e.code}: {err_body}")
            raise e

    def count_table(self, table: str, query_filter: str = "") -> int:
        """Obtém contagem exata sem carregar linhas inteiras para a memória."""
        endpoint = f"{table}?select=id{('&' + query_filter) if query_filter else ''}"
        url = f"{self.base_url}/rest/v1/{endpoint}"
        req_headers = dict(self.headers)
        req_headers["Prefer"] = "count=exact"
        req_headers["Range"] = "0-0"

        req = urllib.request.Request(url=url, headers=req_headers, method="GET")
        try:
            with urllib.request.urlopen(req, timeout=10) as response:
                content_range = response.headers.get("Content-Range", "")
                if "/" in content_range:
                    total_str = content_range.split("/")[1]
                    if total_str.isdigit():
                        return int(total_str)
            return 0
        except urllib.error.HTTPError as e:
            if e.code == 416: # Range not satisfiable (tabela vazia)
                content_range = e.headers.get("Content-Range", "")
                if "/" in content_range:
                    total_str = content_range.split("/")[1]
                    if total_str.isdigit():
                        return int(total_str)
            return 0

    # -------------------------------------------------------------
    # 1. Usuários e Motoristas
    # -------------------------------------------------------------
    def get_user_by_email(self, email: str) -> Optional[Dict[str, Any]]:
        rows = self._request(f"users?email=eq.{email}&limit=1")
        return rows[0] if rows else None

    def get_user_by_id(self, user_id: str) -> Optional[Dict[str, Any]]:
        rows = self._request(f"users?id=eq.{user_id}&limit=1")
        return rows[0] if rows else None

    def insert_user(self, user_data: Dict[str, Any]) -> Dict[str, Any]:
        rows = self._request("users", method="POST", data=user_data, prefer="return=representation")
        return rows[0] if rows else user_data

    def get_driver_by_user_id(self, user_id: str) -> Optional[Dict[str, Any]]:
        rows = self._request(f"drivers?user_id=eq.{user_id}&limit=1")
        return rows[0] if rows else None

    def get_driver_by_id(self, driver_id: str) -> Optional[Dict[str, Any]]:
        rows = self._request(f"drivers?id=eq.{driver_id}&limit=1")
        return rows[0] if rows else None

    def insert_driver(self, driver_data: Dict[str, Any]) -> Dict[str, Any]:
        rows = self._request("drivers", method="POST", data=driver_data, prefer="return=representation")
        return rows[0] if rows else driver_data

    # -------------------------------------------------------------
    # 2. Veículos e Custos Operacionais
    # -------------------------------------------------------------
    def get_vehicles(self, driver_id: str) -> List[Dict[str, Any]]:
        return self._request(f"vehicles?driver_id=eq.{driver_id}&order=created_at.desc")

    def get_active_vehicle(self, driver_id: str) -> Optional[Dict[str, Any]]:
        rows = self._request(f"vehicles?driver_id=eq.{driver_id}&is_active=eq.true&limit=1")
        return rows[0] if rows else None

    def insert_vehicle(self, vehicle_data: Dict[str, Any]) -> Dict[str, Any]:
        rows = self._request("vehicles", method="POST", data=vehicle_data, prefer="return=representation")
        return rows[0] if rows else vehicle_data

    def update_vehicle(self, vehicle_id: str, updates: Dict[str, Any]) -> Optional[Dict[str, Any]]:
        rows = self._request(f"vehicles?id=eq.{vehicle_id}", method="PATCH", data=updates, prefer="return=representation")
        return rows[0] if rows else None

    def get_vehicle_costs(self, vehicle_id: str) -> Optional[Dict[str, Any]]:
        rows = self._request(f"vehicle_costs?vehicle_id=eq.{vehicle_id}&limit=1")
        return rows[0] if rows else None

    def upsert_vehicle_costs(self, costs_data: Dict[str, Any]) -> Dict[str, Any]:
        vehicle_id = costs_data.get("vehicle_id")
        existing = self.get_vehicle_costs(vehicle_id) if vehicle_id else None
        if existing:
            rows = self._request(f"vehicle_costs?vehicle_id=eq.{vehicle_id}", method="PATCH", data=costs_data, prefer="return=representation")
            return rows[0] if rows else costs_data
        else:
            rows = self._request("vehicle_costs", method="POST", data=costs_data, prefer="return=representation")
            return rows[0] if rows else costs_data

    # -------------------------------------------------------------
    # 3. Metas Operacionais
    # -------------------------------------------------------------
    def get_driver_goal(self, driver_id: str) -> Optional[Dict[str, Any]]:
        rows = self._request(f"driver_goals?driver_id=eq.{driver_id}&limit=1")
        return rows[0] if rows else None

    def upsert_driver_goal(self, goal_data: Dict[str, Any]) -> Dict[str, Any]:
        driver_id = goal_data.get("driver_id")
        existing = self.get_driver_goal(driver_id) if driver_id else None
        if existing:
            rows = self._request(f"driver_goals?driver_id=eq.{driver_id}", method="PATCH", data=goal_data, prefer="return=representation")
            return rows[0] if rows else goal_data
        else:
            rows = self._request("driver_goals", method="POST", data=goal_data, prefer="return=representation")
            return rows[0] if rows else goal_data

    # -------------------------------------------------------------
    # 4. Registros Financeiros (Combustível, Manutenção, Despesas)
    # -------------------------------------------------------------
    def get_fuel_records(self, driver_id: str, limit: int = 50) -> List[Dict[str, Any]]:
        return self._request(f"fuel_records?driver_id=eq.{driver_id}&order=date.desc,odometer_km.desc&limit={limit}")

    def insert_fuel_record(self, record: Dict[str, Any]) -> Dict[str, Any]:
        rows = self._request("fuel_records", method="POST", data=record, prefer="return=representation")
        return rows[0] if rows else record

    def get_maintenance_records(self, driver_id: str, limit: int = 50) -> List[Dict[str, Any]]:
        return self._request(f"maintenance_records?driver_id=eq.{driver_id}&order=date.desc&limit={limit}")

    def insert_maintenance_record(self, record: Dict[str, Any]) -> Dict[str, Any]:
        rows = self._request("maintenance_records", method="POST", data=record, prefer="return=representation")
        return rows[0] if rows else record

    def get_expenses(self, driver_id: str, limit: int = 50) -> List[Dict[str, Any]]:
        return self._request(f"vehicle_expenses?driver_id=eq.{driver_id}&order=date.desc&limit={limit}")

    def insert_expense(self, record: Dict[str, Any]) -> Dict[str, Any]:
        rows = self._request("vehicle_expenses", method="POST", data=record, prefer="return=representation")
        return rows[0] if rows else record

    # -------------------------------------------------------------
    # 5. Avaliações de Corridas
    # -------------------------------------------------------------
    def get_evaluations(
        self,
        driver_id: str,
        limit: int = 50,
        classification: Optional[str] = None
    ) -> List[Dict[str, Any]]:
        filter_str = f"driver_id=eq.{driver_id}"
        if classification:
            filter_str += f"&classification=eq.{classification}"
        return self._request(f"ride_evaluations?{filter_str}&order=evaluated_at.desc&limit={limit}")

    def insert_evaluation(self, evaluation: Dict[str, Any]) -> Dict[str, Any]:
        rows = self._request("ride_evaluations", method="POST", data=evaluation, prefer="return=representation")
        return rows[0] if rows else evaluation

    # -------------------------------------------------------------
    # 6. Planos e Assinaturas
    # -------------------------------------------------------------
    def get_plans(self) -> List[Dict[str, Any]]:
        return self._request("subscription_plans?is_active=eq.true&order=price_cents.asc")

    def insert_pix_order(self, order_data: Dict[str, Any]) -> Any:
        return self._request("pix_transactions", method="POST", data=order_data, prefer="return=representation")

    def get_pix_order(self, order_id: str) -> Optional[Dict[str, Any]]:
        rows = self._request(f"pix_transactions?order_id=eq.{order_id}&limit=1")
        return rows[0] if rows else None

    def update_pix_order(self, order_id: str, updates: Dict[str, Any]) -> Any:
        return self._request(f"pix_transactions?order_id=eq.{order_id}", method="PATCH", data=updates)

    def insert_play_receipt(self, receipt: Dict[str, Any]) -> Any:
        return self._request("play_billing_receipts", method="POST", data=receipt, prefer="return=representation")

    def insert_telemetry_batch(self, events: List[Dict[str, Any]]) -> Any:
        return self._request("sanitized_telemetry_events", method="POST", data=events)

    # -------------------------------------------------------------
    # 7. Sincronização em Lote (Offline-first)
    # -------------------------------------------------------------
    def save_sync_evaluations(self, evaluations: List[Dict[str, Any]]) -> Any:
        if not evaluations:
            return []
        return self._request("ride_evaluations", method="POST", data=evaluations, prefer="return=representation")

    def save_sync_fuel(self, fuel_records: List[Dict[str, Any]]) -> Any:
        if not fuel_records:
            return []
        return self._request("fuel_records", method="POST", data=fuel_records, prefer="return=representation")

    def save_sync_maintenance(self, maintenance_records: List[Dict[str, Any]]) -> Any:
        if not maintenance_records:
            return []
        return self._request("maintenance_records", method="POST", data=maintenance_records, prefer="return=representation")

    def save_sync_expenses(self, expenses: List[Dict[str, Any]]) -> Any:
        if not expenses:
            return []
        return self._request("vehicle_expenses", method="POST", data=expenses, prefer="return=representation")

    # -------------------------------------------------------------
    # 8. Gestão Administrativa, Feature Flags e LGPD
    # -------------------------------------------------------------
    def get_drivers_list(self, limit: int = 50) -> List[Dict[str, Any]]:
        return self._request(f"drivers?order=created_at.desc&limit={limit}") or []

    _in_memory_flags = {
        "copilot_voice_tts": {"key": "copilot_voice_tts", "is_enabled": True, "description": "Síntese vocal do Copiloto TTS"},
        "deadhead_prediction": {"key": "deadhead_prediction", "is_enabled": True, "description": "Predição de volta vazia (deadhead)"},
        "pix_instant_checkout": {"key": "pix_instant_checkout", "is_enabled": True, "description": "Checkout instantâneo via PIX Copia e Cola"}
    }

    def get_feature_flags(self) -> List[Dict[str, Any]]:
        try:
            res = self._request("feature_flags?order=key.asc")
            if res:
                return res
        except Exception:
            pass
        return list(self._in_memory_flags.values())

    def upsert_feature_flag(self, flag_data: Dict[str, Any]) -> Any:
        key = flag_data.get("key")
        if key:
            self._in_memory_flags[key] = flag_data
        try:
            existing = self._request(f"feature_flags?key=eq.{key}&limit=1")
            if existing:
                return self._request(f"feature_flags?key=eq.{key}", method="PATCH", data=flag_data, prefer="return=representation")
            else:
                return self._request("feature_flags", method="POST", data=flag_data, prefer="return=representation")
        except Exception:
            return flag_data

    def insert_lgpd_deletion(self, deletion_data: Dict[str, Any]) -> Any:
        try:
            return self._request("lgpd_deletion_requests", method="POST", data=deletion_data, prefer="return=representation")
        except Exception:
            return deletion_data

    def export_driver_full_data(self, driver_id: str, user_id: str) -> Dict[str, Any]:
        user = self.get_user_by_id(user_id) or {}
        driver = self.get_driver_by_id(driver_id) or {}
        vehicles = self.get_vehicles(driver_id) or []
        evaluations = self.get_evaluations(driver_id, limit=200) or []
        fuel = self.get_fuel_records(driver_id, limit=200) or []
        maintenance = self.get_maintenance_records(driver_id, limit=200) or []
        expenses = self.get_expenses(driver_id, limit=200) or []

        return {
            "exported_at": os.getenv("EXPORT_TIMESTAMP", ""),
            "user": {
                "id": user.get("id"),
                "email": user.get("email"),
                "full_name": user.get("full_name"),
                "created_at": user.get("created_at")
            },
            "driver": driver,
            "vehicles": vehicles,
            "ride_evaluations_count": len(evaluations),
            "ride_evaluations": evaluations,
            "fuel_records": fuel,
            "maintenance_records": maintenance,
            "expenses": expenses
        }

    # -------------------------------------------------------------
    # 9. Assinaturas e Pagamentos PIX (Comercialização)
    # -------------------------------------------------------------
    def get_subscription_plans(self) -> List[Dict[str, Any]]:
        try:
            return self._request("subscription_plans?is_active=eq.true&order=price_cents.asc") or []
        except Exception:
            return [
                {
                    "code": "pro_monthly",
                    "name": "ROTA IQ Pro Mensal",
                    "price_cents": 2990,
                    "billing_period": "month",
                    "features": ["Avaliações ilimitadas", "HUD Flutuante", "Copiloto por Voz TTS", "Filtro de Aceite Avançado"]
                },
                {
                    "code": "pro_annual",
                    "name": "ROTA IQ Pro Anual",
                    "price_cents": 23990,
                    "billing_period": "year",
                    "features": ["Avaliações ilimitadas", "HUD Flutuante", "Copiloto por Voz TTS", "Filtro de Aceite Avançado", "33% de Desconto"]
                }
            ]

    def create_pix_transaction(self, pix_data: Dict[str, Any]) -> Any:
        try:
            return self._request("pix_transactions", method="POST", data=pix_data, prefer="return=representation")
        except Exception:
            return pix_data

    def get_pix_transaction(self, order_id: str) -> Optional[Dict[str, Any]]:
        try:
            rows = self._request(f"pix_transactions?order_id=eq.{order_id}&limit=1")
            return rows[0] if rows else None
        except Exception:
            return None

    def update_pix_status(self, order_id: str, status: str, paid_at: Optional[str] = None) -> Any:
        try:
            payload = {"status": status}
            if paid_at:
                payload["paid_at"] = paid_at
            return self._request(f"pix_transactions?order_id=eq.{order_id}", method="PATCH", data=payload, prefer="return=representation")
        except Exception:
            return None

    def create_or_update_subscription(self, sub_data: Dict[str, Any]) -> Any:
        try:
            return self._request("subscriptions", method="POST", data=sub_data, prefer="return=representation")
        except Exception:
            return sub_data

    def get_active_subscription(self, driver_id: str) -> Optional[Dict[str, Any]]:
        try:
            rows = self._request(f"subscriptions?driver_id=eq.{driver_id}&status=eq.ACTIVE&order=created_at.desc&limit=1")
            return rows[0] if rows else None
        except Exception:
            return None

    def insert_telemetry_batch(self, events: List[Dict[str, Any]]) -> Any:
        if not events:
            return []
        try:
            return self._request("telemetry_events", method="POST", data=events, prefer="return=representation")
        except Exception:
            return events

supabase = SupabaseClient()

