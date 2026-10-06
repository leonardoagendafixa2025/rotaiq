# Roadmap do Produto ROTA IQ

Este documento define o plano mestre de evolução do **ROTA IQ** da Fase 1 até a escala comercial.

---

## 🟢 FASE 1: Fundação e Motores Principais (CONCLUÍDA)
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
- [x] Suíte de 24 testes unitários automatizados cobrindo todos os cenários operacionais.
- [x] Geração de APK de Debug (`app-debug.apk`).

---

## 🟡 FASE 2: Gestão Financeira Avançada e Sincronização
- [ ] Registro granular de abastecimentos (`FuelRecord`) com cálculo de consumo real na bomba.
- [ ] Registro e alertas preventivos de manutenção (troca de óleo, pastilhas, rodízio de pneus).
- [ ] Dashboards financeiros consolidados: Diário, Semanal, Mensal e Anual.
- [ ] Relatórios de exportação em PDF e planilha CSV para declaração de imposto e controle.
- [ ] Implementação de sincronização assíncrona com PostgreSQL via WorkManager.

---

## 🟡 FASE 3: Android Inteligente e Automação ao Volante
- [ ] Implementação do `AccessibilityService` com permissões granulares e tela educativa.
- [ ] Leitura em tempo real na tela de ofertas da Uber e 99.
- [ ] HUD Flutuante (Overlay / WindowManager) compacto e translúcido para exibição sobre outros apps.
- [ ] Alertas por síntese de voz (TTS) com opção de ligar/desligar.

---

## 🟡 FASE 4: Inteligência Geográfica e Análise Preditiva
- [ ] Integração com mapas (Google Maps / Mapbox) para geofencing e rotas.
- [ ] Heatmaps de alta e baixa rentabilidade por horário.
- [ ] Histórico de taxa de retorno e risco de viagem para áreas sem retorno (Deadhead).
- [ ] Painel comparativo de performance: Uber vs 99.

---

## 🟡 FASE 5: Produto Comercial e Escala
- [ ] Backend escalável em Kotlin/Spring Boot ou Go/Node.js com PostgreSQL.
- [ ] Gateway de pagamentos e gestão de assinaturas (Google Play Billing, PIX, Cartão).
- [ ] Painel administrativo para métricas de negócio e feature flags.
- [ ] Auditoria de segurança, telemetria segura e publicação na Google Play Store.
