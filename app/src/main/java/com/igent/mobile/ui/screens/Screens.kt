package com.igent.mobile.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.igent.mobile.ui.Screen
import com.igent.mobile.ui.auth.AuthScreen
import com.igent.mobile.ui.chat.ChatScreen
import com.igent.mobile.ui.analysis.FinancialAnalysisScreen
import com.igent.mobile.ui.psx.PsxScreen
import com.igent.mobile.ui.rss.RssScreen
import com.igent.mobile.ui.idea.IdeaCardsScreen
import com.igent.mobile.ui.settings.NotificationSettingsScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IgentAppShell(initialTargetScreen: String? = null) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Handle deep linking on launch once navController is ready
    LaunchedEffect(initialTargetScreen) {
        if (!initialTargetScreen.isNullOrBlank()) {
            val destination = when (initialTargetScreen.lowercase()) {
                "stocks", "stock" -> Screen.Stocks.route
                "rss", "news" -> Screen.Rss.route
                else -> Screen.Chat.route
            }
            navController.navigate(destination) {
                popUpTo(Screen.Auth.route) { inclusive = false }
                launchSingleTop = true
            }
        }
    }

    val showBottomBar = currentRoute != Screen.Auth.route

    Scaffold(
        topBar = {
            if (showBottomBar) {
                TopAppBar(
                    title = { Text(text = "Igent Mobile") },
                    actions = {
                        IconButton(onClick = { navController.navigate(Screen.Settings.route) }) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        },
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    val items = listOf(
                        Triple(Screen.Chat, "Chat", Icons.Default.Chat),
                        Triple(Screen.Stocks, "Stocks", Icons.Default.TrendingUp),
                        Triple(Screen.Analysis, "Audit", Icons.Default.Analytics),
                        Triple(Screen.IdeaCards, "Ideas", Icons.Default.Lightbulb),
                        Triple(Screen.Rss, "RSS", Icons.Default.RssFeed),
                        Triple(Screen.Settings, "Settings", Icons.Default.Settings)
                    )
                    items.forEach { (screen, label, icon) ->
                        NavigationBarItem(
                            icon = { Icon(icon, contentDescription = label) },
                            label = { Text(label) },
                            selected = currentRoute == screen.route,
                            onClick = {
                                if (currentRoute != screen.route) {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Auth.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Auth.route) {
                AuthScreen(
                    onNavigateToMain = {
                        navController.navigate(Screen.Chat.route) {
                            popUpTo(Screen.Auth.route) { inclusive = true }
                        }
                    },
                    onNavigateToSettings = {
                        navController.navigate(Screen.Settings.route) {
                            popUpTo(Screen.Auth.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.Chat.route) { ChatScreen() }
            composable(Screen.Stocks.route) { PsxScreen() }
            composable(Screen.Analysis.route) { FinancialAnalysisScreen() }
            composable(Screen.IdeaCards.route) { IdeaCardsScreen() }
            composable(Screen.Rss.route) { RssScreen() }
            composable(Screen.Settings.route) {
                SettingsHubScreen(
                    onNavigateToNotificationSettings = {
                        navController.navigate(Screen.NotificationSettings.route)
                    },
                    onNavigateToAuth = {
                        navController.navigate(Screen.Auth.route)
                    }
                )
            }
            composable(Screen.NotificationSettings.route) {
                NotificationSettingsScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsHubScreen(
    onNavigateToNotificationSettings: () -> Unit,
    onNavigateToAuth: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings Hub") }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                onClick = onNavigateToAuth,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Column {
                            Text(
                                text = "Authentication & API Keys",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Manage API keys and re-authenticate",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Card(
                onClick = onNavigateToNotificationSettings,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Column {
                            Text(
                                text = "Notification Preferences",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Configure push, stock, and RSS alerts",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
