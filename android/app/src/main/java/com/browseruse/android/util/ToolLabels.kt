package com.browseruse.android.util

import kotlinx.serialization.json.JsonObject

object ToolLabels {

    fun label(name: String): String = when (name) {
        "navigate" -> "Navigated"
        "search" -> "Searched"
        "go_back" -> "Went back"
        "wait" -> "Waited"
        "click" -> "Clicked"
        "input" -> "Typed"
        "upload_file" -> "Uploaded file"
        "switch" -> "Switched tab"
        "close" -> "Closed tab"
        "extract" -> "Extracted"
        "search_page" -> "Searched page"
        "find_elements" -> "Found elements"
        "scroll" -> "Scrolled"
        "send_keys" -> "Sent keys"
        "find_text" -> "Found text"
        "screenshot" -> "Took screenshot"
        "save_as_pdf" -> "Saved PDF"
        "dropdown_options" -> "Read options"
        "select_dropdown" -> "Selected option"
        "write_file" -> "Wrote file"
        "replace_file" -> "Edited file"
        "read_file" -> "Read file"
        "evaluate" -> "Evaluated JS"
        "done" -> "Finished"
        else -> name.replace('_', ' ').replaceFirstChar { it.uppercase() }
    }

    fun value(name: String, params: JsonObject?): String {
        if (params == null) return ""
        val raw = when (name) {
            "navigate" -> params.text("url")
            "search" -> params.text("query")
            "go_back", "wait", "screenshot", "done" -> params.text("text")
            "click" -> params.text("index")
            "input" -> listOfNotNull(params.text("index"), params.text("text")).joinToString("  ")
            "upload_file" -> listOfNotNull(params.text("index"), params.text("path")).joinToString("  ")
            "switch", "close" -> params.text("tab_id")
            "extract" -> params.text("query")
            "search_page" -> params.text("pattern")
            "find_elements" -> params.text("selector")
            "scroll" -> params.text("pages")
            "send_keys" -> params.text("keys")
            "find_text" -> params.text("text")
            "save_as_pdf" -> params.text("file_name")
            "dropdown_options" -> params.text("index")
            "select_dropdown" -> listOfNotNull(params.text("index"), params.text("text")).joinToString("  ")
            "write_file", "replace_file", "read_file" -> params.text("file_name")
            "evaluate" -> params.text("code")
            else -> params.entries.firstOrNull()?.value?.toString()?.trim('"')
        }.orEmpty()
        return if (raw.length > 80) raw.take(77) + "..." else raw
    }

    private fun JsonObject.text(key: String): String? = runCatching {
        this[key]?.toString()?.trim('"')
    }.getOrNull()?.takeUnless { it.isBlank() || it == "null" }
}
