package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val viewModel = ViewModelProvider(this)[MainViewModel::class.java]
            val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
            val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()

            MyApplicationTheme(darkTheme = isDarkMode) {
                if (currentUser == null) {
                    AuthScreen(viewModel = viewModel, onLoginSuccess = {})
                } else {
                    var bottomBarTab by remember { mutableStateOf(0) } // 0: Dashboard, 1: Settings

                    Scaffold(
                        bottomBar = {
                            NavigationBar(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                tonalElevation = 0.dp
                            ) {
                                NavigationBarItem(
                                    selected = bottomBarTab == 0,
                                    onClick = { bottomBarTab = 0 },
                                    label = { Text("Dashboard") },
                                    icon = {
                                        Icon(
                                            imageVector = if (bottomBarTab == 0) Icons.Filled.Dashboard else Icons.Outlined.Dashboard,
                                            contentDescription = "Dashboard",
                                            modifier = Modifier.size(24.dp)
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                        selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    modifier = Modifier.testTag("nav_dashboard")
                                )
                                NavigationBarItem(
                                    selected = bottomBarTab == 1,
                                    onClick = { bottomBarTab = 1 },
                                    label = { Text("Settings") },
                                    icon = {
                                        Icon(
                                            imageVector = if (bottomBarTab == 1) Icons.Filled.Settings else Icons.Outlined.Settings,
                                            contentDescription = "Settings",
                                            modifier = Modifier.size(24.dp)
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                        selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    modifier = Modifier.testTag("nav_settings")
                                )
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    ) { innerPadding ->
                        val baseModifier = Modifier.padding(innerPadding)

                        if (bottomBarTab == 0) {
                            when (currentUser?.role) {
                                "Admin" -> AdminDashboard(viewModel = viewModel, modifier = baseModifier)
                                "Headmaster" -> HeadmasterDashboard(viewModel = viewModel, modifier = baseModifier)
                                else -> CleanerDashboard(viewModel = viewModel, modifier = baseModifier)
                            }
                        } else {
                            SettingsScreen(viewModel = viewModel, modifier = baseModifier)
                        }
                    }
                }
            }
        }
    }
}
