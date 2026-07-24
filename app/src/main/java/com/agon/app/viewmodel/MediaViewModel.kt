package com.agon.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.agon.app.data.MediaScanner
import com.agon.app.data.model.AudioItem
import com.agon.app.data.model.FolderItem
import com.agon.app.data.model.Playlist
import com.agon.app.data.model.VideoItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MediaUiState(
    val isLoading: Boolean = false,
    val videos: List<VideoItem> = emptyList(),
    val audio: List<AudioItem> = emptyList(),
    val folders: List<FolderItem> = emptyList(),
    val playlists: List<Playlist> = emptyList(),
    val favorites: List<VideoItem> = emptyList(),
    val history: List<VideoItem> = emptyList(),
    val searchQuery: String = "",
    val errorMessage: String? = null,
)

class MediaViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(MediaUiState())
    val uiState: StateFlow<MediaUiState> = _uiState.asStateFlow()

    init {
        loadSamplePlaylists()
    }

    fun scanMedia() {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            try {
                val context = getApplication<Application>().applicationContext
                val videos = MediaScanner.scanVideos(context)
                val audio = MediaScanner.scanAudio(context)
                val folders = MediaScanner.extractFolders(videos, audio)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    videos = videos,
                    audio = audio,
                    folders = folders,
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to scan media: ${e.message}",
                )
            }
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    val filteredVideos: List<VideoItem>
        get() = if (_uiState.value.searchQuery.isBlank()) {
            _uiState.value.videos
        } else {
            _uiState.value.videos.filter {
                it.title.contains(_uiState.value.searchQuery, ignoreCase = true) ||
                    it.folderName.contains(_uiState.value.searchQuery, ignoreCase = true)
            }
        }

    val filteredAudio: List<AudioItem>
        get() = if (_uiState.value.searchQuery.isBlank()) {
            _uiState.value.audio
        } else {
            _uiState.value.audio.filter {
                it.title.contains(_uiState.value.searchQuery, ignoreCase = true) ||
                    it.artist.contains(_uiState.value.searchQuery, ignoreCase = true) ||
                    it.album.contains(_uiState.value.searchQuery, ignoreCase = true)
            }
        }

    fun addToFavorites(video: VideoItem) {
        val current = _uiState.value.favorites.toMutableList()
        if (!current.any { it.id == video.id }) {
            current.add(0, video)
            _uiState.value = _uiState.value.copy(favorites = current)
        }
    }

    fun removeFromFavorites(video: VideoItem) {
        _uiState.value = _uiState.value.copy(
            favorites = _uiState.value.favorites.filter { it.id != video.id },
        )
    }

    fun addToHistory(video: VideoItem) {
        val current = _uiState.value.history.toMutableList()
        current.removeAll { it.id == video.id }
        current.add(0, video)
        _uiState.value = _uiState.value.copy(history = current.take(50))
    }

    fun createPlaylist(name: String) {
        val current = _uiState.value.playlists.toMutableList()
        current.add(
            Playlist(
                id = System.currentTimeMillis(),
                name = name,
            ),
        )
        _uiState.value = _uiState.value.copy(playlists = current)
    }

    private fun loadSamplePlaylists() {
        _uiState.value = _uiState.value.copy(
            playlists = listOf(
                Playlist(1, "Favorites", 12),
                Playlist(2, "Watch Later", 5),
                Playlist(3, "Workout", 8),
            ),
        )
    }
}
