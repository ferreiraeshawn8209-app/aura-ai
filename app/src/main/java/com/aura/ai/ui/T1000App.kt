package com.aura.ai.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.windowInsetsPadding
import com.aura.ai.ui.chat.ChatScreen
import com.aura.ai.ui.memory.MemoryScreen
import com.aura.ai.ui.settings.SettingsScreen
import com.aura.ai.ui.skills.SkillsScreen

private enum class Dest(val route: String, val label: String, val icon: ImageVector) {
    Chat("chat", "Agent", Icons.Outlined.Forum),
    Skills("skills", "Skills", Icons.Outlined.AutoAwesome),
    Memory("memory", "Memory", Icons.Outlined.Memory),
    Settings("settings", "Settings", Icons.Outlined.Settings),
}

@Composable
fun T1000App() {
    val nav = rememberNavController()
    val backStack by nav.currentBackStackEntryAsState()
    val current = backStack?.destination

    Scaffold(
        bottomBar = {
            NavigationBar {
                Dest.entries.forEach { dest ->
                    val selected = current?.hierarchy?.any { it.route == dest.route } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            nav.navigate(dest.route) {
                                popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(dest.icon, contentDescription = dest.label) },
                        label = { Text(dest.label) },
                    )
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = nav,
            startDestination = Dest.Chat.route,
            modifier = Modifier.padding(padding),
        ) {
            composable(Dest.Chat.route) { ChatScreen() }
            composable(Dest.Skills.route) { SkillsScreen() }
            composable(Dest.Memory.route) { MemoryScreen() }
            composable(Dest.Settings.route) { SettingsScreen() }
        }
    }
}
