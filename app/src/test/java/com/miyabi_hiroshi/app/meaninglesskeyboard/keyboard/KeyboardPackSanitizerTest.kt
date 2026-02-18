// SPDX-License-Identifier: GPL-3.0-or-later
package com.miyabi_hiroshi.app.meaninglesskeyboard.keyboard

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class KeyboardPackSanitizerTest {

    private val json = Json { ignoreUnknownKeys = true }

    private fun minimalPack(
        name: String = "Test",
        author: String = "Author",
        version: Int = 1,
        defaultLayout: String = "main",
        layouts: Map<String, KeyboardLayoutDef> = mapOf(
            "main" to KeyboardLayoutDef(
                rows = listOf(KeyRow(keys = listOf(KeyDef(label = "a", output = "a"))))
            )
        )
    ) = KeyboardPack(
        name = name,
        author = author,
        version = version,
        defaultLayout = defaultLayout,
        layouts = layouts
    )

    private fun packJson(pack: KeyboardPack): String = json.encodeToString(pack)

    // --- Valid packs ---

    @Test
    fun `minimal valid pack passes sanitization`() {
        val result = KeyboardPackSanitizer.sanitize(packJson(minimalPack()))
        assertTrue(result.isSuccess)
    }

    @Test
    fun `valid pack result is a KeyboardPack with correct fields`() {
        val pack = minimalPack()
        val result = KeyboardPackSanitizer.sanitize(packJson(pack)).getOrThrow()
        assertEquals("Test", result.name)
        assertEquals("Author", result.author)
        assertEquals(1, result.version)
        assertEquals("main", result.defaultLayout)
        assertEquals(1, result.layouts.size)
    }

    // --- Version ---

    @Test
    fun `reject version 0`() {
        val result = KeyboardPackSanitizer.sanitize(packJson(minimalPack(version = 0)))
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()!!.message!!.contains("version"))
    }

    @Test
    fun `reject version 2`() {
        val result = KeyboardPackSanitizer.sanitize(packJson(minimalPack(version = 2)))
        assertTrue(result.isFailure)
    }

    // --- File size ---

    @Test
    fun `reject file exceeding 512 KB`() {
        val hugeJson = " ".repeat(512 * 1024 + 1)
        val result = KeyboardPackSanitizer.sanitize(hugeJson.encodeToByteArray())
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()!!.message!!.contains("size"))
    }

    // --- Names ---

    @Test
    fun `reject pack name exceeding 64 chars`() {
        val result = KeyboardPackSanitizer.sanitize(packJson(minimalPack(name = "x".repeat(65))))
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()!!.message!!.contains("name"))
    }

    @Test
    fun `accept pack name at exactly 64 chars`() {
        val result = KeyboardPackSanitizer.sanitize(packJson(minimalPack(name = "x".repeat(64))))
        assertTrue(result.isSuccess)
    }

    @Test
    fun `reject author exceeding 64 chars`() {
        val result = KeyboardPackSanitizer.sanitize(packJson(minimalPack(author = "x".repeat(65))))
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()!!.message!!.contains("Author"))
    }

    // --- Layouts ---

    @Test
    fun `reject empty layouts`() {
        val result = KeyboardPackSanitizer.sanitize(
            packJson(minimalPack(layouts = emptyMap()))
        )
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()!!.message!!.contains("at least one layout"))
    }

    @Test
    fun `reject more than 10 layouts`() {
        val layouts = (1..11).associate { "layout$it" to KeyboardLayoutDef(
            rows = listOf(KeyRow(keys = listOf(KeyDef(label = "a", output = "a"))))
        ) }
        val result = KeyboardPackSanitizer.sanitize(
            packJson(minimalPack(defaultLayout = "layout1", layouts = layouts))
        )
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()!!.message!!.contains("many layouts"))
    }

    @Test
    fun `reject missing defaultLayout`() {
        val result = KeyboardPackSanitizer.sanitize(
            packJson(minimalPack(defaultLayout = "nonexistent"))
        )
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()!!.message!!.contains("not found"))
    }

    // --- Rows ---

    @Test
    fun `reject more than 50 rows per layout`() {
        val rows = (1..51).map { KeyRow(keys = listOf(KeyDef(label = "a", output = "a"))) }
        val layouts = mapOf("main" to KeyboardLayoutDef(rows = rows))
        val result = KeyboardPackSanitizer.sanitize(packJson(minimalPack(layouts = layouts)))
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()!!.message!!.contains("many rows"))
    }

    // --- Keys ---

    @Test
    fun `reject more than 20 keys per row`() {
        val keys = (1..21).map { KeyDef(label = "a", output = "a") }
        val layouts = mapOf("main" to KeyboardLayoutDef(rows = listOf(KeyRow(keys = keys))))
        val result = KeyboardPackSanitizer.sanitize(packJson(minimalPack(layouts = layouts)))
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()!!.message!!.contains("many keys"))
    }

    @Test
    fun `reject key with no action`() {
        val layouts = mapOf(
            "main" to KeyboardLayoutDef(
                rows = listOf(KeyRow(keys = listOf(KeyDef(label = "x"))))
            )
        )
        val result = KeyboardPackSanitizer.sanitize(packJson(minimalPack(layouts = layouts)))
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()!!.message!!.contains("must have one of"))
    }

    @Test
    fun `reject key with multiple actions`() {
        val layouts = mapOf(
            "main" to KeyboardLayoutDef(
                rows = listOf(
                    KeyRow(keys = listOf(KeyDef(label = "x", output = "a", keycode = "ENTER")))
                )
            )
        )
        val result = KeyboardPackSanitizer.sanitize(packJson(minimalPack(layouts = layouts)))
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()!!.message!!.contains("only one"))
    }

    // --- Label / Output ---

    @Test
    fun `reject label exceeding 8 chars`() {
        val layouts = mapOf(
            "main" to KeyboardLayoutDef(
                rows = listOf(
                    KeyRow(keys = listOf(KeyDef(label = "x".repeat(9), output = "a")))
                )
            )
        )
        val result = KeyboardPackSanitizer.sanitize(packJson(minimalPack(layouts = layouts)))
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()!!.message!!.contains("label"))
    }

    @Test
    fun `reject output exceeding 32 chars`() {
        val layouts = mapOf(
            "main" to KeyboardLayoutDef(
                rows = listOf(
                    KeyRow(keys = listOf(KeyDef(label = "a", output = "x".repeat(33))))
                )
            )
        )
        val result = KeyboardPackSanitizer.sanitize(packJson(minimalPack(layouts = layouts)))
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()!!.message!!.contains("output"))
    }

    // --- Keycode ---

    @Test
    fun `reject unknown keycode`() {
        val layouts = mapOf(
            "main" to KeyboardLayoutDef(
                rows = listOf(
                    KeyRow(keys = listOf(KeyDef(label = "?", keycode = "UNKNOWN")))
                )
            )
        )
        val result = KeyboardPackSanitizer.sanitize(packJson(minimalPack(layouts = layouts)))
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()!!.message!!.contains("unknown keycode"))
    }

    @Test
    fun `accept valid keycode`() {
        val layouts = mapOf(
            "main" to KeyboardLayoutDef(
                rows = listOf(
                    KeyRow(keys = listOf(KeyDef(label = "BS", keycode = "BACKSPACE")))
                )
            )
        )
        val result = KeyboardPackSanitizer.sanitize(packJson(minimalPack(layouts = layouts)))
        assertTrue(result.isSuccess)
    }

    // --- SwitchLayout ---

    @Test
    fun `reject switchLayout referencing non-existent layout`() {
        val layouts = mapOf(
            "main" to KeyboardLayoutDef(
                rows = listOf(
                    KeyRow(keys = listOf(KeyDef(label = "123", switchLayout = "numbers")))
                )
            )
        )
        val result = KeyboardPackSanitizer.sanitize(packJson(minimalPack(layouts = layouts)))
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()!!.message!!.contains("unknown layout"))
    }

    @Test
    fun `accept switchLayout referencing existing layout`() {
        val layouts = mapOf(
            "main" to KeyboardLayoutDef(
                rows = listOf(
                    KeyRow(keys = listOf(KeyDef(label = "123", switchLayout = "numbers")))
                )
            ),
            "numbers" to KeyboardLayoutDef(
                rows = listOf(
                    KeyRow(keys = listOf(KeyDef(label = "abc", switchLayout = "main")))
                )
            )
        )
        val result = KeyboardPackSanitizer.sanitize(packJson(minimalPack(layouts = layouts)))
        assertTrue(result.isSuccess)
    }

    // --- Width ---

    @Test
    fun `reject width below 0_5`() {
        val layouts = mapOf(
            "main" to KeyboardLayoutDef(
                rows = listOf(
                    KeyRow(keys = listOf(KeyDef(label = "a", output = "a", width = 0.4)))
                )
            )
        )
        val result = KeyboardPackSanitizer.sanitize(packJson(minimalPack(layouts = layouts)))
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()!!.message!!.contains("width"))
    }

    @Test
    fun `reject width above 10`() {
        val layouts = mapOf(
            "main" to KeyboardLayoutDef(
                rows = listOf(
                    KeyRow(keys = listOf(KeyDef(label = "a", output = "a", width = 10.1)))
                )
            )
        )
        val result = KeyboardPackSanitizer.sanitize(packJson(minimalPack(layouts = layouts)))
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()!!.message!!.contains("width"))
    }

    // --- JSON ---

    @Test
    fun `reject malformed JSON`() {
        val result = KeyboardPackSanitizer.sanitize("{not valid json!!!")
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()!!.message!!.contains("JSON"))
    }

    // --- Round-trip ---

    @Test
    fun `valid pack round-trips cleanly`() {
        val original = minimalPack()
        val firstPass = KeyboardPackSanitizer.sanitize(packJson(original)).getOrThrow()
        val secondPass = KeyboardPackSanitizer.sanitize(packJson(firstPass)).getOrThrow()
        assertEquals(firstPass, secondPass)
    }
}
