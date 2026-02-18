package com.miyabi_hiroshi.app.meaninglesskeyboard.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

data class PackWithLayouts(
    val pack: KeyboardPackEntity,
    val layouts: List<KeyboardLayoutEntity>
)

@Dao
interface KeyboardDao {

    @Insert
    suspend fun insertPack(pack: KeyboardPackEntity): Long

    @Insert
    suspend fun insertLayout(layout: KeyboardLayoutEntity)

    @Insert
    suspend fun insertLayouts(layouts: List<KeyboardLayoutEntity>)

    @Query("SELECT * FROM keyboard_packs ORDER BY sort_order ASC")
    fun getAllPacksFlow(): Flow<List<KeyboardPackEntity>>

    @Query("SELECT * FROM keyboard_packs WHERE is_enabled = 1 ORDER BY sort_order ASC")
    fun getEnabledPacksFlow(): Flow<List<KeyboardPackEntity>>

    @Query("SELECT * FROM keyboard_packs WHERE is_enabled = 1 ORDER BY sort_order ASC")
    suspend fun getEnabledPacks(): List<KeyboardPackEntity>

    @Query("SELECT * FROM keyboard_layouts WHERE pack_id = :packId")
    suspend fun getLayoutsForPack(packId: Long): List<KeyboardLayoutEntity>

    @Query("SELECT COUNT(*) FROM keyboard_packs")
    suspend fun getPackCount(): Int

    @Query("SELECT COUNT(*) FROM keyboard_packs WHERE is_enabled = 1")
    suspend fun getEnabledPackCount(): Int

    @Query("UPDATE keyboard_packs SET is_enabled = :enabled WHERE id = :packId")
    suspend fun setPackEnabled(packId: Long, enabled: Boolean)

    @Query("UPDATE keyboard_packs SET sort_order = :sortOrder WHERE id = :packId")
    suspend fun updateSortOrder(packId: Long, sortOrder: Int)

    @Query("DELETE FROM keyboard_packs WHERE id = :packId AND is_builtin = 0")
    suspend fun deletePack(packId: Long)

    @Query("DELETE FROM keyboard_packs WHERE is_builtin = 1")
    suspend fun deleteBuiltinPacks()

    @Query("SELECT MAX(sort_order) FROM keyboard_packs")
    suspend fun getMaxSortOrder(): Int?

    @Query("SELECT * FROM keyboard_packs WHERE id = :packId")
    suspend fun getPackById(packId: Long): KeyboardPackEntity?

    @Transaction
    suspend fun getPackWithLayouts(packId: Long): PackWithLayouts? {
        val pack = getPackById(packId) ?: return null
        val layouts = getLayoutsForPack(packId)
        return PackWithLayouts(pack, layouts)
    }

    @Transaction
    suspend fun getAllEnabledPacksWithLayouts(): List<PackWithLayouts> {
        return getEnabledPacks().map { pack ->
            PackWithLayouts(pack, getLayoutsForPack(pack.id))
        }
    }
}
