# Configuração e Ambiente Android

## 1. Ferramental e Requisitos

O projeto **ROTA IQ** foi configurado para ser 100% autônomo e compilável via linha de comando, sem dependência do ambiente gráfico do Android Studio.

- **Sistema Operacional**: Windows 11 (compatível também com Linux e macOS)
- **Java / JDK**: Microsoft Build of OpenJDK with Hotspot 17 LTS (x64)
- **Android SDK**: Android SDK 34 (`platforms;android-34`, `build-tools;34.0.0`, `cmdline-tools;12.0`)
- **Gradle**: Gradle 8.7 com Gradle Wrapper (`gradlew.bat` / `gradlew`)
- **KSP**: Kotlin Symbol Processing `1.9.24-1.0.20` para Room

---

## 2. Variáveis de Ambiente Necessárias

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

## 3. Comandos de Compilação e Validação

### Executar Testes Unitários:
```powershell
.\gradlew.bat testDebugUnitTest
```

### Gerar APK de Debug:
```powershell
.\gradlew.bat assembleDebug
```
*Saída gerada:* `app/build/outputs/apk/debug/app-debug.apk`

### Verificar Lint e Qualidade de Código:
```powershell
.\gradlew.bat lintDebug
```

### Limpar e Recompilar:
```powershell
.\gradlew.bat clean assembleDebug
```

---

## 4. Gerenciamento de Dependências: Version Catalog

Todas as bibliotecas e plugins estão centralizados em `gradle/libs.versions.toml`:
- Compose BOM: `2024.05.00`
- Room: `2.6.1`
- Navigation Compose: `2.7.7`
- Coroutines: `1.8.0`
- Android Gradle Plugin: `8.4.1`
- Kotlin: `1.9.24`
