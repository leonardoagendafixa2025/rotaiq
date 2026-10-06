# Configuração e Ambiente Android

## 1. Ferramental e Requisitos

O projeto **ROTA IQ** foi configurado para ser 100% autônomo e compilável via linha de comando, sem dependência do ambiente gráfico do Android Studio.

- **Sistema Operacional**: Windows 11 (compatível também com Linux e macOS)
- **Java / JDK**: Microsoft Build of OpenJDK with Hotspot 17 LTS (x64)
- **Android SDK**: Android SDK 34 (`platforms;android-34`, `build-tools;34.0.0`, `cmdline-tools;12.0`)
- **Gradle**: Gradle 8.7 com Gradle Wrapper (`gradlew.bat` / `gradlew`)
- **KSP**: Kotlin Symbol Processing `1.9.24-1.0.20` para Room Database
- **Material 3 / Jetpack Compose**: `1.2.1` com Kotlin Compiler `1.9.24`

---

## 2. Permissões de Sistema e Serviços Especiais

O aplicativo declara e gerencia as seguintes permissões no `AndroidManifest.xml`:

- `android.permission.INTERNET`: Sincronização remota e verificação de assinaturas com o backend.
- `android.permission.ACCESS_NETWORK_STATE`: Detecção de conectividade para operação offline-first.
- `android.permission.SYSTEM_ALERT_WINDOW`: Exibição do card flutuante translúcido (`FloatingHudView`) sobre o Uber e 99.
- `android.permission.FOREGROUND_SERVICE` e `FOREGROUND_SERVICE_SPECIAL_USE`: Manutenção do serviço em segundo plano no Android 14.
- `android.permission.POST_NOTIFICATIONS`: Exibição da notificação contínua do copiloto ativo.
- `android.permission.BIND_ACCESSIBILITY_SERVICE`: Vinculação segura do `RotaIqAccessibilityService` pelo Android via `@xml/accessibility_service_config`.

---

## 3. Módulos Comerciais e Novas Telas (Fase 5)

Implementadas 3 novas telas em Jetpack Compose com Cockpit Dark e integração na navegação:

1. **`SubscriptionPaywallScreen`** (`feature/subscription`):
   - Seletor de planos Pro Mensal (R$ 29,90) vs Pro Anual (R$ 19,99/mês - 33% OFF);
   - Matriz comparativa de benefícios Free vs Pro;
   - Modal interativo de PIX Copia e Cola com cálculo real de CRC-16 EMV BR Code;
   - Ação de compra via Google Play Billing e restauração de compras.

2. **`PrivacySettingsScreen`** (`feature/privacy`):
   - Toggles de consentimento (Telemetria, Benchmarking de regiões);
   - Exportação completa em JSON dos dados do motorista (Art. 18, V da LGPD);
   - Diálogo seguro para exclusão de conta e expurgo definitivo de dados locais (Art. 18, VI).

3. **`AdminMetricsScreen`** (`feature/admin`):
   - KPIs de negócio em tempo real (MRR estimado, assinantes ativos, total de avaliações);
   - Gerenciador ao vivo de Feature Flags com toggles em tempo real;
   - Monitoramento da fila de telemetria local com PII mascarada.

---

## 4. Variáveis de Ambiente Necessárias

Para execução em qualquer terminal PowerShell:

```powershell
$env:JAVA_HOME = "C:\Users\User\AppData\Local\Programs\Microsoft\jdk-17.0.10.7-hotspot"
$env:ANDROID_HOME = "C:\Users\User\AppData\Local\Android\Sdk"
$env:Path = "$env:JAVA_HOME\bin;$env:ANDROID_HOME\cmdline-tools\latest\bin;$env:ANDROID_HOME\platform-tools;$env:Path"
```

O arquivo `local.properties` na raiz do projeto aponta diretamente para o SDK:
```properties
sdk.dir=C\:\\Users\\User\\AppData\\Local\\Android\\Sdk
```

---

## 5. Comandos de Compilação e Validação

### Executar Suíte Completa de Testes Unitários (73 Testes em 22 Suítes):
```powershell
$env:JAVA_HOME = 'C:\Users\User\AppData\Local\Programs\Microsoft\jdk-17.0.10.7-hotspot'
$env:ANDROID_HOME = 'C:\Users\User\AppData\Local\Android\Sdk'
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
& 'C:\Users\User\AppData\Local\Programs\gradle-8.7\bin\gradle.bat' testDebugUnitTest
```
*Status:* `BUILD SUCCESSFUL in 1m 20s (73 passed, 0 failed)`

### Gerar APK de Debug:
```powershell
$env:JAVA_HOME = 'C:\Users\User\AppData\Local\Programs\Microsoft\jdk-17.0.10.7-hotspot'
$env:ANDROID_HOME = 'C:\Users\User\AppData\Local\Android\Sdk'
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
& 'C:\Users\User\AppData\Local\Programs\gradle-8.7\bin\gradle.bat' assembleDebug
```
*Saída gerada:* `app/build/outputs/apk/debug/app-debug.apk` (16.6 MB)
