package com.rotai.iq

import com.google.common.truth.Truth.assertThat
import com.rotai.iq.core.automation.accessibility.AccessibilityNodeExtractor
import org.junit.Test

class AccessibilityNodeExtractorTest {

    @Test
    fun buildCombinedText_emptyList_returnsEmptyString() {
        val result = AccessibilityNodeExtractor.buildCombinedText(emptyList())
        assertThat(result).isEmpty()
    }

    @Test
    fun buildCombinedText_multipleLines_joinsCorrectlyWithNewlines() {
        val lines = listOf(
            "UberX",
            "R$ 32,80",
            "1,2 km (4 min)",
            "8,5 km (22 min)"
        )
        val combined = AccessibilityNodeExtractor.buildCombinedText(lines)
        assertThat(combined).isEqualTo("UberX\nR$ 32,80\n1,2 km (4 min)\n8,5 km (22 min)")
        assertThat(combined).contains("R$ 32,80")
    }

    @Test
    fun extractAllTexts_nullNode_returnsEmptyList() {
        val result = AccessibilityNodeExtractor.extractAllTexts(null)
        assertThat(result).isEmpty()
    }
}
