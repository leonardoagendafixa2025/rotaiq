package com.rotai.iq.core.automation.accessibility

import android.view.accessibility.AccessibilityNodeInfo

object AccessibilityNodeExtractor {

    /**
     * Extrai recursivamente todo o texto legível de uma árvore de nós de acessibilidade.
     * Retorna uma lista de strings ordenada e limpa.
     */
    fun extractAllTexts(rootNode: AccessibilityNodeInfo?): List<String> {
        if (rootNode == null) return emptyList()
        val results = mutableListOf<String>()
        collectTextsRecursive(rootNode, results)
        return results
    }

    private fun collectTextsRecursive(node: AccessibilityNodeInfo, list: MutableList<String>) {
        val text = node.text?.toString()?.trim()
        val desc = node.contentDescription?.toString()?.trim()

        if (!text.isNullOrEmpty()) {
            list.add(text)
        } else if (!desc.isNullOrEmpty()) {
            list.add(desc)
        }

        for (i in 0 until node.childCount) {
            val child = try {
                node.getChild(i)
            } catch (e: Exception) {
                null
            }
            if (child != null) {
                collectTextsRecursive(child, list)
            }
        }
    }

    /**
     * Concatena os textos extraídos em um bloco único para alimentar os parsers de plataformas.
     */
    fun buildCombinedText(texts: List<String>): String {
        return texts.joinToString("\n")
    }
}
