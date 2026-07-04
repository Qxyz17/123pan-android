package com.pan123nextgen.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.pan123nextgen.android.ui.cloud.CloudScreen
import com.pan123nextgen.android.ui.files.FileListScreen
import com.pan123nextgen.android.ui.login.LoginScreen
import com.pan123nextgen.android.ui.settings.SettingsScreen
import com.pan123nextgen.android.ui.theme.Pan123NextGenTheme
import com.pan123nextgen.android.ui.transfer.TransferScreen

sealed class Screen(val route: String, val title: String, val icon: ImageVector, val selectedIcon: ImageVector) {
    data object Files : Screen("files", "文件", Icons.Outlined.Folder, Icons.Filled.Folder)
    data object Transfer : Screen("transfer", "传输", Icons.Outlined.Sync, Icons.Filled.Sync)
    data object Cloud : Screen("cloud", "云盘", Icons.Outlined.Cloud, Icons.Filled.Cloud)
    data object Settings : Screen("settings", "设置", Icons.Outlined.Settings, Icons.Filled.Settings)
    data object Login : Screen("login", "登录", Icons.Outlined.Login, Icons.Filled.Login)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Pan123NextGenTheme {
                Pan123NextGenApp()
            }
        }
    }
}

@Composable
fun Pan123NextGenApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomNavItems = listOf(Screen.Files, Screen.Transfer, Screen.Cloud, Screen.Settings)
    val showBottomBar = currentRoute in bottomNavItems.map { it.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                Pan123BottomNavBar(
                    currentRoute = currentRoute,
                    items = bottomNavItems,
                    onItemSelected = { screen ->
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Login.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Login.route) {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(Screen.Files.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.Files.route) {
                FileListScreen()
            }
            composable(Screen.Transfer.route) {
                TransferScreen()
            }
            composable(Screen.Cloud.route) {
                CloudScreen(
                    onLogout = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.Settings.route) {
                SettingsScreen()
            }
        }
    }
}

@Composable
fun Pan123BottomNavBar(
    currentRoute: String?,
    items: List<Screen>,
    onItemSelected: (Screen) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = NavigationBarDefaults.Elevation
    ) {
        items.forEach { screen ->
            val selected = currentRoute == screen.route
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = if (selected) screen.selectedIcon else screen.icon,
                        contentDescription = screen.title
                    )
                },
                label = { Text(screen.title) },
                selected = selected,
                onClick = { onItemSelected(screen) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    }
}