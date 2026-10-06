package com.rotai.iq.core.featureflags

class FeatureFlagManager {

    companion object {
        const val FLAG_ENABLE_PIX_CHECKOUT = "flag_enable_pix_checkout"
        const val FLAG_ENABLE_TTS_COPILOT = "flag_enable_tts_copilot"
        const val FLAG_ENABLE_DEADHEAD_PREDICTOR = "flag_enable_deadhead_predictor"
        const val FLAG_ENABLE_PLATFORM_COMPARISON = "flag_enable_platform_comparison"
        const val FLAG_ENABLE_PROMO_ANNUAL_DISCOUNT = "flag_enable_promo_annual_discount"
    }

    private val defaultFlags = mapOf(
        FLAG_ENABLE_PIX_CHECKOUT to true,
        FLAG_ENABLE_TTS_COPILOT to true,
        FLAG_ENABLE_DEADHEAD_PREDICTOR to true,
        FLAG_ENABLE_PLATFORM_COMPARISON to true,
        FLAG_ENABLE_PROMO_ANNUAL_DISCOUNT to true
    )

    private val runtimeOverrides = mutableMapOf<String, Boolean>()

    fun isEnabled(flagKey: String): Boolean {
        return runtimeOverrides[flagKey] ?: defaultFlags[flagKey] ?: false
    }

    fun setOverride(flagKey: String, enabled: Boolean) {
        runtimeOverrides[flagKey] = enabled
    }

    fun clearOverrides() {
        runtimeOverrides.clear()
    }

    fun getAllFlags(): Map<String, Boolean> {
        val merged = defaultFlags.toMutableMap()
        merged.putAll(runtimeOverrides)
        return merged
    }
}
