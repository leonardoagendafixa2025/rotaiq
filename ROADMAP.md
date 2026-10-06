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
- [x] 13 novos testes unitários adicionados (total de **37 testes unitários**, 100% aprovados).
- [x] Novo APK Debug gerado e validado via Gradle CLI.

---

## 🟡 FASE 3: Android Inteligente e Automação ao Volante (PRÓXIMA FASE)
- [ ] Implementação do `AccessibilityService` com permissões granulares e onboarding educativo transparente.
- [ ] Leitura em tempo real na tela de ofertas das plataformas (Uber, 99).
- [ ] HUD Flutuante (Overlay / WindowManager) compacto e translúcido para exibição sobre outros apps em menos de 100ms.
- [ ] Alertas por síntese de voz (TTS) com opção de ligar/desligar para não desviar a atenção do trânsito.
- [ ] Suíte de testes para os parsers de acessibilidade e ciclo de vida do overlay.

---

## ⚪ FASE 4: Inteligência Geográfica e Análise Preditiva
- [ ] Integração com mapas (Google Maps / Mapbox) para geofencing e análise de trajeto.
- [ ] Heatmaps de alta e baixa rentabilidade por horário e bairro.
- [ ] Análise preditiva de taxa de retorno e risco de deadhead para viagens fora de áreas centrais.
- [ ] Painel comparativo de performance operacional: Uber vs 99 vs inDrive.

---

## ⚪ FASE 5: Produto Comercial e Escala
- [ ] Backend escalável em Kotlin/Spring Boot ou Go com PostgreSQL.
- [ ] Gateway de pagamentos e gestão de assinaturas (Google Play Billing, PIX).
- [ ] Painel administrativo para métricas de negócio e feature flags.
- [ ] Auditoria de segurança, telemetria segura e publicação na Google Play Store.
