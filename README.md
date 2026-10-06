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
- **37 Testes Unitários Automatizados**: 100% de aprovação em todos os testes unitários da aplicação.

---

## 🛠️ Tecnologias Utilizadas

- **Linguagem**: Kotlin 1.9.24
- **Interface**: Jetpack Compose com Material 3 & Navigation Compose
- **Persistência Local**: Room 2.6.1 + KSP
- **Assincronismo**: Kotlin Coroutines & Flow
- **Build System**: Gradle 8.7 com Version Catalog (`libs.versions.toml`)
- **JVM**: Microsoft OpenJDK 17 LTS
- **Testes**: JUnit 4, Google Truth, Kotlinx Coroutines Test, Mockk

---

## 🚀 Como Executar o Projeto

O projeto foi configurado para ser executado diretamente pelo terminal (CLI do Antigravity), sem necessidade do Android Studio:

### 1. Compilar e Executar Testes Unitários (37 Testes)
```powershell
.\gradlew.bat testDebugUnitTest
```

### 2. Gerar o APK Debug
```powershell
.\gradlew.bat assembleDebug
```
O APK final será gerado em:
`app/build/outputs/apk/debug/app-debug.apk` (16.2 MB)

### 3. Instalar no Dispositivo ou Emulador
```powershell
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

---

## 📁 Estrutura do Repositório

Consulte a documentação completa:
- [ARCHITECTURE.md](ARCHITECTURE.md) - Arquitetura de software, camadas e fluxo de sincronização.
- [DATABASE.md](DATABASE.md) - Modelagem do banco local (Room v2) e remoto (PostgreSQL).
- [ANDROID.md](ANDROID.md) - Configurações do SDK, Gradle e ecossistema Android.
- [SECURITY.md](SECURITY.md) - Políticas de segurança, privacidade e LGPD.
- [ROADMAP.md](ROADMAP.md) - Planejamento e status detalhado de todas as fases.
- [DECISIONS.md](DECISIONS.md) - Registro de decisões arquiteturais (ADRs).
- [TESTING.md](TESTING.md) - Estratégia de testes unitários e cobertura dos 37 cenários.
