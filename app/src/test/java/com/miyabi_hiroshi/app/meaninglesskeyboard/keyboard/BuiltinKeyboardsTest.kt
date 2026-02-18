// SPDX-License-Identifier: GPL-3.0-or-later
package com.miyabi_hiroshi.app.meaninglesskeyboard.keyboard

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class BuiltinKeyboardsTest {

    private val keyboardsDir = File("src/main/assets/keyboards")

    @Test
    fun `bopomofo_dachen json passes sanitization`() {
        val bytes = File(keyboardsDir, "bopomofo_dachen.json").readBytes()
        val result = KeyboardPackSanitizer.sanitize(bytes)
        assertTrue("bopomofo_dachen.json failed: ${result.exceptionOrNull()?.message}", result.isSuccess)
    }

    @Test
    fun `emoji json passes sanitization`() {
        val bytes = File(keyboardsDir, "emoji.json").readBytes()
        val result = KeyboardPackSanitizer.sanitize(bytes)
        assertTrue("emoji.json failed: ${result.exceptionOrNull()?.message}", result.isSuccess)
    }

    @Test
    fun `en_qwerty json passes sanitization`() {
        val bytes = File(keyboardsDir, "en_qwerty.json").readBytes()
        val result = KeyboardPackSanitizer.sanitize(bytes)
        assertTrue("en_qwerty.json failed: ${result.exceptionOrNull()?.message}", result.isSuccess)
    }

    @Test
    fun `number_pad json passes sanitization`() {
        val bytes = File(keyboardsDir, "number_pad.json").readBytes()
        val result = KeyboardPackSanitizer.sanitize(bytes)
        assertTrue("number_pad.json failed: ${result.exceptionOrNull()?.message}", result.isSuccess)
    }

    @Test
    fun `all keyboard json files in assets directory pass sanitization`() {
        val files = keyboardsDir.listFiles { file -> file.extension == "json" }
        assertTrue("No JSON files found in $keyboardsDir", files != null && files.isNotEmpty())
        for (file in files!!) {
            val result = KeyboardPackSanitizer.sanitize(file.readBytes())
            assertTrue("${file.name} failed: ${result.exceptionOrNull()?.message}", result.isSuccess)
        }
    }
}
