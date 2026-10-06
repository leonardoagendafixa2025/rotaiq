-- ====================================================================
-- ROTA IQ - MIGRATION 001: ESQUEMA INICIAL COMPLETO
-- PostgreSQL 15+ / PostGIS compatível
-- ====================================================================

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- 1. USUÁRIOS
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    phone VARCHAR(30) UNIQUE,
    full_name VARCHAR(150) NOT NULL,
    is_active BOOLEAN DEFAULT TRUE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL
);

-- 2. MOTORISTAS
CREATE TABLE drivers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    cpf VARCHAR(14) UNIQUE,
    cnh_number VARCHAR(20),
    city VARCHAR(100) NOT NULL,
    state VARCHAR(2) NOT NULL,
    status VARCHAR(30) DEFAULT 'ACTIVE' NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL
);

-- 3. VEÍCULOS
CREATE TABLE vehicles (
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

-- 4. CUSTOS DE VEÍCULO
CREATE TABLE vehicle_costs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    vehicle_id UUID NOT NULL REFERENCES vehicles(id) ON DELETE CASCADE,
    monthly_insurance_cost NUMERIC(10,2) DEFAULT 0.0,
    annual_taxes_cost NUMERIC(10,2) DEFAULT 0.0,
    monthly_depreciation NUMERIC(10,2) DEFAULT 0.0,
    monthly_other_costs NUMERIC(10,2) DEFAULT 0.0,
    estimated_monthly_km NUMERIC(8,2) DEFAULT 3000.0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL
);

-- 5. PREFERÊNCIAS E METAS DO MOTORISTA
CREATE TABLE driver_preferences (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    driver_id UUID UNIQUE NOT NULL REFERENCES drivers(id) ON DELETE CASCADE,
    min_rate_per_km NUMERIC(6,2) DEFAULT 2.20 NOT NULL,
    min_rate_per_hour NUMERIC(6,2) DEFAULT 40.00 NOT NULL,
    max_pickup_distance_km NUMERIC(6,2) DEFAULT 3.50 NOT NULL,
    max_stops INT DEFAULT 1 NOT NULL,
    prefer_short_trips BOOLEAN DEFAULT FALSE NOT NULL,
    audio_alerts_enabled BOOLEAN DEFAULT TRUE NOT NULL,
    overlay_hud_enabled BOOLEAN DEFAULT TRUE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL
);

CREATE TABLE driver_goals (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    driver_id UUID UNIQUE NOT NULL REFERENCES drivers(id) ON DELETE CASCADE,
    daily_gross_target NUMERIC(10,2) DEFAULT 300.00 NOT NULL,
    daily_net_target NUMERIC(10,2) DEFAULT 220.00 NOT NULL,
    weekly_gross_target NUMERIC(10,2) DEFAULT 1800.00 NOT NULL,
    monthly_gross_target NUMERIC(10,2) DEFAULT 7500.00 NOT NULL,
    target_hourly_rate NUMERIC(6,2) DEFAULT 45.00 NOT NULL,
    target_km_rate NUMERIC(6,2) DEFAULT 2.40 NOT NULL,
    shift_target_hours NUMERIC(4,1) DEFAULT 8.0 NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL
);

-- 6. ZONAS GEOGRÁFICAS
CREATE TABLE driver_zones (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    driver_id UUID NOT NULL REFERENCES drivers(id) ON DELETE CASCADE,
    name VARCHAR(150) NOT NULL,
    profitability_multiplier NUMERIC(4,2) DEFAULT 1.0 NOT NULL,
    is_avoid_zone BOOLEAN DEFAULT FALSE NOT NULL,
    is_favorite_zone BOOLEAN DEFAULT FALSE NOT NULL,
    latitude NUMERIC(10,7),
    longitude NUMERIC(10,7),
    radius_meters INT DEFAULT 2000,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW() NOT NULL
);

-- 7. PLATAFORMAS DE TRANSPORTE
CREATE TABLE ride_platforms (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(50) UNIQUE NOT NULL,
    package_name VARCHAR(150),
    is_supported BOOLEAN DEFAULT TRUE NOT NULL
);

INSERT INTO ride_platforms (name, package_name) VALUES
('Uber', 'com.ubercab.driver'),
('99', 'com.taxis99'),
('inDrive', 'sinet.bm.driver');
