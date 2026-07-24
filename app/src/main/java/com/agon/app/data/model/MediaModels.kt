package com.agon.app.data.model

import android.net.Uri

sealed class MediaItem(
    open val id: Long,
    open val title: String,
    open val uri: Uri,
    open val path: String,
    open val durationMs: Long,
    open val size: Long,
    open val dateAdded: Long,
) {
    abstract val type: MediaType
}

data class VideoItem(
    override val id: Long,
    override val title: String,
    override val uri: Uri,
    override val path: String,
    override val durationMs: Long,
    override val size: Long,
    override val dateAdded: Long,
    val width: Int = 0,
    val height: Int = 0,
    val resolution: String = "",
    val thumbnailUri: Uri? = null,
    val folderName: String = "",
) : MediaItem(id, title, uri, path, durationMs, size, dateAdded) {
    override val type: MediaType = MediaType.VIDEO
}

data class AudioItem(
    override val id: Long,
    override val title: String,
    override val uri: Uri,
    override val path: String,
    override val durationMs: Long,
    override val size: Long,
    override val dateAdded: Long,
    val artist: String = "Unknown Artist",
    val album: String = "Unknown Album",
    val albumArtUri: Uri? = null,
    val folderName: String = "",
) : MediaItem(id, title, uri, path, durationMs, size, dateAdded) {
    override val type: MediaType = MediaType.AUDIO
}

enum class MediaType {
    VIDEO, AUDIO
}

data class FolderItem(
    val name: String,
    val path: String,
    val videoCount: Int = 0,
    val audioCount: Int = 0,
    val thumbnailUri: Uri? = null,
)

data class Playlist(
    val id: Long,
    val name: String,
    val itemCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
)

data class NetworkStream(
    val id: Long,
    val name: String,
    val url: String,
    val type: StreamType = StreamType.HTTP,
)

enum class StreamType {
    HTTP, HTTPS, FTP, SMB, DLNA, NAS, WebDAV, RTSP
}

data class PlaybackState(
    val isPlaying: Boolean = false,
    val currentPosition: Long = 0L,
    val duration: Long = 0L,
    val bufferedPosition: Long = 0L,
    val playbackSpeed: Float = 1f,
    val isLoading: Boolean = false,
    val currentMediaItem: MediaItem? = null,
    val repeatMode: Int = 0,
    val shuffleMode: Boolean = false,
)
