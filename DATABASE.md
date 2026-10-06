# Modelagem de Banco de Dados: Local (Room v2) e Remoto (PostgreSQL)

## 1. Banco Local (Android Room v2)

O banco local utiliza SQLite através do Android Room (`RotaIqDatabase`). Na Fase 2, o esquema foi expandido da versão 1 para a **versão 2** com suporte a controle granular de abastecimentos, manutenções e despesas adicionais.

### 1.1 Tabelas Implementadas no Banco Local

#### `vehicles`
- `id` (VARCHAR PK)
- `name`, `model`, `plate`, `year`
- `fuelType` (GASOLINE, ETHANOL, CNG, DIESEL, ELECTRIC, HYBRID)
- `consumptionKmPerLiter` (DOUBLE)
- `fuelPricePerLiter` (DOUBLE)
- `maintenanceCostPerKm` (DOUBLE)
- `monthlyInsuranceCost`, `annualTaxesCost`, `monthlyDepreciation`, `monthlyOtherCosts`
- `estimatedMonthlyKm` (DOUBLE)
- `isActive` (BOOLEAN)

#### `fuel_records` (Fase 2)
- `id` (VARCHAR PK)
- `vehicleId` (VARCHAR FK)
- `date` (VARCHAR - YYYY-MM-DD)
- `odometerKm` (DOUBLE) - Odômetro no momento do abastecimento
- `liters` (DOUBLE) - Quantidade abastecida
- `pricePerLiter` (DOUBLE) - Valor pago por litro
- `totalPaid` (DOUBLE) - Total pago na bomba
- `fuelType` (VARCHAR) - Tipo de combustível utilizado
- `isFullTank` (BOOLEAN) - Se encheu o tanque (necessário para o método 2 tanques)
- `calculatedKmPerLiter` (DOUBLE NULL) - Consumo apurado entre tanques cheios
- `calculatedCostPerKm` (DOUBLE NULL) - Custo real apurado por km rodado
- `notes` (TEXT NULL)
- `syncedWithServer` (BOOLEAN)
- `createdAt` (BIGINT)

#### `maintenance_records` (Fase 2)
- `id` (VARCHAR PK)
- `vehicleId` (VARCHAR FK)
- `date` (VARCHAR - YYYY-MM-DD)
- `odometerKm` (DOUBLE) - Odômetro na realização do serviço
- `type` (VARCHAR) - OIL_CHANGE, TIRES, BRAKES, SUSPENSION, REVISION, OTHER
- `description` (TEXT)
- `cost` (DOUBLE)
- `nextServiceKm` (DOUBLE NULL) - Odômetro previsto para a próxima revisão
- `isCompleted` (BOOLEAN)
- `notes` (TEXT NULL)
- `syncedWithServer` (BOOLEAN)
- `createdAt` (BIGINT)

#### `vehicle_expenses` (Fase 2)
- `id` (VARCHAR PK)
- `vehicleId` (VARCHAR FK)
- `date` (VARCHAR - YYYY-MM-DD)
- `category` (VARCHAR) - FUEL, MAINTENANCE, INSURANCE, TAXES, FINANCING, PARKING, TOLL, CAR_WASH, OTHER
- `description` (TEXT)
- `amount` (DOUBLE)
- `notes` (TEXT NULL)
- `syncedWithServer` (BOOLEAN)
- `createdAt` (BIGINT)

#### `driver_goals`
- `id` (VARCHAR PK)
- `dailyGrossTarget`, `dailyNetTarget`
- `targetHourlyRate`, `targetKmRate`
- `shiftTargetHours`, `hoursWorkedToday`, `kmDrivenToday`
- `currentDailyGross`, `currentDailyNet`

#### `driver_preferences`
- `id` (VARCHAR PK)
- `minRatePerKm`, `minRatePerHour`
- `maxPickupDistanceKm`, `maxStops`
- `preferShortTrips`, `audioAlertsEnabled`, `overlayHudEnabled`

#### `ride_evaluations`
- `id` (VARCHAR PK)
- `platform` (UBER, NINETY_NINE, INDRAVE, OTHER)
- `grossFare`, `distanceKm`, `durationMinutes`
- `pickupDistanceKm`, `pickupDurationMinutes`, `stopsCount`, `category`
- `score` (INTEGER 0..100)
- `classification` (EXCELLENT, GOOD, ACCEPTABLE, BAD, AVOID)
- `estimatedCost`, `netProfit`, `profitMarginPercent`
- `grossRatePerKm`, `netRatePerKm`, `grossRatePerHour`, `netRatePerHour`, `grossRatePerMinute`
- `totalDistanceKm`, `totalDurationMinutes`
- `reasons` (TEXT), `alerts` (TEXT)
- `evaluatedAt` (BIGINT), `rawText` (TEXT NULL)

#### `daily_financials`
- `date` (VARCHAR PK - YYYY-MM-DD)
- `totalGrossRevenue`, `totalEstimatedCosts`, `totalNetProfit`
- `totalRidesEvaluated`, `totalRidesAccepted`, `totalRidesRejected`
- `totalKmDriven`, `totalHoursOnline`
- `avgGrossRatePerKm`, `avgGrossRatePerHour`, `avgNetProfitPerHour`

---

## 2. Banco Remoto PostgreSQL (Fase 2 - Migrações DDL)

Criados scripts SQL completos e versionados no diretório `backend/migrations/`:

### 2.1 Migração `001_initial_schema.sql`
- **`users`**: Autenticação, dados cadastrais, telefone, status de conta.
- **`drivers`**: Cidade, estado, perfil operacional, rating e status de ativação.
- **`vehicles`**: Cadastro detalhado da frota com placa, combustível e ano.
- **`vehicle_costs`**: Parâmetros de custos fixos e variáveis configurados pelo motorista.
- **`driver_preferences`**: Parâmetros de corte operacional (R$/km mín, R$/h mín, deadhead máx).
- **`driver_goals`**: Metas diárias, semanais e mensais de faturamento e horas.
- **`driver_zones`**: Mapeamento de regiões seguras e zonas a serem evitadas (`avoid`).
- **`ride_platforms`**: Cadastro das plataformas integradas (Uber, 99, inDrive).

### 2.2 Migração `002_financial_and_sync.sql`
- **`fuel_records`**: Histórico detalhado de abastecimentos na bomba com cálculo de consumo.
- **`maintenance_records`**: Controle de manutenções preventivas com odômetro de próxima revisão.
- **`vehicle_expenses`**: Despesas operacionais avulsas categorizadas.
- **`ride_offers`**: Ofertas capturadas na íntegra com texto bruto OCR/Accessibility.
- **`ride_evaluations`**: Notas, classificações e diagnósticos calculados pelo motor de decisão.
- **`ride_history`**: Registro de corridas aceitas/rejeitadas e dados consolidados.
- **`daily_sessions`**: Turnos de trabalho com horas online e quilômetros rodados.
- **`daily_financials` & `monthly_financials`**: Fechamento financeiro DRE consolidado.
- **`plans` & `subscriptions`**: Monetização, tiers (Free, Pro Mensal, Pro Anual) e expiração.
- **`payments`**: Transações financeiras via PIX e cartão com status e gateways.
- **`audit_logs`**: Trilha imutável de auditoria com IP, user-agent e payloads JSONB.
- **`feature_flags`**: Liberação gradual de recursos por usuário ou região.
- **`notifications`**: Fila de alertas push e mensagens de sistema.
- **`recommendations`**: Sugestões preditivas geradas pelo motor de inteligência.
- **`model_predictions`**: Logs de predição do modelo de precificação e demanda.
- **`support_tickets`**: Atendimento e chamados de suporte dos motoristas.
