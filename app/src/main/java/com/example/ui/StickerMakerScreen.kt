package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.ui.components.CharacterPickerSheet
import com.example.ui.components.ControlsPanel
import com.example.ui.components.InfoDialog
import com.example.ui.components.StickerCanvasCard
import com.example.ui.components.openExternalUrl
import com.example.ui.theme.SekaiStickersTheme

@Composable
fun StickerMakerScreen(
    viewModel: StickerViewModel,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is StickerUiEvent.ShowMessage -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                is StickerUiEvent.LaunchShare -> {
                    val shareTitle = context.getString(R.string.action_share)
                    context.startActivity(
                        android.content.Intent.createChooser(event.intent, shareTitle),
                    )
                }
            }
        }
    }

    if (uiState.isPickerOpen) {
        BackHandler { viewModel.setPickerOpen(false) }
    } else if (uiState.isInfoOpen) {
        BackHandler { viewModel.setInfoOpen(false) }
    }

    SekaiStickersTheme(
        dominantColor = uiState.dominantColor,
        backgroundColor = uiState.backgroundColor,
    ) {
        val bgColor = MaterialTheme.colorScheme.background
        val accentColor = MaterialTheme.colorScheme.primary

        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            containerColor = bgColor,
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    drawRect(bgColor)
                    drawRect(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                accentColor.copy(alpha = 0.16f),
                                bgColor.copy(alpha = 0f),
                            ),
                            center = Offset(size.width * 0.5f, size.height * 0.22f),
                            radius = size.maxDimension * 0.72f,
                        ),
                    )
                },
        ) { innerPadding ->
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .imePadding(),
            ) {
                val isWideLayout = maxWidth >= 720.dp

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    HeaderSection(
                        onOpenGitHub = {
                            openExternalUrl(context, "https://github.com/mcsstudio/sekai-stickers")
                        },
                    )

                    if (isWideLayout) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .widthIn(max = 1040.dp),
                            horizontalArrangement = Arrangement.spacedBy(24.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier.weight(1f),
                                contentAlignment = Alignment.Center,
                            ) {
                                StickerCanvasCard(
                                    characterBitmap = uiState.characterBitmap,
                                    characterName = uiState.selectedCharacter?.name,
                                    isLoading = uiState.isLoadingCharacter,
                                    text = uiState.text,
                                    textColorHex = uiState.selectedCharacter?.color ?: "#FFFFFF",
                                    posX = uiState.posX,
                                    posY = uiState.posY,
                                    rotate = uiState.rotate,
                                    fontSize = uiState.fontSize,
                                    spaceSize = uiState.spaceSize,
                                    curve = uiState.curve,
                                    onHorizontalChange = viewModel::onHorizontalPositionChange,
                                    onVerticalSliderChange = viewModel::onVerticalSliderChange,
                                    onCanvasDrag = viewModel::onCanvasDrag,
                                    onOpenPicker = { viewModel.setPickerOpen(true) },
                                    onResetDefaults = viewModel::resetToCharacterDefaults,
                                )
                            }

                            VerticalDivider(
                                modifier = Modifier.height(340.dp),
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                            )

                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                            ) {
                                ControlsPanel(
                                    rotate = uiState.rotate,
                                    fontSize = uiState.fontSize,
                                    spaceSize = uiState.spaceSize,
                                    curve = uiState.curve,
                                    text = uiState.text,
                                    onRotateChange = viewModel::onRotateChange,
                                    onFontSizeChange = viewModel::onFontSizeChange,
                                    onSpaceSizeChange = viewModel::onSpaceSizeChange,
                                    onCurveChange = viewModel::onCurveChange,
                                    onTextChange = viewModel::onTextChange,
                                )

                                ActionButtonsBar(
                                    isExporting = uiState.isExporting,
                                    onCopy = viewModel::copySticker,
                                    onDownload = viewModel::downloadSticker,
                                    onShare = viewModel::shareSticker,
                                )
                            }
                        }
                    } else {
                        StickerCanvasCard(
                            characterBitmap = uiState.characterBitmap,
                            characterName = uiState.selectedCharacter?.name,
                            isLoading = uiState.isLoadingCharacter,
                            text = uiState.text,
                            textColorHex = uiState.selectedCharacter?.color ?: "#FFFFFF",
                            posX = uiState.posX,
                            posY = uiState.posY,
                            rotate = uiState.rotate,
                            fontSize = uiState.fontSize,
                            spaceSize = uiState.spaceSize,
                            curve = uiState.curve,
                            onHorizontalChange = viewModel::onHorizontalPositionChange,
                            onVerticalSliderChange = viewModel::onVerticalSliderChange,
                            onCanvasDrag = viewModel::onCanvasDrag,
                            onOpenPicker = { viewModel.setPickerOpen(true) },
                            onResetDefaults = viewModel::resetToCharacterDefaults,
                        )

                        ControlsPanel(
                            rotate = uiState.rotate,
                            fontSize = uiState.fontSize,
                            spaceSize = uiState.spaceSize,
                            curve = uiState.curve,
                            text = uiState.text,
                            onRotateChange = viewModel::onRotateChange,
                            onFontSizeChange = viewModel::onFontSizeChange,
                            onSpaceSizeChange = viewModel::onSpaceSizeChange,
                            onCurveChange = viewModel::onCurveChange,
                            onTextChange = viewModel::onTextChange,
                        )

                        ActionButtonsBar(
                            isExporting = uiState.isExporting,
                            onCopy = viewModel::copySticker,
                            onDownload = viewModel::downloadSticker,
                            onShare = viewModel::shareSticker,
                        )
                    }

                    AuthorInfoButton(
                        onOpenInfo = { viewModel.setInfoOpen(true) },
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        if (uiState.isPickerOpen) {
            CharacterPickerSheet(
                characters = uiState.characters,
                selectedIndex = uiState.selectedIndex,
                searchQuery = uiState.searchQuery,
                onSearchQueryChange = viewModel::onSearchQueryChange,
                onSelectCharacter = viewModel::selectCharacter,
                onDismiss = { viewModel.setPickerOpen(false) },
            )
        }

        if (uiState.isInfoOpen) {
            InfoDialog(
                onDismiss = { viewModel.setInfoOpen(false) },
            )
        }
    }
}

@Composable
private fun HeaderSection(
    onOpenGitHub: () -> Unit,
) {
    val accent = MaterialTheme.colorScheme.primary

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 720.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.main_title),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(
            onClick = onOpenGitHub,
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, accent.copy(alpha = 0.65f)),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
            modifier = Modifier
                .heightIn(min = 48.dp)
                .testTag("github_star_button"),
        ) {
            Icon(
                imageVector = Icons.Default.Code,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.github_star_button),
                style = MaterialTheme.typography.labelLarge,
                color = accent,
            )
        }
    }
}

@Composable
private fun AuthorInfoButton(
    onOpenInfo: () -> Unit,
) {
    val accent = MaterialTheme.colorScheme.primary

    FilledTonalButton(
        onClick = onOpenInfo,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = accent,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 520.dp)
            .heightIn(min = 48.dp)
            .testTag("info_dialog_button"),
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = stringResource(R.string.action_info),
            modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = stringResource(R.string.action_author),
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

@Composable
private fun ActionButtonsBar(
    isExporting: Boolean,
    onCopy: () -> Unit,
    onDownload: () -> Unit,
    onShare: () -> Unit,
) {
    val accent = MaterialTheme.colorScheme.primary

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 520.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedButton(
            onClick = onCopy,
            enabled = !isExporting,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, accent.copy(alpha = 0.7f)),
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 48.dp)
                .testTag("copy_sticker_button"),
        ) {
            if (isExporting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = accent,
                )
            } else {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(18.dp),
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = stringResource(R.string.action_copy),
                style = MaterialTheme.typography.labelLarge,
                color = accent,
            )
        }

        Button(
            onClick = onDownload,
            enabled = !isExporting,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = accent,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 48.dp)
                .testTag("download_sticker_button"),
        ) {
            Icon(
                imageVector = Icons.Default.Download,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = stringResource(R.string.action_download),
                style = MaterialTheme.typography.labelLarge,
            )
        }

        FilledTonalIconButton(
            onClick = onShare,
            enabled = !isExporting,
            colors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = accent,
            ),
            modifier = Modifier
                .minimumInteractiveComponentSize()
                .testTag("share_sticker_button"),
        ) {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = stringResource(R.string.action_share),
            )
        }
    }
}
