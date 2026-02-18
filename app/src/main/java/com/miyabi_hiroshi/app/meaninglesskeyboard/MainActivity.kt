package com.miyabi_hiroshi.app.meaninglesskeyboard

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.miyabi_hiroshi.app.meaninglesskeyboard.db.AppDatabase
import com.miyabi_hiroshi.app.meaninglesskeyboard.db.KeyboardPackEntity
import com.miyabi_hiroshi.app.meaninglesskeyboard.db.KeyboardRepository
import com.miyabi_hiroshi.app.meaninglesskeyboard.ui.theme.MeaninglessKeyboardTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var repository: KeyboardRepository

    private val importLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) return@registerForActivityResult
        try {
            val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() }
            if (bytes == null) {
                Toast.makeText(this, "Failed to read file", Toast.LENGTH_SHORT).show()
                return@registerForActivityResult
            }
            if (bytes.size > 512 * 1024) {
                Toast.makeText(this, "File too large (max 512 KB)", Toast.LENGTH_SHORT).show()
                return@registerForActivityResult
            }
            val jsonString = bytes.decodeToString()
            kotlinx.coroutines.MainScope().launch {
                val result = repository.importPack(jsonString)
                result.onSuccess {
                    Toast.makeText(this@MainActivity, "Layout imported successfully", Toast.LENGTH_SHORT).show()
                }.onFailure { e ->
                    Toast.makeText(this@MainActivity, "Import failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val dao = AppDatabase.getInstance(this).keyboardDao()
        repository = KeyboardRepository(dao)

        enableEdgeToEdge()
        setContent {
            MeaninglessKeyboardTheme {
                SettingsScreen(
                    repository = repository,
                    onImportClick = { importLauncher.launch(arrayOf("application/json", "*/*")) },
                    onOpenImeSettings = {
                        startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsScreen(
    repository: KeyboardRepository,
    onImportClick: () -> Unit,
    onOpenImeSettings: () -> Unit
) {
    val packs by repository.getAllPacksFlow().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Meaningless Keyboard") })
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = "Enable this keyboard in system settings, then select it as your input method.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(onClick = onOpenImeSettings, modifier = Modifier.weight(1f)) {
                    Text("Input Method Settings")
                }
                OutlinedButton(onClick = onImportClick, modifier = Modifier.weight(1f)) {
                    Text("Import Layout")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Keyboard Packs",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                itemsIndexed(packs, key = { _, pack -> pack.id }) { index, pack ->
                    PackCard(
                        pack = pack,
                        isFirst = index == 0,
                        isLast = index == packs.lastIndex,
                        onToggleEnabled = { enabled ->
                            scope.launch {
                                repository.toggleEnabled(pack.id, enabled)
                            }
                        },
                        onMoveUp = {
                            if (index > 0) {
                                scope.launch {
                                    val prev = packs[index - 1]
                                    repository.updateSortOrder(pack.id, prev.sortOrder)
                                    repository.updateSortOrder(prev.id, pack.sortOrder)
                                }
                            }
                        },
                        onMoveDown = {
                            if (index < packs.lastIndex) {
                                scope.launch {
                                    val next = packs[index + 1]
                                    repository.updateSortOrder(pack.id, next.sortOrder)
                                    repository.updateSortOrder(next.id, pack.sortOrder)
                                }
                            }
                        },
                        onDelete = {
                            scope.launch {
                                repository.deletePack(pack.id)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun PackCard(
    pack: KeyboardPackEntity,
    isFirst: Boolean,
    isLast: Boolean,
    onToggleEnabled: (Boolean) -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = pack.name,
                        style = MaterialTheme.typography.titleSmall
                    )
                    if (pack.author.isNotBlank()) {
                        Text(
                            text = "by ${pack.author}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (pack.isBuiltin) {
                        Text(
                            text = "Built-in",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Switch(
                    checked = pack.isEnabled,
                    onCheckedChange = onToggleEnabled
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                if (!isFirst) {
                    IconButton(onClick = onMoveUp) {
                        Text("\u2191")
                    }
                }
                if (!isLast) {
                    IconButton(onClick = onMoveDown) {
                        Text("\u2193")
                    }
                }
                if (!pack.isBuiltin) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onDelete,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Delete")
                    }
                }
            }
        }
    }
}
