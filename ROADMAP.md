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
- [x] Demonstrativo financeiro consolidado multi-período (`AdvancedFinancialEngine`) para Diário, Semanal, Mensal e Anual.
- [x] Monitoramento simultâneo de metas multi-período com coaching dinâmico de ritmo horário (`MultiPeriodGoalEngine`).
- [x] `FinancialHubScreen` e `FinancialHubViewModel` com navegação por abas dinâmicas e modais de lançamento rápido.
- [x] Evolução do banco local Room para a Versão 2 (novas tabelas, DAOs e repositório reativo com Flow).
- [x] Modelagem e scripts SQL de migração remota PostgreSQL (`001_initial_schema.sql` e `002_financial_and_sync.sql` com 25 tabelas).
- [x] Arquitetura de sincronização offline-first (`SyncManager`, `SyncPushPayload`, `SyncResponse`).
- [x] 13 novos testes unitários adicionados (total de 37 testes unitários).

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
- [x] 9 novos testes unitários automatizados (total de **52 testes unitários**, 16 suítes, 100% de aprovação).
- [x] Novo APK Debug gerado e validado (`app-debug.apk` de 16.6 MB).

---

## 🟡 FASE 5: Produto Comercial e Escala (PRÓXIMA FASE)
- [ ] Backend escalável em Kotlin/Spring Boot ou Go com PostgreSQL.
- [ ] Gateway de pagamentos e gestão de assinaturas (Google Play Billing, PIX).
- [ ] Painel administrativo para métricas de negócio e feature flags.
- [ ] Auditoria de segurança, telemetria segura e publicação na Google Play Store.
