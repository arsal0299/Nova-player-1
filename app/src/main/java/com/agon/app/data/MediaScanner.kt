package com.agon.app.data

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import com.agon.app.data.model.AudioItem
import com.agon.app.data.model.FolderItem
import com.agon.app.data.model.VideoItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object MediaScanner {

    private val VIDEO_EXTENSIONS = setOf(
        "mp4", "mkv", "avi", "mov", "3gp", "webm", "flv", "wmv", "m4v", "ts", "mpg", "mpeg",
    )

    suspend fun scanVideos(context: Context): List<VideoItem> = withContext(Dispatchers.IO) {
        val videos = mutableListOf<VideoItem>()
        val contentResolver: ContentResolver = context.contentResolver

        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.TITLE,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.DATE_ADDED,
            MediaStore.Video.Media.WIDTH,
            MediaStore.Video.Media.HEIGHT,
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
        )

        val sortOrder = "${MediaStore.Video.Media.DATE_ADDED} DESC"

        contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            sortOrder,
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE)
            val displayNameColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
            val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)
            val widthColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.WIDTH)
            val heightColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.HEIGHT)
            val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
            val bucketColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_DISPLAY_NAME)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val title = cursor.getString(titleColumn)
                    ?: cursor.getString(displayNameColumn)?.substringBeforeLast(".") ?: "Unknown"
                val duration = cursor.getLong(durationColumn)
                val size = cursor.getLong(sizeColumn)
                val dateAdded = cursor.getLong(dateAddedColumn)
                val width = cursor.getInt(widthColumn)
                val height = cursor.getInt(heightColumn)
                val path = cursor.getString(dataColumn) ?: ""
                val folder = cursor.getString(bucketColumn) ?: ""

                val contentUri = Uri.withAppendedPath(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id.toString())
                val thumbUri = Uri.withAppendedPath(MediaStore.Video.Thumbnails.EXTERNAL_CONTENT_URI, id.toString())

                videos.add(
                    VideoItem(
                        id = id,
                        title = title,
                        uri = contentUri,
                        path = path,
                        durationMs = duration,
                        size = size,
                        dateAdded = dateAdded,
                        width = width,
                        height = height,
                        resolution = if (width > 0 && height > 0) "${width}x$height" else "",
                        thumbnailUri = thumbUri,
                        folderName = folder,
                    ),
                )
            }
        }

        // Fallback: MediaStore ka database purane ya newly-copied files ko
        // index nahi karta jab tak system scan na kare. Agar MediaStore se
        // kuch na mile, to seedha storage folders scan karo.
        if (videos.isEmpty()) {
            videos.addAll(scanVideosFromFileSystem())
        }

        videos
    }

    private fun scanVideosFromFileSystem(): List<VideoItem> {
        val results = mutableListOf<VideoItem>()
        val root = Environment.getExternalStorageDirectory()
        val commonFolders = listOf(
            root,
            File(root, "Movies"),
            File(root, "DCIM"),
            File(root, "Download"),
            File(root, "Downloads"),
            File(root, "WhatsApp/Media/WhatsApp Video"),
            File(root, "Android/media/com.whatsapp/WhatsApp/Media/WhatsApp Video"),
            File(root, "Telegram/Telegram Video"),
            File(root, "Movies/CameraRecordings"),
        )

        val visited = HashSet<String>()
        var idCounter = -1L

        fun walk(dir: File, depth: Int) {
            if (depth > 4) return
            val canonical = try { dir.canonicalPath } catch (e: Exception) { return }
            if (!visited.add(canonical)) return
            val children = dir.listFiles() ?: return
            for (file in children) {
                if (file.isDirectory) {
                    walk(file, depth + 1)
                } else if (file.extension.lowercase() in VIDEO_EXTENSIONS && file.length() > 0) {
                    val uri = Uri.fromFile(file)
                    results.add(
                        VideoItem(
                            id = idCounter--,
                            title = file.nameWithoutExtension,
                            uri = uri,
                            path = file.absolutePath,
                            durationMs = 0L,
                            size = file.length(),
                            dateAdded = file.lastModified() / 1000,
                            resolution = "",
                            thumbnailUri = uri,
                            folderName = file.parentFile?.name ?: "",
                        ),
                    )
                }
            }
        }

        for (folder in commonFolders) {
            if (folder.exists() && folder.isDirectory) {
                walk(folder, 0)
            }
        }

        return results
    }

    suspend fun scanAudio(context: Context): List<AudioItem> = withContext(Dispatchers.IO) {
        val audioList = mutableListOf<AudioItem>()
        val contentResolver: ContentResolver = context.contentResolver

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.BUCKET_DISPLAY_NAME,
        )

        val sortOrder = "${MediaStore.Audio.Media.DATE_ADDED} DESC"

        contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            "${MediaStore.Audio.Media.IS_MUSIC} != 0",
            null,
            sortOrder,
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val displayNameColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
            val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
            val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val bucketColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.BUCKET_DISPLAY_NAME)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val title = cursor.getString(titleColumn)
                    ?: cursor.getString(displayNameColumn)?.substringBeforeLast(".") ?: "Unknown"
                val duration = cursor.getLong(durationColumn)
                val size = cursor.getLong(sizeColumn)
                val dateAdded = cursor.getLong(dateAddedColumn)
                val artist = cursor.getString(artistColumn) ?: "Unknown Artist"
                val album = cursor.getString(albumColumn) ?: "Unknown Album"
                val path = cursor.getString(dataColumn) ?: ""
                val folder = cursor.getString(bucketColumn) ?: ""

                val contentUri = Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id.toString())
                val albumArtUri = Uri.parse("content://media/external/audio/albumart")
                    .buildUpon()
                    .appendPath(id.toString())
                    .build()

                audioList.add(
                    AudioItem(
                        id = id,
                        title = title,
                        uri = contentUri,
                        path = path,
                        durationMs = duration,
                        size = size,
                        dateAdded = dateAdded,
                        artist = artist,
                        album = album,
                        albumArtUri = albumArtUri,
                        folderName = folder,
                    ),
                )
            }
        }

        audioList
    }

    fun extractFolders(videos: List<VideoItem>, audio: List<AudioItem>): List<FolderItem> {
        val grouped = mutableMapOf<String, Pair<String, MutableList<VideoItem>>>()
        videos.forEach { video ->
            val path = video.path.substringBeforeLast("/", "")
            val name = video.folderName.ifBlank { path.substringAfterLast("/", "Internal") }
            grouped.getOrPut(path) { name to mutableListOf() }.second.add(video)
        }
        return grouped.map { (path, pair) ->
            FolderItem(
                name = pair.first,
                path = path,
                videoCount = pair.second.size,
                audioCount = audio.count { it.path.substringBeforeLast("/", "") == path },
                thumbnailUri = pair.second.firstOrNull()?.thumbnailUri,
            )
        }.sortedByDescending { it.videoCount + it.audioCount }
    }

    fun formatDuration(ms: Long): String {
        if (ms <= 0) return "00:00"
        val totalSeconds = ms / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return if (hours > 0) {
            String.format("%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }

    fun formatSize(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        var size = bytes.toDouble()
        var unitIndex = 0
        while (size >= 1024 && unitIndex < units.size - 1) {
            size /= 1024
            unitIndex++
        }
        return String.format("%.1f %s", size, units[unitIndex])
    }
}
