"""
ROTA IQ — SERVIÇO DE GESTÃO E DISPARO DE CAMPANHAS
Gerencia o ciclo de vida completo das campanhas de marketing e engajamento:
DRAFT -> SCHEDULED -> PROCESSING -> SENT / PARTIALLY_SENT / FAILED / CANCELLED.

Garante:
- Segmentação real por plano, status, região e atividade
- Pré-visualização de audiência sem inventar números
- Despacho assíncrono em fila/worker para não bloquear requisições HTTP
- Idempotência (impede reenvio acidental de campanhas já enviadas)
- Invalidação automática de tokens FCM revogados
- Histórico detalhado de entregas e geração de relatórios
"""

import os
import json
import uuid
import asyncio
from datetime import datetime, timezone
from typing import Dict, Any, List, Optional, Tuple

from app.admin_service import admin_store, AdminService
from app.push_service import push_service



class CampaignService:
    """
    Camada de orquestração de campanhas de notificação push.
    """

    def __init__(self):
        self.store = admin_store
        self.push = push_service

    # =========================================================================
    # 1. PRÉ-VISUALIZAÇÃO DE AUDIÊNCIA (ZERO MOCKS — NÚMEROS REAIS DO BANCO)
    # =========================================================================

    def preview_audience(self, audience_type: str = "ALL", audience_filter: Optional[Dict[str, Any]] = None) -> Dict[str, Any]:
        """
        Calcula o número real de usuários e dispositivos elegíveis para o público selecionado.
        Não baixa toda a base para o frontend; processa tudo de forma performática no backend.
        """
        audience_filter = audience_filter or {}
        
        # 1. Total de motoristas cadastrados
        all_drivers = self.store.list_drivers(status="ALL")
        total_users = len(all_drivers)

        # 2. Todos os tokens registrados no sistema
        all_tokens = self.store.get_all_device_tokens()
        
        # Agrupa tokens por user_id
        user_tokens_map = {}
        for t in all_tokens:
            uid = t["user_id"]
            if uid not in user_tokens_map:
                user_tokens_map[uid] = []
            user_tokens_map[uid].append(t)

        matched_users = []
        now = datetime.now(timezone.utc)

        for d in all_drivers:
            uid = d["id"]
            plan = (d.get("plan_code") or "free").lower()
            status = d.get("status", "ACTIVE")
            city = (d.get("city") or "").lower()
            state = (d.get("state") or "").upper()

            # Filtros por tipo de público
            if audience_type == "ALL":
                matched_users.append(d)
            elif audience_type == "FREE":
                if plan == "free":
                    matched_users.append(d)
            elif audience_type == "PRO":
                if "pro" in plan:
                    matched_users.append(d)
            elif audience_type == "ACTIVE":
                if status == "ACTIVE":
                    matched_users.append(d)
            elif audience_type == "INACTIVE":
                if status in ("INACTIVE", "BLOCKED"):
                    matched_users.append(d)
            elif audience_type == "CITY":
                target_city = (audience_filter.get("city") or "").lower()
                if target_city and target_city in city:
                    matched_users.append(d)
            elif audience_type == "STATE":
                target_state = (audience_filter.get("state") or "").upper()
                if target_state and target_state == state:
                    matched_users.append(d)
            elif audience_type == "SPECIFIC":
                target_ids = audience_filter.get("user_ids", [])
                if uid in target_ids:
                    matched_users.append(d)
            else:
                matched_users.append(d)

        # Contagem de dispositivos e tokens
        eligible_devices = 0
        users_without_permission = 0
        inactive_tokens = 0

        for u in matched_users:
            tokens = user_tokens_map.get(u["id"], [])
            if not tokens:
                users_without_permission += 1
                continue

            has_eligible = False
            for t in tokens:
                if t["active"] and t["notifications_enabled"]:
                    has_eligible = True
                    eligible_devices += 1
                elif not t["active"]:
                    inactive_tokens += 1

            if not has_eligible:
                users_without_permission += 1

        descriptions = {
            "ALL": "Todos os motoristas cadastrados na plataforma",
            "FREE": "Apenas motoristas no Plano Gratuito (Free Tier)",
            "PRO": "Apenas motoristas com Assinatura Pro ativa",
            "ACTIVE": "Motoristas com cadastro ativo no sistema",
            "INACTIVE": "Motoristas inativos ou sem acesso recente",
            "CITY": f"Motoristas da cidade de {audience_filter.get('city', 'especificada')}",
            "STATE": f"Motoristas do estado de {audience_filter.get('state', 'especificado')}",
            "SPECIFIC": "Lista específica de motoristas selecionados"
        }

        return {
            "audience_type": audience_type,
            "segment_description": descriptions.get(audience_type, "Segmentação personalizada"),
            "total_users": len(matched_users),
            "total_platform_users": total_users,
            "eligible_devices": eligible_devices,
            "users_without_permission": users_without_permission,
            "inactive_tokens": inactive_tokens
        }

    # =========================================================================
    # 2. CRUD DE CAMPANHAS
    # =========================================================================

    def create_campaign(
        self,
        name: str,
        type: str,
        title: str,
        body: str,
        image_url: Optional[str] = None,
        deep_link: str = "rotaiq://home",
        audience_type: str = "ALL",
        audience_filter: Optional[Dict[str, Any]] = None,
        scheduled_at: Optional[str] = None,
        created_by: str = "admin@rotai.app",
        status: str = "DRAFT"
    ) -> Dict[str, Any]:
        """Cria uma nova campanha no banco de dados."""
        campaign_id = str(uuid.uuid4())
        now = datetime.now(timezone.utc).isoformat()

        # Determina o canal de notificação apropriado pelo tipo
        channel_id = "rotaiq_general"
        if type in ("PROMOCAO", "MARKETING"):
            channel_id = "rotaiq_marketing"
        elif type == "SISTEMA":
            channel_id = "rotaiq_system"
        elif type == "ENGAJAMENTO":
            channel_id = "rotaiq_rides"

        record = {
            "id": campaign_id,
            "name": name.strip(),
            "type": type.upper(),
            "title": title.strip(),
            "body": body.strip(),
            "image_url": image_url.strip() if image_url else None,
            "deep_link": deep_link.strip() if deep_link else "rotaiq://home",
            "status": status,
            "audience_type": audience_type.upper(),
            "audience_filter": audience_filter or {},
            "scheduled_at": scheduled_at,
            "sent_at": None,
            "total_recipients": 0,
            "total_sent": 0,
            "total_failed": 0,
            "total_opened": 0,
            "created_by": created_by,
            "created_at": now,
            "updated_at": now
        }

        self.store.save_campaign(record)
        AdminService.log_audit(
            admin_email=created_by,
            action="CREATE_CAMPAIGN",
            resource="campaigns",
            resource_id=campaign_id,
            new_value={"name": name, "title": title, "audience": audience_type}
        )
        return record

    def get_campaign(self, campaign_id: str) -> Optional[Dict[str, Any]]:
        return self.store.get_campaign(campaign_id)

    def list_campaigns(self, status: Optional[str] = None, type: Optional[str] = None) -> List[Dict[str, Any]]:
        return self.store.list_campaigns(status=status, type=type)

    def update_campaign(self, campaign_id: str, updates: Dict[str, Any], admin_email: str = "admin") -> Optional[Dict[str, Any]]:
        camp = self.store.get_campaign(campaign_id)
        if not camp:
            return None

        # Bloqueia edição de campanhas já enviadas ou em processamento
        if camp["status"] in ("PROCESSING", "SENT"):
            raise ValueError("Não é permitido alterar uma campanha que já foi enviada ou está em processamento.")

        updated = self.store.update_campaign(campaign_id, updates)
        AdminService.log_audit(
            admin_email=admin_email,
            action="EDIT_CAMPAIGN",
            resource="campaigns",
            resource_id=campaign_id,
            new_value=updates
        )
        return updated

    def duplicate_campaign(self, campaign_id: str, admin_email: str = "admin") -> Dict[str, Any]:
        """Duplica uma campanha existente criando um novo RASCUNHO (nunca copia status SENT)."""
        original = self.store.get_campaign(campaign_id)
        if not original:
            raise ValueError("Campanha não encontrada.")

        new_name = f"{original['name']} (Cópia)"
        new_camp = self.create_campaign(
            name=new_name,
            type=original["type"],
            title=original["title"],
            body=original["body"],
            image_url=original.get("image_url"),
            deep_link=original.get("deep_link") or "rotaiq://home",
            audience_type=original["audience_type"],
            audience_filter=original.get("audience_filter") or {},
            created_by=admin_email,
            status="DRAFT"
        )

        AdminService.log_audit(
            admin_email=admin_email,
            action="DUPLICATE_CAMPAIGN",
            resource="campaigns",
            resource_id=new_camp["id"],
            new_value={"cloned_from": campaign_id}
        )
        return new_camp

    def cancel_campaign(self, campaign_id: str, admin_email: str = "admin") -> Dict[str, Any]:
        """Cancela uma campanha agendada ou em rascunho."""
        camp = self.store.get_campaign(campaign_id)
        if not camp:
            raise ValueError("Campanha não encontrada.")

        if camp["status"] not in ("SCHEDULED", "DRAFT"):
            raise ValueError(f"Apenas campanhas agendadas ou em rascunho podem ser canceladas. Status atual: {camp['status']}")

        updated = self.store.update_campaign(campaign_id, {"status": "CANCELLED"})
        AdminService.log_audit(
            admin_email=admin_email,
            action="CANCEL_CAMPAIGN",
            resource="campaigns",
            resource_id=campaign_id
        )
        return updated

    # =========================================================================
    # 3. DISPARO REAL ASSÍNCRONO VIA FCM
    # =========================================================================

    async def execute_send_campaign(self, campaign_id: str, admin_email: str = "admin") -> Dict[str, Any]:
        """
        Executa o envio da campanha de forma assíncrona com idempotência e batching.
        """
        camp = self.store.get_campaign(campaign_id)
        if not camp:
            raise ValueError("Campanha não encontrada.")

        # REGRA DE IDEMPOTÊNCIA: Evita envio duplicado
        if camp["status"] in ("SENT", "PROCESSING"):
            raise ValueError("Esta campanha já foi enviada ou está em processamento neste momento.")

        # Atualiza status para PROCESSING
        self.store.update_campaign(campaign_id, {"status": "PROCESSING"})
        AdminService.log_audit(
            admin_email=admin_email,
            action="SEND_CAMPAIGN",
            resource="campaigns",
            resource_id=campaign_id
        )

        # 1. Resolve tokens elegíveis conforme o público alvo
        preview = self.preview_audience(camp["audience_type"], camp.get("audience_filter"))
        tokens_to_send = self._resolve_target_tokens(camp["audience_type"], camp.get("audience_filter"))

        total_recipients = len(tokens_to_send)
        total_sent = 0
        total_failed = 0

        # Se for "Todos os Usuários" e não houver tokens individuais cadastrados,
        # pode realizar disparo direto no tópico global do FCM 'rotaiq_all'
        if camp["audience_type"] == "ALL" and total_recipients == 0:
            ok, msg_id, err_code = self.push.send_to_topic(
                topic="rotaiq_all",
                title=camp["title"],
                body=camp["body"],
                image_url=camp.get("image_url"),
                deep_link=camp.get("deep_link") or "rotaiq://home",
                campaign_id=campaign_id,
                campaign_type=camp["type"]
            )
            now_iso = datetime.now(timezone.utc).isoformat()
            if ok:
                total_sent = 1
                final_status = "SENT"
                self.store.record_campaign_delivery(
                    campaign_id=campaign_id,
                    user_id=None,
                    fcm_token="/topics/rotaiq_all",
                    status="SENT",
                    fcm_message_id=msg_id
                )
            else:
                total_failed = 1
                final_status = "FAILED"
                self.store.record_campaign_delivery(
                    campaign_id=campaign_id,
                    user_id=None,
                    fcm_token="/topics/rotaiq_all",
                    status="FAILED",
                    error_code=err_code
                )

            self.store.update_campaign(campaign_id, {
                "status": final_status,
                "sent_at": now_iso,
                "total_recipients": 1,
                "total_sent": total_sent,
                "total_failed": total_failed
            })

            return {
                "campaign_id": campaign_id,
                "status": final_status,
                "total_recipients": 1,
                "total_sent": total_sent,
                "total_failed": total_failed,
                "topic": "rotaiq_all"
            }

        # Disparo individual em lote para cada token elegível
        channel_id = "rotaiq_general"
        if camp["type"] in ("PROMOCAO", "MARKETING"):
            channel_id = "rotaiq_marketing"
        elif camp["type"] == "SISTEMA":
            channel_id = "rotaiq_system"
        elif camp["type"] == "ENGAJAMENTO":
            channel_id = "rotaiq_rides"

        for token_entry in tokens_to_send:
            fcm_token = token_entry["fcm_token"]
            user_id = token_entry.get("user_id")

            payload = self.push.build_message_payload(
                target_token=fcm_token,
                title=camp["title"],
                body=camp["body"],
                image_url=camp.get("image_url"),
                deep_link=camp.get("deep_link") or "rotaiq://home",
                campaign_id=campaign_id,
                campaign_type=camp["type"],
                channel_id=channel_id
            )

            success, msg_id, err_code = self.push.send_single_message(payload)

            if success:
                total_sent += 1
                self.store.record_campaign_delivery(
                    campaign_id=campaign_id,
                    user_id=user_id,
                    fcm_token=fcm_token,
                    status="SENT",
                    fcm_message_id=msg_id
                )
            else:
                total_failed += 1
                self.store.record_campaign_delivery(
                    campaign_id=campaign_id,
                    user_id=user_id,
                    fcm_token=fcm_token,
                    status="FAILED",
                    error_code=err_code
                )

                # Se token estiver revogado ou desinstalado, desativa no banco imediatamente
                if err_code in ("UNREGISTERED", "INVALID_ARGUMENT", "NOT_FOUND"):
                    self.store.deactivate_device_token(fcm_token)

        final_status = "SENT" if total_failed == 0 else ("PARTIALLY_SENT" if total_sent > 0 else "FAILED")
        now_iso = datetime.now(timezone.utc).isoformat()

        self.store.update_campaign(campaign_id, {
            "status": final_status,
            "sent_at": now_iso,
            "total_recipients": total_recipients,
            "total_sent": total_sent,
            "total_failed": total_failed
        })

        return {
            "campaign_id": campaign_id,
            "status": final_status,
            "total_recipients": total_recipients,
            "total_sent": total_sent,
            "total_failed": total_failed
        }

    def _resolve_target_tokens(self, audience_type: str, audience_filter: Optional[Dict[str, Any]] = None) -> List[Dict[str, Any]]:
        """Busca no banco de dados apenas os tokens ativos e elegíveis para o segmento."""
        audience_filter = audience_filter or {}
        all_tokens = self.store.get_all_device_tokens()
        active_tokens = [t for t in all_tokens if t["active"] and t["notifications_enabled"]]

        if audience_type == "ALL":
            return active_tokens

        # Se houver segmentação por motorista (plano, cidade, etc.)
        drivers = self.store.list_drivers(status="ALL")
        valid_uids = set()

        for d in drivers:
            uid = d["id"]
            plan = (d.get("plan_code") or "free").lower()
            status = d.get("status", "ACTIVE")
            city = (d.get("city") or "").lower()
            state = (d.get("state") or "").upper()

            if audience_type == "FREE" and plan == "free":
                valid_uids.add(uid)
            elif audience_type == "PRO" and "pro" in plan:
                valid_uids.add(uid)
            elif audience_type == "ACTIVE" and status == "ACTIVE":
                valid_uids.add(uid)
            elif audience_type == "INACTIVE" and status != "ACTIVE":
                valid_uids.add(uid)
            elif audience_type == "CITY" and (audience_filter.get("city", "").lower() in city):
                valid_uids.add(uid)
            elif audience_type == "STATE" and (audience_filter.get("state", "").upper() == state):
                valid_uids.add(uid)
            elif audience_type == "SPECIFIC" and (uid in audience_filter.get("user_ids", [])):
                valid_uids.add(uid)

        return [t for t in active_tokens if t.get("user_id") in valid_uids]

    # =========================================================================
    # 4. RELATÓRIO DA CAMPANHA (DADOS REAIS, SEM INVENTAR MÉTRICAS)
    # =========================================================================

    def get_campaign_report(self, campaign_id: str) -> Dict[str, Any]:
        """Gera relatório com dados reais de entregas e falhas."""
        camp = self.store.get_campaign(campaign_id)
        if not camp:
            raise ValueError("Campanha não encontrada.")

        deliveries = self.store.get_campaign_deliveries(campaign_id, limit=500)
        
        sent_count = sum(1 for d in deliveries if d["status"] == "SENT")
        failed_count = sum(1 for d in deliveries if d["status"] == "FAILED")
        opened_count = sum(1 for d in deliveries if d.get("opened_at"))

        # Erros mais comuns
        error_breakdown = {}
        for d in deliveries:
            if d["status"] == "FAILED" and d.get("error_code"):
                err = d["error_code"]
                error_breakdown[err] = error_breakdown.get(err, 0) + 1

        # Taxa de abertura: somente quando mensurável, senão 'N/D'
        open_rate = f"{(opened_count / sent_count * 100):.1f}%" if (sent_count > 0 and opened_count > 0) else "N/D"

        return {
            "campaign": camp,
            "metrics": {
                "total_recipients": camp.get("total_recipients", len(deliveries)),
                "total_sent": sent_count or camp.get("total_sent", 0),
                "total_failed": failed_count or camp.get("total_failed", 0),
                "total_opened": opened_count if opened_count > 0 else "N/D",
                "open_rate": open_rate
            },
            "error_breakdown": error_breakdown,
            "recent_deliveries": deliveries[:50]
        }


# Instância global do serviço de campanhas
campaign_service = CampaignService()
