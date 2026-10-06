# Arquitetura do ROTA IQ

## 1. Visão Geral Arquitetural

O **ROTA IQ** segue os princípios de **Clean Architecture**, **Modularidade Orientada a Domínio** e **Offline-First**.

```
┌─────────────────────────────────────────────────────────────┐
│                       UI / PRESENTATION                     │
│  Jetpack Compose • Material 3 • Navigation • Cockpit Theme  │
│  [Dashboard] [CopilotHub] [GeoInsights] [FinanceHub] ...    │
└──────────────────────────────┬──────────────────────────────┘
                               │ StateFlow / Actions
┌──────────────────────────────▼──────────────────────────────┐
│                         VIEWMODELS                          │
│  DashboardViewModel • AutomationViewModel • GeoInsightsVM   │
│  FinancialHubViewModel • SimulatorViewModel • VehicleVM     │
└──────────────────────────────┬──────────────────────────────┘
                               │
┌──────────────────────────────▼──────────────────────────────┐
│                    AUTOMATION & SYSTEM LAYER                │
│  - RotaIqAccessibilityService (Leitura de ofertas na tela)  │
│  - AccessibilityNodeExtractor (Parsing hierárquico de nós)  │
│  - OverlayManager & FloatingHudView (WindowManager Overlay) │
│  - OverlayService (Foreground Service Android 14)           │
│  - VoiceAlertManager & TtsMessageFormatter (Áudio TTS pt-BR)│
└──────────────────────────────┬──────────────────────────────┘
                               │
┌──────────────────────────────▼──────────────────────────────┐
│                        DOMAIN LAYER                         │
│  Motores Determinísticos Puros (Sem dependências de Framework)│
│  - DeadheadPredictorEngine (Risco preditivo de volta vazia) │
│  - GeoHeatmapEngine (Modelagem de zonas e janelas horárias) │
│  - PlatformComparisonEngine (Comparativo Uber vs 99)        │
│  - RideEvaluationEngine (Motor multi-fatorial de decisão)   │
│  - AdvancedFinancialEngine & FinancialEngine (DRE completo) │
│  - FuelEngine (Consumo real bomba método 2 tanques cheios)  │
│  - MaintenanceSchedulerEngine (Alertas preventivos odômetro)│
│  - MultiPeriodGoalEngine & GoalEngine (Metas e ritmo)       │
│  - VehicleCostEngine (Custo granular do carro por km e hora)│
│  - PlatformDetector & OfferParsers (Uber, 99, inDrive)      │
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
 │    │    ├── res/
 │    │    │    └── xml/accessibility_service_config.xml
 │    │    ├── java/com/rotai/iq/
 │    │    │    ├── core/
 │    │    │    │    ├── domain/
 │    │    │    │    │    ├── model/         # GeographicModels, Vehicle, RideOffer, etc.
 │    │    │    │    │    ├── engine/        # DeadheadPredictor, GeoHeatmap, PlatformComparison
 │    │    │    │    │    └── parser/        # Parsers desacoplados (Uber, 99, Detector)
 │    │    │    │    ├── automation/        # Camada de Sistema & Automação ao Volante
 │    │    │    │    │    ├── accessibility/ # RotaIqAccessibilityService, NodeExtractor
 │    │    │    │    │    ├── overlay/       # OverlayManager, FloatingHudView, OverlayService
 │    │    │    │    │    └── tts/           # VoiceAlertManager, TtsMessageFormatter
 │    │    │    │    ├── data/
 │    │    │    │    │    ├── local/
 │    │    │    │    │    │    ├── db/      # RotaIqDatabase (v2), TypeConverters
 │    │    │    │    │    │    ├── entity/  # Entidades Room (v2)
 │    │    │    │    │    │    └── dao/     # Interfaces DAO (Fuel, Maint, Expense, Ride)
 │    │    │    │    │    └── repository/  # Repositório e Mappers
 │    │    │    │    ├── network/           # SyncManager, SyncModels (Push/Pull)
 │    │    │    │    └── ui/
 │    │    │    │         ├── theme/            # Cores, Tipografia, Tema Cockpit Dark
 │    │    │    │         └── components/       # HUD, Badges, Cards, MetricWidgets
 │    │    │    ├── feature/
 │    │    │    │    ├── dashboard/             # Painel principal
 │    │    │    │    ├── automation/            # Gestão do Copiloto & Automação
 │    │    │    │    ├── geographic/            # Inteligência Geográfica, Zonas & Deadhead (Fase 4)
 │    │    │    │    ├── rides/                 # Simulador e Histórico
 │    │    │    │    ├── finance/               # Hub Financeiro Avançado
 │    │    │    │    ├── vehicle/               # Custos do veículo
 │    │    │    │    └── goals/                 # Metas e produtividade
 │    │    │    ├── navigation/                 # Rotas e NavGraph Compose
 │    │    │    ├── MainActivity.kt
 │    │    │    ├── RotaIqApplication.kt
 │    │    │    └── RotaIqViewModelFactory.kt
 │    └── test/                                # 52 Testes unitários automatizados
backend/
 └── migrations/
      ├── 001_initial_schema.sql               # Esquema base PostgreSQL
      └── 002_financial_and_sync.sql           # Esquema financeiro e sincronização
```

---

## 3. Os Novos Motores de Inteligência Preditiva (Fase 4)

### 3.1 `DeadheadPredictorEngine`
Avalia a probabilidade de retorno de passageiro no local de desembarque:
- Determina o risco de volta vazia: `1.0 - returnTripProbability`.
- Calcula os quilômetros vazios esperados até uma região com passageiros: `deadheadKmToCenter * risco`.
- Multiplica pela taxa real de custo/km do veículo (`vehicle.totalCostPerKm`) para encontrar o custo de combustível/manutenção do retorno.
- Abate esse custo do lucro líquido da corrida, gerando o **Lucro Líquido Real Ajustado**.
- Emite alerta de **Armadilha de Deadhead** se o retorno vazio consumir mais de 55% do lucro ou transformar a corrida em prejuízo.

### 3.2 `GeoHeatmapEngine`
Classifica a atratividade das zonas metropolitanas considerando 5 faixas horárias do dia (Pico Matutino, Entrepico, Pico Vespertino, Noite e Madrugada), calibrando a probabilidade de retorno e identificando áreas com tarifa dinâmica frequente ou restrições de segurança.

### 3.3 `PlatformComparisonEngine`
Agrega o histórico de avaliações e corridas, consolidando para cada aplicativo (Uber, 99, inDrive):
- Lucro Líquido Real Médio por Hora (R$/h).
- Taxa média por KM rodado (R$/km).
- Margem Líquida Percentual (%).
- Recomendação estratégica dinâmica indicando qual plataforma priorizar no turno atual.
