package com.legal.transcriber.ui.navigation

import android.net.Uri
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.legal.transcriber.shared.auth.AuthService
import com.legal.transcriber.shared.viewmodel.TranscriptionViewModel
import com.legal.transcriber.ui.screens.ChatConversationScreen
import com.legal.transcriber.ui.screens.ChatScreen
import com.legal.transcriber.ui.screens.HistoryScreen
import com.legal.transcriber.ui.screens.HomeScreen
import com.legal.transcriber.ui.screens.PaywallScreen
import com.legal.transcriber.ui.screens.ProfileScreen
import com.legal.transcriber.ui.screens.TranscriptScreen
import com.legal.transcriber.ui.theme.CardWhite
import com.legal.transcriber.ui.theme.Cream
import com.legal.transcriber.ui.theme.Gold
import com.legal.transcriber.ui.theme.MutedInk
import com.legal.transcriber.ui.theme.Navy
import com.legal.transcriber.ui.theme.Separator

object Routes {
    const val HOME = "home"
    const val HISTORY = "history"
    const val TRANSCRIPT = "transcript"
    const val CHAT = "chat"
    const val CHAT_CONVERSATION = "chat_conversation"
    const val PROFILE = "profile"
    const val PAYWALL = "paywall"
}

@Composable
fun MainTabView(
    viewModel: TranscriptionViewModel,
    authService: AuthService,
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val showBottomBar = currentRoute in listOf(Routes.HOME, Routes.HISTORY, Routes.CHAT, Routes.PROFILE)

    val showPaywall: () -> Unit = { navController.navigate(Routes.PAYWALL) }

    Scaffold(
        containerColor = Cream,
        bottomBar = {
            if (showBottomBar) {
                BottomNavBar(
                    currentRoute = currentRoute ?: Routes.HOME,
                    onNavigate = { route ->
                        viewModel.resetTranscriptionState()
                        navController.navigate(route) {
                            popUpTo(navController.graph.startDestinationId) {
                                inclusive = false
                            }
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(padding),
        ) {
            composable(Routes.HOME) {
                HomeScreen(
                    viewModel = viewModel,
                    onNavigateToHistory = { navController.navigate(Routes.HISTORY) },
                    onTranscriptionComplete = { fileName ->
                        val encoded = Uri.encode(fileName)
                        navController.navigate("${Routes.TRANSCRIPT}/$encoded") {
                            launchSingleTop = true
                        }
                    },
                    onShowPaywall = showPaywall,
                )
            }
            composable(Routes.HISTORY) {
                HistoryScreen(
                    viewModel = viewModel,
                    onOpenTranscript = { history ->
                        viewModel.resetTranscriptionState()
                        viewModel.loadTranscriptFromHistory(history)
                        val encoded = Uri.encode(history.fileName)
                        navController.navigate("${Routes.TRANSCRIPT}/$encoded")
                    },
                )
            }
            composable(Routes.CHAT) {
                ChatScreen(
                    viewModel = viewModel,
                    onOpenConversation = { navController.navigate(Routes.CHAT_CONVERSATION) },
                )
            }
            composable(Routes.CHAT_CONVERSATION) {
                ChatConversationScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                )
            }
            composable(Routes.PROFILE) {
                ProfileScreen(
                    viewModel = viewModel,
                    authService = authService,
                    onShowPaywall = showPaywall,
                )
            }
            composable("${Routes.TRANSCRIPT}/{fileName}") { backStackEntry ->
                val fileName = backStackEntry.arguments?.getString("fileName") ?: ""
                TranscriptScreen(
                    viewModel = viewModel,
                    fileName = fileName,
                    onBack = {
                        viewModel.resetTranscriptionState()
                        navController.popBackStack()
                    },
                    onShowPaywall = showPaywall,
                )
            }
            composable(Routes.PAYWALL) {
                PaywallScreen(
                    onDismiss = { navController.popBackStack() },
                )
            }
        }
    }
}

@Composable
private fun BottomNavBar(
    currentRoute: String,
    onNavigate: (String) -> Unit,
) {
    val items = listOf(
        Triple(Routes.HOME, "Home", Icons.Rounded.Home),
        Triple(Routes.CHAT, "AI Chat", Icons.AutoMirrored.Rounded.Chat),
        Triple(Routes.HISTORY, "History", Icons.Rounded.History),
        Triple(Routes.PROFILE, "Profile", Icons.Rounded.AccountCircle),
    )

    HorizontalDivider(thickness = 0.5.dp, color = Separator.copy(alpha = 0.5f))
    NavigationBar(
        containerColor = CardWhite,
        tonalElevation = 0.dp,
    ) {
        items.forEach { (route, label, icon) ->
            val selected = currentRoute == route
            NavigationBarItem(
                selected = selected,
                onClick = { onNavigate(route) },
                icon = {
                    Icon(
                        icon,
                        contentDescription = label,
                        modifier = Modifier.size(24.dp),
                    )
                },
                label = {
                    Text(
                        label,
                        fontFamily = FontFamily.Default,
                        fontSize = 11.sp,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Navy,
                    selectedTextColor = Navy,
                    unselectedIconColor = MutedInk,
                    unselectedTextColor = MutedInk,
                    indicatorColor = Gold.copy(alpha = 0.16f),
                ),
            )
        }
    }
}
