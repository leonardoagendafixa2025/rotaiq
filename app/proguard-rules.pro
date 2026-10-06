# ====================================================================
# ROTA IQ - REGRAS R8 / PROGUARD DE PRODUÇÃO E HARDENING
# ====================================================================

# 1. Preservar Entidades e DAOs do Room Database
-keep class com.rotai.iq.core.data.local.entity.** { *; }
-keep class com.rotai.iq.core.data.local.dao.** { *; }
-keep class com.rotai.iq.core.data.local.db.** { *; }
-keep class * extends androidx.room.RoomDatabase

# 2. Preservar Modelos de Domínio e Enums
-keep class com.rotai.iq.core.domain.model.** { *; }
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# 3. Preservar Contratos e DTOs de Sincronização e Telemetria
-keep class com.rotai.iq.core.network.** { *; }
-keep class com.rotai.iq.core.telemetry.** { *; }

# 4. Preservar Jetpack Compose Runtime
-keepclassmembers class * extends androidx.compose.ui.Modifier { *; }
-dontwarn androidx.compose.**

# 5. Otimizações de Bytecode e Ofuscação
-repackageclasses 'com.rotai.iq.obf'
-allowaccessmodification

# 6. Remover Logs Verbosos e de Debug em Produção
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
}

# 7. Preservar Kotlin Metadata necessária
-keepclassmembers class * {
    @androidx.annotation.Keep *;
}
