package com.browseruse.android.util

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

fun JsonObject.str(key: String): String? {
    val element = this[key] ?: return null
    if (element is JsonNull) return null
    return runCatching { element.jsonPrimitive.content }.getOrNull()
}

fun JsonObject.int(key: String): Int? {
    val element = this[key] ?: return null
    if (element is JsonNull) return null
    return runCatching { element.jsonPrimitive.intOrNull }.getOrNull()
}

fun JsonObject.bool(key: String): Boolean? {
    val element = this[key] ?: return null
    if (element is JsonNull) return null
    return runCatching { element.jsonPrimitive.booleanOrNull }.getOrNull()
}

fun JsonObject.double(key: String): Double? {
    val element = this[key] ?: return null
    if (element is JsonNull) return null
    return runCatching { element.jsonPrimitive.doubleOrNull }.getOrNull()
}

fun JsonObject.obj(key: String): JsonObject? {
    val element = this[key] ?: return null
    if (element is JsonNull) return null
    return runCatching { element.jsonObject }.getOrNull()
}

fun JsonObject.arr(key: String): JsonArray? {
    val element = this[key] ?: return null
    if (element is JsonNull) return null
    return runCatching { element.jsonArray }.getOrNull()
}

fun JsonObject.primitive(key: String): String? {
    val element = this[key] ?: return null
    if (element is JsonNull) return null
    return runCatching { (element as JsonPrimitive).content }.getOrNull()
}
