package com.miyabi_hiroshi.app.meaninglesskeyboard.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "keyboard_packs")
data class KeyboardPackEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val author: String = "",
    val version: Int = 1,
    @ColumnInfo(name = "is_builtin")
    val isBuiltin: Boolean = false,
    @ColumnInfo(name = "is_enabled")
    val isEnabled: Boolean = true,
    @ColumnInfo(name = "sort_order")
    val sortOrder: Int = 0
)
