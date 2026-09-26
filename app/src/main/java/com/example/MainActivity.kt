package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.AICopilotScreen
import com.example.ui.screens.AssetsScreen
import com.example.ui.screens.DiagnosticsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.RatingDialog
import com.example.ui.screens.RunbooksScreen
import com.example.ui.screens.ServicesScreen
import com.example.ui.screens.TicketsScreen
import com.example.ui.theme.CyanLight
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.EmeraldStatusOk
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.RoseStatusCritical
import com.example.ui.theme.TechSurfaceDark
import com.example.viewmodel.AppTab
import com.example.viewmodel.HelpDeskViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                HelpDeskApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpDeskApp(
    viewModel: HelpDeskViewModel = viewModel()
) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val activeTicketsCount by viewModel.activeTicketsCount.collectAsStateWithLifecycle()
    val showRatingDialog by viewModel.showRatingDialog.collectAsStateWithLifecycle()

    // Handle back button if not on HOME
    BackHandler(enabled = currentTab != AppTab.HOME) {
        viewModel.setTab(AppTab.HOME)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(EmeraldStatusOk)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "IT Help",
                            fontWeight = FontWeight.Black,
                            fontSize = 19.sp,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFF0C4A6E),
                            modifier = Modifier.padding(start = 2.dp)
                        ) {
                            Text(
                                text = "ENGINEER",
                                color = CyanLight,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                },
                actions = {
                    // AI Copilot Action Button
                    IconButton(
                        onClick = { viewModel.setTab(AppTab.AI_COPILOT) },
                        modifier = Modifier.testTag("topbar_ai_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "IT AI Copilot",
                            tint = if (currentTab == AppTab.AI_COPILOT) CyanLight else Color(0xFF38BDF8)
                        )
                    }

                    // 5-Star CSAT Rating action button
                    IconButton(
                        onClick = { viewModel.setShowRatingDialog(true) },
                        modifier = Modifier.testTag("topbar_rate_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Rate IT Help Desk 5-Stars",
                            tint = Color(0xFFFBBF24)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = TechSurfaceDark,
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = TechSurfaceDark,
                contentColor = Color.White,
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("bottom_nav_bar")
            ) {
                // HOME Tab
                NavigationBarItem(
                    selected = currentTab == AppTab.HOME,
                    onClick = { viewModel.setTab(AppTab.HOME) },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Overview", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = CyanLight,
                        indicatorColor = CyanPrimary,
                        unselectedIconColor = Color(0xFF94A3B8),
                        unselectedTextColor = Color(0xFF94A3B8)
                    ),
                    modifier = Modifier.testTag("nav_tab_home")
                )

                // TICKETS Tab (with badge)
                NavigationBarItem(
                    selected = currentTab == AppTab.TICKETS,
                    onClick = { viewModel.setTab(AppTab.TICKETS) },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (activeTicketsCount > 0) {
                                    Badge(containerColor = RoseStatusCritical) {
                                        Text("$activeTicketsCount")
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Assignment, contentDescription = "Tickets")
                        }
                    },
                    label = { Text("Tickets", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = CyanLight,
                        indicatorColor = CyanPrimary,
                        unselectedIconColor = Color(0xFF94A3B8),
                        unselectedTextColor = Color(0xFF94A3B8)
                    ),
                    modifier = Modifier.testTag("nav_tab_tickets")
                )

                // DIAGNOSTICS Tab
                NavigationBarItem(
                    selected = currentTab == AppTab.DIAGNOSTICS,
                    onClick = { viewModel.setTab(AppTab.DIAGNOSTICS) },
                    icon = { Icon(Icons.Default.Build, contentDescription = "Diagnostics") },
                    label = { Text("Tools", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = CyanLight,
                        indicatorColor = CyanPrimary,
                        unselectedIconColor = Color(0xFF94A3B8),
                        unselectedTextColor = Color(0xFF94A3B8)
                    ),
                    modifier = Modifier.testTag("nav_tab_diagnostics")
                )

                // AI COPILOT Tab (Maps Grounding, Voice Dispatch, Diagram Studio)
                NavigationBarItem(
                    selected = currentTab == AppTab.AI_COPILOT,
                    onClick = { viewModel.setTab(AppTab.AI_COPILOT) },
                    icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "AI Copilot") },
                    label = { Text("AI Copilot", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = CyanLight,
                        indicatorColor = CyanPrimary,
                        unselectedIconColor = Color(0xFF94A3B8),
                        unselectedTextColor = Color(0xFF94A3B8)
                    ),
                    modifier = Modifier.testTag("nav_tab_ai_copilot")
                )

                // SERVICES Tab
                NavigationBarItem(
                    selected = currentTab == AppTab.SERVICES,
                    onClick = { viewModel.setTab(AppTab.SERVICES) },
                    icon = { Icon(Icons.Default.CloudDone, contentDescription = "Services") },
                    label = { Text("Services", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = CyanLight,
                        indicatorColor = CyanPrimary,
                        unselectedIconColor = Color(0xFF94A3B8),
                        unselectedTextColor = Color(0xFF94A3B8)
                    ),
                    modifier = Modifier.testTag("nav_tab_services")
                )

                // RUNBOOKS Tab
                NavigationBarItem(
                    selected = currentTab == AppTab.RUNBOOKS,
                    onClick = { viewModel.setTab(AppTab.RUNBOOKS) },
                    icon = { Icon(Icons.Default.Description, contentDescription = "Runbooks") },
                    label = { Text("Runbooks", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = CyanLight,
                        indicatorColor = CyanPrimary,
                        unselectedIconColor = Color(0xFF94A3B8),
                        unselectedTextColor = Color(0xFF94A3B8)
                    ),
                    modifier = Modifier.testTag("nav_tab_runbooks")
                )

                // ASSETS Tab
                NavigationBarItem(
                    selected = currentTab == AppTab.ASSETS,
                    onClick = { viewModel.setTab(AppTab.ASSETS) },
                    icon = { Icon(Icons.Default.Computer, contentDescription = "Assets") },
                    label = { Text("Assets", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = CyanLight,
                        indicatorColor = CyanPrimary,
                        unselectedIconColor = Color(0xFF94A3B8),
                        unselectedTextColor = Color(0xFF94A3B8)
                    ),
                    modifier = Modifier.testTag("nav_tab_assets")
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (currentTab) {
                AppTab.HOME -> HomeScreen(viewModel = viewModel)
                AppTab.TICKETS -> TicketsScreen(viewModel = viewModel)
                AppTab.DIAGNOSTICS -> DiagnosticsScreen(viewModel = viewModel)
                AppTab.AI_COPILOT -> AICopilotScreen(viewModel = viewModel)
                AppTab.SERVICES -> ServicesScreen(viewModel = viewModel)
                AppTab.RUNBOOKS -> RunbooksScreen(viewModel = viewModel)
                AppTab.ASSETS -> AssetsScreen(viewModel = viewModel)
            }
        }
    }

    if (showRatingDialog) {
        RatingDialog(
            onDismiss = { viewModel.setShowRatingDialog(false) },
            onSubmit = { stars, name, feedback ->
                viewModel.submitRating(stars, name, feedback)
            }
        )
    }
}
