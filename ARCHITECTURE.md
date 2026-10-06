# Arquitetura do ROTA IQ

## 1. Visão Geral Arquitetural

O **ROTA IQ** segue os princípios de **Clean Architecture**, **Modularidade Orientada a Domínio** e **Offline-First**.

```
┌─────────────────────────────────────────────────────────────┐
│                       UI / PRESENTATION                     │
│  Jetpack Compose • Material 3 • Navigation • Cockpit Theme  │
│  [Dashboard] [RideSimulator] [FinanceHub] [Vehicle] [Goals] │
└──────────────────────────────┬──────────────────────────────┘
                               │ StateFlow / Actions
┌──────────────────────────────▼──────────────────────────────┐
│                         VIEWMODELS                          │
│  DashboardViewModel • SimulatorViewModel • FinancialViewModel
│  VehicleViewModel • GoalsViewModel                          │
└──────────────────────────────┬──────────────────────────────┘
                               │
┌──────────────────────────────▼──────────────────────────────┐
│                        DOMAIN LAYER                         │
│  Motores Determinísticos Puros (Sem dependências de Framework)│
│  - RideEvaluationEngine                                     │
│  - AdvancedFinancialEngine & FinancialEngine                │
│  - FuelEngine (Consumo real bomba método 2 tanques cheios)  │
│  - MaintenanceSchedulerEngine (Alertas preventivos odômetro)│
│  - MultiPeriodGoalEngine & GoalEngine                       │
│  - VehicleCostEngine                                        │
│  - OfferParser / Normalizer (Uber, 99, inDrive)             │
│  - Domain Models & Enums                                    │
└──────────────────────────────┬──────────────────────────────┘
                               │
┌──────────────────────────────▼──────────────────────────────┐
│                         DATA LAYER                          │
│  - RotaIqRepository (Interface & Implementação Local/Cache) │
│  - Room Database v2 (Entities, DAOs, TypeConverters)        │
│  - Camada de Sincronização: SyncManager, SyncModels         │
│  - (Back-end) PostgreSQL Remoto (25 tabelas / Migrações)    │
└─────────────────────────────────────────────────────────────┘
```

---

## 2. Estrutura de Diretórios da Base Android

```
app/
 ├── src/
 │    ├── main/
 │    │    ├── AndroidManifest.xml
 │    │    ├── java/com/rotai/iq/
 │    │    │    ├── core/
 │    │    │    │    ├── domain/
 │    │    │    │    │    ├── model/         # Modelos de domínio puros
 │    │    │    │    │    ├── engine/        # Motores matemáticos e de decisão
 │    │    │    │    │    └── parser/        # Parsers desacoplados (Uber, 99)
 │    │    │    │    ├── data/
 │    │    │    │    │    ├── local/
 │    │    │    │    │    │    ├── db/      # RotaIqDatabase (v2), TypeConverters
 │    │    │    │    │    │    ├── entity/  # Entidades Room (v2)
 │    │    │    │    │    │    └── dao/     # Interfaces DAO (Fuel, Maint, Expense)
 │    │    │    │    │    └── repository/  # Repositório e Mappers
 │    │    │    │    ├── network/           # SyncManager, SyncModels (Push/Pull)
 │    │    │    │    └── ui/
 │    │    │    │         ├── theme/            # Cores, Tipografia, Tema Cockpit Dark
 │    │    │    │         └── components/       # HUD, Badges, Cards, MetricWidgets
 │    │    │    ├── feature/
 │    │    │    │    ├── dashboard/             # Painel principal
 │    │    │    │    ├── rides/                 # Simulador e Histórico
 │    │    │    │    ├── finance/               # Hub Financeiro Avançado (Fase 2)
 │    │    │    │    ├── vehicle/               # Custos do veículo
 │    │    │    │    └── goals/                 # Metas e produtividade
 │    │    │    ├── navigation/                 # Rotas e NavGraph Compose
 │    │    │    ├── MainActivity.kt
 │    │    │    ├── RotaIqApplication.kt
 │    │    │    └── RotaIqViewModelFactory.kt
 │    └── test/                                # 37 Testes unitários automatizados
backend/
 └── migrations/
      ├── 001_initial_schema.sql               # Esquema base PostgreSQL
      └── 002_financial_and_sync.sql           # Esquema financeiro e sincronização
```

---

## 3. Os Motores de Negócio (Camada de Domínio)

### 3.1 `RideEvaluationEngine`
Motor determinístico multi-fatorial. Combina 6 dimensões de análise:
1. **Rentabilidade por Hora vs Meta Horária** (Peso 35%)
2. **Rentabilidade por Quilômetro vs Meta por KM** (Peso 25%)
3. **Deslocamento até o Passageiro (Deadhead)** (Peso 20%)
4. **Custo do Veículo e Margem Líquida Real** (Peso 15%)
5. **Paradas Intermediárias** (Penalidade cumulativa)
6. **Contexto Geográfico e Zonas** (Zonas de risco e sem retorno)

### 3.2 `FuelEngine`
Calcula o consumo real e custo por km na bomba através do método de 2 tanques cheios consecutivos:
- Diferença de odômetro dividido pelo volume do segundo abastecimento.
- Identificação de abastecimentos parciais.
- Média ponderada de múltiplos abastecimentos para calibrar os custos reais do veículo.

### 3.3 `MaintenanceSchedulerEngine`
Compara o odômetro atual do veículo com o odômetro programado para os serviços preventivos:
- `OK`: Quilometragem restante confortável (> 1.000 km).
- `UPCOMING`: Vencimento iminente (<= 1.000 km).
- `OVERDUE`: Manutenção vencida (odômetro excedido).

### 3.4 `AdvancedFinancialEngine`
Gera demonstrativo completo de resultado operacional (DRE) para períodos diário, semanal, mensal e anual:
- Faturamento Bruto total.
- Custos Variáveis (Combustível real, Manutenção preventiva).
- Custos Fixos proporcionais (Seguro, IPVA, Depreciação).
- Lucro Líquido Real e Margem Operacional (%).
- Taxas médias por KM e por Hora.

### 3.5 `MultiPeriodGoalEngine`
Acompanhamento simultâneo de metas diárias, semanais e mensais:
- Percentuais de conclusão e valores faltantes.
- Projeção de ritmo horário no turno ativo.
- Mensagens de coaching dinâmico ao motorista.

---

## 4. Princípio Offline-First e Sincronização

O motorista frequentemente opera em locais de sinal instável:
- Todos os motores executam de forma síncrona e determinística no dispositivo em menos de 5ms.
- Todo o armazenamento é local no Room Database v2.
- A sincronização com o banco remoto PostgreSQL segue o padrão **Outbox/Push-Ack**:
  - Alterações locais recebem `syncedWithServer = false`.
  - O `SyncManager` compila o payload `SyncPushPayload`.
  - O servidor processa e retorna `acknowledgedIds`.
  - O repositório atualiza os registros locais para `syncedWithServer = true`.
