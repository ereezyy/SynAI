package com.example.synapseai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.synapseai.presentation.common.PlaceholderScreen
import com.example.synapseai.presentation.dashboard.DashboardScreen
import com.example.synapseai.presentation.email.EmailDraftingScreen
import com.example.synapseai.presentation.meeting.MeetingDetailsScreen
import com.example.synapseai.presentation.permissions.PermissionsScreen
import com.example.synapseai.presentation.recording.RecordingScreen
import com.example.synapseai.presentation.theme.SynapseAITheme
import com.example.synapseai.util.PermissionsHandler
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SynapseAITheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    NavHost(
                        navController = navController,
                        startDestination = "permissions"
                    ) {
                        // Permissions screen
                        composable("permissions") {
                            PermissionsScreen(
                                onPermissionsGranted = {
                                    navController.navigate("dashboard") {
                                        popUpTo("permissions") { inclusive = true }
                                    }
                                }
                            )
                        }

                        // Dashboard screen
                        composable("dashboard") {
                            DashboardScreen(
                                onMeetingClick = { meetingId ->
                                    navController.navigate("meeting_details/$meetingId")
                                },
                                onCreateMeetingSuccess = { meetingId ->
                                    navController.navigate("recording/$meetingId")
                                }
                            )
                        }

                        // Meeting details screen
                        composable(
                            route = "meeting_details/{meetingId}",
                            arguments = listOf(
                                navArgument("meetingId") { type = NavType.LongType }
                            )
                        ) { backStackEntry ->
                            val meetingId = backStackEntry.arguments?.getLong("meetingId") ?: 0L
                            MeetingDetailsScreen(
                                meetingId = meetingId,
                                onNavigateBack = { navController.popBackStack() },
                                onNavigateToEmailDraft = { id ->
                                    navController.navigate("email/$id") {
                                        popUpTo("meeting_details/$id") { inclusive = false }
                                    }
                                }
                            )
                        }

                        // Recording screen
                        composable(
                            route = "recording/{meetingId}",
                            arguments = listOf(
                                navArgument("meetingId") { type = NavType.LongType }
                            )
                        ) { backStackEntry ->
                            val meetingId = backStackEntry.arguments?.getLong("meetingId") ?: 0L
                            RecordingScreen(
                                meetingId = meetingId,
                                onNavigateBack = { navController.popBackStack() },
                                onRecordingComplete = { id ->
                                    navController.navigate("meeting_details/$id") {
                                        popUpTo("recording/$id") { inclusive = true }
                                    }
                                }
                            )
                        }

                        // Email drafting screen
                        composable(
                            route = "email/{meetingId}",
                            arguments = listOf(
                                navArgument("meetingId") { type = NavType.LongType }
                            )
                        ) { backStackEntry ->
                            val meetingId = backStackEntry.arguments?.getLong("meetingId") ?: 0L
                            EmailDraftingScreen(
                                meetingId = meetingId,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
