package com.agon.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.agon.app.ui.screens.AudioPlayerScreen
import com.agon.app.ui.screens.HomeScreen
import com.agon.app.ui.screens.MusicScreen
import com.agon.app.ui.screens.NetworkScreen
import com.agon.app.ui.screens.PlayerScreen
import com.agon.app.ui.screens.SearchScreen
import com.agon.app.ui.screens.SettingsScreen
import com.agon.app.ui.screens.SplashScreen
import com.agon.app.ui.screens.VideosScreen
import com.agon.app.ui.theme.AgonAppTheme
import com.agon.app.viewmodel.MediaViewModel
import com.agon.app.viewmodel.PlayerViewModel
import java.net.URLDecoder

class MainActivity : ComponentActivity() {

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            Toast.makeText(this, "Storage permission granted", Toast.LENGTH_SHORT).show()
            if (::mediaViewModel.isInitialized) {
                mediaViewModel.scanMedia()
            }
        } else {
            Toast.makeText(this, "Storage permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    private lateinit var mediaViewModel: MediaViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            AgonAppTheme {
                mediaViewModel = viewModel()
                val playerViewModel: PlayerViewModel = viewModel()

                LaunchedEffect(Unit) {
                    val allGranted = requestMediaPermissions()
                    if (allGranted) {
                        mediaViewModel.scanMedia()
                    }
                }

                MainApp(
                    mediaViewModel = mediaViewModel,
                    playerViewModel = playerViewModel,
                )
            }
        }
    }

    private fun requestMediaPermissions(): Boolean {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_MEDIA_AUDIO,
                Manifest.permission.POST_NOTIFICATIONS,
            )
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        val missingPermissions = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        return if (missingPermissions.isNotEmpty()) {
            permissionLauncher.launch(missingPermissions.toTypedArray())
            false
        } else {
            true
        }
    }
}

@Composable
fun MainApp(
    mediaViewModel: MediaViewModel,
    playerViewModel: PlayerViewModel,
) {
    val navController = rememberNavController()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = { BottomNav(navController) },
        floatingActionButton = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route
            if (currentRoute == "home" || currentRoute == "videos" || currentRoute == "music") {
                FloatingActionButton(
                    onClick = { mediaViewModel.scanMedia() },
                    containerColor = MaterialTheme.colorScheme.primary,
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayCircle,
                        contentDescription = "Scan Storage",
                        tint = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "splash",
            modifier = Modifier.padding(innerPadding),
        ) {
            composable("splash") {
                SplashScreen(navController)
            }
            composable("home") {
                HomeScreen(
                    navController = navController,
                    viewModel = mediaViewModel,
                )
            }
            composable("videos") {
                VideosScreen(
                    navController = navController,
                    viewModel = mediaViewModel,
                )
            }
            composable("music") {
                MusicScreen(
                    navController = navController,
                    viewModel = mediaViewModel,
                )
            }
            composable("network") {
                NetworkScreen(navController = navController)
            }
            composable("search") {
                SearchScreen(
                    navController = navController,
                    viewModel = mediaViewModel,
                )
            }
            composable("settings") {
                SettingsScreen(navController = navController)
            }
            composable(
                route = "player/{videoId}",
                arguments = listOf(navArgument("videoId") { type = NavType.LongType }),
            ) { backStackEntry ->
                val videoId = backStackEntry.arguments?.getLong("videoId") ?: 0L
                PlayerScreen(
                    navController = navController,
                    videoId = videoId,
                    viewModel = mediaViewModel,
                    playerViewModel = playerViewModel,
                )
            }
            composable(
                route = "player_stream/{url}",
                arguments = listOf(navArgument("url") { type = NavType.StringType }),
            ) { backStackEntry ->
                val encodedUrl = backStackEntry.arguments?.getString("url") ?: ""
                val url = URLDecoder.decode(encodedUrl, "UTF-8")
                PlayerScreen(
                    navController = navController,
                    videoId = -1,
                    viewModel = mediaViewModel,
                    playerViewModel = playerViewModel,
                )
                // Also start stream playback
                playerViewModel.playStream(url)
            }
            composable(
                route = "audio_player/{audioId}",
                arguments = listOf(navArgument("audioId") { type = NavType.LongType }),
            ) { backStackEntry ->
                val audioId = backStackEntry.arguments?.getLong("audioId") ?: 0L
                AudioPlayerScreen(
                    navController = navController,
                    audioId = audioId,
                    viewModel = mediaViewModel,
                    playerViewModel = playerViewModel,
                )
            }
        }
    }
}

@Composable
fun BottomNav(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val items = listOf(
        Triple("home", "Home", Icons.Default.Home),
        Triple("videos", "Videos", Icons.Default.VideoLibrary),
        Triple("music", "Music", Icons.Default.MusicNote),
        Triple("network", "Network", Icons.Default.Wifi),
        Triple("settings", "Settings", Icons.Default.Settings),
    )

    if (currentRoute in listOf("home", "videos", "music", "network", "settings")) {
        NavigationBar {
            items.forEach { (route, label, icon) ->
                NavigationBarItem(
                    icon = { Icon(icon, contentDescription = label) },
                    label = { Text(label) },
                    selected = currentRoute == route,
                    onClick = {
                        navController.navigate(route) {
                            popUpTo("home") { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }
        }
    }
}
