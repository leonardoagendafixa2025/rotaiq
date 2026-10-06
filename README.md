# ROTA IQ 🚀
### Plataforma Inteligente de Decisão, Produtividade e Gestão Financeira para Motoristas de Aplicativos

O **ROTA IQ** é um copiloto financeiro e operacional nativo para motoristas de aplicativos no Brasil (Uber, 99, inDrive e outros). Diferente de simples calculadoras de R$/km, o ROTA IQ avalia se uma corrida realmente vale a pena com base no custo operacional real do veículo, metas diárias do motorista, tempo total, distância até o passageiro (deadhead), paradas intermediárias e zonas geográficas.

---

## 📱 Destaques da Fase 1

- **Nativo Android**: Construído 100% em Kotlin com Jetpack Compose e Material 3.
- **Cockpit Dark**: Design de alto contraste desenvolvido para evitar cansaço visual e consumo de bateria.
- **RideEvaluationEngine**: Motor determinístico multi-fatorial com pontuação de 0 a 100 e classificações (`EXCELENTE`, `BOA`, `ACEITÁVEL`, `RUIM`, `EVITAR`).
- **VehicleCostEngine**: Cálculo granular do custo real do veículo por km, hora, dia, mês e ano (combustível, consumo, manutenção preventiva, seguro, IPVA, licenciamento e depreciação).
- **GoalEngine**: Monitoramento de ritmo em tempo real com projeção de fechamento de turno e orientações dinâmicas ("Você precisa faturar mais R$ X hoje").
- **FinancialEngine**: Margem líquida real, R$/km bruto e líquido, R$/hora bruto e líquido.
- **Arquitetura de Parsers**: Normalizador e parsers desacoplados (`UberParser`, `NinetyNineParser`, `PlatformDetector`).
- **Offline-First com Room Database**: Persistência local robusta sem dependência obrigatória de rede.
- **Suíte de Testes Automatizados**: 24 testes unitários cobrindo todos os motores e cenários operacionais.

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

### 1. Compilar e Executar Testes Unitários
```bash
./gradlew testDebugUnitTest
```

### 2. Gerar o APK Debug
```bash
./gradlew assembleDebug
```
O APK final será gerado em:
`app/build/outputs/apk/debug/app-debug.apk`

### 3. Instalar no Dispositivo ou Emulador
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

---

## 📁 Estrutura do Repositório

Consulte a documentação completa:
- [ARCHITECTURE.md](ARCHITECTURE.md) - Arquitetura de software e camadas.
- [DATABASE.md](DATABASE.md) - Modelagem do banco local e planejamento remoto.
- [ANDROID.md](ANDROID.md) - Configurações do SDK, Gradle e ecossistema Android.
- [SECURITY.md](SECURITY.md) - Políticas de segurança, privacidade e LGPD.
- [ROADMAP.md](ROADMAP.md) - Planejamento de todas as fases (Fase 1 à Fase 5).
- [DECISIONS.md](DECISIONS.md) - Registro de decisões arquiteturais (ADRs).
- [TESTING.md](TESTING.md) - Estratégia de testes unitários e cobertura.
