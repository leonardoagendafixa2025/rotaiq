-- ====================================================================
-- ROTA IQ - MIGRATION 005: DISPOSITIVOS (FCM), CAMPANHAS E PUSH NOTIFICATIONS
-- ====================================================================

-- 1. TABELA DE TOKENS DE DISPOSITIVOS ANDROID (FCM)
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

-- 2. TABELA DE CAMPANHAS DE NOTIFICAÇÃO PUSH
CREATE TABLE IF NOT EXISTS campaigns (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(200) NOT NULL,
    type VARCHAR(50) DEFAULT 'MARKETING' NOT NULL, -- MARKETING, INFORMATIVA, ATUALIZACAO, PROMOCAO, ENGAJAMENTO, SISTEMA
    title VARCHAR(200) NOT NULL,
    body TEXT NOT NULL,
    image_url TEXT,
    deep_link VARCHAR(255) DEFAULT 'rotaiq://home',
    status VARCHAR(50) DEFAULT 'DRAFT' NOT NULL, -- DRAFT, SCHEDULED, PROCESSING, SENT, PARTIALLY_SENT, FAILED, CANCELLED
    audience_type VARCHAR(50) DEFAULT 'ALL' NOT NULL, -- ALL, FREE, PRO, ACTIVE, INACTIVE, CITY, STATE, CUSTOM, SPECIFIC
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

-- 3. TABELA DE ENTREGAS INDIVIDUAIS (LOGS DE DISPARO FCM)
CREATE TABLE IF NOT EXISTS campaign_deliveries (
    id BIGSERIAL PRIMARY KEY,
    campaign_id UUID REFERENCES campaigns(id) ON DELETE CASCADE NOT NULL,
    user_id UUID,
    fcm_token TEXT NOT NULL,
    status VARCHAR(50) NOT NULL, -- SENT, FAILED, OPENED
    fcm_message_id VARCHAR(255),
    error_code VARCHAR(100),
    sent_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL,
    opened_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_campaign_deliveries_camp ON campaign_deliveries(campaign_id);
CREATE INDEX IF NOT EXISTS idx_campaign_deliveries_user ON campaign_deliveries(user_id);
CREATE INDEX IF NOT EXISTS idx_campaign_deliveries_status ON campaign_deliveries(status);

-- 4. TABELA DE TEMPLATES REUTILIZÁVEIS
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

-- Seeds de Templates Padrão
INSERT INTO campaign_templates (name, type, title, body, deep_link, created_by)
VALUES 
    ('Atualização Disponível', 'ATUALIZACAO', '🚀 Nova versão do ROTA IQ!', 'Atualizamos o copiloto com novas métricas de lucro e suporte aprimorado. Toque para atualizar.', 'rotaiq://home', 'system'),
    ('Oferta Especial Pro', 'PROMOCAO', '💎 30% OFF no ROTA IQ Pro!', 'Desbloqueie avaliações ilimitadas e HUD flutuante com desconto especial para você.', 'rotaiq://subscription', 'system'),
    ('Alerta de Alta Demanda', 'ENGAJAMENTO', '🔥 Chuva de Corridas na sua Região!', 'A demanda está alta agora na sua praça. Abra o app e ative o filtro inteligente de corridas.', 'rotaiq://rides', 'system')
ON CONFLICT DO NOTHING;
