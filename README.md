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
- **43 Testes Unitários Automatizados**: 13 suítes de testes com 100% de aprovação.

---

## 🛠️ Tecnologias Utilizadas

- **Linguagem**: Kotlin 1.9.24
- **Interface**: Jetpack Compose com Material 3 & Navigation Compose
- **Persistência Local**: Room 2.6.1 + KSP
- **Automação & Sistema**: Android AccessibilityService, WindowManager Overlay, TextToSpeech
- **Assincronismo**: Kotlin Coroutines & Flow
- **Build System**: Gradle 8.7 com Version Catalog (`libs.versions.toml`)
- **JVM**: Microsoft OpenJDK 17 LTS
- **Testes**: JUnit 4, Google Truth, Kotlinx Coroutines Test, Mockk

---

## 🚀 Como Executar o Projeto

O projeto foi configurado para ser executado diretamente pelo terminal (CLI do Antigravity), sem necessidade do Android Studio:

### 1. Compilar e Executar Testes Unitários (43 Testes)
```powershell
.\gradlew.bat testDebugUnitTest
```

### 2. Gerar o APK Debug
```powershell
.\gradlew.bat assembleDebug
```
O APK final será gerado em:
`app/build/outputs/apk/debug/app-debug.apk` (16.6 MB)

### 3. Instalar no Dispositivo ou Emulador
```powershell
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

---

## 📁 Estrutura do Repositório

Consulte a documentação completa:
- [ARCHITECTURE.md](ARCHITECTURE.md) - Arquitetura de software, camadas, fluxo de acessibilidade, overlay e TTS.
- [DATABASE.md](DATABASE.md) - Modelagem do banco local (Room v2) e remoto (PostgreSQL).
- [ANDROID.md](ANDROID.md) - Configurações do SDK, permissões especiais e ecossistema Android.
- [SECURITY.md](SECURITY.md) - Políticas de segurança, privacidade e diretrizes da LGPD para Acessibilidade.
- [ROADMAP.md](ROADMAP.md) - Planejamento e status detalhado de todas as fases.
- [DECISIONS.md](DECISIONS.md) - Registro de decisões arquiteturais (ADRs).
- [TESTING.md](TESTING.md) - Estratégia de testes unitários e cobertura dos 43 cenários.
