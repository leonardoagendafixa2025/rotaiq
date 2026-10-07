# Roadmap do Produto ROTA IQ

Este documento define o plano mestre de evolução do **ROTA IQ** da Fundação à Escala Comercial.

---

## 🟢 FASE 1: Fundação e Motores Principais (CONCLUÍDA ✅)
- [x] Estrutura e ambiente Android nativo com Kotlin, Jetpack Compose e Material 3.
- [x] Gradle Version Catalog e scripts de build CLI sem dependência do Android Studio.
- [x] Arquitetura Clean e Modular (Domain, Data, UI, Features).
- [x] Banco local SQLite com Room Database, DAOs e Repositório Reativo (Flow).
- [x] **RideEvaluationEngine**: Motor multi-fatorial com score de 0 a 100 e classificações (`EXCELENTE` a `EVITAR`).
- [x] **VehicleCostEngine**: Cálculo granular do custo real do veículo por km, hora, dia, mês e ano.
- [x] **GoalEngine**: Monitoramento dinâmico de ritmo diário e orientações de velocidade operacional.
- [x] **FinancialEngine**: Margens, receitas e taxas horárias.
- [x] **Arquitetura de Parsers**: `OfferNormalizer`, `UberParser`, `NinetyNineParser`, `PlatformDetector`.
- [x] Telas implementadas:
  * `DashboardScreen` (Painel com Cockpit Dark e status de operação)
  * `RideSimulatorScreen` (Simulador interativo de ofertas com HUD Card)
  * `VehicleScreen` (Configuração detalhada de custos de veículo)
  * `GoalsScreen` (Definição de metas e orientações em tempo real)
  * `RideHistoryScreen` (Histórico de ofertas analisadas)
- [x] Suíte inicial de 24 testes unitários automatizados.
- [x] Geração de APK de Debug (`app-debug.apk`).

---

## 🟢 FASE 2: Gestão Financeira Avançada e Sincronização (CONCLUÍDA ✅)
- [x] Registro granular de abastecimentos (`FuelRecord`) com cálculo de consumo real na bomba via método 2 tanques (`FuelEngine`).
- [x] Registro e alertas preventivos de manutenção por odômetro (`MaintenanceSchedulerEngine` com status `OK`, `UPCOMING`, `OVERDUE`).
- [x] Controle detalhado de despesas veiculares avulsas (`VehicleExpense`).
- [x] Demonstrativo financeiro consolidado multi-período (`AdvancedFinancialEngine`: Diário, Semanal, Mensal, Anual com DRE completo).
- [x] Sistema de Metas Multi-Período (`MultiPeriodGoalEngine`) com projeção de ritmo e velocidade necessária.
- [x] Repositório e banco Room atualizados para v2 com 3 novas entidades e DAOs.
- [x] **`FinancialHubScreen`**: Interface completa com 4 abas (Resumo DRE, Abastecimentos, Manutenções, Despesas).
- [x] **Migrações PostgreSQL Remoto**: 25 tabelas relacionais em `backend/migrations/` (001_initial e 002_financial).
- [x] **Sincronização Offline-First**: `SyncManager` e modelos de push/ack para sincronização com retries.
- [x] 13 novos testes unitários (total de 37 testes unitários).

---

## 🟢 FASE 3: Android Inteligente e Automação ao Volante (CONCLUÍDA ✅)
- [x] **`RotaIqAccessibilityService`**: Leitura e interceptação em tempo real na tela de ofertas da Uber e 99 (`AccessibilityNodeInfo`).
- [x] Filtro inteligente de eventos com debounce de 2 segundos e deduplicação de cartões por hash.
- [x] **HUD Flutuante Dinâmico (`FloatingHudView` & `OverlayManager`)**: Card sobreposto translúcido renderizado em menos de 100ms sobre outros apps via `WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY`.
- [x] Controles interativos no HUD: Score (0-100), Lucro Líquido Real ("Sobra Limpo"), R$/h, R$/km, arrastável por toque e auto-dismiss em 15 segundos.
- [x] **`OverlayService`**: Foreground Service com notificação contínua no Android 14.
- [x] **Copiloto por Voz (`VoiceAlertManager` & `TtsMessageFormatter`)**: Síntese vocal em português brasileiro (TTS) concisa para decisão hands-free.
- [x] **`AutomationHubScreen` & `AutomationViewModel`**: Tela de controle do Copiloto com monitoramento de permissões e laboratório prático de simulação.
- [x] Suíte de automação com 6 novos testes unitários (total de 43 testes unitários).

---

## 🟢 FASE 4: Inteligência Geográfica e Análise Preditiva (CONCLUÍDA ✅)
- [x] **`DeadheadPredictorEngine`**: Cálculo preditivo de volta vazia (`expectedEmptyReturnKm`), custo do combustível de retorno sem passageiro (`emptyReturnCost`) e lucro líquido ajustado (`adjustedNetProfit`).
- [x] Detecção inteligente de **Armadilhas de Deadhead** (`isDeadheadTrap`) em corridas de alto valor bruto para regiões sem demanda de volta.
- [x] **`GeoHeatmapEngine`**: Mapeamento de zonas metropolitanas com faixas de demanda temporal (`DemandLevel`), 5 janelas horárias e probabilidades de retorno de passageiro.
- [x] **`PlatformComparisonEngine`**: Painel comparativo de eficiência operacional entre **Uber** e **99**, calculando lucro/hora real, ticket médio, margens e recomendações estratégicas dinâmicas.
- [x] **`GeoInsightsScreen` & `GeoInsightsViewModel`**: Interface completa com abas para Heatmap de Zonas, Simulador Preditivo de Deadhead e Comparativo Uber vs 99.
- [x] 9 novos testes unitários automatizados (total de 52 testes unitários).

---

## 🟢 FASE 5: Produto Comercial, Assinaturas e Escala (CONCLUÍDA ✅)
- [x] **Backend Escalável e API Assíncrona (`backend/app/main.py`)**: Endpoints de autenticação, planos, pedidos PIX, webhooks, verificação de recibos Play Billing, feature flags, ingestão de telemetria e admin.
- [x] **Migração 003 PostgreSQL (`003_commercial_subscriptions_and_admin.sql`)**: Tabelas de planos, transações PIX, recibos Play Store, solicitações LGPD, telemetria segura e snapshots administrativos.
- [x] **Deploy de Produção Containerizado**: `backend/Dockerfile` e `backend/docker-compose.yml` prontos para escala com PostgreSQL.
- [x] **Modelo de Monetização & Planos Pro**: Planos `FREE` (15 avaliações/dia), `PRO_MONTHLY` (R$ 29,90) e `PRO_ANNUAL` (R$ 239,90 com 33% de desconto).
- [x] **`FeatureGateManager`**: Motor de controle de limites diários e bloqueio granular de recursos Pro.
- [x] **`BillingManager` & `PixPaymentManager`**: Gestão de compras Google Play e gerador nativo de EMV BR Code (PIX Copia e Cola com cálculo real de CRC-16/CCITT).
- [x] **`SubscriptionPaywallScreen` & `SubscriptionPaywallViewModel`**: Paywall Cockpit Dark com seletor de planos, tabela comparativa, modal PIX interativo e compra Play Store.
- [x] **Conformidade Estrita com a LGPD (Lei 13.709/2018)**:
  * `LgpdManager`: Exportação completa de dados em JSON (Art. 18, V) e Direito ao Esquecimento / Expurgo total (Art. 18, VI).
  * `PrivacySettingsScreen`: Painel de preferências, download de dados e exclusão de conta.
- [x] **Painel Administrativo & Telemetria Segura**:
  * `AdminMetricsScreen` & `AdminMetricsViewModel`: Painel em tempo real de MRR, assinantes, avaliações diárias e toggles de feature flags.
  * `TelemetryManager` & `PiiSanitizer`: Fila com expurgo de eventos após 30 dias e sanitização automática de CPFs, e-mails, telefones e placas.
- [x] **Hardening R8 / ProGuard (`proguard-rules.pro`)**: Regras completas de ofuscação e otimização para build de release.
- [x] **21 novos testes unitários automatizados** (total de **73 testes unitários**, 22 suítes, 100% de aprovação).
- [x] Novo APK Debug gerado e validado (`app-debug.apk` de 16.6 MB).

---

## 🟢 FASE 6: Recursos Avançados do App e Inteligência Operacional (CONCLUÍDA ✅)
- [x] **Modo Carro & Bluetooth Auto-Trigger**:
  * `CarModeManager` & `CarModePreferencesDataSource`: Gerenciador reativo do ciclo de vida veicular.
  * `BluetoothCarReceiver`: Detecção em tempo real de pareamento veicular (`ACTION_ACL_CONNECTED` e `ACTION_ACL_DISCONNECTED`) disparando automaticamente o Copiloto e HUD Flutuante sem intervenção manual.
  * Permissões declaradas no Manifest (`BLUETOOTH`, `BLUETOOTH_CONNECT`) com fallback compatível.
  * Síntese vocal de boas-vindas e status de conexão veicular hands-free.
- [x] **Suporte a Novas Plataformas (inDrive & Entregas Expressas)**:
  * `InDriveParser`: Extração de valor ofertado pelo passageiro, distâncias de coleta e percurso.
  * `InDriveCounterOfferEngine`: Motor de contraproposta inteligente calculando a contraproposta ideal em múltiplos de R$ 2,00 (+2, +4, +6) para garantir a meta líquida horária do motorista.
  * `DeliveryParser`: Suporte a Uber Flash, 99 Entrega e Lalamove com categorização `DELIVERY` e compensação de overhead de coleta e entrega de pacotes (+5 min).
  * `PlatformDetector`: Detector unificado de 4 plataformas simultâneas (Uber, 99, inDrive, Entregas/Flash).
- [x] **Livro Caixa Digital & Demonstrativo Fiscal MEI / IRPF (`DriverTaxReportEngine`)**:
  * Alinhamento estrito à legislação tributária brasileira (LC 123/2006, art. 14 e RIR/2018): presunção de rendimento isento de 16% (transporte de passageiros) e 60% (transporte de cargas/entregas).
  * Abatimento integral de despesas operacionais comprovadas (combustíveis com odômetro, manutenções preventivas, IPVA e seguro proporcional).
  * Monitoramento visual do teto MEI de R$ 81.000,00/ano e percentual de faturamento utilizado.
  * Diagnóstico fiscal automatizado de blindagem contra imposto a pagar no IRPF.
  * Exportação de Livro Caixa completo em CSV para a contabilidade ou declaração anual DASN-SIMEI.
- [x] **Calculadora de Rejeição Estratégica & Custo da Espera (`StrategicRejectionEngine`)**:
  * Quebra do medo psicológico de rejeitar chamadas deficitárias.
  * Cálculo matemático do Ponto de Equilíbrio de Espera (*Break-even wait minutes*): tempo máximo tolerável para aguardar parado uma corrida boa antes de empatar com a corrida ruim.
  * Quantificação do custo operacional evitado e preservação do desgaste do veículo em km.
- [x] **`AdvancedToolsScreen` & `AdvancedToolsViewModel`**:
  * Interface Cockpit Dark completa com 4 abas interativas (Modo Carro, inDrive & Entregas, Livro Caixa & MEI, Rejeição Estratégica).
  * Integrada à navegação central (`Screen.AdvancedTools`) e atalho em destaque no `DashboardScreen`.
- [x] **18 novos testes unitários automatizados** (total de **91 testes unitários**, 28 suítes, 100% de aprovação).
- [x] APK de Debug compilado e validado.
