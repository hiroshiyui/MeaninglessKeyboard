package com.miyabi_hiroshi.app.meaninglesskeyboard.db

import android.content.Context
import com.miyabi_hiroshi.app.meaninglesskeyboard.keyboard.KeyboardLayoutDef
import com.miyabi_hiroshi.app.meaninglesskeyboard.keyboard.KeyboardPack
import com.miyabi_hiroshi.app.meaninglesskeyboard.keyboard.KeyboardPackSanitizer
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

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
        if (dao.getPackCount() > 0) return

        val assetFiles = context.assets.list("keyboards") ?: return
        var sortOrder = 0
        for (fileName in assetFiles.sorted()) {
            if (!fileName.endsWith(".json")) continue
            val jsonString = context.assets.open("keyboards/$fileName").bufferedReader().readText()
            val sanitized = KeyboardPackSanitizer.sanitize(jsonString)
            if (sanitized.isSuccess) {
                insertPack(sanitized.getOrThrow(), isBuiltin = true, sortOrder = sortOrder)
                sortOrder++
            }
        }
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
