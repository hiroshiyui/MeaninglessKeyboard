package com.miyabi_hiroshi.app.meaninglesskeyboard.keyboard

import com.miyabi_hiroshi.app.meaninglesskeyboard.db.KeyboardLayoutEntity
import com.miyabi_hiroshi.app.meaninglesskeyboard.db.KeyboardRepository
import com.miyabi_hiroshi.app.meaninglesskeyboard.db.PackWithLayouts
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json

data class KeyboardDisplayState(
    val packName: String = "",
    val packIndex: Int = 0,
    val packCount: Int = 0,
    val currentLayout: KeyboardLayoutDef? = null,
    val currentLayoutName: String = "",
    val isShifted: Boolean = false
)

class KeyboardState(private val repository: KeyboardRepository) {

    private val json = Json { ignoreUnknownKeys = true }

    private var packs: List<PackWithLayouts> = emptyList()
    private var currentPackIndex = 0
    private var currentLayoutName = ""
    private var isShifted = false

    private val _displayState = MutableStateFlow(KeyboardDisplayState())
    val displayState: StateFlow<KeyboardDisplayState> = _displayState.asStateFlow()

    suspend fun loadPacks() {
        packs = repository.getEnabledPacksWithLayouts()
        if (packs.isNotEmpty()) {
            currentPackIndex = 0
            switchToDefaultLayout()
        }
    }

    fun swipeLeft() {
        if (packs.isEmpty()) return
        currentPackIndex = (currentPackIndex + 1) % packs.size
        isShifted = false
        switchToDefaultLayout()
    }

    fun swipeRight() {
        if (packs.isEmpty()) return
        currentPackIndex = (currentPackIndex - 1 + packs.size) % packs.size
        isShifted = false
        switchToDefaultLayout()
    }

    fun switchSubLayout(layoutName: String) {
        val pack = packs.getOrNull(currentPackIndex) ?: return
        val layout = pack.layouts.find { it.layoutName == layoutName }
        if (layout != null) {
            currentLayoutName = layoutName
            updateDisplayState(layout)
        }
    }

    fun toggleShift() {
        if (packs.isEmpty()) return
        isShifted = !isShifted
        val pack = packs[currentPackIndex]
        val targetLayoutName = if (isShifted) "upper" else "lower"
        val layout = pack.layouts.find { it.layoutName == targetLayoutName }
        if (layout != null) {
            currentLayoutName = targetLayoutName
            updateDisplayState(layout)
        } else {
            updateDisplayState(pack.layouts.find { it.layoutName == currentLayoutName })
        }
    }

    fun resetShift() {
        if (!isShifted) return
        isShifted = false
        val pack = packs.getOrNull(currentPackIndex) ?: return
        val lowerLayout = pack.layouts.find { it.layoutName == "lower" }
        if (lowerLayout != null) {
            currentLayoutName = "lower"
            updateDisplayState(lowerLayout)
        }
    }

    private fun switchToDefaultLayout() {
        val pack = packs.getOrNull(currentPackIndex) ?: return
        val defaultLayout = pack.layouts.find { it.isDefault }
            ?: pack.layouts.firstOrNull()
        if (defaultLayout != null) {
            currentLayoutName = defaultLayout.layoutName
            updateDisplayState(defaultLayout)
        }
    }

    private fun updateDisplayState(layout: KeyboardLayoutEntity?) {
        val parsedLayout = layout?.let {
            try {
                json.decodeFromString<KeyboardLayoutDef>(it.layoutJson)
            } catch (_: Exception) {
                null
            }
        }
        val pack = packs.getOrNull(currentPackIndex)
        _displayState.value = KeyboardDisplayState(
            packName = pack?.pack?.name ?: "",
            packIndex = currentPackIndex,
            packCount = packs.size,
            currentLayout = parsedLayout,
            currentLayoutName = currentLayoutName,
            isShifted = isShifted
        )
    }
}
