package com.miyabi_hiroshi.app.meaninglesskeyboard

import android.inputmethodservice.InputMethodService
import android.view.KeyEvent
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.miyabi_hiroshi.app.meaninglesskeyboard.db.AppDatabase
import com.miyabi_hiroshi.app.meaninglesskeyboard.db.KeyboardRepository
import com.miyabi_hiroshi.app.meaninglesskeyboard.feedback.FeedbackManager
import com.miyabi_hiroshi.app.meaninglesskeyboard.keyboard.KeyAction
import com.miyabi_hiroshi.app.meaninglesskeyboard.keyboard.KeyDef
import com.miyabi_hiroshi.app.meaninglesskeyboard.keyboard.KeyboardState
import com.miyabi_hiroshi.app.meaninglesskeyboard.ui.KeyboardView
import com.miyabi_hiroshi.app.meaninglesskeyboard.ui.theme.MeaninglessKeyboardTheme
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class MeaninglessKeyboardService : InputMethodService(), LifecycleOwner, SavedStateRegistryOwner {

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val savedStateRegistry get() = savedStateRegistryController.savedStateRegistry

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private lateinit var repository: KeyboardRepository
    private lateinit var keyboardState: KeyboardState
    private lateinit var feedbackManager: FeedbackManager
    private var inputView: View? = null
    private val seedingComplete = CompletableDeferred<Unit>()

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)

        val dao = AppDatabase.getInstance(this).keyboardDao()
        repository = KeyboardRepository(dao)
        keyboardState = KeyboardState(repository)
        feedbackManager = FeedbackManager(this)

        serviceScope.launch {
            try {
                repository.seedBuiltinPacks(this@MeaninglessKeyboardService)
                keyboardState.loadPacks()
            } finally {
                seedingComplete.complete(Unit)
            }
        }
    }

    override fun onEvaluateInputViewShown(): Boolean = true

    override fun onCreateInputView(): View {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

        // Install lifecycle/savedstate owners on the IME window's decor view
        // so Compose can find them when walking up the view tree.
        window?.window?.decorView?.let { decorView ->
            decorView.setViewTreeLifecycleOwner(this@MeaninglessKeyboardService)
            decorView.setViewTreeSavedStateRegistryOwner(this@MeaninglessKeyboardService)
        }

        val composeView = ComposeView(this).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                KeyboardContent()
            }
        }
        inputView = composeView

        serviceScope.launch {
            seedingComplete.await()
            keyboardState.loadPacks()
        }

        return composeView
    }

    @Composable
    private fun KeyboardContent() {
        MeaninglessKeyboardTheme {
            KeyboardView(
                keyboardState = keyboardState,
                onKeyAction = { keyDef -> handleKeyAction(keyDef) },
                onKeyReleased = { keyDef -> handleKeyReleased(keyDef) }
            )
        }
    }

    private fun handleKeyAction(keyDef: KeyDef) {
        feedbackManager.performKeyPressFeedback(inputView)

        val action = KeyAction.fromKeyDef(keyDef) ?: return
        val ic = currentInputConnection ?: return

        when (action) {
            is KeyAction.CommitText -> {
                ic.commitText(action.text, 1)
            }
            is KeyAction.Keycode -> {
                when (action.code) {
                    "BACKSPACE" -> {
                        ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL))
                        ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL))
                    }
                    "ENTER" -> {
                        ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
                        ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
                    }
                    "SHIFT" -> keyboardState.toggleShift()
                    "TAB" -> {
                        ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_TAB))
                        ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_TAB))
                    }
                    "ARROW_LEFT" -> {
                        ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_LEFT))
                        ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DPAD_LEFT))
                    }
                    "ARROW_RIGHT" -> {
                        ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_RIGHT))
                        ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DPAD_RIGHT))
                    }
                }
            }
            is KeyAction.SwitchLayout -> {
                keyboardState.switchSubLayout(action.layoutName)
            }
        }
    }

    private fun handleKeyReleased(keyDef: KeyDef) {
        // Reset shift after release so the preview stays visible during the press
        if (keyDef.output != null) {
            keyboardState.resetShift()
        }
    }

    override fun onDestroy() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        serviceScope.cancel()
        super.onDestroy()
    }
}
