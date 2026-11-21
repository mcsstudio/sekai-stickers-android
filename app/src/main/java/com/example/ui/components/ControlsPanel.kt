package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import kotlin.math.roundToInt

@Composable
fun ControlsPanel(
    rotate: Float,
    fontSize: Float,
    spaceSize: Float,
    curve: Boolean,
    text: String,
    onRotateChange: (Float) -> Unit,
    onFontSizeChange: (Float) -> Unit,
    onSpaceSizeChange: (Float) -> Unit,
    onCurveChange: (Boolean) -> Unit,
    onTextChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val accent = MaterialTheme.colorScheme.primary

    Surface(
        modifier = modifier.widthIn(max = 520.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
        tonalElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SliderControlRow(
                label = stringResource(R.string.label_rotation),
                valueText = String.format(java.util.Locale.US, "%.1f", rotate),
                value = rotate.coerceIn(-10f, 10f),
                valueRange = -10f..10f,
                onValueChange = onRotateChange,
                testTag = "rotation_slider",
            )

            SliderControlRow(
                label = stringResource(R.string.label_font_size),
                valueText = "${fontSize.roundToInt()}px",
                value = fontSize.coerceIn(10f, 100f),
                valueRange = 10f..100f,
                onValueChange = onFontSizeChange,
                testTag = "font_size_slider",
            )

            SliderControlRow(
                label = stringResource(R.string.label_line_spacing),
                valueText = "${spaceSize.roundToInt()}px",
                value = spaceSize.coerceIn(18f, 100f),
                valueRange = 18f..100f,
                onValueChange = onSpaceSizeChange,
                testTag = "line_spacing_slider",
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onCurveChange(!curve) }
                    .minimumInteractiveComponentSize()
                    .padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = stringResource(R.string.label_curve_text),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Switch(
                    checked = curve,
                    onCheckedChange = onCurveChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = accent,
                    ),
                    modifier = Modifier.testTag("curve_text_switch"),
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            OutlinedTextField(
                value = text,
                onValueChange = onTextChange,
                label = {
                    Text(
                        text = stringResource(R.string.label_text_input),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                },
                minLines = 2,
                maxLines = 4,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = accent,
                    focusedLabelColor = accent,
                    cursorColor = accent,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("sticker_text_input"),
            )
        }
    }
}

@Composable
private fun SliderControlRow(
    label: String,
    valueText: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
    testTag: String,
) {
    val accent = MaterialTheme.colorScheme.primary

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.width(104.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = valueText,
                style = MaterialTheme.typography.bodyMedium,
                color = accent,
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = accent,
                activeTrackColor = accent,
                inactiveTrackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f),
            ),
            modifier = Modifier
                .weight(1f)
                .testTag(testTag),
        )
    }
}
