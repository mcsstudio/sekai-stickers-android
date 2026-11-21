# Project Sekai Stickers (Android)

An Android Jetpack Compose rewrite of the **Sekai Stickers** maker (`mcsstudio/sekai-stickers`), preserving the full collection of 359 Project Sekai character sticker templates, custom text rendering, dynamic character color theming, and export workflows.

## Features

- **359 Character Sticker Templates**: Browse and search by character name, ID, or sticker title in an adaptive grid picker.
- **Live Sticker Canvas**: Real-time `296×256` logical coordinate sticker preview with crisp white rounded outlines, multi-line text, and curved arc text mode.
- **Interactive Positioning & Typography Controls**:
  - Drag text directly on the sticker canvas or fine-tune with horizontal (`0–296`) and vertical (`0–256`) sliders.
  - Adjust rotation (`-10` to `+10`), font size (`10–100px`), line spacing (`18–100px`), and curved text toggle.
  - Reset back to each character's default text and placement at any time.
- **Dynamic Character Theming**: Extracts each character's dominant color using the `sqrt` average-color algorithm and derives a matching dark surface palette.
- **Export & Share**:
  - **Sao chép (Copy)**: Copies a transparent `592×512` PNG sticker to the Android clipboard via `FileProvider`.
  - **Tải xuống (Download)**: Saves the PNG directly to `Pictures/SekaiStickers` via `MediaStore` without broad storage permissions.
  - **Chia sẻ (Share)**: Opens the Android share sheet with the rendered PNG sticker.

## Tech Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose + Material 3 (Adaptive layout for phones and tablets)
- **Image Loading**: Coil Compose
- **Serialization**: `kotlinx.serialization`
- **Build System**: Gradle Kotlin DSL (AGP 9.1.1, Android SDK 36, minSdk 26)

## Credits

- Original concept & web app: [TheOriginalAyaka](https://github.com/theoriginalayaka) & [Modder4869](https://github.com/modder4869)
- Sticker artwork: [u/SherenPlaysGames](https://www.reddit.com/r/ProjectSekai/comments/x1h4v1/after_an_ungodly_amount_of_time_i_finally_made/)
- Vietnamese localization & source repo: [mcsstudio/sekai-stickers](https://github.com/mcsstudio/sekai-stickers)

## License

Distributed under the MIT License. See [`LICENSE`](./LICENSE) for details.
