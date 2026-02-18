package com.miyabi_hiroshi.app.meaninglesskeyboard.keyboard

sealed class KeyAction {
    data class CommitText(val text: String) : KeyAction()
    data class Keycode(val code: String) : KeyAction()
    data class SwitchLayout(val layoutName: String) : KeyAction()

    companion object {
        val ALLOWED_KEYCODES = setOf(
            "BACKSPACE", "ENTER", "SHIFT", "TAB", "ARROW_LEFT", "ARROW_RIGHT"
        )

        fun fromKeyDef(keyDef: KeyDef): KeyAction? = when {
            keyDef.output != null -> CommitText(keyDef.output)
            keyDef.keycode != null -> Keycode(keyDef.keycode)
            keyDef.switchLayout != null -> SwitchLayout(keyDef.switchLayout)
            else -> null
        }
    }
}
