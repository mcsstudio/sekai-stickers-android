package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.core.graphics.withSave
import com.example.R
import com.example.rendering.StickerRenderer

@Composable
fun StickerCanvasCard(
    characterBitmap: Bitmap?,
    characterName: String?,
    isLoading: Boolean,
    text: String,
    textColorHex: String,
    posX: Float,
    posY: Float,
    rotate: Float,
    fontSize: Float,
    spaceSize: Float,
    curve: Boolean,
    onHorizontalChange: (Float) -> Unit,
    onVerticalSliderChange: (Float) -> Unit,
    onCanvasDrag: (Float, Float) -> Unit,
    onOpenPicker: () -> Unit,
    onResetDefaults: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val fontSet = remember(context) { StickerRenderer.loadFontSet(context) }

    val verticalSliderValue = remember(posY, curve, fontSize) {
        val raw = if (curve) {
            StickerRenderer.LOGICAL_HEIGHT - posY + fontSize * 3f
        } else {
            StickerRenderer.LOGICAL_HEIGHT - posY
        }
        raw.coerceIn(0f, StickerRenderer.LOGICAL_HEIGHT)
    }

    val canvasCd = stringResource(R.string.cd_sticker_preview)
    val accent = MaterialTheme.colorScheme.primary

    Column(
        modifier = modifier.widthIn(max = 420.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (!characterName.isNullOrBlank()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    border = BorderStroke(1.dp, accent.copy(alpha = 0.4f)),
                ) {
                    Text(
                        text = characterName,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    )
                }

                FilledTonalIconButton(
                    onClick = onResetDefaults,
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = accent,
                    ),
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("reset_defaults_button"),
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = stringResource(R.string.cd_reset_defaults),
                    )
                }
            }
        }

        // Row containing 296:256 Canvas + Vertical Position Slider
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val canvasShape = RoundedCornerShape(12.dp)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(StickerRenderer.LOGICAL_WIDTH / StickerRenderer.LOGICAL_HEIGHT)
                    .clip(canvasShape)
                    .background(Color(0xFF1A1A1E).copy(alpha = 0.45f))
                    .border(
                        width = 1.dp,
                        color = Color(0xFFEEEEEE).copy(alpha = 0.85f),
                        shape = canvasShape,
                    )
                    .semantics { contentDescription = canvasCd }
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            val scaleX = StickerRenderer.LOGICAL_WIDTH / size.width.coerceAtLeast(1)
                            val scaleY = StickerRenderer.LOGICAL_HEIGHT / size.height.coerceAtLeast(1)
                            onCanvasDrag(dragAmount.x * scaleX, dragAmount.y * scaleY)
                        }
                    }
                    .testTag("sticker_preview_canvas"),
                contentAlignment = Alignment.Center,
            ) {
                if (characterBitmap != null) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val scaleX = size.width / StickerRenderer.LOGICAL_WIDTH
                        val scaleY = size.height / StickerRenderer.LOGICAL_HEIGHT
                        drawIntoCanvas { canvas ->
                            canvas.nativeCanvas.withSave {
                                scale(scaleX, scaleY)
                                StickerRenderer.drawSticker(
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
                        }
                    }
                } else if (isLoading) {
                    CircularProgressIndicator(
                        color = accent,
                        modifier = Modifier.size(36.dp),
                    )
                } else {
                    Text(
                        text = stringResource(R.string.canvas_empty_hint),
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White,
                        modifier = Modifier.padding(16.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Vertical position slider column
            Column(
                modifier = Modifier
                    .width(48.dp)
                    .height(220.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowUp,
                    contentDescription = stringResource(R.string.cd_vertical_position_up),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    Slider(
                        value = verticalSliderValue,
                        onValueChange = onVerticalSliderChange,
                        valueRange = 0f..StickerRenderer.LOGICAL_HEIGHT,
                        colors = SliderDefaults.colors(
                            thumbColor = accent,
                            activeTrackColor = accent.copy(alpha = 0.35f),
                            inactiveTrackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f),
                        ),
                        modifier = Modifier
                            .fillMaxHeight()
                            .layout { measurable, constraints ->
                                val placeable = measurable.measure(
                                    Constraints(
                                        minWidth = constraints.minHeight,
                                        maxWidth = constraints.maxHeight,
                                        minHeight = constraints.minWidth,
                                        maxHeight = constraints.maxWidth,
                                    ),
                                )
                                layout(placeable.height, placeable.width) {
                                    placeable.place(
                                        x = -(placeable.width - placeable.height) / 2,
                                        y = (placeable.width - placeable.height) / 2,
                                    )
                                }
                            }
                            .graphicsLayer { rotationZ = -90f }
                            .testTag("vertical_position_slider"),
                    )
                }

                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = stringResource(R.string.cd_vertical_position_down),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Horizontal Position Slider + Character Picker Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = stringResource(R.string.cd_horizontal_position_left),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp),
            )

            Slider(
                value = posX.coerceIn(0f, StickerRenderer.LOGICAL_WIDTH),
                onValueChange = onHorizontalChange,
                valueRange = 0f..StickerRenderer.LOGICAL_WIDTH,
                colors = SliderDefaults.colors(
                    thumbColor = accent,
                    activeTrackColor = accent.copy(alpha = 0.35f),
                    inactiveTrackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f),
                ),
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp)
                    .testTag("horizontal_position_slider"),
            )

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = stringResource(R.string.cd_horizontal_position_right),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp),
            )

            Spacer(modifier = Modifier.width(8.dp))

            FilledTonalIconButton(
                onClick = onOpenPicker,
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = accent,
                ),
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("open_character_picker_button"),
            ) {
                Icon(
                    imageVector = Icons.Default.PersonSearch,
                    contentDescription = stringResource(R.string.cd_pick_character),
                )
            }
        }
    }
}
