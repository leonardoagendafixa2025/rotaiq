# ROTA IQ 🚀
### Plataforma Inteligente de Decisão, Produtividade e Gestão Financeira para Motoristas de Aplicativos

O **ROTA IQ** é um copiloto financeiro e operacional nativo para motoristas de aplicativos no Brasil (Uber, 99, inDrive e outros). Diferente de simples calculadoras de R$/km, o ROTA IQ avalia se uma corrida realmente vale a pena com base no custo operacional real do veículo, metas diárias do motorista, tempo total, distância até o passageiro (deadhead), paradas intermediárias e zonas geográficas.

---

## 📱 Destaques da Fase 1 (Fundação e Motores)
- **Nativo Android**: Construído 100% em Kotlin com Jetpack Compose e Material 3.
- **Cockpit Dark**: Design de alto contraste desenvolvido para evitar cansaço visual e consumo de bateria.
- **RideEvaluationEngine**: Motor determinístico multi-fatorial com pontuação de 0 a 100 e classificações (`EXCELENTE`, `BOA`, `ACEITÁVEL`, `RUIM`, `EVITAR`).
- **VehicleCostEngine**: Cálculo granular do custo real do veículo por km, hora, dia, mês e ano.
- **GoalEngine**: Monitoramento de ritmo em tempo real com projeção de fechamento de turno.
- **FinancialEngine**: Margem líquida real, R$/km bruto e líquido, R$/hora bruto e líquido.
- **Arquitetura de Parsers**: Normalizador e parsers desacoplados (`UberParser`, `NinetyNineParser`, `PlatformDetector`).
- **Offline-First com Room Database**: Persistência local robusta sem dependência obrigatória de rede.

---

## 💎 Destaques da Fase 2 (Gestão Financeira Avançada & Sincronização)
- **FuelEngine**: Cálculo de consumo real na bomba usando o método de 2 tanques cheios consecutivos (`calculateConsumptionBetweenFills`), custo real por km rodado (`realCostPerKm`) e média ponderada.
- **MaintenanceSchedulerEngine**: Alertas preventivos por odômetro (`OK`, `UPCOMING`, `OVERDUE`) para troca de óleo, pneus, freios, suspensão e revisões periódicas.
- **AdvancedFinancialEngine**: Consolidação de DRE operacional com receita bruta, custos de combustível, manutenção, despesas fixas, lucro líquido real e margens em 4 períodos: **Diário**, **Semanal**, **Mensal** e **Anual**.
- **MultiPeriodGoalEngine**: Monitoramento integrado de metas diárias, semanais e mensais com projeção horária de ritmo e mensagens dinâmicas de coaching financeiro.
- **FinancialHubScreen & ViewModel**: Painel financeiro completo com tabs dinâmicas (Visão Geral, Abastecimentos, Manutenções, Despesas), métricas em cards cockpit e modais de lançamento rápido.
- **Room Database v2**: Novas entidades (`fuel_records`, `maintenance_records`, `vehicle_expenses`), DAOs dedicados e migração automatizada.
- **Esquema Remoto PostgreSQL**: Scripts SQL profissionais de migração (`001_initial_schema.sql` e `002_financial_and_sync.sql`) com 25 tabelas, índices e triggers.
- **SyncManager & Sync Models**: Camada de sincronização de dados offline-first com push/pull estruturado para backend.

---

## ⚡ Destaques da Fase 3 (Android Inteligente & Automação ao Volante)
- **RotaIqAccessibilityService**: Detecção e leitura em tempo real na tela de ofertas da Uber e 99 via eventos `TYPE_WINDOW_CONTENT_CHANGED` e extração recursiva de nós com debounce e deduplicação de cartões.
- **Floating HUD View & OverlayManager**: Card flutuante sobreposto (`SYSTEM_ALERT_WINDOW`) com renderização em menos de 100ms, exibindo Score, Lucro Líquido Real ("Sobra Limpo"), taxas R$/h e R$/km, totalmente arrastável por toque e com auto-dismiss inteligente.
- **OverlayService**: Serviço em segundo plano com notificação contínua no Android 14 para manter o HUD operacional em segundo plano.
- **VoiceAlertManager & TtsMessageFormatter**: Síntese vocal em português brasileiro (TTS) vocalizando o diagnóstico em 3 segundos ("Corrida Excelente! Sobra cerca de 27 reais, 81 reais por hora"), permitindo decidir sem tirar os olhos do trânsito.
- **AutomationHubScreen & ViewModel**: Painel de gerenciamento do copiloto com monitoramento de status das permissões, atalhos diretos para as Configurações do Android e laboratório de simulação de ofertas em tempo real.
- **100% de Conformidade LGPD**: Leitura restrita estritamente aos nós visíveis de oferta de corrida, sem captura de dados pessoais, mensagens ou senhas.

---

## 🗺️ Destaques da Fase 4 (Inteligência Geográfica & Análise Preditiva)
- **DeadheadPredictorEngine**: Motor preditivo que calcula o risco de volta vazia (`expectedEmptyReturnKm`), custo do combustível de retorno sem passageiro (`emptyReturnCost`) e abate do lucro líquido real da oferta (`adjustedNetProfit`), alertando quando uma corrida de valor aparente alto se torna deficitária.
- **GeoHeatmapEngine**: Modelagem de zonas metropolitanas com faixas de demanda temporal (`DemandLevel`: *Very High*, *High*, *Balanced*, *Low*, *Dead Zone*, *High Risk*), janelas horárias (Pico Matutino, Entrepico, Vespertino, Noite, Madrugada) e probabilidades de retorno de passageiro.
- **PlatformComparisonEngine**: Relatório consolidado comparativo de performance entre plataformas (Uber vs 99 vs inDrive), calculando rentabilidade por hora real apurada, margem líquida percentual, ticket médio e recomendações táticas de qual app priorizar.
- **GeoInsightsScreen & ViewModel**: Painel avançado com abas intuitivas para Heatmap de Demanda Metropolitano, Simulador Interativo de Deadhead e Comparativo de Eficiência Uber vs 99.

---

## 💰 Destaques da Fase 5 (Produto Comercial, Assinaturas e Escala)
- **Backend Escalável e API RESTful (`backend/app/main.py`)**: API assíncrona em FastAPI/Uvicorn com endpoints completos para planos, assinaturas, checkout PIX, webhooks, recibos Play Store, feature flags, telemetria segura e métricas de administração.
- **Banco PostgreSQL Completo (`backend/migrations/`)**: 3 migrações SQL com esquemas para usuários, motoristas, frotas, histórico de corridas, assinaturas, pagamentos PIX, logs de auditoria e solicitações de LGPD.
- **Deploy Containerizado**: `backend/Dockerfile` e `backend/docker-compose.yml` prontos para deploy de produção com PostgreSQL 15.
- **Sistema de Monetização e Feature Gating**:
  * `FeatureGateManager`: Controle de limites para o plano Gratuito (15 avaliações/dia) e desbloqueio integral para planos Pro.
  * Planos Pro: Mensal (R$ 29,90) e Anual (R$ 239,90 com 33% OFF).
  * `SubscriptionPaywallScreen`: Paywall moderno Cockpit Dark com seletor de planos, tabela comparativa de benefícios e checkout rápido.
- **Checkout Instantâneo PIX Nativo (`PixPaymentManager`)**: Gerador oficial de payload EMV BR Code conforme especificações do Banco Central do Brasil com cálculo determinístico de CRC-16/CCITT e Copia e Cola instantâneo.
- **Google Play In-App Billing (`BillingManager`)**: Gerenciamento de recibos, SKUs de assinatura e renovação automática.
- **Conformidade Estrita com a LGPD (Lei 13.709/2018)**:
  * `LgpdManager` & `PrivacySettingsScreen`: Portabilidade em JSON (Art. 18, V) e Direito ao Esquecimento com expurgo atômico de dados (Art. 18, VI).
  * `PiiSanitizer`: Mascaramento automático de CPF, e-mail, telefone, placas e coordenadas para todos os logs e telemetria.
  * `AndroidSecureStorage`: Armazenamento de credenciais e tokens protegido via criptografia simétrica AES-256 GCM.
- **Painel Administrativo & Telemetria (`AdminMetricsScreen`)**: Monitoramento ao vivo de MRR, contagem de assinantes, volume de avaliações diárias, toggles de Feature Flags e fila de telemetria sem dados pessoais.
- **Hardening de Produção R8 (`proguard-rules.pro`)**: Regras completas de ofuscação de código e remoção de logs de debug para a Google Play Store.

---

## 🛠️ Tecnologias Utilizadas

- **Linguagem**: Kotlin 1.9.24 & Python 3.11 (Backend)
- **Interface**: Jetpack Compose com Material 3 & Navigation Compose
- **Persistência Local**: Room 2.6.1 + KSP, Encrypted SharedPreferences
- **Backend & Banco Remoto**: FastAPI, Uvicorn, PostgreSQL 15, Docker & Docker Compose
- **Automação & Sistema**: Android AccessibilityService, WindowManager Overlay, TextToSpeech
- **Faturamento**: Google Play In-App Billing, PIX Banco Central BR Code EMV
- **Segurança**: AES-256 GCM, Android Keystore, R8 Proguard Hardening, LGPD Compliance
- **Assincronismo**: Kotlin Coroutines & Flow
- **Build System**: Gradle 8.7 com Version Catalog (`libs.versions.toml`)
- **JVM**: Microsoft OpenJDK 17 LTS

---

## 🧪 Suíte de Testes Automatizados

O projeto conta com **73 testes unitários automatizados** em **22 suítes de teste**, com **100% de taxa de aprovação**:

```powershell
$env:JAVA_HOME = 'C:\Users\User\AppData\Local\Programs\Microsoft\jdk-17.0.10.7-hotspot'
$env:ANDROID_HOME = 'C:\Users\User\AppData\Local\Android\Sdk'
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
& 'C:\Users\User\AppData\Local\Programs\gradle-8.7\bin\gradle.bat' testDebugUnitTest
```

---

## 📦 Como Gerar o APK

```powershell
$env:JAVA_HOME = 'C:\Users\User\AppData\Local\Programs\Microsoft\jdk-17.0.10.7-hotspot'
$env:ANDROID_HOME = 'C:\Users\User\AppData\Local\Android\Sdk'
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
& 'C:\Users\User\AppData\Local\Programs\gradle-8.7\bin\gradle.bat' assembleDebug
```
*Localização do binário:* `app/build/outputs/apk/debug/app-debug.apk` (16.6 MB).
