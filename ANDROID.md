# Configuração e Ambiente Android

## 1. Ferramental e Requisitos

O projeto **ROTA IQ** foi configurado para ser 100% autônomo e compilável via linha de comando, sem dependência do ambiente gráfico do Android Studio.

- **Sistema Operacional**: Windows 11 (compatível também com Linux e macOS)
- **Java / JDK**: Microsoft Build of OpenJDK with Hotspot 17 LTS (x64)
- **Android SDK**: Android SDK 34 (`platforms;android-34`, `build-tools;34.0.0`, `cmdline-tools;12.0`)
- **Gradle**: Gradle 8.7 com Gradle Wrapper (`gradlew.bat` / `gradlew`)
- **KSP**: Kotlin Symbol Processing `1.9.24-1.0.20` para Room

---

## 2. Permissões de Sistema e Serviços Especiais (Fase 3)

O aplicativo declara e utiliza as seguintes permissões gerenciadas no `AndroidManifest.xml`:

- `android.permission.INTERNET`: Sincronização remota com backend PostgreSQL.
- `android.permission.ACCESS_NETWORK_STATE`: Detecção de conectividade para operação offline-first.
- `android.permission.SYSTEM_ALERT_WINDOW`: Exibição do card flutuante translúcido (`FloatingHudView`) sobre a tela de ofertas do Uber e 99.
- `android.permission.FOREGROUND_SERVICE` e `FOREGROUND_SERVICE_SPECIAL_USE`: Manutenção do serviço em segundo plano no Android 14.
- `android.permission.POST_NOTIFICATIONS`: Exibição da notificação contínua do copiloto ativo.
- `android.permission.BIND_ACCESSIBILITY_SERVICE`: Vinculação segura do `RotaIqAccessibilityService` pelo sistema operacional Android com arquivo de configuração `@xml/accessibility_service_config`.

---

## 3. Variáveis de Ambiente Necessárias

Para execução em qualquer terminal:

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

## 4. Comandos de Compilação e Validação

### Executar Testes Unitários (43 Testes):
```powershell
.\gradlew.bat testDebugUnitTest
```

### Gerar APK de Debug:
```powershell
.\gradlew.bat assembleDebug
```
*Saída gerada:* `app/build/outputs/apk/debug/app-debug.apk` (16.6 MB)

---

## 5. Gerenciamento de Dependências: Version Catalog

Todas as bibliotecas e plugins estão centralizados em `gradle/libs.versions.toml`:
- Compose BOM: `2024.05.00`
- Room: `2.6.1`
- Navigation Compose: `2.7.7`
- Coroutines: `1.8.0`
- Android Gradle Plugin: `8.4.1`
- Kotlin: `1.9.24`
