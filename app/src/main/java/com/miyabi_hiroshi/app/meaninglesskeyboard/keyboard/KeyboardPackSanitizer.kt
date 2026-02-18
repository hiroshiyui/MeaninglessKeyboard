package com.miyabi_hiroshi.app.meaninglesskeyboard.keyboard

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object KeyboardPackSanitizer {

    private const val MAX_FILE_SIZE = 512 * 1024 // 512 KB
    private const val MAX_NAME_LENGTH = 64
    private const val MAX_AUTHOR_LENGTH = 64
    private const val MAX_LAYOUTS = 10
    private const val MAX_ROWS_PER_LAYOUT = 8
    private const val MAX_KEYS_PER_ROW = 20
    private const val MAX_LABEL_LENGTH = 8
    private const val MAX_OUTPUT_LENGTH = 32
    private const val MIN_WIDTH = 0.5
    private const val MAX_WIDTH = 10.0

    private val json = Json { ignoreUnknownKeys = true }

    fun sanitize(rawBytes: ByteArray): Result<KeyboardPack> {
        if (rawBytes.size > MAX_FILE_SIZE) {
            return Result.failure(IllegalArgumentException("File size exceeds ${MAX_FILE_SIZE / 1024} KB limit"))
        }

        val jsonString = rawBytes.decodeToString()

        val pack = try {
            json.decodeFromString<KeyboardPack>(jsonString)
        } catch (e: Exception) {
            return Result.failure(IllegalArgumentException("Invalid JSON format: ${e.message}"))
        }

        return validate(pack)
    }

    fun sanitize(jsonString: String): Result<KeyboardPack> {
        return sanitize(jsonString.encodeToByteArray())
    }

    private fun validate(pack: KeyboardPack): Result<KeyboardPack> {
        if (pack.version != 1) {
            return Result.failure(IllegalArgumentException("Unsupported version: ${pack.version}. Only version 1 is supported"))
        }
        if (pack.name.length > MAX_NAME_LENGTH) {
            return Result.failure(IllegalArgumentException("Pack name exceeds $MAX_NAME_LENGTH characters"))
        }
        if (pack.author.length > MAX_AUTHOR_LENGTH) {
            return Result.failure(IllegalArgumentException("Author name exceeds $MAX_AUTHOR_LENGTH characters"))
        }
        if (pack.layouts.isEmpty()) {
            return Result.failure(IllegalArgumentException("Pack must contain at least one layout"))
        }
        if (pack.layouts.size > MAX_LAYOUTS) {
            return Result.failure(IllegalArgumentException("Too many layouts (max $MAX_LAYOUTS)"))
        }
        if (pack.defaultLayout !in pack.layouts) {
            return Result.failure(IllegalArgumentException("Default layout '${pack.defaultLayout}' not found in layouts"))
        }

        val layoutNames = pack.layouts.keys

        for ((layoutName, layout) in pack.layouts) {
            if (layout.rows.size > MAX_ROWS_PER_LAYOUT) {
                return Result.failure(IllegalArgumentException("Layout '$layoutName' has too many rows (max $MAX_ROWS_PER_LAYOUT)"))
            }
            for ((rowIndex, row) in layout.rows.withIndex()) {
                if (row.keys.size > MAX_KEYS_PER_ROW) {
                    return Result.failure(IllegalArgumentException("Layout '$layoutName' row $rowIndex has too many keys (max $MAX_KEYS_PER_ROW)"))
                }
                for ((keyIndex, key) in row.keys.withIndex()) {
                    val keyRef = "Layout '$layoutName' row $rowIndex key $keyIndex"
                    val keyResult = validateKey(key, keyRef, layoutNames)
                    if (keyResult.isFailure) return keyResult.map { pack }
                }
            }
        }

        return Result.success(reserialize(pack))
    }

    private fun validateKey(key: KeyDef, ref: String, layoutNames: Set<String>): Result<Unit> {
        if (key.label.length > MAX_LABEL_LENGTH) {
            return Result.failure(IllegalArgumentException("$ref: label exceeds $MAX_LABEL_LENGTH characters"))
        }

        val actionCount = listOfNotNull(key.output, key.keycode, key.switchLayout).size
        if (actionCount == 0) {
            return Result.failure(IllegalArgumentException("$ref: key must have one of output, keycode, or switchLayout"))
        }
        if (actionCount > 1) {
            return Result.failure(IllegalArgumentException("$ref: key must have only one of output, keycode, or switchLayout"))
        }

        if (key.output != null && key.output.length > MAX_OUTPUT_LENGTH) {
            return Result.failure(IllegalArgumentException("$ref: output exceeds $MAX_OUTPUT_LENGTH characters"))
        }

        if (key.keycode != null && key.keycode !in KeyAction.ALLOWED_KEYCODES) {
            return Result.failure(IllegalArgumentException("$ref: unknown keycode '${key.keycode}'"))
        }

        if (key.switchLayout != null && key.switchLayout !in layoutNames) {
            return Result.failure(IllegalArgumentException("$ref: switchLayout '${key.switchLayout}' references unknown layout"))
        }

        if (key.width < MIN_WIDTH || key.width > MAX_WIDTH) {
            return Result.failure(IllegalArgumentException("$ref: width ${key.width} out of range ($MIN_WIDTH-$MAX_WIDTH)"))
        }

        return Result.success(Unit)
    }

    private fun reserialize(pack: KeyboardPack): KeyboardPack {
        val cleanJson = json.encodeToString(pack)
        return json.decodeFromString(cleanJson)
    }
}
