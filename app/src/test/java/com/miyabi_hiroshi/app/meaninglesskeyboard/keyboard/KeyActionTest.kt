// SPDX-License-Identifier: GPL-3.0-or-later
package com.miyabi_hiroshi.app.meaninglesskeyboard.keyboard

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class KeyActionTest {

    @Test
    fun `fromKeyDef with output returns CommitText`() {
        val keyDef = KeyDef(label = "a", output = "a")
        val action = KeyAction.fromKeyDef(keyDef)
        assertEquals(KeyAction.CommitText("a"), action)
    }

    @Test
    fun `fromKeyDef with keycode returns Keycode`() {
        val keyDef = KeyDef(label = "BS", keycode = "BACKSPACE")
        val action = KeyAction.fromKeyDef(keyDef)
        assertEquals(KeyAction.Keycode("BACKSPACE"), action)
    }

    @Test
    fun `fromKeyDef with switchLayout returns SwitchLayout`() {
        val keyDef = KeyDef(label = "123", switchLayout = "numbers")
        val action = KeyAction.fromKeyDef(keyDef)
        assertEquals(KeyAction.SwitchLayout("numbers"), action)
    }

    @Test
    fun `fromKeyDef with no action returns null`() {
        val keyDef = KeyDef(label = "empty")
        val action = KeyAction.fromKeyDef(keyDef)
        assertNull(action)
    }

    @Test
    fun `fromKeyDef prioritizes output over keycode and switchLayout`() {
        val keyDef = KeyDef(label = "a", output = "a", keycode = "ENTER", switchLayout = "num")
        val action = KeyAction.fromKeyDef(keyDef)
        assertEquals(KeyAction.CommitText("a"), action)
    }

    @Test
    fun `ALLOWED_KEYCODES contains expected set`() {
        val expected = setOf("BACKSPACE", "ENTER", "SHIFT", "TAB", "ARROW_LEFT", "ARROW_RIGHT")
        assertEquals(expected, KeyAction.ALLOWED_KEYCODES)
    }

    @Test
    fun `ALLOWED_KEYCODES does not contain arbitrary strings`() {
        assertTrue("DELETE" !in KeyAction.ALLOWED_KEYCODES)
        assertTrue("SPACE" !in KeyAction.ALLOWED_KEYCODES)
    }
}
