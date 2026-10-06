# Modelagem de Banco de Dados: Local (Room) e Remoto (PostgreSQL)

## 1. Banco Local (Android Room)

O banco local utiliza SQLite através do Android Room (`RotaIqDatabase`).

### 1.1 Tabelas Implementadas na Fase 1

- **`vehicles`**:
  * `id` (VARCHAR PK)
  * `name`, `model`, `plate`, `year`
  * `fuelType` (GASOLINE, ETHANOL, CNG, DIESEL, ELECTRIC, HYBRID)
  * `consumptionKmPerLiter` (DOUBLE)
  * `fuelPricePerLiter` (DOUBLE)
  * `maintenanceCostPerKm` (DOUBLE)
  * `monthlyInsuranceCost`, `annualTaxesCost`, `monthlyDepreciation`, `monthlyOtherCosts`
  * `estimatedMonthlyKm` (DOUBLE)
  * `isActive` (BOOLEAN)

- **`driver_goals`**:
  * `id` (VARCHAR PK)
  * `dailyGrossTarget`, `dailyNetTarget`
  * `targetHourlyRate`, `targetKmRate`
  * `shiftTargetHours`, `hoursWorkedToday`, `kmDrivenToday`
  * `currentDailyGross`, `currentDailyNet`

- **`driver_preferences`**:
  * `id` (VARCHAR PK)
  * `minRatePerKm`, `minRatePerHour`
  * `maxPickupDistanceKm`, `maxStops`
  * `preferShortTrips`, `audioAlertsEnabled`, `overlayHudEnabled`

- **`ride_evaluations`**:
  * `id` (VARCHAR PK)
  * `platform` (UBER, NINETY_NINE, INDRAVE, OTHER)
  * `grossFare`, `distanceKm`, `durationMinutes`
  * `pickupDistanceKm`, `pickupDurationMinutes`, `stopsCount`, `category`
  * `score` (INTEGER 0..100)
  * `classification` (EXCELLENT, GOOD, ACCEPTABLE, BAD, AVOID)
  * `estimatedCost`, `netProfit`, `profitMarginPercent`
  * `grossRatePerKm`, `netRatePerKm`, `grossRatePerHour`, `netRatePerHour`, `grossRatePerMinute`
  * `totalDistanceKm`, `totalDurationMinutes`
  * `reasons` (TEXT / TYPE_CONVERTER), `alerts` (TEXT / TYPE_CONVERTER)
  * `evaluatedAt` (BIGINT), `rawText` (TEXT NULL)

- **`daily_financials`**:
  * `date` (VARCHAR PK - YYYY-MM-DD)
  * `totalGrossRevenue`, `totalEstimatedCosts`, `totalNetProfit`
  * `totalRidesEvaluated`, `totalRidesAccepted`, `totalRidesRejected`
  * `totalKmDriven`, `totalHoursOnline`
  * `avgGrossRatePerKm`, `avgGrossRatePerHour`, `avgNetProfitPerHour`

---

## 2. Banco Remoto Projetado (PostgreSQL)

Para as Fases 2 e 5, a arquitetura remota do backend contemplará o esquema relacional em PostgreSQL com suporte a multi-inquilino seguro e auditoria:

```sql
-- Usuários e Autenticação
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    phone VARCHAR(30) UNIQUE,
    full_name VARCHAR(150),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Motoristas
CREATE TABLE drivers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    city VARCHAR(100),
    state VARCHAR(2),
    status VARCHAR(50) DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Veículos e Estrutura de Custos
CREATE TABLE vehicles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    driver_id UUID REFERENCES drivers(id) ON DELETE CASCADE,
    name VARCHAR(100),
    plate VARCHAR(20),
    fuel_type VARCHAR(30),
    consumption_km_per_liter NUMERIC(5,2),
    fuel_price_per_liter NUMERIC(6,2),
    maintenance_cost_per_km NUMERIC(6,3),
    is_active BOOLEAN DEFAULT TRUE
);

-- Histórico de Avaliações Sincronizado
CREATE TABLE ride_evaluations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    driver_id UUID REFERENCES drivers(id) ON DELETE CASCADE,
    platform VARCHAR(50) NOT NULL,
    gross_fare NUMERIC(10,2) NOT NULL,
    distance_km NUMERIC(8,2) NOT NULL,
    duration_minutes NUMERIC(8,2) NOT NULL,
    score INTEGER NOT NULL,
    classification VARCHAR(30) NOT NULL,
    estimated_cost NUMERIC(10,2) NOT NULL,
    net_profit NUMERIC(10,2) NOT NULL,
    evaluated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Assinaturas e Planos
CREATE TABLE subscriptions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    driver_id UUID REFERENCES drivers(id) ON DELETE CASCADE,
    plan_tier VARCHAR(50) NOT NULL, -- FREE, PRO_MONTHLY, PRO_ANNUAL
    status VARCHAR(50) NOT NULL,   -- ACTIVE, PAST_DUE, CANCELLED
    expires_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Trilha de Auditoria
CREATE TABLE audit_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id UUID,
    action VARCHAR(100) NOT NULL,
    ip_address INET,
    user_agent TEXT,
    payload JSONB,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);
```
