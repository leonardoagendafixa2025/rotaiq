# Arquitetura do ROTA IQ

## 1. Visão Geral Arquitetural

O **ROTA IQ** segue os princípios de **Clean Architecture**, **Modularidade Orientada a Domínio** e **Offline-First**.

```
┌─────────────────────────────────────────────────────────────┐
│                       UI / PRESENTATION                     │
│  Jetpack Compose • Material 3 • Navigation • Cockpit Theme  │
│  [Dashboard] [CopilotHub] [FinanceHub] [Simulator] [Vehicle]│
└──────────────────────────────┬──────────────────────────────┘
                               │ StateFlow / Actions
┌──────────────────────────────▼──────────────────────────────┐
│                         VIEWMODELS                          │
│  DashboardViewModel • AutomationViewModel • FinancialViewModel
│  SimulatorViewModel • VehicleViewModel • GoalsViewModel     │
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
│  - RideEvaluationEngine                                     │
│  - AdvancedFinancialEngine & FinancialEngine                │
│  - FuelEngine (Consumo real bomba método 2 tanques cheios)  │
│  - MaintenanceSchedulerEngine (Alertas preventivos odômetro)│
│  - MultiPeriodGoalEngine & GoalEngine                       │
│  - VehicleCostEngine                                        │
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
 │    │    │    │    │    ├── model/         # Modelos de domínio (Vehicle, RideOffer, etc.)
 │    │    │    │    │    ├── engine/        # Motores matemáticos de decisão e finanças
 │    │    │    │    │    └── parser/        # Parsers desacoplados (Uber, 99, Detector)
 │    │    │    │    ├── automation/        # Camada de Sistema & Automação ao Volante (Fase 3)
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
 │    │    │    │    ├── automation/            # Gestão do Copiloto & Automação (Fase 3)
 │    │    │    │    ├── rides/                 # Simulador e Histórico
 │    │    │    │    ├── finance/               # Hub Financeiro Avançado (Fase 2)
 │    │    │    │    ├── vehicle/               # Custos do veículo
 │    │    │    │    └── goals/                 # Metas e produtividade
 │    │    │    ├── navigation/                 # Rotas e NavGraph Compose
 │    │    │    ├── MainActivity.kt
 │    │    │    ├── RotaIqApplication.kt
 │    │    │    └── RotaIqViewModelFactory.kt
 │    └── test/                                # 43 Testes unitários automatizados
backend/
 └── migrations/
      ├── 001_initial_schema.sql               # Esquema base PostgreSQL
      └── 002_financial_and_sync.sql           # Esquema financeiro e sincronização
```

---

## 3. Fluxo de Execução do Copiloto ao Volante (Fase 3)

1. **Captura do Evento**: O app Uber ou 99 emite um novo cartão de corrida na tela.
2. **Interceptação Acessível**: O `RotaIqAccessibilityService` recebe o evento `TYPE_WINDOW_CONTENT_CHANGED`.
3. **Extração & Debounce**: O `AccessibilityNodeExtractor` recupera os nós visíveis; caso o hash do texto tenha sido avaliado há menos de 2 segundos, descarta para evitar retrabalho.
4. **Detecção da Plataforma**: O `PlatformDetector` roteia o texto para o parser correspondente (`UberParser` ou `NinetyNineParser`) e gera o `RideOffer`.
5. **Avaliação Determinística**: O `RideEvaluationEngine` combina o perfil do carro (`Vehicle`), metas (`DriverGoal`) e preferências (`DriverPreference`) do motorista, gerando o `RideEvaluation` em menos de 2 milissegundos.
6. **Projeção Visual no HUD Flutuante**: O `OverlayManager` projeta o `FloatingHudView` com cor semântica do score e dados resumidos ("Sobra: R$ 27,00 | R$ 81/h").
7. **Alerta de Voz TTS**: O `VoiceAlertManager` vocaliza a frase concisa em português brasileiro.
8. **Persistência Histórica**: A avaliação é persistida de forma reativa no Room Database local.
