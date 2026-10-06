# Modelagem de Banco de Dados: Local (Room v2) e Remoto (PostgreSQL)

## 1. Banco Local (Android Room v2) e Repositório Comercial

O banco local utiliza SQLite através do Android Room (`RotaIqDatabase`) e SharedPreferences encriptadas para gestão de credenciais e ciclo de vida de assinaturas.

### 1.1 Tabelas Implementadas no Banco Local Room

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

#### `fuel_records`
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

#### `maintenance_records`
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

#### `vehicle_expenses`
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

### 1.2 Repositório Comercial e LGPD Local (`CommercialRepository`)
- **Estado de Assinatura**: `sub_tier` (FREE, PRO_MONTHLY, PRO_ANNUAL), `sub_status` (ACTIVE, EXPIRED), `sub_expires`, `sub_gateway`, `sub_token`.
- **Consentimentos LGPD**: `lgpd_terms`, `lgpd_privacy`, `lgpd_telemetry`, `lgpd_benchmark`, timestamp e versão.
- **Contador Diário de Uso**: `eval_count_YYYY-MM-DD` com auto-expiração diária para imposição do limite gratuito (15 avaliações/dia).

---

## 2. Banco Remoto PostgreSQL (Migrações DDL Versionadas)

Os scripts SQL estão versionados no diretório `backend/migrations/`:

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
- **`ride_offers`**: Ofertas capturadas na íntegra com texto bruto.
- **`ride_evaluations`**: Notas, classificações e diagnósticos calculados pelo motor de decisão.
- **`ride_history`**: Registro de corridas aceitas/rejeitadas e dados consolidados.
- **`daily_sessions`**: Turnos de trabalho com horas online e quilômetros rodados.
- **`daily_financials` & `monthly_financials`**: Fechamento financeiro DRE consolidado.
- **`audit_logs`**: Trilha imutável de auditoria com IP, user-agent e payloads JSONB.
- **`notifications`**: Fila de alertas push e mensagens de sistema.
- **`recommendations`**: Sugestões preditivas geradas pelo motor de inteligência.
- **`model_predictions`**: Logs de predição do modelo de precificação e demanda.
- **`support_tickets`**: Atendimento e chamados de suporte dos motoristas.

### 2.3 Migração `003_commercial_subscriptions_and_admin.sql` (Fase 5)
- **`subscription_plans`**: Cadastro dos planos oficiais (Free, Pro Mensal a R$ 29,90, Pro Anual a R$ 239,90) com features em JSONB.
- **`pix_transactions`**: Transações e pedidos PIX vinculados a drivers com payload EMV BR Code, orderId, chave PIX e status.
- **`play_billing_receipts`**: Recibos do Google Play In-App Billing com tokens de compra e estado de validação (`acknowledged`).
- **`lgpd_deletion_requests`**: Registro formal de Direito ao Esquecimento (Art. 18, VI da Lei 13.709/2018) com tombstone criptográfico de auditoria.
- **`admin_metrics_snapshots`**: Histórico consolidado de KPIs executivos: motoristas cadastrados, assinantes Pro, MRR e taxa de churn.
- **`telemetry_events`**: Ingestão de telemetria assíncrona sanitizada com hash anônimo do motorista.
