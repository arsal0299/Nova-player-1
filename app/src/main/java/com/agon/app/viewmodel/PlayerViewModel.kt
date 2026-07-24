package com.agon.app.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.agon.app.data.model.AudioItem
import com.agon.app.data.model.MediaItem as AppMediaItem
import com.agon.app.data.model.PlaybackState
import com.agon.app.data.model.VideoItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class PlayerViewModel(application: Application) : AndroidViewModel(application) {

    val exoPlayer: ExoPlayer = ExoPlayer.Builder(application).build().apply {
        playWhenReady = true
    }

    private val _playbackState = MutableStateFlow(PlaybackState())
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private val _currentVideo = MutableStateFlow<VideoItem?>(null)
    val currentVideo: StateFlow<VideoItem?> = _currentVideo.asStateFlow()

    private val _currentAudio = MutableStateFlow<AudioItem?>(null)
    val currentAudio: StateFlow<AudioItem?> = _currentAudio.asStateFlow()

    private val listener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            updateState()
        }

        override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
            updateState()
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            updateState()
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            updateState()
        }
    }

    init {
        exoPlayer.addListener(listener)
        startProgressUpdates()
    }

    private fun startProgressUpdates() {
        viewModelScope.launch {
            while (isActive) {
                updateState()
                delay(500)
            }
        }
    }

    private fun updateState() {
        _playbackState.value = PlaybackState(
            isPlaying = exoPlayer.isPlaying,
            currentPosition = exoPlayer.currentPosition,
            duration = exoPlayer.duration.coerceAtLeast(0L),
            bufferedPosition = exoPlayer.bufferedPosition,
            playbackSpeed = exoPlayer.playbackParameters.speed,
            isLoading = exoPlayer.isLoading,
            currentMediaItem = _currentVideo.value ?: _currentAudio.value,
            repeatMode = exoPlayer.repeatMode,
            shuffleMode = exoPlayer.shuffleModeEnabled,
        )
    }

    fun playVideo(video: VideoItem, playlist: List<VideoItem> = emptyList()) {
        _currentVideo.value = video
        _currentAudio.value = null
        val mediaItems = if (playlist.isEmpty()) {
            listOf(MediaItem.fromUri(video.uri))
        } else {
            playlist.map { MediaItem.fromUri(it.uri) }
        }
        exoPlayer.setMediaItems(mediaItems)
        val startIndex = playlist.indexOfFirst { it.id == video.id }.coerceAtLeast(0)
        exoPlayer.seekTo(startIndex, 0L)
        exoPlayer.prepare()
        exoPlayer.play()
        updateState()
    }

    fun playAudio(audio: AudioItem, playlist: List<AudioItem> = emptyList()) {
        _currentAudio.value = audio
        _currentVideo.value = null
        val mediaItems = if (playlist.isEmpty()) {
            listOf(MediaItem.fromUri(audio.uri))
        } else {
            playlist.map { MediaItem.fromUri(it.uri) }
        }
        exoPlayer.setMediaItems(mediaItems)
        val startIndex = playlist.indexOfFirst { it.id == audio.id }.coerceAtLeast(0)
        exoPlayer.seekTo(startIndex, 0L)
        exoPlayer.prepare()
        exoPlayer.play()
        updateState()
    }

    fun playStream(url: String, title: String = "Network Stream") {
        _currentVideo.value = null
        _currentAudio.value = null
        exoPlayer.setMediaItem(MediaItem.fromUri(Uri.parse(url)))
        exoPlayer.prepare()
        exoPlayer.play()
        updateState()
    }

    fun togglePlayPause() {
        if (exoPlayer.isPlaying) {
            exoPlayer.pause()
        } else {
            exoPlayer.play()
        }
    }

    fun seekTo(positionMs: Long) {
        exoPlayer.seekTo(positionMs.coerceIn(0L, exoPlayer.duration.coerceAtLeast(0L)))
    }

    fun seekForward(milliseconds: Long = 10000) {
        exoPlayer.seekTo(exoPlayer.currentPosition + milliseconds)
    }

    fun seekBackward(milliseconds: Long = 10000) {
        exoPlayer.seekTo((exoPlayer.currentPosition - milliseconds).coerceAtLeast(0L))
    }

    fun setPlaybackSpeed(speed: Float) {
        exoPlayer.setPlaybackSpeed(speed)
    }

    fun setRepeatMode(repeatMode: Int) {
        exoPlayer.repeatMode = repeatMode
    }

    fun toggleShuffle() {
        exoPlayer.shuffleModeEnabled = !exoPlayer.shuffleModeEnabled
    }

    fun playNext() {
        if (exoPlayer.hasNextMediaItem()) {
            exoPlayer.seekToNextMediaItem()
        }
    }

    fun playPrevious() {
        if (exoPlayer.hasPreviousMediaItem()) {
            exoPlayer.seekToPreviousMediaItem()
        }
    }

    fun setVolume(volume: Float) {
        exoPlayer.volume = volume.coerceIn(0f, 1f)
    }

    override fun onCleared() {
        super.onCleared()
        exoPlayer.removeListener(listener)
        exoPlayer.release()
    }
}
