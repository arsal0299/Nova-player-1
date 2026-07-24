package com.agon.app.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.agon.app.data.MediaScanner
import com.agon.app.data.model.AudioItem
import com.agon.app.data.model.FolderItem
import com.agon.app.data.model.Playlist
import com.agon.app.data.model.VideoItem
import com.agon.app.ui.components.AudioListItem
import com.agon.app.ui.components.EmptyState
import com.agon.app.ui.components.FolderCard
import com.agon.app.ui.components.LoadingState
import com.agon.app.ui.components.PlaylistCard
import com.agon.app.ui.components.VideoCard
import com.agon.app.viewmodel.MediaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavHostController,
    viewModel: MediaViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Videos", "Audio", "Folders", "Playlist", "Favorites", "History")

    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        TopAppBar(
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Nova Player",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                }
            },
            actions = {
                IconButton(onClick = { navController.navigate("search") }) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                    )
                }
                IconButton(onClick = { }) {
                    Icon(
                        imageVector = Icons.Default.Sort,
                        contentDescription = "Sort",
                    )
                }
                IconButton(onClick = { navController.navigate("settings") }) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background,
                titleContentColor = MaterialTheme.colorScheme.onBackground,
                actionIconContentColor = MaterialTheme.colorScheme.onBackground,
            ),
        )

        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.primary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = MaterialTheme.colorScheme.primary,
                    height = 3.dp,
                )
            },
            edgePadding = 16.dp,
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                        )
                    },
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
        ) {
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    fadeIn(tween(300)) togetherWith fadeOut(tween(300))
                },
                label = "home_tabs",
            ) { tab ->
                when (tab) {
                    0 -> VideosTab(
                        videos = uiState.videos,
                        isLoading = uiState.isLoading,
                        onVideoClick = { video ->
                            viewModel.addToHistory(video)
                            navController.navigate("player/${video.id}")
                        },
                        onFavoriteToggle = { video ->
                            if (uiState.favorites.any { it.id == video.id }) {
                                viewModel.removeFromFavorites(video)
                            } else {
                                viewModel.addToFavorites(video)
                            }
                        },
                        favorites = uiState.favorites,
                    )

                    1 -> AudioTab(
                        audio = uiState.audio,
                        isLoading = uiState.isLoading,
                        onAudioClick = { audio ->
                            navController.navigate("audio_player/${audio.id}")
                        },
                    )

                    2 -> FoldersTab(
                        folders = uiState.folders,
                        isLoading = uiState.isLoading,
                    )

                    3 -> PlaylistTab(
                        playlists = uiState.playlists,
                        onCreatePlaylist = { viewModel.createPlaylist(it) },
                    )

                    4 -> FavoritesTab(
                        favorites = uiState.favorites,
                        onVideoClick = { navController.navigate("player/${it.id}") },
                        onRemove = { viewModel.removeFromFavorites(it) },
                    )

                    5 -> HistoryTab(
                        history = uiState.history,
                        onVideoClick = { navController.navigate("player/${it.id}") },
                    )
                }
            }
        }
    }
}

@Composable
private fun VideosTab(
    videos: List<VideoItem>,
    isLoading: Boolean,
    onVideoClick: (VideoItem) -> Unit,
    onFavoriteToggle: (VideoItem) -> Unit,
    favorites: List<VideoItem>,
) {
    if (isLoading) {
        LoadingState()
        return
    }
    if (videos.isEmpty()) {
        EmptyState(
            icon = Icons.Default.Movie,
            title = "No videos found",
            subtitle = "Tap the scan button to discover videos on your device.",
        )
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        items(videos) { video ->
            VideoCard(
                video = video,
                onClick = { onVideoClick(video) },
                onFavoriteToggle = { onFavoriteToggle(video) },
                isFavorite = favorites.any { it.id == video.id },
            )
        }
    }
}

@Composable
private fun AudioTab(
    audio: List<AudioItem>,
    isLoading: Boolean,
    onAudioClick: (AudioItem) -> Unit,
) {
    if (isLoading) {
        LoadingState()
        return
    }
    if (audio.isEmpty()) {
        EmptyState(
            icon = Icons.Default.MusicNote,
            title = "No music found",
            subtitle = "Tap the scan button to discover music on your device.",
        )
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(audio) { item ->
            AudioListItem(
                audio = item,
                onClick = { onAudioClick(item) },
            )
        }
    }
}

@Composable
private fun FoldersTab(
    folders: List<FolderItem>,
    isLoading: Boolean,
) {
    if (isLoading) {
        LoadingState()
        return
    }
    if (folders.isEmpty()) {
        EmptyState(
            icon = androidx.compose.material.icons.Icons.Default.Folder,
            title = "No folders found",
            subtitle = "Media folders will appear here after scanning.",
        )
        return
    }
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 150.dp),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(folders) { folder ->
            FolderCard(
                folder = folder,
                onClick = { },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlaylistTab(
    playlists: List<Playlist>,
    onCreatePlaylist: (String) -> Unit,
) {
    var showDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            CardWithAction(
                title = "Create Playlist",
                subtitle = "Organize your favorite media",
                onClick = { showDialog = true },
            )
        }
        items(playlists) { playlist ->
            PlaylistCard(
                playlist = playlist,
                onClick = { },
            )
        }
    }

    if (showDialog) {
        CreatePlaylistDialog(
            onDismiss = { showDialog = false },
            onCreate = {
                onCreatePlaylist(it)
                showDialog = false
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreatePlaylistDialog(
    onDismiss: () -> Unit,
    onCreate: (String) -> Unit,
) {
    var text by remember { mutableStateOf("") }
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Playlist") },
        text = {
            androidx.compose.material3.OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Playlist name") },
                singleLine = true,
            )
        },
        confirmButton = {
            androidx.compose.material3.TextButton(
                onClick = { if (text.isNotBlank()) onCreate(text) },
                enabled = text.isNotBlank(),
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}

@Composable
private fun CardWithAction(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    androidx.compose.material3.Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
        ),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.PlaylistPlay,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp),
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun FavoritesTab(
    favorites: List<VideoItem>,
    onVideoClick: (VideoItem) -> Unit,
    onRemove: (VideoItem) -> Unit,
) {
    if (favorites.isEmpty()) {
        EmptyState(
            icon = Icons.Default.FavoriteBorder,
            title = "No favorites yet",
            subtitle = "Tap the heart icon on any video to add it here.",
        )
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        items(favorites) { video ->
            VideoCard(
                video = video,
                onClick = { onVideoClick(video) },
                onFavoriteToggle = { onRemove(video) },
                isFavorite = true,
            )
        }
    }
}

@Composable
private fun HistoryTab(
    history: List<VideoItem>,
    onVideoClick: (VideoItem) -> Unit,
) {
    if (history.isEmpty()) {
        EmptyState(
            icon = Icons.Default.History,
            title = "No watch history",
            subtitle = "Videos you play will appear here.",
        )
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        items(history) { video ->
            VideoCard(
                video = video,
                onClick = { onVideoClick(video) },
            )
        }
    }
}
