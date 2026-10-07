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

    @Test
    fun buildCombinedText_singleLine_returnsExactString() {
        val result = AccessibilityNodeExtractor.buildCombinedText(listOf("Uber Comfort R$ 45,00"))
        assertThat(result).isEqualTo("Uber Comfort R$ 45,00")
    }

    @Test
    fun buildCombinedText_generatesDeterministicHashForDebounce() {
        val lines1 = listOf("UberX", "R$ 30,00", "5 km")
        val lines2 = listOf("UberX", "R$ 30,00", "5 km")
        val text1 = AccessibilityNodeExtractor.buildCombinedText(lines1)
        val text2 = AccessibilityNodeExtractor.buildCombinedText(lines2)

        assertThat(text1.hashCode()).isEqualTo(text2.hashCode())
    }
}
