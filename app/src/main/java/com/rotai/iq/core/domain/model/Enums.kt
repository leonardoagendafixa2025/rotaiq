package com.rotai.iq.core.domain.model

enum class EvaluationClassification(
    val label: String,
    val description: String,
    val hexColor: Long,
    val emoji: String
) {
    EXCELLENT(
        label = "EXCELENTE",
        description = "Excelente rentabilidade e alinhamento com metas",
        hexColor = 0xFF00E676,
        emoji = "🟢"
    ),
    GOOD(
        label = "BOA",
        description = "Boa relação valor/tempo/distância",
        hexColor = 0xFF76FF03,
        emoji = "🟢"
    ),
    ACCEPTABLE(
        label = "ACEITÁVEL",
        description = "Margem aceitável dentro das médias operacionais",
        hexColor = 0xFFFFD600,
        emoji = "🟡"
    ),
    BAD(
        label = "RUIM",
        description = "Rentabilidade baixa após descontar custos do veículo",
        hexColor = 0xFFFF9100,
        emoji = "🟠"
    ),
    AVOID(
        label = "EVITAR",
        description = "Prejuízo ou risco operacional/financeiro alto",
        hexColor = 0xFFFF1744,
        emoji = "🔴"
    );

    companion object {
        fun fromScore(score: Int): EvaluationClassification {
            return when {
                score >= 80 -> EXCELLENT
                score >= 65 -> GOOD
                score >= 50 -> ACCEPTABLE
                score >= 35 -> BAD
                else -> AVOID
            }
        }
    }
}

enum class RidePlatform(val displayName: String, val packageName: String?) {
    UBER("Uber", "com.ubercab.driver"),
    NINETY_NINE("99", "com.taxis99"),
    INDRAVE("inDrive", "sinet.bm.driver"),
    OTHER("Outra", null);

    companion object {
        fun fromPackageName(packageName: String): RidePlatform {
            return entries.find { it.packageName == packageName } ?: OTHER
        }
    }
}

enum class RideCategory(val displayName: String) {
    UBER_X("UberX"),
    UBER_COMFORT("Uber Comfort"),
    UBER_BLACK("Uber Black"),
    POP_99("99Pop"),
    PLUS_99("99Plus"),
    DELIVERY("Entrega"),
    STANDARD("Padrão");
}

enum class FuelType(val displayName: String) {
    GASOLINE("Gasolina"),
    ETHANOL("Etanol"),
    CNG("GNV"),
    DIESEL("Diesel"),
    ELECTRIC("Elétrico"),
    HYBRID("Híbrido")
}
