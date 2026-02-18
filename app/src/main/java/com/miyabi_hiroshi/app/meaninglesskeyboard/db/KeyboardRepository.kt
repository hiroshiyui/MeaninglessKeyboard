package com.miyabi_hiroshi.app.meaninglesskeyboard.db

import android.content.Context
import com.miyabi_hiroshi.app.meaninglesskeyboard.keyboard.KeyboardLayoutDef
import com.miyabi_hiroshi.app.meaninglesskeyboard.keyboard.KeyboardPack
import com.miyabi_hiroshi.app.meaninglesskeyboard.keyboard.KeyboardPackSanitizer
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.security.MessageDigest

class KeyboardRepository(private val dao: KeyboardDao) {

    private val json = Json { ignoreUnknownKeys = true }

    fun getAllPacksFlow(): Flow<List<KeyboardPackEntity>> = dao.getAllPacksFlow()

    fun getEnabledPacksFlow(): Flow<List<KeyboardPackEntity>> = dao.getEnabledPacksFlow()

    suspend fun getEnabledPacksWithLayouts(): List<PackWithLayouts> =
        dao.getAllEnabledPacksWithLayouts()

    suspend fun importPack(jsonString: String): Result<Long> {
        val sanitized = KeyboardPackSanitizer.sanitize(jsonString)
        if (sanitized.isFailure) {
            return Result.failure(sanitized.exceptionOrNull()!!)
        }
        val pack = sanitized.getOrThrow()
        return Result.success(insertPack(pack, isBuiltin = false))
    }

    suspend fun toggleEnabled(packId: Long, enabled: Boolean): Result<Unit> {
        if (!enabled) {
            val enabledCount = dao.getEnabledPackCount()
            if (enabledCount <= 1) {
                return Result.failure(IllegalStateException("At least one pack must remain enabled"))
            }
        }
        dao.setPackEnabled(packId, enabled)
        return Result.success(Unit)
    }

    suspend fun updateSortOrder(packId: Long, newOrder: Int) {
        dao.updateSortOrder(packId, newOrder)
    }

    suspend fun deletePack(packId: Long): Result<Unit> {
        val pack = dao.getPackById(packId)
            ?: return Result.failure(IllegalArgumentException("Pack not found"))
        if (pack.isBuiltin) {
            return Result.failure(IllegalStateException("Cannot delete built-in packs"))
        }
        dao.deletePack(packId)
        return Result.success(Unit)
    }

    suspend fun seedBuiltinPacks(context: Context) {
        val currentChecksum = computeAssetsChecksum(context)
        val prefs = context.getSharedPreferences("builtin_keyboards", Context.MODE_PRIVATE)
        val storedChecksum = prefs.getString("assets_checksum", null)

        if (currentChecksum == storedChecksum) return

        dao.deleteBuiltinPacks()

        val baseSortOrder = (dao.getMaxSortOrder() ?: -1) + 1
        val assetFiles = context.assets.list("keyboards") ?: return
        var sortOrder = baseSortOrder
        for (fileName in assetFiles.sorted()) {
            if (!fileName.endsWith(".json")) continue
            val jsonString = context.assets.open("keyboards/$fileName").bufferedReader().readText()
            val sanitized = KeyboardPackSanitizer.sanitize(jsonString)
            if (sanitized.isSuccess) {
                insertPack(sanitized.getOrThrow(), isBuiltin = true, sortOrder = sortOrder)
                sortOrder++
            }
        }

        prefs.edit().putString("assets_checksum", currentChecksum).apply()
    }

    private fun computeAssetsChecksum(context: Context): String {
        val md = MessageDigest.getInstance("SHA-256")
        val files = context.assets.list("keyboards")?.sorted() ?: return ""
        for (fileName in files) {
            if (!fileName.endsWith(".json")) continue
            md.update(fileName.toByteArray())
            context.assets.open("keyboards/$fileName").use { md.update(it.readBytes()) }
        }
        return md.digest().joinToString("") { "%02x".format(it) }
    }

    suspend fun getLayoutsForPack(packId: Long): List<KeyboardLayoutEntity> =
        dao.getLayoutsForPack(packId)

    fun parseLayoutJson(layoutJson: String): KeyboardLayoutDef =
        json.decodeFromString(layoutJson)

    private suspend fun insertPack(
        pack: KeyboardPack,
        isBuiltin: Boolean,
        sortOrder: Int? = null
    ): Long {
        val order = sortOrder ?: ((dao.getMaxSortOrder() ?: -1) + 1)
        val packId = dao.insertPack(
            KeyboardPackEntity(
                name = pack.name,
                author = pack.author,
                version = pack.version,
                isBuiltin = isBuiltin,
                isEnabled = true,
                sortOrder = order
            )
        )
        val layoutEntities = pack.layouts.map { (layoutName, layoutDef) ->
            KeyboardLayoutEntity(
                packId = packId,
                layoutName = layoutName,
                isDefault = layoutName == pack.defaultLayout,
                layoutJson = json.encodeToString(layoutDef)
            )
        }
        dao.insertLayouts(layoutEntities)
        return packId
    }
}
