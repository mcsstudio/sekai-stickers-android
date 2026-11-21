package com.example.ui

import android.app.Application
import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.R
import com.example.data.StickerRepository
import com.example.model.StickerCharacter
import com.example.rendering.StickerRenderer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class StickerUiState(
    val characters: List<StickerCharacter> = emptyList(),
    val selectedIndex: Int = -1,
    val selectedCharacter: StickerCharacter? = null,
    val characterBitmap: Bitmap? = null,
    val isLoadingCharacter: Boolean = true,
    val text: String = "",
    val posX: Float = 148f,
    val posY: Float = 70f,
    val rotate: Float = -2f,
    val fontSize: Float = 38f,
    val spaceSize: Float = 36f,
    val curve: Boolean = false,
    val dominantColor: Color = Color(0xFF3F50B5),
    val backgroundColor: Color = Color(0xFF212121),
    val searchQuery: String = "",
    val isPickerOpen: Boolean = false,
    val isInfoOpen: Boolean = false,
    val isExporting: Boolean = false,
)

sealed interface StickerUiEvent {
    data class ShowMessage(val message: String) : StickerUiEvent
    data class LaunchShare(val intent: Intent) : StickerUiEvent
}

class StickerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = StickerRepository(application.applicationContext)
    private val fontSet: StickerRenderer.FontSet by lazy {
        StickerRenderer.loadFontSet(application.applicationContext)
    }

    private val _uiState = MutableStateFlow(StickerUiState())
    val uiState: StateFlow<StickerUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<StickerUiEvent>()
    val events: SharedFlow<StickerUiEvent> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            val loadedCharacters = repository.loadCharacters()
            if (loadedCharacters.isNotEmpty()) {
                val defaultIndex = if (loadedCharacters.size > 49) 49 else 0
                _uiState.update { it.copy(characters = loadedCharacters) }
                selectCharacter(defaultIndex)
            } else {
                _uiState.update { it.copy(isLoadingCharacter = false) }
            }
        }
    }

    fun selectCharacter(index: Int) {
        val list = _uiState.value.characters
        if (index !in list.indices) return
        val character = list[index]

        _uiState.update {
            it.copy(
                selectedIndex = index,
                selectedCharacter = character,
                isLoadingCharacter = true,
                text = character.defaultText.text,
                posX = character.defaultText.x,
                posY = character.defaultText.y,
                rotate = character.defaultText.r,
                fontSize = character.defaultText.s,
                isPickerOpen = false,
            )
        }

        viewModelScope.launch {
            val bitmap = repository.loadCharacterBitmap(character.img)
            val (dominant, background) = if (bitmap != null) {
                repository.extractThemeColors(bitmap)
            } else {
                Color(0xFF3F50B5) to Color(0xFF212121)
            }

            // Guard against rapid selection changes
            if (_uiState.value.selectedIndex == index) {
                _uiState.update {
                    it.copy(
                        characterBitmap = bitmap,
                        isLoadingCharacter = false,
                        dominantColor = dominant,
                        backgroundColor = background,
                    )
                }
            }
        }
    }

    fun resetToCharacterDefaults() {
        val character = _uiState.value.selectedCharacter ?: return
        _uiState.update {
            it.copy(
                text = character.defaultText.text,
                posX = character.defaultText.x,
                posY = character.defaultText.y,
                rotate = character.defaultText.r,
                fontSize = character.defaultText.s,
                spaceSize = 36f,
                curve = false,
            )
        }
    }

    fun onTextChange(newText: String) {
        _uiState.update { it.copy(text = newText) }
    }

    fun onHorizontalPositionChange(x: Float) {
        _uiState.update { it.copy(posX = x.coerceIn(0f, StickerRenderer.LOGICAL_WIDTH)) }
    }

    fun onVerticalSliderChange(sliderValue: Float) {
        _uiState.update { state ->
            val newY = if (state.curve) {
                StickerRenderer.LOGICAL_HEIGHT + state.fontSize * 3f - sliderValue
            } else {
                StickerRenderer.LOGICAL_HEIGHT - sliderValue
            }
            state.copy(posY = newY)
        }
    }

    fun onCanvasDrag(deltaLogicalX: Float, deltaLogicalY: Float) {
        _uiState.update { state ->
            val minY = if (state.curve) state.fontSize * 3f else 0f
            val maxY = if (state.curve) {
                StickerRenderer.LOGICAL_HEIGHT + state.fontSize * 3f
            } else {
                StickerRenderer.LOGICAL_HEIGHT
            }
            state.copy(
                posX = (state.posX + deltaLogicalX).coerceIn(0f, StickerRenderer.LOGICAL_WIDTH),
                posY = (state.posY + deltaLogicalY).coerceIn(minY, maxY),
            )
        }
    }

    fun onRotateChange(newRotate: Float) {
        val snapped = (newRotate * 5f).let { kotlin.math.round(it) / 5f }
        _uiState.update { it.copy(rotate = snapped.coerceIn(-10f, 10f)) }
    }

    fun onFontSizeChange(newSize: Float) {
        _uiState.update { it.copy(fontSize = newSize.coerceIn(10f, 100f)) }
    }

    fun onSpaceSizeChange(newSpace: Float) {
        _uiState.update { it.copy(spaceSize = newSpace.coerceIn(18f, 100f)) }
    }

    fun onCurveChange(enabled: Boolean) {
        _uiState.update {
            it.copy(
                curve = enabled,
                posX = 100f,
                posY = 150f,
            )
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun setPickerOpen(open: Boolean) {
        _uiState.update { it.copy(isPickerOpen = open) }
    }

    fun setInfoOpen(open: Boolean) {
        _uiState.update { it.copy(isInfoOpen = open) }
    }

    fun copySticker() {
        val state = _uiState.value
        val char = state.selectedCharacter ?: return
        if (state.isExporting) return

        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true) }
            val rendered = renderCurrentSticker(state)
            val ok = repository.copyStickerToClipboard(rendered, char.name)
            rendered.recycle()
            _uiState.update { it.copy(isExporting = false) }
            val msgRes = if (ok) R.string.snackbar_copied else R.string.snackbar_copy_error
            _events.emit(StickerUiEvent.ShowMessage(getApplication<Application>().getString(msgRes)))
        }
    }

    fun downloadSticker() {
        val state = _uiState.value
        val char = state.selectedCharacter ?: return
        if (state.isExporting) return

        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true) }
            val rendered = renderCurrentSticker(state)
            val ok = repository.saveStickerToGallery(rendered, char.name)
            rendered.recycle()
            _uiState.update { it.copy(isExporting = false) }
            val msgRes = if (ok) R.string.snackbar_downloaded else R.string.snackbar_download_error
            _events.emit(StickerUiEvent.ShowMessage(getApplication<Application>().getString(msgRes)))
        }
    }

    fun shareSticker() {
        val state = _uiState.value
        val char = state.selectedCharacter ?: return
        if (state.isExporting) return

        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true) }
            val rendered = renderCurrentSticker(state)
            val intent = repository.createShareIntent(rendered, char.name)
            rendered.recycle()
            _uiState.update { it.copy(isExporting = false) }
            _events.emit(StickerUiEvent.LaunchShare(intent))
        }
    }

    private suspend fun renderCurrentSticker(state: StickerUiState): Bitmap =
        withContext(Dispatchers.Default) {
            StickerRenderer.renderToBitmap(
                characterBitmap = state.characterBitmap,
                text = state.text,
                textColorHex = state.selectedCharacter?.color ?: "#FFFFFF",
                posX = state.posX,
                posY = state.posY,
                rotate = state.rotate,
                fontSize = state.fontSize,
                spaceSize = state.spaceSize,
                curve = state.curve,
                fontSet = fontSet,
            )
        }
}
