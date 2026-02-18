package com.miyabi_hiroshi.app.meaninglesskeyboard.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "keyboard_layouts",
    foreignKeys = [
        ForeignKey(
            entity = KeyboardPackEntity::class,
            parentColumns = ["id"],
            childColumns = ["pack_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("pack_id")]
)
data class KeyboardLayoutEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "pack_id")
    val packId: Long,
    @ColumnInfo(name = "layout_name")
    val layoutName: String,
    @ColumnInfo(name = "is_default")
    val isDefault: Boolean = false,
    @ColumnInfo(name = "layout_json")
    val layoutJson: String
)
