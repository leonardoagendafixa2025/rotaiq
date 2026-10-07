package com.rotai.iq.core.ui.theme

import androidx.compose.ui.graphics.Color

// ==========================================
// ROTA IQ - PALETA VISUAL PREMIUM (2026)
// ==========================================

// Base quase preta (Profundidade e ergonomia dark)
val RotaBlack = Color(0xFF080808)
val RotaDarkCanvas = Color(0xFF0D0D0D)
val RotaCardBackground = Color(0xFF141414)
val RotaCardElevated = Color(0xFF1C1C1C)
val RotaCardSubtle = Color(0xFF222222)

// Bordas discretas e translúcidas
val RotaBorderSubtle = Color(0xFF262626)
val RotaBorderMedium = Color(0xFF333333)
val RotaBorderHighlight = Color(0x33FFFFFF)

// Cor Principal: Laranja Vibrante / Electric Amber
val RotaOrangePrimary = Color(0xFFFF7A00)
val RotaOrangeHover = Color(0xFFFF6A00)
val RotaOrangeLight = Color(0xFFFF8A00)
val RotaOrangeDeep = Color(0xFFE65100)
val RotaOrangeGlow = Color(0x33FF7A00)
val RotaOrangeSubtleBg = Color(0x1AFF7A00)

// Status e Veredito de Corrida
val RotaExcellent = Color(0xFF00E676)       // Verde Neon - Excelente
val RotaGood = Color(0xFF76FF03)            // Verde Claro - Bom
val RotaAttention = Color(0xFFFFD600)       // Amarelo Ouro - Atenção / Aceitável
val RotaWarning = Color(0xFFFF9100)         // Laranja Alerta - Regular / Ruim
val RotaAvoid = Color(0xFFFF334B)           // Vermelho Alerta - Evitar

// Glows de Status
val RotaGlowGreen = Color(0x2600E676)
val RotaGlowYellow = Color(0x26FFD600)
val RotaGlowRed = Color(0x26FF334B)

// Tipografia e Contraste
val RotaTextWhite = Color(0xFFFFFFFF)
val RotaTextPrimary = Color(0xFFF5F5F7)
val RotaTextSecondary = Color(0xFFA0A0A5)
val RotaTextTertiary = Color(0xFF6B6B70)
val RotaTextMuted = Color(0xFF48484A)

// ==========================================
// ALIASES DE COMPATIBILIDADE RETRÓGRADA
// ==========================================
val CockpitBackground = RotaBlack
val CockpitSurface = RotaDarkCanvas
val CockpitSurfaceVariant = RotaCardBackground
val CockpitBorder = RotaBorderSubtle

val BrandPrimary = RotaOrangePrimary
val BrandSecondary = RotaOrangeLight

val ClassExcellent = RotaExcellent
val ClassGood = RotaGood
val ClassAcceptable = RotaAttention
val ClassBad = RotaWarning
val ClassAvoid = RotaAvoid

val TextPrimary = RotaTextPrimary
val TextSecondary = RotaTextSecondary
val TextTertiary = RotaTextTertiary

val CardGlowGreen = RotaGlowGreen
val CardGlowRed = RotaGlowRed
val CardGlowCyan = RotaOrangeGlow
