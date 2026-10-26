package com.browseruse.android

import com.browseruse.android.util.ToolLabels
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.junit.Assert.assertEquals
import org.junit.Test

class ToolLabelsTest {

    @Test
    fun `formats navigation params`() {
        val params = buildJsonObject { put("url", "https://example.com") }
        assertEquals("Navigated", ToolLabels.label("navigate"))
        assertEquals("https://example.com", ToolLabels.value("navigate", params))
    }

    @Test
    fun `formats input params`() {
        val params = buildJsonObject {
            put("index", 4)
            put("text", "hello")
        }
        assertEquals("4  hello", ToolLabels.value("input", params))
    }

    @Test
    fun `falls back to a readable label`() {
        assertEquals("Custom tool", ToolLabels.label("custom_tool"))
    }
}
