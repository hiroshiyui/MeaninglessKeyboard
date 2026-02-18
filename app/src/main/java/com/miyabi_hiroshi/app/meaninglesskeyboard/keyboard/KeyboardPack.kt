package com.miyabi_hiroshi.app.meaninglesskeyboard.keyboard

import kotlinx.serialization.Serializable

@Serializable
data class KeyboardPack(
    val name: String,
    val author: String = "",
    val version: Int = 1,
    val defaultLayout: String,
    val layouts: Map<String, KeyboardLayoutDef>
)

@Serializable
data class KeyboardLayoutDef(
    val rows: List<KeyRow>
)

@Serializable
data class KeyRow(
    val keys: List<KeyDef>
)

@Serializable
data class KeyDef(
    val label: String,
    val output: String? = null,
    val keycode: String? = null,
    val switchLayout: String? = null,
    val width: Double = 1.0,
    val repeatable: Boolean = false
)
