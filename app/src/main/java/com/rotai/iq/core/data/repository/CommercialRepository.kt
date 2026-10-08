package com.rotai.iq.core.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.rotai.iq.core.domain.model.LgpdConsent
import com.rotai.iq.core.domain.model.PaymentGateway
import com.rotai.iq.core.domain.model.SubscriptionInfo
import com.rotai.iq.core.domain.model.SubscriptionStatus
import com.rotai.iq.core.domain.model.SubscriptionTier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

interface CommercialRepository {
    fun getSubscriptionInfo(): Flow<SubscriptionInfo>
    suspend fun saveSubscriptionInfo(info: SubscriptionInfo)

    fun getLgpdConsent(): Flow<LgpdConsent>
    suspend fun saveLgpdConsent(consent: LgpdConsent)

    fun getTodayEvaluationsCount(): Int
    fun incrementTodayEvaluationsCount(): Int
    fun resetTodayEvaluationsCount()

    suspend fun purgeAllUserData()
}

class CommercialRepositoryImpl(
    context: Context
) : CommercialRepository {

    private val prefs: SharedPreferences = context.getSharedPreferences("rotai_commercial_prefs", Context.MODE_PRIVATE)
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    private val _subscriptionFlow = MutableStateFlow(loadSubscription())
    private val _consentFlow = MutableStateFlow(loadConsent())

    override fun getSubscriptionInfo(): Flow<SubscriptionInfo> = _subscriptionFlow.asStateFlow()

    override suspend fun saveSubscriptionInfo(info: SubscriptionInfo) {
        prefs.edit()
            .putBoolean("sub_initialized", true)
            .putString("sub_tier", info.tier.code)
            .putString("sub_status", info.status.name)
            .putLong("sub_expires", info.expiresAtEpochMs ?: 0L)
            .putLong("sub_trial_start", info.trialStartedAtEpochMs ?: 0L)
            .putBoolean("sub_autorenew", info.autoRenew)
            .putString("sub_gateway", info.gateway.name)
            .putString("sub_token", info.purchaseToken ?: "")
            .apply()
        _subscriptionFlow.value = info
    }

    override fun getLgpdConsent(): Flow<LgpdConsent> = _consentFlow.asStateFlow()

    override suspend fun saveLgpdConsent(consent: LgpdConsent) {
        prefs.edit()
            .putBoolean("lgpd_terms", consent.termsAccepted)
            .putBoolean("lgpd_privacy", consent.privacyPolicyAccepted)
            .putBoolean("lgpd_telemetry", consent.telemetryOptIn)
            .putBoolean("lgpd_benchmark", consent.anonymousBenchmarkingOptIn)
            .putLong("lgpd_timestamp", consent.consentTimestampEpochMs)
            .putString("lgpd_version", consent.appVersionName)
            .apply()
        _consentFlow.value = consent
    }

    override fun getTodayEvaluationsCount(): Int {
        val today = getTodayKey()
        return prefs.getInt("eval_count_$today", 0)
    }

    override fun incrementTodayEvaluationsCount(): Int {
        val today = getTodayKey()
        val count = prefs.getInt("eval_count_$today", 0) + 1
        prefs.edit().putInt("eval_count_$today", count).apply()
        return count
    }

    override fun resetTodayEvaluationsCount() {
        val today = getTodayKey()
        prefs.edit().remove("eval_count_$today").apply()
    }

    override suspend fun purgeAllUserData() {
        prefs.edit().clear().apply()
        _subscriptionFlow.value = SubscriptionInfo.FREE_DEFAULT
        _consentFlow.value = LgpdConsent(termsAccepted = false, privacyPolicyAccepted = false, telemetryOptIn = false)
    }

    private fun getTodayKey(): String = dateFormat.format(Date())

    private fun loadSubscription(): SubscriptionInfo {
        // Novo motorista: libera período gratuito por 7 dias
        if (!prefs.contains("sub_initialized")) {
            val trial = SubscriptionInfo.createTrial(7)
            prefs.edit()
                .putBoolean("sub_initialized", true)
                .putString("sub_tier", trial.tier.code)
                .putString("sub_status", trial.status.name)
                .putLong("sub_expires", trial.expiresAtEpochMs ?: 0L)
                .putLong("sub_trial_start", trial.trialStartedAtEpochMs ?: 0L)
                .putBoolean("sub_autorenew", false)
                .putString("sub_gateway", trial.gateway.name)
                .apply()
            return trial
        }

        val tierCode = prefs.getString("sub_tier", "free") ?: "free"
        val statusName = prefs.getString("sub_status", SubscriptionStatus.NONE.name) ?: SubscriptionStatus.NONE.name
        val expires = prefs.getLong("sub_expires", 0L).let { if (it > 0L) it else null }
        val trialStart = prefs.getLong("sub_trial_start", 0L).let { if (it > 0L) it else null }
        val autoRenew = prefs.getBoolean("sub_autorenew", false)
        val gatewayName = prefs.getString("sub_gateway", PaymentGateway.NONE.name) ?: PaymentGateway.NONE.name
        val token = prefs.getString("sub_token", null)?.takeIf { it.isNotBlank() }

        var tier = SubscriptionTier.entries.find { it.code == tierCode } ?: SubscriptionTier.FREE
        var status = try {
            SubscriptionStatus.valueOf(statusName)
        } catch (e: Exception) {
            SubscriptionStatus.NONE
        }

        // Se estava no período de teste de 7 dias e os 7 dias encerraram:
        if (status == SubscriptionStatus.TRIALING && expires != null && expires <= System.currentTimeMillis()) {
            status = SubscriptionStatus.EXPIRED
            tier = SubscriptionTier.FREE
            prefs.edit()
                .putString("sub_tier", tier.code)
                .putString("sub_status", status.name)
                .apply()
        }

        val gateway = try {
            PaymentGateway.valueOf(gatewayName)
        } catch (e: Exception) {
            PaymentGateway.NONE
        }

        return SubscriptionInfo(
            tier = tier,
            status = status,
            expiresAtEpochMs = expires,
            trialStartedAtEpochMs = trialStart,
            autoRenew = autoRenew,
            gateway = gateway,
            purchaseToken = token
        )
    }

    private fun loadConsent(): LgpdConsent {
        return LgpdConsent(
            termsAccepted = prefs.getBoolean("lgpd_terms", true),
            privacyPolicyAccepted = prefs.getBoolean("lgpd_privacy", true),
            telemetryOptIn = prefs.getBoolean("lgpd_telemetry", true),
            anonymousBenchmarkingOptIn = prefs.getBoolean("lgpd_benchmark", true),
            consentTimestampEpochMs = prefs.getLong("lgpd_timestamp", System.currentTimeMillis()),
            appVersionName = prefs.getString("lgpd_version", "1.0.0") ?: "1.0.0"
        )
    }
}
