# Arquitetura do ROTA IQ

## 1. Visão Geral Arquitetural

O **ROTA IQ** segue os princípios de **Clean Architecture**, **Modularidade Orientada a Domínio** e **Offline-First**.

```
┌─────────────────────────────────────────────────────────────┐
│                       UI / PRESENTATION                     │
│  Jetpack Compose • Material 3 • Navigation • Cockpit Theme  │
│  [Dashboard] [CopilotHub] [GeoInsights] [SubscriptionPaywall]
│  [PrivacySettings] [AdminMetrics] [FinanceHub] [Simulator]  │
└──────────────────────────────┬──────────────────────────────┘
                               │ StateFlow / Actions
┌──────────────────────────────▼──────────────────────────────┐
│                         VIEWMODELS                          │
│  SubscriptionViewModel • PrivacyViewModel • AdminMetricsVM  │
│  DashboardViewModel • AutomationViewModel • GeoInsightsVM   │
│  FinancialHubViewModel • SimulatorViewModel • VehicleVM     │
└──────────────────────────────┬──────────────────────────────┘
                               │
┌──────────────────────────────▼──────────────────────────────┐
│                    COMMERCIAL & SECURITY LAYER (Fase 5)     │
│  - FeatureGateManager (Controle de uso diário e planos)     │
│  - BillingManager (Google Play In-App Purchases)            │
│  - PixPaymentManager (Payload EMV BACEN e CRC-16 nativo)    │
│  - LgpdManager (Portabilidade JSON Art. 18, V e Expurgo VI) │
│  - PiiSanitizer (Mascaramento de CPF, e-mail, telefone)     │
│  - SecureStorage (AES-256 GCM / Android Keystore)           │
│  - TelemetryManager & FeatureFlagManager                    │
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
│  - CommercialRepository (Assinaturas, limites diários, LGPD)│
│  - RotaIqRepository (Interface & Implementação Local/Cache) │
│  - Room Database v2 (Entities, DAOs, TypeConverters)        │
│  - Camada de Sincronização: SyncManager, SyncModels         │
│  - (Back-end) PostgreSQL Remoto + REST API FastAPI          │
└─────────────────────────────────────────────────────────────┘
```

---

## 2. Estrutura de Diretórios da Base Android e Backend

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
 │    │    │    │    │    ├── model/         # CommercialModels, LgpdModels, Geographic, etc.
 │    │    │    │    │    ├── engine/        # FeatureGate, PixPayment, Billing, Deadhead, etc.
 │    │    │    │    │    └── parser/        # Parsers desacoplados (Uber, 99, Detector)
 │    │    │    │    ├── security/          # PiiSanitizer, LgpdManager, SecureStorage
 │    │    │    │    ├── telemetry/         # TelemetryManager, TelemetryEvent
 │    │    │    │    ├── featureflags/      # FeatureFlagManager
 │    │    │    │    ├── automation/        # Camada de Sistema & Automação ao Volante
 │    │    │    │    ├── data/
 │    │    │    │    │    ├── local/        # Room Database v2, DAOs, Entities
 │    │    │    │    │    └── repository/   # RotaIqRepository, CommercialRepository
 │    │    │    │    └── ui/theme/         # Tema Cockpit Dark e componentes
 │    │    │    ├── feature/
 │    │    │    │    ├── subscription/      # SubscriptionPaywallScreen & ViewModel
 │    │    │    │    ├── privacy/           # PrivacySettingsScreen & ViewModel
 │    │    │    │    ├── admin/             # AdminMetricsScreen & ViewModel
 │    │    │    │    ├── dashboard/         # Painel principal com Pro Banner
 │    │    │    │    ├── automation/        # Gestão do Copiloto & Automação
 │    │    │    │    ├── geographic/        # Zonas, Heatmap & Deadhead
 │    │    │    │    ├── rides/             # Simulador e Histórico
 │    │    │    │    ├── finance/           # Hub Financeiro Avançado
 │    │    │    │    ├── vehicle/           # Custos do veículo
 │    │    │    │    └── goals/             # Metas e produtividade
 │    │    │    ├── navigation/             # Rotas e NavGraph Compose
 │    │    │    ├── MainActivity.kt
 │    │    │    ├── RotaIqApplication.kt
 │    │    │    └── RotaIqViewModelFactory.kt
 │    └── test/                             # 73 Testes unitários automatizados (22 suítes)
backend/
 ├── app/
 │    └── main.py                           # REST API assíncrona (FastAPI / Uvicorn)
 ├── migrations/
 │    ├── 001_initial_schema.sql            # Esquema base PostgreSQL
 │    ├── 002_financial_and_sync.sql        # Esquema financeiro e sincronização
 │    └── 003_commercial_subscriptions_and_admin.sql # Esquema de monetização e LGPD
 ├── Dockerfile                             # Containerização do backend
 ├── docker-compose.yml                     # Orquestração com PostgreSQL
 └── requirements.txt                       # Dependências do backend
```

---

## 3. Modelo de Monetização e Feature Gating (Fase 5)

### 3.1 Planos de Assinatura
1. **Plano Gratuito (`FREE`)**:
   - 15 avaliações diárias de ofertas de corrida com reset diário automático;
   - Cálculo básico de custo do veículo e metas.
2. **Plano Pro Mensal (`PRO_MONTHLY`) - R$ 29,90/mês**:
   - Avaliações ilimitadas sem interrupções;
   - HUD flutuante dinâmico sobre Uber e 99;
   - Copiloto vocal sintetizado (TTS);
   - Preditor de retorno vazio (Deadhead) e desconto de custo de volta;
   - Comparativo avançado Uber vs 99.
3. **Plano Pro Anual (`PRO_ANNUAL`) - R$ 239,90/ano (R$ 19,99/mês)**:
   - 33% de desconto (economia de R$ 118,90 / 4 meses grátis);
   - Todos os recursos do Pro Mensal;
   - Relatórios fiscais consolidados para IRPF;
   - Atendimento prioritário VIP.

### 3.2 Gateways de Pagamento Integrados
- **Google Play In-App Billing**: Subscriptions oficiais via Play Store API gerenciadas pelo `BillingManager`.
- **PIX Instantâneo Oficial**: Implementação nativa com especificação EMV do Banco Central do Brasil gerando QR Code e código Copia e Cola com cálculo de checksum CRC-16/CCITT.
