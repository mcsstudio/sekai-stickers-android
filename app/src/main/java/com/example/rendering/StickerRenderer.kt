package com.example.rendering

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.createBitmap
import androidx.core.graphics.toColorInt
import androidx.core.graphics.withSave
import com.example.R
import kotlin.math.PI

object StickerRenderer {
    const val LOGICAL_WIDTH = 296f
    const val LOGICAL_HEIGHT = 256f
    const val EXPORT_SCALE = 2

    data class FontSet(
        val primary: Typeface,
        val roundedFallback: Typeface,
        val cjkFallback: Typeface,
        val systemFallback: Typeface = Typeface.DEFAULT_BOLD,
    )

    fun loadFontSet(context: Context): FontSet {
        val yuruka = ResourcesCompat.getFont(context, R.font.yuruka_std)
        val baloo = ResourcesCompat.getFont(context, R.font.baloo_2) ?: Typeface.DEFAULT_BOLD
        val cjk = ResourcesCompat.getFont(context, R.font.m_plus_rounded_1c) ?: baloo
        return FontSet(
            primary = yuruka ?: baloo,
            roundedFallback = baloo,
            cjkFallback = cjk,
            systemFallback = Typeface.DEFAULT_BOLD,
        )
    }

    fun renderToBitmap(
        characterBitmap: Bitmap?,
        text: String,
        textColorHex: String,
        posX: Float,
        posY: Float,
        rotate: Float,
        fontSize: Float,
        spaceSize: Float,
        curve: Boolean,
        fontSet: FontSet,
        scale: Int = EXPORT_SCALE,
    ): Bitmap {
        val widthPx = (LOGICAL_WIDTH * scale).toInt()
        val heightPx = (LOGICAL_HEIGHT * scale).toInt()
        val output = createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        canvas.withSave {
            scale(scale.toFloat(), scale.toFloat())
            drawSticker(
                canvas = this,
                characterBitmap = characterBitmap,
                text = text,
                textColorHex = textColorHex,
                posX = posX,
                posY = posY,
                rotate = rotate,
                fontSize = fontSize,
                spaceSize = spaceSize,
                curve = curve,
                fontSet = fontSet,
            )
        }
        return output
    }

    fun drawSticker(
        canvas: Canvas,
        characterBitmap: Bitmap?,
        text: String,
        textColorHex: String,
        posX: Float,
        posY: Float,
        rotate: Float,
        fontSize: Float,
        spaceSize: Float,
        curve: Boolean,
        fontSet: FontSet,
    ) {
        if (characterBitmap == null) return

        val hRatio = LOGICAL_WIDTH / characterBitmap.width.toFloat()
        val vRatio = LOGICAL_HEIGHT / characterBitmap.height.toFloat()
        val ratio = minOf(hRatio, vRatio)
        val drawWidth = characterBitmap.width * ratio
        val drawHeight = characterBitmap.height * ratio
        val shiftX = (LOGICAL_WIDTH - drawWidth) / 2f
        val shiftY = (LOGICAL_HEIGHT - drawHeight) / 2f

        val destRect = RectF(shiftX, shiftY, shiftX + drawWidth, shiftY + drawHeight)
        val imagePaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        canvas.drawBitmap(characterBitmap, null, destRect, imagePaint)

        if (text.isEmpty()) return

        val fillColor = try {
            textColorHex.toColorInt()
        } catch (_: IllegalArgumentException) {
            Color.WHITE
        }

        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 9f
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
            color = Color.WHITE
            textSize = fontSize
            textAlign = Paint.Align.LEFT
            typeface = fontSet.primary
        }

        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = fillColor
            textSize = fontSize
            textAlign = Paint.Align.LEFT
            typeface = fontSet.primary
        }

        val lines = text.split("\n")
        val angleRadians = (PI * text.length) / 7.0

        canvas.withSave {
            translate(posX, posY)
            rotate(Math.toDegrees((rotate / 10.0)).toFloat())

            if (curve) {
                for (line in lines) {
                    if (line.isEmpty()) continue
                    val stepDegrees = Math.toDegrees(angleRadians / line.length / 2.5).toFloat()
                    var i = 0
                    while (i < line.length) {
                        val cp = line.codePointAt(i)
                        val charCount = Character.charCount(cp)
                        val glyph = line.substring(i, i + charCount)
                        rotate(stepDegrees)
                        withSave {
                            translate(0f, -1f * fontSize * 3.5f)
                            drawCenteredGlyphRun(
                                canvas = this,
                                text = glyph,
                                baselineY = 0f,
                                strokePaint = strokePaint,
                                fillPaint = fillPaint,
                                fontSet = fontSet,
                            )
                        }
                        i += charCount
                    }
                }
            } else {
                var k = 0f
                for (line in lines) {
                    drawCenteredGlyphRun(
                        canvas = this,
                        text = line,
                        baselineY = k,
                        strokePaint = strokePaint,
                        fillPaint = fillPaint,
                        fontSet = fontSet,
                    )
                    k += spaceSize
                }
            }
        }
    }

    private fun drawCenteredGlyphRun(
        canvas: Canvas,
        text: String,
        baselineY: Float,
        strokePaint: Paint,
        fillPaint: Paint,
        fontSet: FontSet,
    ) {
        if (text.isEmpty()) return

        val runs = splitRunsByTypeface(text, strokePaint, fontSet)
        var totalWidth = 0f
        for ((segment, typeface) in runs) {
            fillPaint.typeface = typeface
            totalWidth += fillPaint.measureText(segment)
        }

        var cursorX = -totalWidth / 2f
        for ((segment, typeface) in runs) {
            strokePaint.typeface = typeface
            fillPaint.typeface = typeface
            canvas.drawText(segment, cursorX, baselineY, strokePaint)
            canvas.drawText(segment, cursorX, baselineY, fillPaint)
            cursorX += fillPaint.measureText(segment)
        }
    }

    private fun splitRunsByTypeface(
        text: String,
        probePaint: Paint,
        fontSet: FontSet,
    ): List<Pair<String, Typeface>> {
        val result = mutableListOf<Pair<String, Typeface>>()
        var currentBuilder = StringBuilder()
        var currentTypeface: Typeface? = null

        var i = 0
        while (i < text.length) {
            val cp = text.codePointAt(i)
            val charCount = Character.charCount(cp)
            val glyph = text.substring(i, i + charCount)
            val chosen = resolveTypefaceForGlyph(glyph, probePaint, fontSet)
            if (currentTypeface == null || currentTypeface == chosen) {
                currentTypeface = chosen
                currentBuilder.append(glyph)
            } else {
                result.add(currentBuilder.toString() to currentTypeface)
                currentBuilder = StringBuilder(glyph)
                currentTypeface = chosen
            }
            i += charCount
        }
        if (currentBuilder.isNotEmpty() && currentTypeface != null) {
            result.add(currentBuilder.toString() to currentTypeface)
        }
        return result
    }

    private fun resolveTypefaceForGlyph(
        glyph: String,
        probePaint: Paint,
        fontSet: FontSet,
    ): Typeface {
        if (glyph.isBlank()) return fontSet.primary
        probePaint.typeface = fontSet.primary
        if (probePaint.hasGlyph(glyph)) return fontSet.primary
        probePaint.typeface = fontSet.roundedFallback
        if (probePaint.hasGlyph(glyph)) return fontSet.roundedFallback
        probePaint.typeface = fontSet.cjkFallback
        if (probePaint.hasGlyph(glyph)) return fontSet.cjkFallback
        return fontSet.systemFallback
    }
}
