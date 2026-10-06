-- ====================================================================
-- ROTA IQ - MIGRATION 003: COMERCIAL, ASSINATURAS, PIX, LGPD E ADMIN
-- PostgreSQL 15+ / PostGIS compatível
-- ====================================================================

-- 1. TABELA DE PLANOS DE ASSINATURA
CREATE TABLE IF NOT EXISTS subscription_plans (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(50) UNIQUE NOT NULL, -- 'free', 'pro_monthly', 'pro_annual'
    name VARCHAR(100) NOT NULL,
    price_cents INT NOT NULL,
    interval VARCHAR(20) NOT NULL, -- 'month', 'year', 'none'
    features JSONB NOT NULL DEFAULT '[]',
    is_active BOOLEAN DEFAULT TRUE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL
);

-- Seed de planos oficiais ROTA IQ
INSERT INTO subscription_plans (code, name, price_cents, interval, features)
VALUES 
    ('free', 'Plano Gratuito', 0, 'none', '["15 avaliações diárias", "Cálculo de custos", "Metas de ritmo"]'),
    ('pro_monthly', 'ROTA IQ Pro Mensal', 2990, 'month', '["Avaliações ilimitadas", "HUD Flutuante", "Copiloto por Voz TTS", "Preditor Deadhead", "Comparativo Uber vs 99"]'),
    ('pro_annual', 'ROTA IQ Pro Anual', 23990, 'year', '["Avaliações ilimitadas", "HUD Flutuante", "Copiloto por Voz TTS", "Preditor Deadhead", "Comparativo Uber vs 99", "33% de desconto", "Suporte VIP", "Exportação Fiscal IRPF"]')
ON CONFLICT (code) DO NOTHING;

-- 2. TRANSAÇÕES PIX (BANCO CENTRAL BR CODE)
CREATE TABLE IF NOT EXISTS pix_transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    driver_id UUID NOT NULL REFERENCES drivers(id) ON DELETE CASCADE,
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

-- 3. RECIBOS E TOKENS GOOGLE PLAY BILLING
CREATE TABLE IF NOT EXISTS play_billing_receipts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    driver_id UUID NOT NULL REFERENCES drivers(id) ON DELETE CASCADE,
    order_id VARCHAR(100) NOT NULL,
    sku_id VARCHAR(100) NOT NULL,
    purchase_token TEXT NOT NULL,
    purchase_state VARCHAR(30) NOT NULL, -- PURCHASED, CANCELED, PENDING
    acknowledged BOOLEAN DEFAULT FALSE NOT NULL,
    purchase_time_ms BIGINT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL
);

-- 4. SOLICITAÇÕES LGPD E DIREITO AO ESQUECIMENTO (LEI 13.709/2018 - ART. 18)
CREATE TABLE IF NOT EXISTS lgpd_deletion_requests (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    driver_id UUID NOT NULL,
    user_email_hash VARCHAR(64) NOT NULL,
    reason TEXT,
    status VARCHAR(30) DEFAULT 'COMPLETED' NOT NULL,
    audit_tombstone_hash VARCHAR(64) NOT NULL,
    requested_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL,
    anonymized_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL
);

-- 5. SNAPSHOTS DE MÉTRICAS ADMINISTRATIVAS (MRR, CHURN, USUÁRIOS)
CREATE TABLE IF NOT EXISTS admin_metrics_snapshots (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    snapshot_date DATE NOT NULL,
    total_drivers_registered INT NOT NULL,
    active_pro_subscribers INT NOT NULL,
    mrr_cents BIGINT NOT NULL,
    total_rides_evaluated_today INT NOT NULL,
    churn_rate_percent NUMERIC(5,2) DEFAULT 0.0 NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL
);

-- 6. TELEMETRIA SEGURA (PII SANITIZED)
CREATE TABLE IF NOT EXISTS telemetry_events (
    id BIGSERIAL PRIMARY KEY,
    event_name VARCHAR(100) NOT NULL,
    driver_id_hash VARCHAR(64),
    app_version VARCHAR(20) NOT NULL,
    properties JSONB NOT NULL DEFAULT '{}',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL
);

-- Índices de alta performance
CREATE INDEX IF NOT EXISTS idx_pix_order_id ON pix_transactions(order_id);
CREATE INDEX IF NOT EXISTS idx_pix_status ON pix_transactions(status);
CREATE INDEX IF NOT EXISTS idx_play_billing_token ON play_billing_receipts(purchase_token);
CREATE INDEX IF NOT EXISTS idx_telemetry_event_name ON telemetry_events(event_name);
CREATE INDEX IF NOT EXISTS idx_telemetry_created_at ON telemetry_events(created_at);
