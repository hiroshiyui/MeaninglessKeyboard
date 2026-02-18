# MeaninglessKeyboard

A customizable Android IME (Input Method Editor) built with Jetpack Compose and driven by JSON keyboard layout definitions.

## Features

- **JSON-driven layouts** - Define keyboard layouts in JSON; import custom layouts from files
- **Multiple keyboard packs** - Swipe left/right to switch between enabled packs
- **Sub-layout switching** - Support for lowercase, uppercase, symbols, and custom sub-layouts within a pack
- **Material You theming** - Keyboard colors adapt to your wallpaper via Material 3 dynamic colors (Android 12+)
- **Key preview popup** - Visual feedback showing the pressed key label
- **Haptic & sound feedback** - Vibration and click sound on key press
- **Pack management UI** - Enable/disable, reorder, and delete keyboard packs from the settings screen
- **Security-first import** - Imported JSON is validated, sanitized, and re-serialized before storage

## Built-in Layouts

| Layout | Description |
|--------|-------------|
| English QWERTY | Standard QWERTY with lowercase, uppercase, and symbols sub-layouts |
| Number Pad | Compact numeric keypad |
| Bopomofo (Dachen) | Taiwanese phonetic input layout |

## Building

Requires Android Studio with AGP 9.0+ and JDK 11+.

```bash
./gradlew assembleDebug
./gradlew installDebug
```

## Setup

1. Install the app
2. Open **MeaninglessKeyboard** from the launcher
3. Tap **Input Method Settings** to enable the keyboard in system settings
4. Select **MeaninglessKeyboard** as your active input method

## Importing Custom Layouts

Keyboard packs are defined as JSON files. Example structure:

```json
{
  "name": "My Layout",
  "author": "Your Name",
  "version": 1,
  "defaultLayout": "main",
  "layouts": {
    "main": {
      "rows": [
        {
          "keys": [
            { "label": "A", "output": "a", "width": 1 },
            { "label": "B", "output": "b", "width": 1 }
          ]
        }
      ]
    }
  }
}
```

### Key types

- **Text output**: `{ "label": "A", "output": "a" }` - commits text to the input field
- **Keycode**: `{ "label": "Del", "keycode": "BACKSPACE" }` - sends a key event (supported: `BACKSPACE`, `ENTER`, `SHIFT`, `TAB`, `ARROW_LEFT`, `ARROW_RIGHT`)
- **Layout switch**: `{ "label": "123", "switchLayout": "symbols" }` - switches to another sub-layout

Each key must have exactly one of `output`, `keycode`, or `switchLayout`.

### Validation limits

| Field | Limit |
|-------|-------|
| File size | 512 KB |
| Pack/author name | 64 characters |
| Layouts per pack | 10 |
| Rows per layout | 8 |
| Keys per row | 20 |
| Key label | 8 characters |
| Key output | 32 characters |
| Key width | 0.5 - 10.0 |

## Tech Stack

- Kotlin 2.2.10
- Jetpack Compose (BOM 2025.05.01)
- Material 3 with dynamic colors
- Room 2.7.0
- kotlinx-serialization
- Min SDK 24 (Android 7.0) / Target SDK 36

## License

This project is licensed under the [GNU General Public License v3.0 or later](LICENSE).
