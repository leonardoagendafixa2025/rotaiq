-- ====================================================================
-- ROTA IQ - MIGRATION 004: BACKOFFICE REAL, RBAC, AUDITORIA & CONFIGURAÇÕES
-- ====================================================================

-- 1. TABELA DE ADMINISTRADORES DO SISTEMA (RBAC)
CREATE TABLE IF NOT EXISTS admin_users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(150) NOT NULL,
    role VARCHAR(50) DEFAULT 'ADMIN' NOT NULL, -- SUPER_ADMIN, ADMIN, OPERADOR
    permissions JSONB DEFAULT '["VIEW_DASHBOARD", "MANAGE_DRIVERS", "MANAGE_PLANS", "MANAGE_SUBSCRIPTIONS", "MANAGE_FEATURE_FLAGS", "MANAGE_SETTINGS", "VIEW_AUDIT_LOGS"]' NOT NULL,
    is_active BOOLEAN DEFAULT TRUE NOT NULL,
    last_login_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL
);

-- Seed de Super Admin Padrão
INSERT INTO admin_users (email, password_hash, full_name, role, permissions, is_active)
VALUES (
    'admin@rotai.app',
    'pbkdf2_sha256$100000$5520fb47cd2233c2c6fdc1e9008270f5$cb44d3338b3a580581fb265f8e1b9447e78b2b01ab0fb4cfe3bf2349537ae26e',
    'Administrador Geral ROTA IQ',
    'SUPER_ADMIN',
    '["SUPER_ADMIN", "VIEW_DASHBOARD", "MANAGE_USERS", "MANAGE_DRIVERS", "MANAGE_PLANS", "MANAGE_SUBSCRIPTIONS", "MANAGE_PAYMENTS", "MANAGE_FEATURE_FLAGS", "MANAGE_SETTINGS", "VIEW_AUDIT_LOGS", "MANAGE_SUPPORT", "MANAGE_NOTIFICATIONS"]',
    TRUE
)
ON CONFLICT (email) DO NOTHING;

-- 2. ENRIQUECER TABELA DE MOTORISTAS PARA GESTÃO COMPLETA
ALTER TABLE drivers ADD COLUMN IF NOT EXISTS phone VARCHAR(30);
ALTER TABLE drivers ADD COLUMN IF NOT EXISTS plan_code VARCHAR(50) DEFAULT 'free';
ALTER TABLE drivers ADD COLUMN IF NOT EXISTS block_reason TEXT;
ALTER TABLE drivers ADD COLUMN IF NOT EXISTS blocked_at TIMESTAMP WITH TIME ZONE;
ALTER TABLE drivers ADD COLUMN IF NOT EXISTS blocked_by VARCHAR(255);
ALTER TABLE drivers ADD COLUMN IF NOT EXISTS last_active_at TIMESTAMP WITH TIME ZONE;

-- 3. ENRIQUECER PLANOS DE ASSINATURA (CRUD COMPLETO)
ALTER TABLE subscription_plans ADD COLUMN IF NOT EXISTS description TEXT;
ALTER TABLE subscription_plans ADD COLUMN IF NOT EXISTS trial_days INT DEFAULT 0;
ALTER TABLE subscription_plans ADD COLUMN IF NOT EXISTS currency VARCHAR(10) DEFAULT 'BRL';
ALTER TABLE subscription_plans ADD COLUMN IF NOT EXISTS evaluation_limit INT DEFAULT 15;
ALTER TABLE subscription_plans ADD COLUMN IF NOT EXISTS is_featured BOOLEAN DEFAULT FALSE;
ALTER TABLE subscription_plans ADD COLUMN IF NOT EXISTS display_order INT DEFAULT 0;
ALTER TABLE subscription_plans ADD COLUMN IF NOT EXISTS status VARCHAR(30) DEFAULT 'ACTIVE'; -- ACTIVE, INACTIVE, ARCHIVED

UPDATE subscription_plans SET 
    description = '15 avaliações diárias, ideal para começar e testar',
    evaluation_limit = 15,
    status = 'ACTIVE',
    display_order = 1
WHERE code = 'free';

UPDATE subscription_plans SET 
    description = 'Acesso ilimitado, copiloto de voz e cálculos avançados',
    evaluation_limit = -1,
    is_featured = TRUE,
    status = 'ACTIVE',
    display_order = 2
WHERE code = 'pro_monthly';

UPDATE subscription_plans SET 
    description = 'Economia máxima anual com 33% de desconto e suporte prioritário',
    evaluation_limit = -1,
    status = 'ACTIVE',
    display_order = 3
WHERE code = 'pro_annual';

-- 4. TABELA DE CONFIGURAÇÕES DO SISTEMA (SYSTEM SETTINGS)
CREATE TABLE IF NOT EXISTS system_settings (
    key VARCHAR(100) PRIMARY KEY,
    value TEXT NOT NULL,
    type VARCHAR(30) DEFAULT 'string' NOT NULL, -- string, number, boolean, json
    category VARCHAR(50) DEFAULT 'GERAL' NOT NULL, -- GERAL, FINANCEIRO, ASSINATURAS, LIMITES, SISTEMA
    description TEXT,
    environment VARCHAR(30) DEFAULT 'production' NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL,
    updated_by VARCHAR(255) DEFAULT 'system'
);

INSERT INTO system_settings (key, value, type, category, description) VALUES
    ('FREE_DAILY_EVALUATION_LIMIT', '15', 'number', 'LIMITES', 'Limite diário de avaliações para o plano gratuito'),
    ('PRO_MONTHLY_PRICE', '29.90', 'number', 'ASSINATURAS', 'Preço do plano mensal Pro em reais'),
    ('PRO_ANNUAL_PRICE', '239.90', 'number', 'ASSINATURAS', 'Preço do plano anual Pro em reais'),
    ('MAINTENANCE_MODE', 'false', 'boolean', 'SISTEMA', 'Ativa tela de manutenção geral no aplicativo'),
    ('DEFAULT_SCORE_THRESHOLD', '70', 'number', 'GERAL', 'Nota de corte padrão mínima para recomendação de corrida'),
    ('ENABLE_VOICE', 'true', 'boolean', 'SISTEMA', 'Permitir copiloto por áudio sintetizado TTS'),
    ('ENABLE_OVERLAY', 'true', 'boolean', 'SISTEMA', 'Permitir HUD flutuante sobre outros aplicativos'),
    ('SESSION_DURATION_HOURS', '24', 'number', 'SISTEMA', 'Duração máxima de sessão de token JWT em horas')
ON CONFLICT (key) DO NOTHING;

-- 5. TABELA DE LOGS DE AUDITORIA ADMINISTRATIVA
CREATE TABLE IF NOT EXISTS admin_audit_logs (
    id BIGSERIAL PRIMARY KEY,
    admin_id UUID,
    admin_email VARCHAR(255) NOT NULL,
    action VARCHAR(100) NOT NULL,
    resource VARCHAR(100) NOT NULL,
    resource_id VARCHAR(100),
    old_value JSONB,
    new_value JSONB,
    ip_address VARCHAR(50),
    user_agent TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL
);

-- Índices adicionais para consultas ultrarrápidas
CREATE INDEX IF NOT EXISTS idx_admin_audit_logs_action ON admin_audit_logs(action);
CREATE INDEX IF NOT EXISTS idx_admin_audit_logs_resource ON admin_audit_logs(resource, resource_id);
CREATE INDEX IF NOT EXISTS idx_admin_audit_logs_created_at ON admin_audit_logs(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_drivers_status ON drivers(status);
CREATE INDEX IF NOT EXISTS idx_drivers_plan ON drivers(plan_code);
