-- ====================================================================
-- ROTA IQ - MIGRATION 002: FINANCEIRO, CORRIDAS, SINCRONIZAÇÃO E AUDITORIA
-- ====================================================================

-- 1. REGISTROS DE COMBUSTÍVEL
CREATE TABLE fuel_records (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    vehicle_id UUID NOT NULL REFERENCES vehicles(id) ON DELETE CASCADE,
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

-- 2. REGISTROS DE MANUTENÇÃO
CREATE TABLE maintenance_records (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    vehicle_id UUID NOT NULL REFERENCES vehicles(id) ON DELETE CASCADE,
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

-- 3. SEGUROS, TAXAS E OUTRAS DESPESAS
CREATE TABLE insurance_records (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    vehicle_id UUID NOT NULL REFERENCES vehicles(id) ON DELETE CASCADE,
    insurer_name VARCHAR(100) NOT NULL,
    monthly_cost NUMERIC(10,2) NOT NULL,
    policy_expires_at DATE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL
);

CREATE TABLE tax_records (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    vehicle_id UUID NOT NULL REFERENCES vehicles(id) ON DELETE CASCADE,
    tax_type VARCHAR(50) NOT NULL, -- IPVA, LICENCIAMENTO, DPVAT
    amount NUMERIC(10,2) NOT NULL,
    due_date DATE NOT NULL,
    is_paid BOOLEAN DEFAULT FALSE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL
);

CREATE TABLE vehicle_expenses (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    vehicle_id UUID NOT NULL REFERENCES vehicles(id) ON DELETE CASCADE,
    date DATE NOT NULL,
    category VARCHAR(50) NOT NULL,
    description TEXT NOT NULL,
    amount NUMERIC(10,2) NOT NULL,
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL
);

-- 4. OFERTAS, AVALIAÇÕES E HISTÓRICO DE CORRIDAS
CREATE TABLE ride_offers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    driver_id UUID NOT NULL REFERENCES drivers(id) ON DELETE CASCADE,
    platform_id UUID REFERENCES ride_platforms(id),
    gross_fare NUMERIC(10,2) NOT NULL,
    distance_km NUMERIC(8,2) NOT NULL,
    duration_minutes NUMERIC(8,2) NOT NULL,
    pickup_distance_km NUMERIC(8,2) DEFAULT 0.0 NOT NULL,
    pickup_duration_minutes NUMERIC(8,2) DEFAULT 0.0 NOT NULL,
    stops_count INT DEFAULT 0 NOT NULL,
    category VARCHAR(50) NOT NULL,
    raw_text TEXT,
    offered_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL
);

CREATE TABLE ride_evaluations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    offer_id UUID UNIQUE NOT NULL REFERENCES ride_offers(id) ON DELETE CASCADE,
    score INT NOT NULL,
    classification VARCHAR(30) NOT NULL,
    estimated_cost NUMERIC(10,2) NOT NULL,
    net_profit NUMERIC(10,2) NOT NULL,
    profit_margin_percent NUMERIC(5,2) NOT NULL,
    gross_rate_per_km NUMERIC(6,2) NOT NULL,
    net_rate_per_km NUMERIC(6,2) NOT NULL,
    gross_rate_per_hour NUMERIC(6,2) NOT NULL,
    net_rate_per_hour NUMERIC(6,2) NOT NULL,
    reasons JSONB DEFAULT '[]'::JSONB NOT NULL,
    alerts JSONB DEFAULT '[]'::JSONB NOT NULL,
    evaluated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE ride_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    driver_id UUID NOT NULL REFERENCES drivers(id) ON DELETE CASCADE,
    evaluation_id UUID REFERENCES ride_evaluations(id),
    status VARCHAR(30) NOT NULL, -- ACCEPTED, REJECTED, CANCELLED, IGNORED
    actual_fare NUMERIC(10,2),
    actual_distance_km NUMERIC(8,2),
    started_at TIMESTAMP WITH TIME ZONE,
    finished_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL
);

-- 5. SESSÕES E FECHAMENTOS FINANCEIROS
CREATE TABLE daily_sessions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    driver_id UUID NOT NULL REFERENCES drivers(id) ON DELETE CASCADE,
    date DATE NOT NULL,
    start_time TIMESTAMP WITH TIME ZONE NOT NULL,
    end_time TIMESTAMP WITH TIME ZONE,
    hours_online NUMERIC(5,2) DEFAULT 0.0 NOT NULL,
    hours_driving NUMERIC(5,2) DEFAULT 0.0 NOT NULL,
    total_km NUMERIC(8,2) DEFAULT 0.0 NOT NULL,
    rides_count INT DEFAULT 0 NOT NULL,
    gross_revenue NUMERIC(10,2) DEFAULT 0.0 NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL
);

CREATE TABLE daily_financials (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    driver_id UUID NOT NULL REFERENCES drivers(id) ON DELETE CASCADE,
    date DATE NOT NULL,
    gross_revenue NUMERIC(10,2) DEFAULT 0.0 NOT NULL,
    fuel_costs NUMERIC(10,2) DEFAULT 0.0 NOT NULL,
    maintenance_costs NUMERIC(10,2) DEFAULT 0.0 NOT NULL,
    fixed_costs NUMERIC(10,2) DEFAULT 0.0 NOT NULL,
    other_expenses NUMERIC(10,2) DEFAULT 0.0 NOT NULL,
    net_profit NUMERIC(10,2) DEFAULT 0.0 NOT NULL,
    total_km NUMERIC(8,2) DEFAULT 0.0 NOT NULL,
    hours_online NUMERIC(5,2) DEFAULT 0.0 NOT NULL,
    UNIQUE(driver_id, date)
);

CREATE TABLE monthly_financials (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    driver_id UUID NOT NULL REFERENCES drivers(id) ON DELETE CASCADE,
    year_month VARCHAR(7) NOT NULL, -- YYYY-MM
    gross_revenue NUMERIC(12,2) DEFAULT 0.0 NOT NULL,
    total_costs NUMERIC(12,2) DEFAULT 0.0 NOT NULL,
    net_profit NUMERIC(12,2) DEFAULT 0.0 NOT NULL,
    profit_margin_percent NUMERIC(5,2) DEFAULT 0.0 NOT NULL,
    total_km NUMERIC(10,2) DEFAULT 0.0 NOT NULL,
    hours_online NUMERIC(6,2) DEFAULT 0.0 NOT NULL,
    UNIQUE(driver_id, year_month)
);

-- 6. MONETIZAÇÃO, PLANOS E ASSINATURAS
CREATE TABLE plans (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL,
    tier VARCHAR(50) UNIQUE NOT NULL, -- FREE, PRO_MONTHLY, PRO_ANNUAL
    price_cents INT NOT NULL,
    billing_interval VARCHAR(20) NOT NULL, -- MONTH, YEAR
    is_active BOOLEAN DEFAULT TRUE NOT NULL
);

CREATE TABLE subscriptions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    driver_id UUID NOT NULL REFERENCES drivers(id) ON DELETE CASCADE,
    plan_id UUID NOT NULL REFERENCES plans(id),
    status VARCHAR(30) NOT NULL, -- ACTIVE, PAST_DUE, CANCELLED, TRIAL
    gateway_id VARCHAR(100),
    current_period_start TIMESTAMP WITH TIME ZONE NOT NULL,
    current_period_end TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL
);

CREATE TABLE payments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    subscription_id UUID REFERENCES subscriptions(id),
    driver_id UUID NOT NULL REFERENCES drivers(id),
    amount_cents INT NOT NULL,
    currency VARCHAR(3) DEFAULT 'BRL' NOT NULL,
    status VARCHAR(30) NOT NULL, -- PAID, FAILED, REFUNDED
    gateway_reference VARCHAR(150),
    paid_at TIMESTAMP WITH TIME ZONE
);

-- 7. EVENTOS, AUDITORIA, TELEMETRIA E FEATURE FLAGS
CREATE TABLE audit_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id UUID,
    action VARCHAR(100) NOT NULL,
    ip_address INET,
    user_agent TEXT,
    payload JSONB,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL
);

CREATE TABLE feature_flags (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    key VARCHAR(100) UNIQUE NOT NULL,
    description TEXT,
    is_enabled BOOLEAN DEFAULT FALSE NOT NULL,
    rollout_percentage INT DEFAULT 100
);

CREATE TABLE notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    driver_id UUID NOT NULL REFERENCES drivers(id) ON DELETE CASCADE,
    title VARCHAR(150) NOT NULL,
    body TEXT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL
);

CREATE TABLE recommendations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    driver_id UUID NOT NULL REFERENCES drivers(id) ON DELETE CASCADE,
    title VARCHAR(150) NOT NULL,
    description TEXT NOT NULL,
    action_type VARCHAR(50),
    is_dismissed BOOLEAN DEFAULT FALSE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL
);

CREATE TABLE model_predictions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    zone_id UUID,
    day_of_week INT NOT NULL,
    hour_of_day INT NOT NULL,
    predicted_demand_score INT NOT NULL,
    predicted_hourly_rate NUMERIC(6,2) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL
);

CREATE TABLE support_tickets (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id),
    subject VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    status VARCHAR(30) DEFAULT 'OPEN' NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL
);
