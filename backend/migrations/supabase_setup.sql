-- ====================================================================
-- ROTA IQ - SETUP COMPLETO SUPABASE POSTGRESQL (Fases 1 a 6)
-- Plataforma Inteligente de Decisão, Produtividade e Gestão Financeira
-- ====================================================================

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- -------------------------------------------------------------
-- 1. USUÁRIOS E MOTORISTAS
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    phone VARCHAR(30) UNIQUE,
    full_name VARCHAR(150) NOT NULL,
    is_active BOOLEAN DEFAULT TRUE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL
);

CREATE TABLE IF NOT EXISTS drivers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    cpf VARCHAR(14) UNIQUE,
    cnh_number VARCHAR(20),
    city VARCHAR(100) NOT NULL,
    state VARCHAR(2) NOT NULL,
    status VARCHAR(30) DEFAULT 'ACTIVE' NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL
);

-- -------------------------------------------------------------
-- 2. VEÍCULOS E CUSTOS OPERACIONAIS REAIS
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS vehicles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    driver_id UUID NOT NULL REFERENCES drivers(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    plate VARCHAR(20) NOT NULL,
    model VARCHAR(100) NOT NULL,
    year INT NOT NULL,
    fuel_type VARCHAR(30) NOT NULL,
    consumption_km_per_liter NUMERIC(5,2) NOT NULL,
    fuel_price_per_liter NUMERIC(6,2) NOT NULL,
    maintenance_cost_per_km NUMERIC(6,3) NOT NULL,
    is_active BOOLEAN DEFAULT TRUE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL
);

CREATE TABLE IF NOT EXISTS vehicle_costs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    vehicle_id UUID NOT NULL REFERENCES vehicles(id) ON DELETE CASCADE,
    monthly_insurance_cost NUMERIC(10,2) DEFAULT 0.0,
    annual_taxes_cost NUMERIC(10,2) DEFAULT 0.0,
    monthly_depreciation NUMERIC(10,2) DEFAULT 0.0,
    monthly_other_costs NUMERIC(10,2) DEFAULT 0.0,
    estimated_monthly_km NUMERIC(8,2) DEFAULT 3000.0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL
);

-- -------------------------------------------------------------
-- 3. METAS E PREFERÊNCIAS OPERACIONAIS
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS driver_goals (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    driver_id UUID NOT NULL REFERENCES drivers(id) ON DELETE CASCADE,
    daily_gross_target NUMERIC(10,2) NOT NULL,
    target_hourly_rate NUMERIC(10,2) NOT NULL,
    target_km_rate NUMERIC(10,2) NOT NULL,
    shift_target_hours NUMERIC(4,1) NOT NULL,
    weekly_target NUMERIC(10,2),
    monthly_target NUMERIC(10,2),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL
);

CREATE TABLE IF NOT EXISTS driver_preferences (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    driver_id UUID NOT NULL REFERENCES drivers(id) ON DELETE CASCADE,
    min_score_threshold INT DEFAULT 65 NOT NULL,
    max_pickup_distance_km NUMERIC(5,2) DEFAULT 4.0 NOT NULL,
    reject_negative_profit BOOLEAN DEFAULT TRUE NOT NULL,
    tts_voice_alerts_enabled BOOLEAN DEFAULT TRUE NOT NULL,
    overlay_hud_enabled BOOLEAN DEFAULT TRUE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL
);

-- -------------------------------------------------------------
-- 4. REGISTROS FINANCEIROS (COMBUSTÍVEL 2 TANQUES, MANUTENÇÃO, DESPESAS)
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS fuel_records (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    driver_id UUID REFERENCES drivers(id) ON DELETE CASCADE,
    vehicle_id UUID REFERENCES vehicles(id) ON DELETE CASCADE,
    date DATE NOT NULL,
    odometer_km NUMERIC(10,2) NOT NULL,
    liters NUMERIC(8,3) NOT NULL,
    price_per_liter NUMERIC(6,3) NOT NULL,
    total_paid NUMERIC(10,2) NOT NULL,
    fuel_type VARCHAR(30) NOT NULL,
    is_full_tank BOOLEAN DEFAULT TRUE NOT NULL,
    calculated_km_per_liter NUMERIC(5,2),
    calculated_cost_per_km NUMERIC(6,3),
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL
);

CREATE TABLE IF NOT EXISTS maintenance_records (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    driver_id UUID REFERENCES drivers(id) ON DELETE CASCADE,
    vehicle_id UUID REFERENCES vehicles(id) ON DELETE CASCADE,
    date DATE NOT NULL,
    odometer_km NUMERIC(10,2) NOT NULL,
    service_type VARCHAR(50) NOT NULL,
    description TEXT NOT NULL,
    cost NUMERIC(10,2) NOT NULL,
    next_service_km NUMERIC(10,2),
    is_completed BOOLEAN DEFAULT TRUE NOT NULL,
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL
);

CREATE TABLE IF NOT EXISTS vehicle_expenses (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    driver_id UUID REFERENCES drivers(id) ON DELETE CASCADE,
    vehicle_id UUID REFERENCES vehicles(id) ON DELETE CASCADE,
    date DATE NOT NULL,
    category VARCHAR(50) NOT NULL,
    description TEXT NOT NULL,
    amount NUMERIC(10,2) NOT NULL,
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL
);

-- -------------------------------------------------------------
-- 5. HISTÓRICO DE OFERTAS, AVALIAÇÕES E CORRIDAS
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS ride_evaluations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    driver_id UUID REFERENCES drivers(id) ON DELETE CASCADE,
    platform VARCHAR(30) NOT NULL, -- UBER, NINETY_NINE, INDRAVE, OTHER
    gross_fare NUMERIC(10,2) NOT NULL,
    distance_km NUMERIC(6,2) NOT NULL,
    duration_minutes NUMERIC(6,1) NOT NULL,
    pickup_distance_km NUMERIC(6,2) DEFAULT 0.0 NOT NULL,
    pickup_duration_minutes NUMERIC(6,1) DEFAULT 0.0 NOT NULL,
    category VARCHAR(30) DEFAULT 'STANDARD' NOT NULL,
    stops_count INT DEFAULT 0 NOT NULL,
    score INT NOT NULL,
    classification VARCHAR(30) NOT NULL, -- EXCELLENT, GOOD, ACCEPTABLE, BAD, AVOID
    estimated_cost NUMERIC(10,2) NOT NULL,
    net_profit NUMERIC(10,2) NOT NULL,
    profit_margin_percent NUMERIC(6,2) NOT NULL,
    gross_rate_per_km NUMERIC(6,2) NOT NULL,
    net_rate_per_km NUMERIC(6,2) NOT NULL,
    gross_rate_per_hour NUMERIC(8,2) NOT NULL,
    net_rate_per_hour NUMERIC(8,2) NOT NULL,
    was_accepted BOOLEAN DEFAULT FALSE,
    evaluated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL
);

-- -------------------------------------------------------------
-- 6. PRODUTO COMERCIAL: PLANOS, TRANSAÇÕES PIX E GOOGLE PLAY BILLING
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS subscription_plans (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(50) UNIQUE NOT NULL, -- 'free', 'pro_monthly', 'pro_annual'
    name VARCHAR(100) NOT NULL,
    price_cents INT NOT NULL,
    interval VARCHAR(20) NOT NULL,
    features JSONB NOT NULL DEFAULT '[]',
    is_active BOOLEAN DEFAULT TRUE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL
);

INSERT INTO subscription_plans (code, name, price_cents, interval, features)
VALUES 
    ('free', 'Plano Gratuito', 0, 'none', '["15 avaliações diárias", "Cálculo de custos", "Metas de ritmo"]'),
    ('pro_monthly', 'ROTA IQ Pro Mensal', 2990, 'month', '["Avaliações ilimitadas", "HUD Flutuante", "Copiloto por Voz TTS", "Preditor Deadhead", "Comparativo Uber vs 99", "Modo Carro Bluetooth", "inDrive Contraproposta"]'),
    ('pro_annual', 'ROTA IQ Pro Anual', 23990, 'year', '["Avaliações ilimitadas", "HUD Flutuante", "Copiloto por Voz TTS", "Preditor Deadhead", "Comparativo Uber vs 99", "Modo Carro Bluetooth", "inDrive Contraproposta", "Livro Caixa MEI / IRPF", "33% de desconto", "Suporte VIP"]')
ON CONFLICT (code) DO NOTHING;

CREATE TABLE IF NOT EXISTS subscriptions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    driver_id UUID NOT NULL REFERENCES drivers(id) ON DELETE CASCADE,
    plan_code VARCHAR(50) NOT NULL,
    tier VARCHAR(30) NOT NULL, -- FREE, PRO
    status VARCHAR(30) NOT NULL, -- ACTIVE, EXPIRED, CANCELLED
    provider VARCHAR(30) NOT NULL, -- PLAY_BILLING, PIX, MANUAL
    starts_at TIMESTAMP WITH TIME ZONE NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL
);

CREATE TABLE IF NOT EXISTS pix_transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    driver_id UUID REFERENCES drivers(id) ON DELETE CASCADE,
    order_id VARCHAR(50) UNIQUE NOT NULL,
    tx_id VARCHAR(50) NOT NULL,
    amount_cents INT NOT NULL,
    plan_code VARCHAR(50) NOT NULL,
    emv_payload TEXT NOT NULL,
    status VARCHAR(30) DEFAULT 'PENDING' NOT NULL, -- PENDING, PAID, EXPIRED, CANCELLED
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    paid_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL
);

CREATE TABLE IF NOT EXISTS play_billing_receipts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    driver_id UUID REFERENCES drivers(id) ON DELETE CASCADE,
    order_id VARCHAR(100) NOT NULL,
    sku_id VARCHAR(100) NOT NULL,
    purchase_token TEXT NOT NULL,
    purchase_state VARCHAR(30) NOT NULL,
    acknowledged BOOLEAN DEFAULT FALSE NOT NULL,
    purchase_time_ms BIGINT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL
);

-- -------------------------------------------------------------
-- 7. LGPD, AUDITORIA E TELEMETRIA HIGIENIZADA
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS lgpd_deletion_requests (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    driver_id UUID NOT NULL,
    user_email_hash VARCHAR(64) NOT NULL,
    reason TEXT,
    status VARCHAR(30) DEFAULT 'COMPLETED' NOT NULL,
    requested_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL,
    purged_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL
);

CREATE TABLE IF NOT EXISTS sanitized_telemetry_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_name VARCHAR(100) NOT NULL,
    app_version VARCHAR(30) NOT NULL,
    driver_id_hash VARCHAR(64),
    properties JSONB NOT NULL DEFAULT '{}',
    received_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL
);

CREATE TABLE IF NOT EXISTS feature_flags (
    key VARCHAR(100) PRIMARY KEY,
    description TEXT,
    is_enabled BOOLEAN DEFAULT TRUE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL
);

INSERT INTO feature_flags (key, description, is_enabled) VALUES
    ('copilot_voice_tts', 'Habilita síntese vocal TTS no app motorista', TRUE),
    ('deadhead_prediction', 'Ativa cálculo de volta vazia e zonas mortas', TRUE),
    ('pix_instant_checkout', 'Permite pagamento instantâneo via PIX Copia e Cola', TRUE)
ON CONFLICT (key) DO NOTHING;

-- -------------------------------------------------------------
-- 8. ÍNDICES DE ALTA PERFORMANCE
-- -------------------------------------------------------------
CREATE INDEX IF NOT EXISTS idx_fuel_records_driver ON fuel_records(driver_id, date);
CREATE INDEX IF NOT EXISTS idx_maintenance_records_driver ON maintenance_records(driver_id, date);
CREATE INDEX IF NOT EXISTS idx_expenses_driver ON vehicle_expenses(driver_id, date);
CREATE INDEX IF NOT EXISTS idx_ride_evaluations_driver ON ride_evaluations(driver_id, evaluated_at);
CREATE INDEX IF NOT EXISTS idx_subscriptions_driver ON subscriptions(driver_id, status);
CREATE INDEX IF NOT EXISTS idx_pix_order ON pix_transactions(order_id);

-- -------------------------------------------------------------
-- 9. HABILITAÇÃO DO ROW LEVEL SECURITY (RLS)
-- -------------------------------------------------------------
ALTER TABLE subscription_plans ENABLE ROW LEVEL SECURITY;
ALTER TABLE subscriptions ENABLE ROW LEVEL SECURITY;
ALTER TABLE pix_transactions ENABLE ROW LEVEL SECURITY;
ALTER TABLE fuel_records ENABLE ROW LEVEL SECURITY;
ALTER TABLE maintenance_records ENABLE ROW LEVEL SECURITY;
ALTER TABLE vehicle_expenses ENABLE ROW LEVEL SECURITY;
ALTER TABLE ride_evaluations ENABLE ROW LEVEL SECURITY;

-- Política de leitura pública para planos de assinatura
CREATE POLICY "Planos visíveis para todos" ON subscription_plans
    FOR SELECT USING (is_active = TRUE);

-- -------------------------------------------------------------
-- 10. MÓDULO DE CAMPANHAS E PUSH NOTIFICATIONS (FCM)
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS device_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    fcm_token TEXT UNIQUE NOT NULL,
    platform VARCHAR(30) DEFAULT 'android' NOT NULL,
    device_id VARCHAR(100),
    app_version VARCHAR(50),
    os_version VARCHAR(50),
    active BOOLEAN DEFAULT TRUE NOT NULL,
    notifications_enabled BOOLEAN DEFAULT TRUE NOT NULL,
    last_seen_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_device_tokens_user ON device_tokens(user_id);
CREATE INDEX IF NOT EXISTS idx_device_tokens_active ON device_tokens(active);
CREATE INDEX IF NOT EXISTS idx_device_tokens_fcm ON device_tokens(fcm_token);

CREATE TABLE IF NOT EXISTS campaigns (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(200) NOT NULL,
    type VARCHAR(50) DEFAULT 'MARKETING' NOT NULL,
    title VARCHAR(200) NOT NULL,
    body TEXT NOT NULL,
    image_url TEXT,
    deep_link VARCHAR(255) DEFAULT 'rotaiq://home',
    status VARCHAR(50) DEFAULT 'DRAFT' NOT NULL,
    audience_type VARCHAR(50) DEFAULT 'ALL' NOT NULL,
    audience_filter JSONB DEFAULT '{}',
    scheduled_at TIMESTAMP WITH TIME ZONE,
    sent_at TIMESTAMP WITH TIME ZONE,
    total_recipients INT DEFAULT 0,
    total_sent INT DEFAULT 0,
    total_failed INT DEFAULT 0,
    total_opened INT DEFAULT 0,
    created_by VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_campaigns_status ON campaigns(status);
CREATE INDEX IF NOT EXISTS idx_campaigns_type ON campaigns(type);
CREATE INDEX IF NOT EXISTS idx_campaigns_created_at ON campaigns(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_campaigns_scheduled ON campaigns(scheduled_at);

CREATE TABLE IF NOT EXISTS campaign_deliveries (
    id BIGSERIAL PRIMARY KEY,
    campaign_id UUID REFERENCES campaigns(id) ON DELETE CASCADE NOT NULL,
    user_id UUID,
    fcm_token TEXT NOT NULL,
    status VARCHAR(50) NOT NULL,
    fcm_message_id VARCHAR(255),
    error_code VARCHAR(100),
    sent_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL,
    opened_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_campaign_deliveries_camp ON campaign_deliveries(campaign_id);
CREATE INDEX IF NOT EXISTS idx_campaign_deliveries_user ON campaign_deliveries(user_id);
CREATE INDEX IF NOT EXISTS idx_campaign_deliveries_status ON campaign_deliveries(status);

CREATE TABLE IF NOT EXISTS campaign_templates (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(200) NOT NULL,
    type VARCHAR(50) DEFAULT 'MARKETING' NOT NULL,
    title VARCHAR(200) NOT NULL,
    body TEXT NOT NULL,
    deep_link VARCHAR(255) DEFAULT 'rotaiq://home',
    created_by VARCHAR(255) DEFAULT 'system' NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL
);

INSERT INTO campaign_templates (name, type, title, body, deep_link, created_by)
VALUES 
    ('Atualização Disponível', 'ATUALIZACAO', '🚀 Nova versão do ROTA IQ!', 'Atualizamos o copiloto com novas métricas de lucro e suporte aprimorado. Toque para atualizar.', 'rotaiq://home', 'system'),
    ('Oferta Especial Pro', 'PROMOCAO', '💎 30% OFF no ROTA IQ Pro!', 'Desbloqueie avaliações ilimitadas e HUD flutuante com desconto especial para você.', 'rotaiq://subscription', 'system'),
    ('Alerta de Alta Demanda', 'ENGAJAMENTO', '🔥 Chuva de Corridas na sua Região!', 'A demanda está alta agora na sua praça. Abra o app e ative o filtro inteligente de corridas.', 'rotaiq://rides', 'system')
ON CONFLICT DO NOTHING;

