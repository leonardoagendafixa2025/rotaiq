"""
ROTA IQ - Supabase API Client
Conexão assíncrona/direta com PostgREST e Auth do Supabase utilizando urllib padrão.
"""

import os
import json
import urllib.request
import urllib.error
from typing import Dict, Any, List, Optional

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
        prefer: Optional[str] = None
    ) -> Any:
        url = f"{self.base_url}/rest/v1/{endpoint.lstrip('/')}"
        req_headers = dict(self.headers)
        if prefer:
            req_headers["Prefer"] = prefer

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
            with urllib.request.urlopen(req, timeout=10) as response:
                res_body = response.read().decode("utf-8")
                if res_body:
                    return json.loads(res_body)
                return None
        except urllib.error.HTTPError as e:
            err_body = e.read().decode("utf-8")
            print(f"[SupabaseClient ERROR] {method} {url} -> {e.code}: {err_body}")
            raise e

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

    def save_sync_evaluations(self, evaluations: List[Dict[str, Any]]) -> Any:
        return self._request("ride_evaluations", method="POST", data=evaluations)

    def save_sync_fuel(self, fuel_records: List[Dict[str, Any]]) -> Any:
        return self._request("fuel_records", method="POST", data=fuel_records)

    def save_sync_maintenance(self, maintenance_records: List[Dict[str, Any]]) -> Any:
        return self._request("maintenance_records", method="POST", data=maintenance_records)

    def save_sync_expenses(self, expenses: List[Dict[str, Any]]) -> Any:
        return self._request("vehicle_expenses", method="POST", data=expenses)

supabase = SupabaseClient()
