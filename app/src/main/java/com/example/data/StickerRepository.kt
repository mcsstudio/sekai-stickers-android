package com.example.data

import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.ui.graphics.Color
import androidx.core.content.FileProvider
import com.example.model.StickerCharacter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sqrt

class StickerRepository(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }

    suspend fun loadCharacters(): List<StickerCharacter> = withContext(Dispatchers.IO) {
        context.assets.open("characters.json").bufferedReader().use { reader ->
            json.decodeFromString<List<StickerCharacter>>(reader.readText())
        }
    }

    suspend fun loadCharacterBitmap(imgAssetPath: String): Bitmap? = withContext(Dispatchers.IO) {
        runCatching {
            context.assets.open("img/$imgAssetPath").use { stream ->
                BitmapFactory.decodeStream(stream)
            }
        }.getOrNull()
    }

    /**
     * Ports fast-average-color's "sqrt" algorithm over non-transparent pixels,
     * plus App.jsx's desaturateColor(hex) dark surface derivation.
     */
    suspend fun extractThemeColors(bitmap: Bitmap): Pair<Color, Color> = withContext(Dispatchers.Default) {
        val scaled = if (bitmap.width > 128 || bitmap.height > 128) {
            Bitmap.createScaledBitmap(bitmap, 64, 64, true)
        } else {
            bitmap
        }

        val pixels = IntArray(scaled.width * scaled.height)
        scaled.getPixels(pixels, 0, scaled.width, 0, 0, scaled.width, scaled.height)

        var redSum = 0.0
        var greenSum = 0.0
        var blueSum = 0.0
        var count = 0L

        for (px in pixels) {
            val a = (px ushr 24) and 0xFF
            if (a == 0) continue
            val r = (px ushr 16) and 0xFF
            val g = (px ushr 8) and 0xFF
            val b = px and 0xFF
            redSum += (r * r * a).toDouble()
            greenSum += (g * g * a).toDouble()
            blueSum += (b * b * a).toDouble()
            count += a
        }

        val dominant = if (count > 0) {
            val r = sqrt(redSum / count).roundToInt().coerceIn(0, 255)
            val g = sqrt(greenSum / count).roundToInt().coerceIn(0, 255)
            val b = sqrt(blueSum / count).roundToInt().coerceIn(0, 255)
            Color(r, g, b)
        } else {
            Color(0xFF3F50B5)
        }

        val background = desaturateColor(dominant)
        dominant to background
    }

    private fun desaturateColor(color: Color): Color {
        val r = color.red
        val g = color.green
        val b = color.blue

        val maxVal = maxOf(r, g, b)
        val minVal = minOf(r, g, b)
        var h = 0f
        var s: Float
        var l = (maxVal + minVal) / 2f

        if (maxVal == minVal) {
            h = 0f
            s = 0f
        } else {
            val d = maxVal - minVal
            s = if (l > 0.5f) d / (2f - maxVal - minVal) else d / (maxVal + minVal)
            h = when (maxVal) {
                r -> (g - b) / d + (if (g < b) 6f else 0f)
                g -> (b - r) / d + 2f
                else -> (r - g) / d + 4f
            }
            h /= 6f
        }

        s *= 0.15f
        l = max(0.12f, l * 0.3f)

        fun hue2rgb(p: Float, q: Float, tInput: Float): Float {
            var t = tInput
            if (t < 0f) t += 1f
            if (t > 1f) t -= 1f
            return when {
                t < 1f / 6f -> p + (q - p) * 6f * t
                t < 1f / 2f -> q
                t < 2f / 3f -> p + (q - p) * (2f / 3f - t) * 6f
                else -> p
            }
        }

        val q = if (l < 0.5f) l * (1f + s) else l + s - l * s
        val p = 2f * l - q

        val r2 = hue2rgb(p, q, h + 1f / 3f).coerceIn(0f, 1f)
        val g2 = hue2rgb(p, q, h).coerceIn(0f, 1f)
        val b2 = hue2rgb(p, q, h - 1f / 3f).coerceIn(0f, 1f)

        return Color(r2, g2, b2)
    }

    suspend fun copyStickerToClipboard(bitmap: Bitmap, characterName: String): Boolean =
        withContext(Dispatchers.IO) {
            runCatching {
                val uri = writeStickerToCacheUri(bitmap, characterName)
                withContext(Dispatchers.Main) {
                    val clipboard =
                        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newUri(context.contentResolver, "Sekai Sticker", uri)
                    clipboard.setPrimaryClip(clip)
                }
                true
            }.getOrDefault(false)
        }

    suspend fun saveStickerToGallery(bitmap: Bitmap, characterName: String): Boolean =
        withContext(Dispatchers.IO) {
            runCatching {
                val safeName = characterName.replace(Regex("[^A-Za-z0-9._-]"), "_")
                val fileName = "${safeName}_${System.currentTimeMillis()}.png"

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val values = ContentValues().apply {
                        put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                        put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                        put(
                            MediaStore.Images.Media.RELATIVE_PATH,
                            "${Environment.DIRECTORY_PICTURES}/SekaiStickers",
                        )
                        put(MediaStore.Images.Media.IS_PENDING, 1)
                    }
                    val resolver = context.contentResolver
                    val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                        ?: return@runCatching false
                    resolver.openOutputStream(uri)?.use { out ->
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                    } ?: return@runCatching false
                    values.clear()
                    values.put(MediaStore.Images.Media.IS_PENDING, 0)
                    resolver.update(uri, values, null, null)
                    true
                } else {
                    val picturesDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                        ?: context.filesDir
                    val dir = File(picturesDir, "SekaiStickers").apply { mkdirs() }
                    val file = File(dir, fileName)
                    FileOutputStream(file).use { out ->
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                    }
                    true
                }
            }.getOrDefault(false)
        }

    suspend fun createShareIntent(bitmap: Bitmap, characterName: String): Intent =
        withContext(Dispatchers.IO) {
            val uri: Uri = writeStickerToCacheUri(bitmap, characterName)
            Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        }

    private fun writeStickerToCacheUri(bitmap: Bitmap, characterName: String): Uri {
        val cacheDir = File(context.cacheDir, "stickers").apply { mkdirs() }
        // Keep cache tidy by removing older temporary sticker files
        cacheDir.listFiles()?.forEach { oldFile ->
            if (System.currentTimeMillis() - oldFile.lastModified() > 60_000L) {
                oldFile.delete()
            }
        }
        val safeName = characterName.replace(Regex("[^A-Za-z0-9._-]"), "_")
        val file = File(cacheDir, "${safeName}_${System.currentTimeMillis()}.png")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file,
        )
    }
}
