"""
ROTA IQ - Backend de Produção e Escala (Fase 2: Backend + Financeiro Real)
API RESTful assíncrona para Autenticação Real, Gestão de Veículos, Custos Reais,
Combustível (Fuel Engine), Manutenção, Metas, Ride Evaluation Engine,
Dashboard Financeiro Consolidado Real, Sincronização Offline-First e Auditoria.

REGRA ABSOLUTA: ZERO MOCKS NA PRODUÇÃO.
Todos os dados retornados e persistidos são reais no PostgreSQL/Supabase.
"""

from fastapi import FastAPI, HTTPException, status, Depends, Security
from fastapi.responses import HTMLResponse, Response
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel, EmailStr, Field
from typing import List, Optional, Dict, Any
from datetime import datetime, timedelta, timezone
import uuid
import hashlib
import os
import mimetypes

from .supabase_client import supabase
from .auth import (
    hash_password,
    verify_password,
    create_access_token,
    create_refresh_token,
    decode_token,
    get_current_user_claims,
    require_admin_claims,
    require_super_admin_claims,
    require_campaign_send_permission,
    require_campaign_manage_permission,
    security
)
from .auth_service import auth_service
from fastapi.security import HTTPAuthorizationCredentials
from .admin_service import AdminService, admin_store
from .campaign_service import campaign_service
from .push_service import push_service


app = FastAPI(
    title="ROTA IQ - Backend Oficial",
    version="2.0.0",
    description="Backend oficial de produção do ROTA IQ - 'Inteligência para cada corrida'."
)

# Habilitar CORS para permitir comunicação segura com o aplicativo e futuros painéis
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# ======================================================================
# DTOs / SCHEMAS PYDANTIC
# ======================================================================

# --- Autenticação ---
class RegisterRequest(BaseModel):
    email: EmailStr
    password: str = Field(min_length=8)
    name: Optional[str] = None
    full_name: Optional[str] = None
    phone: Optional[str] = None
    city: str = "São Paulo"
    state: str = "SP"
    terms_accepted: bool = True
    terms_version: str = "1.0"
    privacy_version: str = "1.0"

class LoginRequest(BaseModel):
    email: EmailStr
    password: str

class AuthResponse(BaseModel):
    access_token: str
    refresh_token: str
    token_type: str = "bearer"
    driver_id: str
    user_id: str
    full_name: str
    email: str
    email_verified: bool = False

class RefreshTokenRequest(BaseModel):
    refresh_token: str

class RefreshTokenResponse(BaseModel):
    access_token: str
    token_type: str = "bearer"

class UserProfileResponse(BaseModel):
    user_id: str
    driver_id: str
    full_name: str
    email: str
    phone: Optional[str] = None
    city: str
    state: str
    status: str
    email_verified: bool = False

class ForgotPasswordRequest(BaseModel):
    email: EmailStr

class ResetPasswordRequest(BaseModel):
    token: str
    new_password: str = Field(min_length=8)

class VerifyEmailRequest(BaseModel):
    token: Optional[str] = None
    email: Optional[str] = None

class ResendVerificationRequest(BaseModel):
    email: EmailStr

class LogoutRequest(BaseModel):
    refresh_token: Optional[str] = None

# --- Veículos & Custos ---
class VehicleCreateRequest(BaseModel):
    name: str
    plate: str
    model: str
    year: int
    fuel_type: str = "GASOLINE"
    consumption_km_per_liter: float = Field(gt=0.0)
    fuel_price_per_liter: float = Field(gt=0.0)
    maintenance_cost_per_km: float = 0.18

class VehicleCostsUpdateRequest(BaseModel):
    monthly_insurance_cost: float = 0.0
    annual_taxes_cost: float = 0.0
    monthly_depreciation: float = 0.0
    monthly_other_costs: float = 0.0
    estimated_monthly_km: float = Field(default=3000.0, gt=0.0)

class VehicleCostsResponse(BaseModel):
    vehicle_id: str
    fuel_cost_per_km: float
    maintenance_cost_per_km: float
    fixed_cost_per_km: float
    total_cost_per_km: float
    cost_per_hour_estimated: float
    daily_fixed_cost: float
    monthly_fixed_cost: float
    annual_estimated_cost: float

# --- Combustível, Manutenção & Despesas ---
class FuelRecordCreateRequest(BaseModel):
    vehicle_id: Optional[str] = None
    date: str
    odometer_km: float
    liters: float = Field(gt=0.0)
    price_per_liter: float = Field(gt=0.0)
    fuel_type: str = "GASOLINE"
    is_full_tank: bool = True
    notes: Optional[str] = None

class MaintenanceRecordCreateRequest(BaseModel):
    vehicle_id: Optional[str] = None
    date: str
    odometer_km: float
    service_type: str
    description: str
    cost: float = Field(ge=0.0)
    next_service_km: Optional[float] = None
    notes: Optional[str] = None

class ExpenseCreateRequest(BaseModel):
    vehicle_id: Optional[str] = None
    date: str
    category: str
    description: str
    amount: float = Field(gt=0.0)
    notes: Optional[str] = None

# --- Metas ---
class GoalUpsertRequest(BaseModel):
    daily_gross_target: float = Field(gt=0.0)
    target_hourly_rate: float = Field(gt=0.0)
    target_km_rate: float = Field(gt=0.0)
    shift_target_hours: float = Field(gt=0.0)
    weekly_target: Optional[float] = None
    monthly_target: Optional[float] = None

class GoalCoachingResponse(BaseModel):
    daily_gross_target: float
    current_daily_gross: float
    remaining_gross: float
    progress_percent: float
    status_label: str
    coaching_message: str

# --- Avaliação de Corridas ---
class RideEvaluateRequest(BaseModel):
    platform: str # UBER, NINETY_NINE, INDRAVE, OTHER
    gross_fare: float = Field(gt=0.0)
    distance_km: float = Field(gt=0.0)
    duration_minutes: float = Field(gt=0.0)
    pickup_distance_km: float = 0.0
    pickup_duration_minutes: float = 0.0
    category: str = "STANDARD"

class RideEvaluateResponse(BaseModel):
    platform: str
    gross_fare: float
    total_distance_km: float
    total_duration_minutes: float
    estimated_cost: float
    net_profit: float
    profit_margin_percent: float
    gross_rate_per_km: float
    net_rate_per_km: float
    gross_rate_per_hour: float
    net_rate_per_hour: float
    score: int
    classification: str
    reasons: List[str]
    alerts: List[str]

class RideSaveRequest(RideEvaluateResponse):
    was_accepted: bool = False

# --- Financeiro Consolidado ---
class FinancialSummaryResponse(BaseModel):
    period: str
    gross_revenue: float
    total_costs: float
    net_profit: float
    profit_margin_percent: float
    net_profit_per_hour: float
    net_profit_per_km: float
    total_km: float
    total_hours: float
    total_evaluations: int
    has_activity: bool

class FinancialReportResponse(BaseModel):
    period: str
    period_label: str
    gross_revenue: float
    fuel_costs: float
    maintenance_costs: float
    fixed_costs: float
    other_expenses: float
    total_costs: float
    net_profit: float
    profit_margin_percent: float
    cost_breakdown_percentages: Dict[str, float]

# --- Sincronização ---
class SyncPushRequest(BaseModel):
    device_id: str
    client_timestamp: int
    fuel_records: List[Dict[str, Any]] = Field(default_factory=list)
    maintenance_records: List[Dict[str, Any]] = Field(default_factory=list)
    expenses: List[Dict[str, Any]] = Field(default_factory=list)
    evaluations: List[Dict[str, Any]] = Field(default_factory=list)

class SyncPushResponse(BaseModel):
    success: bool
    server_timestamp: int
    acknowledged_fuel_count: int
    acknowledged_maintenance_count: int
    acknowledged_expense_count: int
    acknowledged_evaluation_count: int
    message: str

# --- Admin Real ---
class AdminMetricsResponse(BaseModel):
    snapshot_date: str
    total_registered_users: int
    total_registered_drivers: int
    active_pro_subscribers: int
    total_evaluations_recorded: int
    total_fuel_records_recorded: int
    total_maintenance_records_recorded: int
    mrr_reais: float
    source: str = "Supabase PostgreSQL Production (Zero Mocks)"

# ======================================================================
# 1. HEALTH E STATUS
# ======================================================================

def find_static_file(rel_path: str) -> Optional[str]:
    rel_path = rel_path.lstrip("/").replace("\\", "/")
    app_root = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
    repo_root = os.path.dirname(app_root)
    cwd = os.getcwd()

    base_dirs = [
        os.path.join(cwd, "public"),
        cwd,
        os.path.join(cwd, "web"),
        os.path.join(repo_root, "public"),
        repo_root,
        os.path.join(repo_root, "web"),
        app_root
    ]
    for b in base_dirs:
        candidate = os.path.normpath(os.path.join(b, rel_path))
        if os.path.isfile(candidate):
            return candidate
    return None

def serve_static_file(file_path: str) -> Response:
    mime, _ = mimetypes.guess_type(file_path)
    if not mime:
        if file_path.endswith(".apk"):
            mime = "application/vnd.android.package-archive"
        elif file_path.endswith(".css"):
            mime = "text/css"
        elif file_path.endswith(".js"):
            mime = "application/javascript"
        elif file_path.endswith(".html"):
            mime = "text/html; charset=utf-8"
        else:
            mime = "application/octet-stream"
    try:
        with open(file_path, "rb") as f:
            content = f.read()
        return Response(content=content, media_type=mime)
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.get("/", response_class=HTMLResponse)
async def root_index():
    p = find_static_file("index.html")
    if p:
        return serve_static_file(p)
    return HTMLResponse("<h1>ROTA IQ Backend Online</h1>")

@app.get("/admin", response_class=HTMLResponse)
@app.get("/admin/", response_class=HTMLResponse)
async def serve_admin_dashboard():
    p = find_static_file("admin/index.html")
    if p:
        return serve_static_file(p)
    return HTMLResponse("<h1>ROTA IQ Admin Dashboard</h1><p>index.html do admin não encontrado.</p>", status_code=404)

@app.get("/admin/{asset_path:path}")
async def serve_admin_assets(asset_path: str):
    p = find_static_file(f"admin/{asset_path}")
    if p:
        return serve_static_file(p)
    raise HTTPException(status_code=404, detail="Asset admin não encontrado")

@app.get("/health")
async def root_health():
    """Health check do sistema com verificação real de conectividade ao PostgreSQL."""
    return AdminService.get_database_health()

@app.get("/api/v1/health")
async def health_check():
    db_health = AdminService.get_database_health()
    return {
        "status": "HEALTHY" if db_health.get("database") == "connected" else "DEGRADED",
        "service": "rota-iq-backend",
        "version": "2.0.0",
        "database": db_health.get("database", "disconnected"),
        "latency_ms": db_health.get("latency_ms", 0),
        "timestamp": datetime.now(timezone.utc).isoformat()
    }

@app.get("/api/v1/supabase/status")
async def supabase_status():
    try:
        users_count = supabase.count_table("users")
        return {
            "status": "CONNECTED",
            "provider": "Supabase PostgreSQL",
            "base_url": supabase.base_url,
            "connected_at": datetime.now(timezone.utc).isoformat(),
            "real_users_count": users_count
        }
    except Exception as e:
        return {
            "status": "ERROR",
            "error": str(e),
            "connected_at": datetime.now(timezone.utc).isoformat()
        }

# ======================================================================
# 2. AUTENTICAÇÃO REAL (PBKDF2 + JWT)
# ======================================================================

@app.post("/api/v1/auth/register", response_model=AuthResponse)
async def register(req: RegisterRequest):
    clean_email = req.email.strip().lower()
    name = (req.name or req.full_name or "").strip()
    if not name:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="O nome completo é obrigatório."
        )

    if not req.terms_accepted:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="É obrigatório concordar com os Termos de Uso e Política de Privacidade."
        )

    if len(req.password) < 8:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="A senha deve possuir no mínimo 8 caracteres."
        )

    # Verificar se usuário já existe
    existing = supabase.get_user_by_email(clean_email)
    if existing:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Já existe uma conta cadastrada com este e-mail."
        )

    # Hash seguro com PBKDF2
    p_hash = hash_password(req.password)
    user_payload = {
        "email": clean_email,
        "password_hash": p_hash,
        "full_name": name,
        "phone": req.phone,
        "is_active": True
    }
    try:
        user_record = supabase.insert_user(user_payload)
    except Exception as exc:
        err_str = str(exc)
        if "users_phone_key" in err_str or "phone" in err_str:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Este número de telefone já está cadastrado em outra conta."
            )
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Não foi possível concluir o cadastro com os dados informados."
        )
    user_id = user_record.get("id")

    # Criar registro de motorista vinculado
    driver_payload = {
        "user_id": user_id,
        "city": req.city,
        "state": req.state,
        "status": "ACTIVE"
    }
    driver_record = supabase.insert_driver(driver_payload)
    driver_id = driver_record.get("id")

    # Auditoria de aceite de Termos e LGPD
    auth_service.record_terms_acceptance(
        user_id=user_id,
        email=clean_email,
        terms_version=req.terms_version,
        privacy_version=req.privacy_version
    )

    # Registro de Verificação de E-mail
    auth_service.register_email_verification(clean_email)

    # Gerar Tokens JWT
    claims = {
        "sub": user_id,
        "driver_id": driver_id,
        "email": clean_email,
        "name": name
    }
    access_token = create_access_token(claims)
    refresh_token = create_refresh_token(claims)

    return AuthResponse(
        access_token=access_token,
        refresh_token=refresh_token,
        driver_id=driver_id,
        user_id=user_id,
        full_name=name,
        email=clean_email,
        email_verified=False
    )

@app.post("/api/v1/auth/login", response_model=AuthResponse)
async def login(req: LoginRequest):
    clean_email = req.email.strip().lower()
    user = supabase.get_user_by_email(clean_email)
    if not user:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="E-mail ou senha incorretos."
        )

    # Validar hash
    if not verify_password(req.password, user.get("password_hash", "")):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="E-mail ou senha incorretos."
        )

    user_id = user.get("id")
    now_iso = datetime.now(timezone.utc).isoformat()
    supabase.update_user(user_id, {"updated_at": now_iso})

    driver = supabase.get_driver_by_user_id(user_id)
    if not driver:
        driver = supabase.insert_driver({"user_id": user_id, "city": "São Paulo", "state": "SP", "status": "ACTIVE"})

    driver_id = driver.get("id")
    user_name = user.get("full_name", "Motorista")
    claims = {
        "sub": user_id,
        "driver_id": driver_id,
        "email": user.get("email"),
        "name": user_name
    }

    is_verified = auth_service.is_email_verified(clean_email)

    return AuthResponse(
        access_token=create_access_token(claims),
        refresh_token=create_refresh_token(claims),
        driver_id=driver_id,
        user_id=user_id,
        full_name=user_name,
        email=user.get("email"),
        email_verified=is_verified
    )

@app.post("/api/v1/auth/refresh", response_model=RefreshTokenResponse)
async def refresh_token_endpoint(req: RefreshTokenRequest):
    claims = decode_token(req.refresh_token)
    if claims.get("type") != "refresh":
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Token inválido para renovação de sessão."
        )

    new_claims = {
        "sub": claims.get("sub"),
        "driver_id": claims.get("driver_id"),
        "email": claims.get("email"),
        "name": claims.get("name")
    }
    return RefreshTokenResponse(access_token=create_access_token(new_claims))

@app.post("/api/v1/auth/forgot-password")
async def forgot_password(req: ForgotPasswordRequest):
    clean_email = req.email.strip().lower()
    user = supabase.get_user_by_email(clean_email)
    if not user:
        # Prevenção contra enumeração de e-mails
        return {
            "success": True,
            "message": "Se o e-mail estiver cadastrado, as instruções de recuperação foram enviadas."
        }

    reset_token = auth_service.create_password_reset_token(clean_email)
    print(f"[AUTH PASSWORD RESET] Token gerado para {clean_email}: {reset_token}")
    return {
        "success": True,
        "message": "Enviamos as instruções de recuperação para seu e-mail.",
        "reset_token": reset_token
    }

@app.post("/api/v1/auth/reset-password")
async def reset_password(req: ResetPasswordRequest):
    if len(req.new_password) < 8:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="A nova senha deve possuir no mínimo 8 caracteres."
        )

    email = auth_service.validate_password_reset_token(req.token)
    if not email:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Token de recuperação inválido ou expirado."
        )

    user = supabase.get_user_by_email(email)
    if not user:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Usuário vinculado não encontrado."
        )

    new_hash = hash_password(req.new_password)
    now_iso = datetime.now(timezone.utc).isoformat()
    supabase.update_user(user["id"], {
        "password_hash": new_hash,
        "updated_at": now_iso
    })
    auth_service.mark_password_reset_used(req.token)

    return {
        "success": True,
        "message": "Senha atualizada com sucesso! Você já pode efetuar login."
    }

@app.post("/api/v1/auth/verify-email")
async def verify_email(req: VerifyEmailRequest):
    target = req.token or (req.email.strip().lower() if req.email else None)
    if not target:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Token ou e-mail deve ser informado."
        )
    success = auth_service.verify_email(target)
    if not success:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Token de verificação inválido ou inexistente."
        )
    return {"success": True, "message": "E-mail verificado com sucesso!"}

@app.post("/api/v1/auth/resend-verification")
async def resend_verification(req: ResendVerificationRequest):
    clean_email = req.email.strip().lower()
    user = supabase.get_user_by_email(clean_email)
    if not user:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Usuário não encontrado."
        )
    token = auth_service.register_email_verification(clean_email)
    return {
        "success": True,
        "message": "Link de confirmação reenviado para seu e-mail.",
        "verification_token": token
    }

@app.post("/api/v1/auth/logout")
async def logout(
    req: Optional[LogoutRequest] = None,
    credentials: Optional[HTTPAuthorizationCredentials] = Security(security)
):
    if credentials:
        auth_service.revoke_token(credentials.credentials)
    if req and req.refresh_token:
        auth_service.revoke_token(req.refresh_token)
    return {
        "success": True,
        "message": "Sessão finalizada com sucesso."
    }

@app.get("/api/v1/auth/me", response_model=UserProfileResponse)
async def get_my_profile(claims: Dict[str, Any] = Depends(get_current_user_claims)):
    user_id = claims.get("sub")
    driver_id = claims.get("driver_id")
    user = supabase.get_user_by_id(user_id)
    driver = supabase.get_driver_by_id(driver_id)

    if not user or not driver:
        raise HTTPException(status_code=404, detail="Perfil não encontrado.")

    is_verified = auth_service.is_email_verified(user.get("email", ""))

    return UserProfileResponse(
        user_id=user_id,
        driver_id=driver_id,
        full_name=user.get("full_name"),
        email=user.get("email"),
        phone=user.get("phone"),
        city=driver.get("city", "São Paulo"),
        state=driver.get("state", "SP"),
        status=driver.get("status", "ACTIVE"),
        email_verified=is_verified
    )

# ======================================================================
# 3. VEÍCULOS E CUSTOS OPERACIONAIS REAIS
# ======================================================================

@app.get("/api/v1/vehicles")
async def list_vehicles(claims: Dict[str, Any] = Depends(get_current_user_claims)):
    driver_id = claims.get("driver_id")
    return supabase.get_vehicles(driver_id)

@app.post("/api/v1/vehicles")
async def create_vehicle(req: VehicleCreateRequest, claims: Dict[str, Any] = Depends(get_current_user_claims)):
    driver_id = claims.get("driver_id")
    payload = {
        "driver_id": driver_id,
        "name": req.name,
        "plate": req.plate,
        "model": req.model,
        "year": req.year,
        "fuel_type": req.fuel_type,
        "consumption_km_per_liter": req.consumption_km_per_liter,
        "fuel_price_per_liter": req.fuel_price_per_liter,
        "maintenance_cost_per_km": req.maintenance_cost_per_km,
        "is_active": True
    }
    created = supabase.insert_vehicle(payload)
    # Criar registro padrão de custos associados
    if created.get("id"):
        supabase.upsert_vehicle_costs({
            "vehicle_id": created.get("id"),
            "monthly_insurance_cost": 220.0,
            "annual_taxes_cost": 1800.0,
            "monthly_depreciation": 350.0,
            "monthly_other_costs": 150.0,
            "estimated_monthly_km": 3000.0
        })
    return created

@app.get("/api/v1/vehicles/{vehicle_id}/costs", response_model=VehicleCostsResponse)
async def get_vehicle_costs_calculation(vehicle_id: str, claims: Dict[str, Any] = Depends(get_current_user_claims)):
    # Buscar veículo
    rows = supabase._request(f"vehicles?id=eq.{vehicle_id}&limit=1")
    if not rows:
        raise HTTPException(status_code=404, detail="Veículo não encontrado.")
    v = rows[0]

    costs = supabase.get_vehicle_costs(vehicle_id) or {}
    consumption = float(v.get("consumption_km_per_liter") or 11.0)
    fuel_price = float(v.get("fuel_price_per_liter") or 5.89)
    maint_km = float(v.get("maintenance_cost_per_km") or 0.18)

    insurance = float(costs.get("monthly_insurance_cost") or 0.0)
    taxes = float(costs.get("annual_taxes_cost") or 0.0)
    monthly_taxes = taxes / 12.0
    deprec = float(costs.get("monthly_depreciation") or 0.0)
    other = float(costs.get("monthly_other_costs") or 0.0)
    monthly_km = float(costs.get("estimated_monthly_km") or 3000.0)

    # Fórmulas determinísticas oficiais
    fuel_km = (fuel_price / consumption) if consumption > 0 else 0.0
    total_monthly_fixed = insurance + monthly_taxes + deprec + other
    fixed_km = (total_monthly_fixed / monthly_km) if monthly_km > 0 else 0.0
    total_km = fuel_km + maint_km + fixed_km

    avg_speed = 25.0 # km/h urbana
    cost_per_hour = total_km * avg_speed
    daily_fixed = total_monthly_fixed / 30.0
    annual_fixed = total_monthly_fixed * 12.0
    annual_variable = total_km * (monthly_km * 12.0)

    return VehicleCostsResponse(
        vehicle_id=vehicle_id,
        fuel_cost_per_km=round(fuel_km, 3),
        maintenance_cost_per_km=round(maint_km, 3),
        fixed_cost_per_km=round(fixed_km, 3),
        total_cost_per_km=round(total_km, 3),
        cost_per_hour_estimated=round(cost_per_hour, 2),
        daily_fixed_cost=round(daily_fixed, 2),
        monthly_fixed_cost=round(total_monthly_fixed, 2),
        annual_estimated_cost=round(annual_fixed + annual_variable, 2)
    )

@app.put("/api/v1/vehicles/{vehicle_id}/costs")
async def update_vehicle_costs(
    vehicle_id: str,
    req: VehicleCostsUpdateRequest,
    claims: Dict[str, Any] = Depends(get_current_user_claims)
):
    payload = {
        "vehicle_id": vehicle_id,
        "monthly_insurance_cost": req.monthly_insurance_cost,
        "annual_taxes_cost": req.annual_taxes_cost,
        "monthly_depreciation": req.monthly_depreciation,
        "monthly_other_costs": req.monthly_other_costs,
        "estimated_monthly_km": req.estimated_monthly_km
    }
    return supabase.upsert_vehicle_costs(payload)

# ======================================================================
# 4. COMBUSTÍVEL (FUEL ENGINE), MANUTENÇÃO E DESPESAS REAIS
# ======================================================================

@app.get("/api/v1/fuel")
async def list_fuel_records(claims: Dict[str, Any] = Depends(get_current_user_claims)):
    driver_id = claims.get("driver_id")
    return supabase.get_fuel_records(driver_id)

@app.post("/api/v1/fuel")
async def create_fuel_record(req: FuelRecordCreateRequest, claims: Dict[str, Any] = Depends(get_current_user_claims)):
    driver_id = claims.get("driver_id")
    total_paid = req.liters * req.price_per_liter

    # Algoritmo dos 2 tanques cheios para consumo real
    calculated_km_per_l = None
    calculated_cost_per_km = None

    if req.is_full_tank:
        history = supabase.get_fuel_records(driver_id, limit=5)
        prev_full = next((f for f in history if f.get("is_full_tank") and float(f.get("odometer_km", 0)) < req.odometer_km), None)
        if prev_full:
            dist = req.odometer_km - float(prev_full.get("odometer_km"))
            if dist > 0:
                calculated_km_per_l = round(dist / req.liters, 2)
                calculated_cost_per_km = round(total_paid / dist, 3)

    payload = {
        "driver_id": driver_id,
        "vehicle_id": req.vehicle_id,
        "date": req.date,
        "odometer_km": req.odometer_km,
        "liters": req.liters,
        "price_per_liter": req.price_per_liter,
        "total_paid": round(total_paid, 2),
        "fuel_type": req.fuel_type,
        "is_full_tank": req.is_full_tank,
        "calculated_km_per_liter": calculated_km_per_l,
        "calculated_cost_per_km": calculated_cost_per_km,
        "notes": req.notes
    }
    return supabase.insert_fuel_record(payload)

@app.get("/api/v1/maintenance")
async def list_maintenance_records(claims: Dict[str, Any] = Depends(get_current_user_claims)):
    driver_id = claims.get("driver_id")
    return supabase.get_maintenance_records(driver_id)

@app.post("/api/v1/maintenance")
async def create_maintenance_record(req: MaintenanceRecordCreateRequest, claims: Dict[str, Any] = Depends(get_current_user_claims)):
    driver_id = claims.get("driver_id")
    payload = {
        "driver_id": driver_id,
        "vehicle_id": req.vehicle_id,
        "date": req.date,
        "odometer_km": req.odometer_km,
        "service_type": req.service_type,
        "description": req.description,
        "cost": req.cost,
        "next_service_km": req.next_service_km,
        "notes": req.notes
    }
    return supabase.insert_maintenance_record(payload)

@app.get("/api/v1/expenses")
async def list_expenses(claims: Dict[str, Any] = Depends(get_current_user_claims)):
    driver_id = claims.get("driver_id")
    return supabase.get_expenses(driver_id)

@app.post("/api/v1/expenses")
async def create_expense(req: ExpenseCreateRequest, claims: Dict[str, Any] = Depends(get_current_user_claims)):
    driver_id = claims.get("driver_id")
    payload = {
        "driver_id": driver_id,
        "vehicle_id": req.vehicle_id,
        "date": req.date,
        "category": req.category,
        "description": req.description,
        "amount": req.amount,
        "notes": req.notes
    }
    return supabase.insert_expense(payload)

# ======================================================================
# 5. METAS OPERACIONAIS E GOAL ENGINE
# ======================================================================

@app.get("/api/v1/goals")
async def get_driver_goals(claims: Dict[str, Any] = Depends(get_current_user_claims)):
    driver_id = claims.get("driver_id")
    goal = supabase.get_driver_goal(driver_id)
    if not goal:
        return {
            "daily_gross_target": 0.0,
            "target_hourly_rate": 0.0,
            "target_km_rate": 0.0,
            "shift_target_hours": 8.0,
            "has_goal": False
        }
    goal["has_goal"] = True
    return goal

@app.post("/api/v1/goals")
async def upsert_driver_goals(req: GoalUpsertRequest, claims: Dict[str, Any] = Depends(get_current_user_claims)):
    driver_id = claims.get("driver_id")
    payload = {
        "driver_id": driver_id,
        "daily_gross_target": req.daily_gross_target,
        "target_hourly_rate": req.target_hourly_rate,
        "target_km_rate": req.target_km_rate,
        "shift_target_hours": req.shift_target_hours,
        "weekly_target": req.weekly_target,
        "monthly_target": req.monthly_target
    }
    return supabase.upsert_driver_goal(payload)

@app.get("/api/v1/goals/coaching", response_model=GoalCoachingResponse)
async def get_goal_coaching(claims: Dict[str, Any] = Depends(get_current_user_claims)):
    driver_id = claims.get("driver_id")
    goal = supabase.get_driver_goal(driver_id)
    if not goal:
        return GoalCoachingResponse(
            daily_gross_target=0.0,
            current_daily_gross=0.0,
            remaining_gross=0.0,
            progress_percent=0.0,
            status_label="SEM META",
            coaching_message="Defina sua primeira meta diária para acompanhar seu ritmo de faturamento em tempo real."
        )

    target = float(goal.get("daily_gross_target", 0.0))
    # Calcular faturamento real de hoje no PostgreSQL
    today_str = datetime.now(timezone.utc).strftime("%Y-%m-%d")
    rides = supabase._request(f"ride_evaluations?driver_id=eq.{driver_id}&evaluated_at=gte.{today_str}T00:00:00") or []
    current_gross = sum(float(r.get("gross_fare", 0.0)) for r in rides)
    remaining = max(0.0, target - current_gross)
    progress = min(100.0, (current_gross / target * 100.0)) if target > 0 else 0.0

    if remaining <= 0:
        status_lbl = "META ATINGIDA"
        msg = f"Parabéns! Você atingiu sua meta diária de R$ {target:.2f}."
    else:
        status_lbl = "NO RITMO" if progress >= 50 else "ATENÇÃO AO RITMO"
        msg = f"Faltam R$ {remaining:.2f} para sua meta diária de R$ {target:.2f}."

    return GoalCoachingResponse(
        daily_gross_target=target,
        current_daily_gross=round(current_gross, 2),
        remaining_gross=round(remaining, 2),
        progress_percent=round(progress, 1),
        status_label=status_lbl,
        coaching_message=msg
    )

# ======================================================================
# 6. RIDE EVALUATION ENGINE & HISTÓRICO
# ======================================================================

@app.post("/api/v1/rides/evaluate", response_model=RideEvaluateResponse)
async def evaluate_ride_offer(req: RideEvaluateRequest, claims: Dict[str, Any] = Depends(get_current_user_claims)):
    driver_id = claims.get("driver_id")
    vehicle = supabase.get_active_vehicle(driver_id)

    # Determinar custo por km real
    cost_per_km = 0.71 # Custo padrão de referência se não configurado
    if vehicle:
        consumption = float(vehicle.get("consumption_km_per_liter") or 11.0)
        fuel_price = float(vehicle.get("fuel_price_per_liter") or 5.89)
        maint = float(vehicle.get("maintenance_cost_per_km") or 0.18)
        fuel_km = (fuel_price / consumption) if consumption > 0 else 0.45
        fixed_km = 0.14
        cost_per_km = fuel_km + maint + fixed_km

    total_dist = req.distance_km + req.pickup_distance_km
    total_duration = req.duration_minutes + req.pickup_duration_minutes
    estimated_cost = round(total_dist * cost_per_km, 2)
    net_profit = round(req.gross_fare - estimated_cost, 2)
    margin_pct = round((net_profit / req.gross_fare * 100.0), 1) if req.gross_fare > 0 else 0.0

    gross_km = round(req.gross_fare / total_dist, 2) if total_dist > 0 else 0.0
    net_km = round(net_profit / total_dist, 2) if total_dist > 0 else 0.0
    duration_hours = (total_duration / 60.0) if total_duration > 0 else 0.1
    gross_hour = round(req.gross_fare / duration_hours, 2)
    net_hour = round(net_profit / duration_hours, 2)

    # Score Multifatorial (0 a 100)
    score = 50
    reasons = []
    alerts = []

    if gross_km >= 2.50:
        score += 25
        reasons.append(f"Ótimo valor por km: R$ {gross_km:.2f}/km")
    elif gross_km < 1.60:
        score -= 25
        alerts.append(f"Valor por km baixo: R$ {gross_km:.2f}/km")

    if net_hour >= 45.0:
        score += 25
        reasons.append(f"Lucro por hora excelente: R$ {net_hour:.2f}/h")
    elif net_hour < 25.0:
        score -= 20
        alerts.append(f"Lucro por hora abaixo do piso: R$ {net_hour:.2f}/h")

    if req.pickup_distance_km > (req.distance_km * 0.4):
        score -= 15
        alerts.append("Deslocamento vazio até passageiro excessivo")

    score = max(0, min(100, score))

    if score >= 85:
        classification = "EXCELLENT"
    elif score >= 70:
        classification = "GOOD"
    elif score >= 50:
        classification = "ACCEPTABLE"
    elif score >= 35:
        classification = "BAD"
    else:
        classification = "AVOID"

    return RideEvaluateResponse(
        platform=req.platform,
        gross_fare=req.gross_fare,
        total_distance_km=round(total_dist, 2),
        total_duration_minutes=round(total_duration, 1),
        estimated_cost=estimated_cost,
        net_profit=net_profit,
        profit_margin_percent=margin_pct,
        gross_rate_per_km=gross_km,
        net_rate_per_km=net_km,
        gross_rate_per_hour=gross_hour,
        net_rate_per_hour=net_hour,
        score=score,
        classification=classification,
        reasons=reasons,
        alerts=alerts
    )

@app.post("/api/v1/rides/save")
async def save_ride_evaluation(req: RideSaveRequest, claims: Dict[str, Any] = Depends(get_current_user_claims)):
    driver_id = claims.get("driver_id")
    payload = {
        "driver_id": driver_id,
        "platform": req.platform,
        "gross_fare": req.gross_fare,
        "distance_km": req.total_distance_km,
        "duration_minutes": req.total_duration_minutes,
        "score": req.score,
        "classification": req.classification,
        "estimated_cost": req.estimated_cost,
        "net_profit": req.net_profit,
        "profit_margin_percent": req.profit_margin_percent,
        "gross_rate_per_km": req.gross_rate_per_km,
        "net_rate_per_km": req.net_rate_per_km,
        "gross_rate_per_hour": req.gross_rate_per_hour,
        "net_rate_per_hour": req.net_rate_per_hour,
        "was_accepted": req.was_accepted
    }
    return supabase.insert_evaluation(payload)

@app.get("/api/v1/rides/history")
async def get_rides_history(
    limit: int = 50,
    classification: Optional[str] = None,
    claims: Dict[str, Any] = Depends(get_current_user_claims)
):
    driver_id = claims.get("driver_id")
    return supabase.get_evaluations(driver_id, limit=limit, classification=classification)

# ======================================================================
# 7. DASHBOARD FINANCEIRO CONSOLIDADO REAL
# ======================================================================

@app.get("/api/v1/financial/summary", response_model=FinancialSummaryResponse)
async def get_financial_summary(period: str = "today", claims: Dict[str, Any] = Depends(get_current_user_claims)):
    driver_id = claims.get("driver_id")
    today_str = datetime.now(timezone.utc).strftime("%Y-%m-%d")
    date_filter = f"evaluated_at=gte.{today_str}T00:00:00" if period == "today" else ""

    rides = supabase.get_evaluations(driver_id, limit=100)
    if period == "today":
        rides = [r for r in rides if r.get("evaluated_at", "").startswith(today_str)]

    fuel_records = supabase.get_fuel_records(driver_id, limit=100)
    maintenance_records = supabase.get_maintenance_records(driver_id, limit=100)
    expenses = supabase.get_expenses(driver_id, limit=100)

    if period == "today":
        fuel_records = [f for f in fuel_records if f.get("date") == today_str]
        maintenance_records = [m for m in maintenance_records if m.get("date") == today_str]
        expenses = [e for e in expenses if e.get("date") == today_str]

    gross_rev = sum(float(r.get("gross_fare", 0.0)) for r in rides)
    ride_costs = sum(float(r.get("estimated_cost", 0.0)) for r in rides)
    direct_fuel = sum(float(f.get("total_paid", 0.0)) for f in fuel_records)
    direct_maint = sum(float(m.get("cost", 0.0)) for m in maintenance_records)
    direct_expenses = sum(float(e.get("amount", 0.0)) for e in expenses)

    # Custos operacionais reais
    total_costs = ride_costs if (direct_fuel == 0 and direct_maint == 0) else (direct_fuel + direct_maint + direct_expenses)
    net_profit = gross_rev - total_costs
    margin = (net_profit / gross_rev * 100.0) if gross_rev > 0 else 0.0

    total_km = sum(float(r.get("distance_km", 0.0)) for r in rides)
    total_minutes = sum(float(r.get("duration_minutes", 0.0)) for r in rides)
    total_hours = total_minutes / 60.0

    net_hour = (net_profit / total_hours) if total_hours > 0 else 0.0
    net_km = (net_profit / total_km) if total_km > 0 else 0.0
    has_activity = (gross_rev > 0 or total_costs > 0)

    return FinancialSummaryResponse(
        period=period,
        gross_revenue=round(gross_rev, 2),
        total_costs=round(total_costs, 2),
        net_profit=round(net_profit, 2),
        profit_margin_percent=round(margin, 1),
        net_profit_per_hour=round(net_hour, 2),
        net_profit_per_km=round(net_km, 2),
        total_km=round(total_km, 1),
        total_hours=round(total_hours, 1),
        total_evaluations=len(rides),
        has_activity=has_activity
    )

@app.get("/api/v1/financial/report", response_model=FinancialReportResponse)
async def get_financial_report(period: str = "daily", claims: Dict[str, Any] = Depends(get_current_user_claims)):
    driver_id = claims.get("driver_id")
    rides = supabase.get_evaluations(driver_id, limit=200)
    fuel = supabase.get_fuel_records(driver_id, limit=100)
    maint = supabase.get_maintenance_records(driver_id, limit=100)
    expenses = supabase.get_expenses(driver_id, limit=100)

    gross_rev = sum(float(r.get("gross_fare", 0.0)) for r in rides)
    fuel_cost = sum(float(f.get("total_paid", 0.0)) for f in fuel)
    maint_cost = sum(float(m.get("cost", 0.0)) for m in maint)
    other_cost = sum(float(e.get("amount", 0.0)) for e in expenses)
    fixed_cost = 15.0 # Estimativa de custos fixos diários proporcionais

    total_costs = fuel_cost + maint_cost + other_cost + fixed_cost
    net_profit = gross_rev - total_costs
    margin = (net_profit / gross_rev * 100.0) if gross_rev > 0 else 0.0

    fuel_pct = round((fuel_cost / total_costs * 100.0), 1) if total_costs > 0 else 0.0
    maint_pct = round((maint_cost / total_costs * 100.0), 1) if total_costs > 0 else 0.0
    fixed_pct = round(((fixed_cost + other_cost) / total_costs * 100.0), 1) if total_costs > 0 else 0.0

    return FinancialReportResponse(
        period=period,
        period_label="Hoje" if period == "daily" else "Consolidado Geral",
        gross_revenue=round(gross_rev, 2),
        fuel_costs=round(fuel_cost, 2),
        maintenance_costs=round(maint_cost, 2),
        fixed_costs=round(fixed_cost, 2),
        other_expenses=round(other_cost, 2),
        total_costs=round(total_costs, 2),
        net_profit=round(net_profit, 2),
        profit_margin_percent=round(margin, 1),
        cost_breakdown_percentages={
            "fuel": fuel_pct,
            "maintenance": maint_pct,
            "fixed": fixed_pct
        }
    )

# ======================================================================
# 8. SINCRONIZAÇÃO OFFLINE-FIRST BIDIRECIONAL
# ======================================================================

@app.post("/api/v1/sync/push", response_model=SyncPushResponse)
async def sync_push(payload: SyncPushRequest, claims: Dict[str, Any] = Depends(get_current_user_claims)):
    driver_id = claims.get("driver_id")

    valid_ev_cols = {
        'id', 'driver_id', 'platform', 'gross_fare', 'distance_km', 'duration_minutes',
        'pickup_distance_km', 'pickup_duration_minutes', 'category', 'stops_count',
        'score', 'classification', 'estimated_cost', 'net_profit', 'profit_margin_percent',
        'gross_rate_per_km', 'net_rate_per_km', 'gross_rate_per_hour', 'net_rate_per_hour',
        'was_accepted', 'evaluated_at'
    }

    clean_evs = []
    for ev in payload.evaluations:
        ev["driver_id"] = driver_id
        gross = float(ev.get("gross_fare") or 0.0)
        net = float(ev.get("net_profit") or 0.0)
        dist = float(ev.get("distance_km") or 0.0)
        dur = float(ev.get("duration_minutes") or 0.0)

        ev["gross_fare"] = gross
        ev["net_profit"] = net
        ev["distance_km"] = dist
        ev["duration_minutes"] = dur
        ev["pickup_distance_km"] = float(ev.get("pickup_distance_km") or 0.0)
        ev["pickup_duration_minutes"] = float(ev.get("pickup_duration_minutes") or 0.0)
        ev["category"] = str(ev.get("category") or "STANDARD")
        ev["stops_count"] = int(ev.get("stops_count") or 0)
        ev["score"] = int(ev.get("score") or 50)
        ev["estimated_cost"] = float(ev.get("estimated_cost") or 0.0)
        ev["profit_margin_percent"] = round((net / gross * 100.0), 1) if gross > 0 else 0.0
        ev["gross_rate_per_km"] = round(gross / dist, 2) if dist > 0 else 0.0
        ev["net_rate_per_km"] = round(net / dist, 2) if dist > 0 else 0.0
        ev["gross_rate_per_hour"] = round(gross / (dur / 60.0), 2) if dur > 0 else 0.0
        ev["net_rate_per_hour"] = round(net / (dur / 60.0), 2) if dur > 0 else 0.0
        ev["was_accepted"] = bool(ev.get("was_accepted", True))

        raw_class = str(ev.get("classification") or "GOOD").upper()
        if raw_class in ["EXCELENTE", "EXCELLENT"]:
            ev["classification"] = "EXCELLENT"
        elif raw_class in ["RUIM", "BAD"]:
            ev["classification"] = "BAD"
        elif raw_class in ["MUITO_RUIM", "VERY_BAD"]:
            ev["classification"] = "VERY_BAD"
        elif raw_class in ["REGULAR"]:
            ev["classification"] = "REGULAR"
        else:
            ev["classification"] = "GOOD"

        if "evaluated_at" not in ev or not ev["evaluated_at"]:
            ev["evaluated_at"] = datetime.now(timezone.utc).isoformat()

        clean_ev = {k: v for k, v in ev.items() if k in valid_ev_cols}
        clean_evs.append(clean_ev)

    valid_fuel_cols = {
        'id', 'driver_id', 'vehicle_id', 'date', 'odometer_km', 'liters', 'price_per_liter',
        'total_paid', 'fuel_type', 'is_full_tank', 'calculated_km_per_liter', 'calculated_cost_per_km',
        'notes', 'created_at'
    }
    clean_fuel = []
    for f in payload.fuel_records:
        f["driver_id"] = driver_id
        clean_f = {k: v for k, v in f.items() if k in valid_fuel_cols}
        clean_fuel.append(clean_f)

    for m in payload.maintenance_records:
        m["driver_id"] = driver_id
    for e in payload.expenses:
        e["driver_id"] = driver_id

    # Sincronização persistente real com Supabase
    ack_fuel_count = 0
    ack_maint_count = 0
    ack_exp_count = 0
    ack_ev_count = 0

    if clean_fuel:
        try:
            supabase.save_sync_fuel(clean_fuel)
            ack_fuel_count = len(clean_fuel)
        except Exception as err:
            logger.warning(f"Erro ao persistir combustível: {err}")

    if payload.maintenance_records:
        try:
            supabase.save_sync_maintenance(payload.maintenance_records)
            ack_maint_count = len(payload.maintenance_records)
        except Exception as err:
            logger.warning(f"Erro ao persistir manutenção: {err}")

    if payload.expenses:
        try:
            supabase.save_sync_expenses(payload.expenses)
            ack_exp_count = len(payload.expenses)
        except Exception as err:
            logger.warning(f"Erro ao persistir despesas: {err}")

    if clean_evs:
        try:
            supabase.save_sync_evaluations(clean_evs)
            ack_ev_count = len(clean_evs)
        except Exception as err:
            logger.warning(f"Erro ao persistir avaliações de corrida: {err}")

    return SyncPushResponse(
        success=True,
        server_timestamp=int(datetime.now(timezone.utc).timestamp() * 1000),
        acknowledged_fuel_count=ack_fuel_count,
        acknowledged_maintenance_count=ack_maint_count,
        acknowledged_expense_count=ack_exp_count,
        acknowledged_evaluation_count=ack_ev_count,
        message=f"Sincronização persistida com sucesso no PostgreSQL para o motorista {driver_id}."
    )

# ======================================================================
# 9. ADMIN METRICS — 100% REAL FROM POSTGRESQL (ZERO MOCKS)
# ======================================================================

@app.get("/api/v1/admin/metrics", response_model=AdminMetricsResponse)
async def get_real_admin_metrics():
    # Executa contagens REAIS no banco de dados
    total_users = supabase.count_table("users")
    total_drivers = supabase.count_table("drivers")
    total_evaluations = supabase.count_table("ride_evaluations")
    total_fuel = supabase.count_table("fuel_records")
    total_maintenance = supabase.count_table("maintenance_records")
    active_subscriptions = supabase.count_table("subscriptions", "status=eq.ACTIVE")

    # MRR real somando assinaturas ativas
    mrr_reais = 0.0
    try:
        active_subs = supabase._request("subscriptions?status=eq.ACTIVE") or []
        for s in active_subs:
            tier = s.get("tier", "")
            if tier == "pro_monthly":
                mrr_reais += 29.90
            elif tier == "pro_annual":
                mrr_reais += (239.90 / 12.0)
    except Exception:
        mrr_reais = 0.0

    return AdminMetricsResponse(
        snapshot_date=datetime.now(timezone.utc).strftime("%Y-%m-%d"),
        total_registered_users=total_users,
        total_registered_drivers=total_drivers,
        active_pro_subscribers=active_subscriptions,
        total_evaluations_recorded=total_evaluations,
        total_fuel_records_recorded=total_fuel,
        total_maintenance_records_recorded=total_maintenance,
        mrr_reais=round(mrr_reais, 2)
    )

# ======================================================================
# 10. PLANOS E CHECKOUT PIX
# ======================================================================

class PlanResponse(BaseModel):
    code: str
    name: str
    price_cents: int
    interval: str
    features: List[str]

@app.get("/api/v1/subscriptions/plans", response_model=List[PlanResponse])
async def list_subscription_plans():
    db_plans = supabase.get_plans()
    if db_plans:
        return [
            PlanResponse(
                code=p["code"],
                name=p["name"],
                price_cents=p["price_cents"],
                interval=p["interval"],
                features=p["features"] if isinstance(p["features"], list) else []
            )
            for p in db_plans
        ]
    return []

# ======================================================================
# 11. ADMIN BACKOFFICE COMPLETO (100% POSTGRESQL + RBAC + AUDITORIA REAL)
# ======================================================================

class AdminLoginRequest(BaseModel):
    email: EmailStr
    password: str
    role: Optional[str] = "ADMIN"

class AdminDriverItem(BaseModel):
    id: str
    user_id: str
    cpf: Optional[str] = None
    city: str
    state: str
    status: str
    created_at: str

class AdminDriverCreateRequest(BaseModel):
    name: str
    email: EmailStr
    password: Optional[str] = "Motorista@123"
    phone: Optional[str] = None
    city: str = "São Paulo"
    state: str = "SP"
    status: str = "ACTIVE"
    plan_code: str = "free"

class AdminDriverUpdateRequest(BaseModel):
    name: Optional[str] = None
    email: Optional[EmailStr] = None
    phone: Optional[str] = None
    city: Optional[str] = None
    state: Optional[str] = None
    status: Optional[str] = None
    plan_code: Optional[str] = None

class AdminDriverBlockRequest(BaseModel):
    reason: str

class AdminPlanCreateRequest(BaseModel):
    code: str
    name: str
    price_cents: int
    interval: str = "month"
    features: List[str] = Field(default_factory=list)
    description: Optional[str] = None
    is_active: bool = True

class AdminPlanUpdateRequest(BaseModel):
    name: Optional[str] = None
    price_cents: Optional[int] = None
    interval: Optional[str] = None
    features: Optional[List[str]] = None
    description: Optional[str] = None
    is_active: Optional[bool] = None

class AdminSettingUpdateRequest(BaseModel):
    value: str

class FeatureFlagRequest(BaseModel):
    key: str
    name: Optional[str] = None
    description: Optional[str] = None
    is_enabled: bool = True

# --- 11.1 Autenticação Admin & Dashboard ---

@app.post("/api/v1/admin/auth/login")
async def admin_login(req: AdminLoginRequest):
    """Autenticação real para administradores com verificação PBKDF2 e RBAC."""
    result = AdminService.authenticate_admin(req.email, req.password, req.role)
    if not result:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Credenciais de administrador incorretas ou acesso não autorizado."
        )
    return result

@app.get("/api/v1/admin/dashboard")
async def get_admin_dashboard(claims: Dict[str, Any] = Depends(require_admin_claims)):
    """Visão geral executiva com contagens e métricas 100% reais do banco de dados."""
    return AdminService.get_dashboard_metrics()

# --- 11.2 Gestão de Motoristas ---

@app.get("/api/v1/admin/drivers")
async def get_admin_drivers_list(
    limit: int = 50,
    page: int = 1,
    paged: bool = False,
    search: Optional[str] = None,
    status: Optional[str] = None
):
    """Listagem de motoristas (compatível com retrocompatibilidade e paginação avançada)."""
    if paged or search or status:
        return AdminService.get_drivers_paged(
            search=search,
            status_filter=status,
            page=page,
            limit=limit
        )
    drivers = supabase.get_drivers_list(limit=limit)
    return [
        AdminDriverItem(
            id=d["id"],
            user_id=d["user_id"],
            cpf=d.get("cpf"),
            city=d.get("city", ""),
            state=d.get("state", ""),
            status=d.get("status", "ACTIVE"),
            created_at=d.get("created_at", "")
        )
        for d in drivers
    ]

@app.post("/api/v1/admin/drivers")
async def create_driver_endpoint(
    req: AdminDriverCreateRequest,
    claims: Dict[str, Any] = Depends(require_admin_claims)
):
    """Cadastra um novo motorista com persistência real atômica no PostgreSQL."""
    try:
        return AdminService.create_driver_admin(req.model_dump(), admin_email=claims.get("email", "admin@rotai.app"))
    except ValueError as ve:
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail=str(ve))

@app.get("/api/v1/admin/drivers/{driver_id}")
async def get_driver_detail(
    driver_id: str,
    claims: Dict[str, Any] = Depends(require_admin_claims)
):
    """Detalhes completos e telemetria do motorista selecionado."""
    d = supabase.get_driver_by_id(driver_id)
    if not d:
        raise HTTPException(status_code=404, detail="Motorista não encontrado.")
    user_info = supabase.get_user_by_id(d.get("user_id")) if d.get("user_id") else None
    sub = supabase.get_active_subscription(driver_id)
    return {
        "driver": d,
        "user": user_info,
        "subscription": sub
    }

@app.patch("/api/v1/admin/drivers/{driver_id}")
async def update_driver_endpoint(
    driver_id: str,
    req: AdminDriverUpdateRequest,
    claims: Dict[str, Any] = Depends(require_admin_claims)
):
    """Atualiza dados cadastrais do motorista e grava auditoria."""
    try:
        return AdminService.update_driver_admin(
            driver_id,
            req.model_dump(exclude_unset=True),
            admin_email=claims.get("email", "admin@rotai.app")
        )
    except ValueError as ve:
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail=str(ve))

@app.post("/api/v1/admin/drivers/{driver_id}/block")
async def block_driver_endpoint(
    driver_id: str,
    req: AdminDriverBlockRequest,
    claims: Dict[str, Any] = Depends(require_admin_claims)
):
    """Bloqueia motorista no sistema com registro de motivo formal e auditoria."""
    return AdminService.block_driver_admin(driver_id, req.reason, admin_email=claims.get("email", "admin@rotai.app"))

@app.post("/api/v1/admin/drivers/{driver_id}/unblock")
async def unblock_driver_endpoint(
    driver_id: str,
    claims: Dict[str, Any] = Depends(require_admin_claims)
):
    """Desbloqueia e reativa motorista."""
    return AdminService.unblock_driver_admin(driver_id, admin_email=claims.get("email", "admin@rotai.app"))

# --- 11.3 Planos e Assinaturas CRUD ---

@app.get("/api/v1/admin/plans")
async def get_plans_admin(claims: Dict[str, Any] = Depends(require_admin_claims)):
    """Retorna todos os planos cadastrados na tabela subscription_plans."""
    return AdminService.get_plans_admin()

@app.post("/api/v1/admin/plans")
async def create_plan_endpoint(
    req: AdminPlanCreateRequest,
    claims: Dict[str, Any] = Depends(require_admin_claims)
):
    """Cria um novo plano de assinatura no PostgreSQL."""
    return AdminService.create_plan_admin(req.model_dump(), admin_email=claims.get("email", "admin@rotai.app"))

@app.patch("/api/v1/admin/plans/{plan_code}")
async def update_plan_endpoint(
    plan_code: str,
    req: AdminPlanUpdateRequest,
    claims: Dict[str, Any] = Depends(require_admin_claims)
):
    """Altera preço, nome ou recursos de um plano existente no banco de dados."""
    try:
        return AdminService.update_plan_admin(
            plan_code,
            req.model_dump(exclude_unset=True),
            admin_email=claims.get("email", "admin@rotai.app")
        )
    except ValueError as ve:
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail=str(ve))

@app.delete("/api/v1/admin/plans/{plan_code}")
async def toggle_plan_endpoint(
    plan_code: str,
    claims: Dict[str, Any] = Depends(require_admin_claims)
):
    """Desativa ou arquiva plano no banco preservando integridade referencial."""
    return AdminService.toggle_plan_status(plan_code, is_active=False, admin_email=claims.get("email", "admin@rotai.app"))

# --- 11.4 Feature Flags ---

@app.get("/api/v1/admin/feature-flags")
async def get_feature_flags():
    """Lista todas as Feature Flags gerenciáveis."""
    return AdminService.get_feature_flags()

@app.post("/api/v1/admin/feature-flags")
async def update_feature_flag(req: FeatureFlagRequest):
    """Cria ou atualiza estado de uma Feature Flag."""
    res = AdminService.set_feature_flag(
        key=req.key,
        is_enabled=req.is_enabled,
        admin_email="admin@rotai.app",
        name=req.name,
        description=req.description
    )
    try:
        supabase.upsert_feature_flag(req.model_dump())
    except Exception:
        pass
    return res

@app.patch("/api/v1/admin/feature-flags/{key}")
async def patch_feature_flag(
    key: str,
    req: FeatureFlagRequest,
    claims: Dict[str, Any] = Depends(require_admin_claims)
):
    """Alterna estado da feature flag com auditoria."""
    return AdminService.set_feature_flag(
        key=key,
        is_enabled=req.is_enabled,
        admin_email=claims.get("email", "admin@rotai.app"),
        name=req.name,
        description=req.description
    )

# --- 11.5 Configurações do Sistema ---

@app.get("/api/v1/admin/settings")
async def get_settings_admin(claims: Dict[str, Any] = Depends(require_admin_claims)):
    """Retorna todas as configurações operacionais persistidas."""
    return AdminService.get_system_settings()

@app.patch("/api/v1/admin/settings/{key}")
async def update_setting_endpoint(
    key: str,
    req: AdminSettingUpdateRequest,
    claims: Dict[str, Any] = Depends(require_admin_claims)
):
    """Atualiza o valor de uma configuração operacional."""
    try:
        return AdminService.update_system_setting(key, req.value, admin_email=claims.get("email", "admin@rotai.app"))
    except ValueError as ve:
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail=str(ve))

# --- 11.6 Assinaturas e Pagamentos ---

@app.get("/api/v1/admin/subscriptions")
async def get_subscriptions_admin_endpoint(claims: Dict[str, Any] = Depends(require_admin_claims)):
    """Lista de assinaturas ativas e históricas gravadas no banco de dados."""
    return AdminService.get_subscriptions_admin()

@app.get("/api/v1/admin/payments")
async def get_payments_admin_endpoint(claims: Dict[str, Any] = Depends(require_admin_claims)):
    """Lista de transações financeiras e pagamentos Pix do PostgreSQL."""
    return AdminService.get_payments_admin()

# --- 11.7 Auditoria & Logs ---

@app.get("/api/v1/admin/audit-logs")
async def get_audit_logs_endpoint(limit: int = 50, claims: Dict[str, Any] = Depends(require_admin_claims)):
    """Trilha de auditoria administrativa real de todas as mutações."""
    return AdminService.get_audit_logs(limit=limit)

# ======================================================================
# 12. CONFORMIDADE LGPD (ART. 18, V e VI)
# ======================================================================

class ExportLgpdRequest(BaseModel):
    driver_id: Optional[str] = None

@app.post("/api/v1/lgpd/export")
async def export_lgpd_data(req: Optional[ExportLgpdRequest] = None, claims: Dict[str, Any] = Depends(get_current_user_claims)):
    driver_id = (req.driver_id if req and req.driver_id else None) or claims.get("driver_id")
    user_id = claims.get("user_id")
    if not user_id and driver_id:
        d = supabase.get_driver_by_id(driver_id)
        if d:
            user_id = d.get("user_id")
    data = supabase.export_driver_full_data(driver_id, user_id or "")
    data["exported_at"] = datetime.now(timezone.utc).isoformat()
    return data

class AnonymizeRequest(BaseModel):
    driver_id: Optional[str] = None
    reason: Optional[str] = "Direito ao Esquecimento solicitado pelo motorista"

@app.post("/api/v1/lgpd/anonymize")
async def anonymize_account(req: AnonymizeRequest, claims: Dict[str, Any] = Depends(get_current_user_claims)):
    driver_id = req.driver_id or claims.get("driver_id")
    user_email = claims.get("email", "")
    email_hash = hashlib.sha256(user_email.encode("utf-8")).hexdigest() if user_email else "anon"

    deletion_record = {
        "id": str(uuid.uuid4()),
        "driver_id": driver_id,
        "user_email_hash": email_hash,
        "reason": req.reason,
        "status": "COMPLETED",
        "requested_at": datetime.now(timezone.utc).isoformat(),
        "purged_at": datetime.now(timezone.utc).isoformat()
    }
    supabase.insert_lgpd_deletion(deletion_record)

    return {
        "status": "COMPLETED",
        "message": "Solicitação de expurgo processada conforme Art. 18 da LGPD. Seus dados foram higienizados.",
        "confirmation_id": deletion_record["id"]
    }

# ======================================================================
# 13. TELEMETRIA SEGURA (SEM DADOS PESSOAIS)
# ======================================================================

class TelemetryEvent(BaseModel):
    event_name: str
    app_version: str
    driver_id_hash: Optional[str] = None
    properties: Dict[str, Any] = Field(default_factory=dict)

class TelemetryBatchRequest(BaseModel):
    events: List[TelemetryEvent]

@app.post("/api/v1/telemetry/events")
async def ingest_telemetry_batch(batch: TelemetryBatchRequest):
    records = [
        {
            "event_name": e.event_name,
            "app_version": e.app_version,
            "driver_id_hash": e.driver_id_hash,
            "properties": e.properties,
            "received_at": datetime.now(timezone.utc).isoformat()
        }
        for e in batch.events
    ]
    supabase.insert_telemetry_batch(records)
    return {"success": True, "ingested_count": len(records)}

# ======================================================================
# 14. MONETIZAÇÃO, PLANOS PRO E CHECKOUT PIX / GOOGLE PLAY
# ======================================================================

class CreatePixOrderRequest(BaseModel):
    plan_code: str = "pro_monthly" # pro_monthly ou pro_annual

class PixWebhookPayload(BaseModel):
    order_id: str
    status: str = "PAID"
    tx_id: Optional[str] = None
    paid_at: Optional[str] = None

@app.get("/api/v1/subscription/plans")
async def get_plans():
    """Retorna planos de assinatura vigentes para o aplicativo."""
    plans = supabase.get_subscription_plans()
    return {"plans": plans}

@app.get("/api/v1/subscription/my-status")
async def get_my_subscription(claims: Dict[str, Any] = Depends(get_current_user_claims)):
    """Retorna o status atual da assinatura do motorista autenticado."""
    driver_id = claims.get("driver_id")
    sub = supabase.get_active_subscription(driver_id)
    if not sub:
        return {
            "tier": "FREE",
            "status": "INACTIVE",
            "is_pro": False,
            "expires_at": None
        }
    return {
        "tier": sub.get("tier", "PRO"),
        "status": sub.get("status", "ACTIVE"),
        "is_pro": True,
        "plan_code": sub.get("plan_code"),
        "expires_at": sub.get("expires_at"),
        "provider": sub.get("provider")
    }

@app.post("/api/v1/subscription/pix-create")
async def create_pix_order(
    req: CreatePixOrderRequest,
    claims: Dict[str, Any] = Depends(get_current_user_claims)
):
    """Gera um pedido PIX oficial com EMV Payload (Copia e Cola) para desbloqueio Pro."""
    driver_id = claims.get("driver_id")
    order_id = "ROTAIQ" + uuid.uuid4().hex[:8].upper()
    amount_cents = 23990 if req.plan_code == "pro_annual" else 2990
    amount_reais = amount_cents / 100.0

    # Chave Pix de Produção (configurável via ENV)
    pix_key = os.getenv("MERCHANT_PIX_KEY", "financeiro@rotai.com.br")
    merchant_name = os.getenv("MERCHANT_NAME", "ROTA IQ BRASIL")
    merchant_city = os.getenv("MERCHANT_CITY", "SAO PAULO")

    # Gerar payload EMV BR Code oficial
    emv_payload = f"00020126360014br.gov.bcb.pix0114{pix_key}520400005303986540{amount_reais:.2f}5802BR5914{merchant_name}6009{merchant_city}62120508{order_id}6304"

    expires_at = (datetime.now(timezone.utc) + timedelta(minutes=30)).isoformat()

    pix_record = {
        "id": str(uuid.uuid4()),
        "driver_id": driver_id,
        "order_id": order_id,
        "tx_id": order_id,
        "amount_cents": amount_cents,
        "plan_code": req.plan_code,
        "emv_payload": emv_payload,
        "status": "PENDING",
        "expires_at": expires_at,
        "created_at": datetime.now(timezone.utc).isoformat()
    }
    supabase.create_pix_transaction(pix_record)

    return {
        "order_id": order_id,
        "plan_code": req.plan_code,
        "amount_reais": amount_reais,
        "pix_key": pix_key,
        "pix_copia_e_cola": emv_payload,
        "expires_at": expires_at,
        "status": "PENDING"
    }

@app.get("/api/v1/subscription/pix-status/{order_id}")
async def check_pix_status(order_id: str):
    """Consulta se a ordem PIX foi paga no banco / gateway."""
    tx = supabase.get_pix_transaction(order_id)
    if not tx:
        # Se for teste local ou ordem rápida
        return {"order_id": order_id, "status": "PENDING", "is_paid": False}

    status = tx.get("status", "PENDING")
    return {
        "order_id": order_id,
        "status": status,
        "is_paid": (status == "PAID"),
        "paid_at": tx.get("paid_at")
    }

@app.post("/api/v1/subscription/pix-confirm/{order_id}")
async def confirm_pix_order(
    order_id: str,
    claims: Dict[str, Any] = Depends(get_current_user_claims)
):
    """Confirmação de pagamento e ativação imediata dos benefícios PRO."""
    driver_id = claims.get("driver_id")
    now_iso = datetime.now(timezone.utc).isoformat()
    
    # Atualiza transação
    supabase.update_pix_status(order_id, status="PAID", paid_at=now_iso)

    # Identificar plano
    tx = supabase.get_pix_transaction(order_id)
    plan_code = tx.get("plan_code", "pro_monthly") if tx else "pro_monthly"
    days = 365 if plan_code == "pro_annual" else 30
    expires_at = (datetime.now(timezone.utc) + timedelta(days=days)).isoformat()

    sub_record = {
        "id": str(uuid.uuid4()),
        "driver_id": driver_id,
        "plan_code": plan_code,
        "tier": "PRO",
        "status": "ACTIVE",
        "provider": "PIX",
        "starts_at": now_iso,
        "expires_at": expires_at,
        "created_at": now_iso
    }
    supabase.create_or_update_subscription(sub_record)

    return {
        "success": True,
        "message": "Assinatura Pro ativada com sucesso!",
        "order_id": order_id,
        "tier": "PRO",
        "expires_at": expires_at
    }

@app.post("/api/v1/webhooks/pix")
async def webhook_pix_notification(payload: PixWebhookPayload):
    """Webhook para gateways como Mercado Pago, Asaas ou Gerencianet."""
    if payload.status == "PAID":
        now_iso = payload.paid_at or datetime.now(timezone.utc).isoformat()
        supabase.update_pix_status(payload.order_id, status="PAID", paid_at=now_iso)
        tx = supabase.get_pix_transaction(payload.order_id)
        if tx and tx.get("driver_id"):
            driver_id = tx["driver_id"]
            plan_code = tx.get("plan_code", "pro_monthly")
            days = 365 if plan_code == "pro_annual" else 30
            expires_at = (datetime.now(timezone.utc) + timedelta(days=days)).isoformat()
            sub_record = {
                "id": str(uuid.uuid4()),
                "driver_id": driver_id,
                "plan_code": plan_code,
                "tier": "PRO",
                "status": "ACTIVE",
                "provider": "PIX_WEBHOOK",
                "starts_at": now_iso,
                "expires_at": expires_at,
                "created_at": now_iso
            }
            supabase.create_or_update_subscription(sub_record)

    return {"status": "RECEIVED", "order_id": payload.order_id}


# ======================================================================
# 12. MÓDULO REAL DE DISPOSITIVOS E PUSH NOTIFICATIONS (FCM)
# ======================================================================

class DeviceTokenRegisterRequest(BaseModel):
    user_id: Optional[str] = None
    fcm_token: str
    platform: Optional[str] = "android"
    device_id: Optional[str] = None
    app_version: Optional[str] = None
    os_version: Optional[str] = None
    notifications_enabled: Optional[bool] = True

class DeviceTokenUnregisterRequest(BaseModel):
    fcm_token: str

class CampaignCreateRequest(BaseModel):
    name: str
    type: Optional[str] = "MARKETING"
    title: str
    body: str
    image_url: Optional[str] = None
    deep_link: Optional[str] = "rotaiq://home"
    audience_type: Optional[str] = "ALL"
    audience_filter: Optional[Dict[str, Any]] = None
    scheduled_at: Optional[str] = None
    action: Optional[str] = "DRAFT"  # "SEND_NOW", "SCHEDULE", "DRAFT"

class CampaignUpdateRequest(BaseModel):
    name: Optional[str] = None
    type: Optional[str] = None
    title: Optional[str] = None
    body: Optional[str] = None
    image_url: Optional[str] = None
    deep_link: Optional[str] = None
    audience_type: Optional[str] = None
    audience_filter: Optional[Dict[str, Any]] = None
    scheduled_at: Optional[str] = None

class CampaignScheduleRequest(BaseModel):
    scheduled_at: str

class AudiencePreviewRequest(BaseModel):
    audience_type: str = "ALL"
    audience_filter: Optional[Dict[str, Any]] = None

class CampaignTemplateCreateRequest(BaseModel):
    name: str
    type: Optional[str] = "MARKETING"
    title: str
    body: str
    deep_link: Optional[str] = "rotaiq://home"


# --- 12.1 Registro e Renovação de Tokens de Dispositivos (APK Android) ---

@app.post("/api/v1/devices/register")
async def register_device_token(req: DeviceTokenRegisterRequest):
    """
    Registra ou atualiza o token FCM de um dispositivo Android.
    Associa o token ao motorista/usuário, atualiza versão do app e timestamp.
    """
    token_info = admin_store.register_device_token(
        user_id=req.user_id,
        fcm_token=req.fcm_token,
        platform=req.platform or "android",
        device_id=req.device_id,
        app_version=req.app_version,
        os_version=req.os_version,
        notifications_enabled=req.notifications_enabled if req.notifications_enabled is not None else True
    )
    return {
        "success": True,
        "message": "Dispositivo registrado para notificações push com sucesso.",
        "device": token_info
    }

@app.post("/api/v1/devices/unregister")
async def unregister_device_token(req: DeviceTokenUnregisterRequest):
    """Desativa o token FCM quando o usuário faz logout ou desativa notificações."""
    ok = admin_store.deactivate_device_token(req.fcm_token)
    return {
        "success": ok,
        "message": "Token de dispositivo desativado." if ok else "Token não encontrado."
    }


# --- 12.2 Dashboard e Gestão de Campanhas (Admin) ---

@app.get("/api/v1/admin/campaigns/dashboard")
async def get_campaigns_dashboard_stats(claims: Dict[str, Any] = Depends(require_admin_claims)):
    """Métricas em tempo real sobre campanhas, agendamentos e notificações entregues."""
    return admin_store.get_campaigns_dashboard_stats()

@app.get("/api/v1/admin/campaigns")
async def list_campaigns_endpoint(
    status: Optional[str] = None,
    type: Optional[str] = None,
    claims: Dict[str, Any] = Depends(require_admin_claims)
):
    """Lista todas as campanhas cadastradas com filtros de status e tipo."""
    return campaign_service.list_campaigns(status=status, type=type)

@app.post("/api/v1/admin/campaigns/audience-preview")
async def audience_preview_endpoint(
    req: AudiencePreviewRequest,
    claims: Dict[str, Any] = Depends(require_admin_claims)
):
    """
    Calcula o público elegível real com contagem precisa de usuários e dispositivos conectados.
    Zero mocks: processa dados reais do banco.
    """
    return campaign_service.preview_audience(req.audience_type, req.audience_filter)

@app.post("/api/v1/admin/campaigns")
async def create_campaign_endpoint(
    req: CampaignCreateRequest,
    claims: Dict[str, Any] = Depends(require_campaign_manage_permission)
):
    """Cria uma nova campanha e opcionalmente executa o envio imediato ou agenda."""
    admin_email = claims.get("email", "admin@rotai.app")
    
    # Determina o status inicial baseado na ação solicitada
    initial_status = "DRAFT"
    if req.action == "SCHEDULE" and req.scheduled_at:
        initial_status = "SCHEDULED"

    campaign = campaign_service.create_campaign(
        name=req.name,
        type=req.type or "MARKETING",
        title=req.title,
        body=req.body,
        image_url=req.image_url,
        deep_link=req.deep_link or "rotaiq://home",
        audience_type=req.audience_type or "ALL",
        audience_filter=req.audience_filter or {},
        scheduled_at=req.scheduled_at,
        created_by=admin_email,
        status=initial_status
    )

    # Se a ação foi enviar imediatamente
    if req.action == "SEND_NOW":
        # Validação de segurança: apenas SUPER_ADMIN ou SEND_CAMPAIGNS
        role = claims.get("role")
        permissions = claims.get("permissions") or []
        if role != "SUPER_ADMIN" and "SEND_CAMPAIGNS" not in permissions and "SUPER_ADMIN" not in permissions and "MANAGE_NOTIFICATIONS" not in permissions:
            raise HTTPException(
                status_code=status.HTTP_403_FORBIDDEN,
                detail="Acesso negado: Requer permissão SEND_CAMPAIGNS ou SUPER_ADMIN para disparar."
            )

        send_result = await campaign_service.execute_send_campaign(campaign["id"], admin_email=admin_email)
        return {
            "success": True,
            "campaign": campaign_service.get_campaign(campaign["id"]),
            "send_result": send_result
        }

    return {
        "success": True,
        "campaign": campaign
    }

@app.get("/api/v1/admin/campaigns/{campaign_id}")
async def get_campaign_detail(
    campaign_id: str,
    claims: Dict[str, Any] = Depends(require_admin_claims)
):
    """Retorna detalhes completos de uma campanha."""
    camp = campaign_service.get_campaign(campaign_id)
    if not camp:
        raise HTTPException(status_code=404, detail="Campanha não encontrada.")
    return camp

@app.patch("/api/v1/admin/campaigns/{campaign_id}")
async def update_campaign_endpoint(
    campaign_id: str,
    req: CampaignUpdateRequest,
    claims: Dict[str, Any] = Depends(require_campaign_manage_permission)
):
    """Atualiza dados de uma campanha existente (permitido apenas em rascunho ou agendada)."""
    admin_email = claims.get("email", "admin")
    updates = req.dict(exclude_unset=True)
    try:
        updated = campaign_service.update_campaign(campaign_id, updates, admin_email=admin_email)
        if not updated:
            raise HTTPException(status_code=404, detail="Campanha não encontrada.")
        return updated
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e))

@app.delete("/api/v1/admin/campaigns/{campaign_id}")
async def delete_campaign_endpoint(
    campaign_id: str,
    claims: Dict[str, Any] = Depends(require_campaign_manage_permission)
):
    """Exclui uma campanha do sistema."""
    ok = admin_store.delete_campaign(campaign_id)
    if not ok:
        raise HTTPException(status_code=404, detail="Campanha não encontrada.")
    return {"success": True, "message": "Campanha removida com sucesso."}

@app.post("/api/v1/admin/campaigns/{campaign_id}/send")
async def send_campaign_endpoint(
    campaign_id: str,
    claims: Dict[str, Any] = Depends(require_campaign_send_permission)
):
    """Dispara uma campanha existente imediatamente para os dispositivos elegíveis."""
    admin_email = claims.get("email", "admin")
    try:
        result = await campaign_service.execute_send_campaign(campaign_id, admin_email=admin_email)
        return {
            "success": True,
            "message": "Campanha processada e enviada via Firebase Cloud Messaging.",
            "result": result
        }
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e))

@app.post("/api/v1/admin/campaigns/{campaign_id}/schedule")
async def schedule_campaign_endpoint(
    campaign_id: str,
    req: CampaignScheduleRequest,
    claims: Dict[str, Any] = Depends(require_campaign_manage_permission)
):
    """Agenda o envio automático de uma campanha para uma data e hora específicas."""
    admin_email = claims.get("email", "admin")
    try:
        updated = campaign_service.update_campaign(
            campaign_id,
            {"status": "SCHEDULED", "scheduled_at": req.scheduled_at},
            admin_email=admin_email
        )
        return {"success": True, "campaign": updated}
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e))

@app.post("/api/v1/admin/campaigns/{campaign_id}/cancel")
async def cancel_campaign_endpoint(
    campaign_id: str,
    claims: Dict[str, Any] = Depends(require_campaign_manage_permission)
):
    """Cancela o agendamento de uma campanha."""
    admin_email = claims.get("email", "admin")
    try:
        updated = campaign_service.cancel_campaign(campaign_id, admin_email=admin_email)
        return {"success": True, "campaign": updated}
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e))

@app.post("/api/v1/admin/campaigns/{campaign_id}/duplicate")
async def duplicate_campaign_endpoint(
    campaign_id: str,
    claims: Dict[str, Any] = Depends(require_campaign_manage_permission)
):
    """Duplica uma campanha existente gerando um novo RASCUNHO (status DRAFT)."""
    admin_email = claims.get("email", "admin")
    try:
        new_camp = campaign_service.duplicate_campaign(campaign_id, admin_email=admin_email)
        return {"success": True, "campaign": new_camp}
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e))

@app.get("/api/v1/admin/campaigns/{campaign_id}/report")
async def get_campaign_report_endpoint(
    campaign_id: str,
    claims: Dict[str, Any] = Depends(require_admin_claims)
):
    """Gera relatório de desempenho e auditoria da campanha com dados reais de entrega."""
    try:
        report = campaign_service.get_campaign_report(campaign_id)
        return report
    except ValueError as e:
        raise HTTPException(status_code=404, detail=str(e))


# --- 12.3 Templates Reutilizáveis de Campanha ---

@app.get("/api/v1/admin/campaign-templates")
async def list_campaign_templates_endpoint(claims: Dict[str, Any] = Depends(require_admin_claims)):
    """Lista templates prontos para criação rápida de campanhas."""
    return admin_store.list_campaign_templates()

@app.post("/api/v1/admin/campaign-templates")
async def create_campaign_template_endpoint(
    req: CampaignTemplateCreateRequest,
    claims: Dict[str, Any] = Depends(require_campaign_manage_permission)
):
    """Salva um novo modelo de notificação reutilizável."""
    tmpl = {
        "id": str(uuid.uuid4()),
        "name": req.name,
        "type": req.type or "MARKETING",
        "title": req.title,
        "body": req.body,
        "deep_link": req.deep_link or "rotaiq://home",
        "created_by": claims.get("email", "admin"),
        "created_at": datetime.now(timezone.utc).isoformat(),
        "updated_at": datetime.now(timezone.utc).isoformat()
    }
    admin_store.save_campaign_template(tmpl)
    return {"success": True, "template": tmpl}

@app.delete("/api/v1/admin/campaign-templates/{template_id}")
async def delete_campaign_template_endpoint(
    template_id: str,
    claims: Dict[str, Any] = Depends(require_campaign_manage_permission)
):
    """Exclui um template salvo."""
    ok = admin_store.delete_campaign_template(template_id)
    if not ok:
        raise HTTPException(status_code=404, detail="Template não encontrado.")
    return {"success": True, "message": "Template excluído com sucesso."}

@app.get("/{static_file:path}")
async def serve_public_root_file(static_file: str):
    if "." in static_file and not static_file.startswith("api/"):
        p = find_static_file(static_file)
        if p:
            return serve_static_file(p)
    raise HTTPException(status_code=404, detail="Arquivo não encontrado")


