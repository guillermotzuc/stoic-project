package com.proyecto360.health

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.proyecto360.health.ui.commitment.CommitmentEditorScreen
import com.proyecto360.health.ui.exam.EveningExamEditorScreen
import com.proyecto360.health.ui.history.HistoryScreen
import com.proyecto360.health.ui.home.HomeScreen
import com.proyecto360.health.ui.post.PostDetailScreen
import com.proyecto360.health.ui.post.PostEditorScreen
import com.proyecto360.health.ui.theme.HealthTheme
import com.proyecto360.health.widget.WidgetIntents
import com.proyecto360.health.widget.WidgetUpdater

class MainActivity : ComponentActivity() {
    private val deepLinkDestination = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        deepLinkDestination.value = intent?.getStringExtra(WidgetIntents.EXTRA_DESTINATION)
        enableEdgeToEdge()
        setContent {
            HealthTheme {
                val destination by remember { deepLinkDestination }
                HealthApp(
                    deepLinkDestination = destination,
                    onDeepLinkConsumed = { deepLinkDestination.value = null }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        deepLinkDestination.value = intent.getStringExtra(WidgetIntents.EXTRA_DESTINATION)
    }

    override fun onResume() {
        super.onResume()
        WidgetUpdater.refresh(this)
    }
}

@Composable
fun HealthApp(
    deepLinkDestination: String? = null,
    onDeepLinkConsumed: () -> Unit = {}
) {
    val app = LocalContext.current.applicationContext as HealthApplication
    val navController = rememberNavController()

    LaunchedEffect(deepLinkDestination) {
        when (deepLinkDestination) {
            WidgetIntents.DEST_POST -> {
                navController.navigate("editor") {
                    launchSingleTop = true
                }
                onDeepLinkConsumed()
            }
            WidgetIntents.DEST_EXAM -> {
                navController.navigate("exam") {
                    launchSingleTop = true
                }
                onDeepLinkConsumed()
            }
        }
    }

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            val vm: HomeViewModel = viewModel(factory = HomeViewModel.factory(app))
            HomeScreen(
                viewModel = vm,
                onAddPost = { navController.navigate("editor") },
                onEditToday = { id -> navController.navigate("editor?postId=$id") },
                onOpenPost = { id -> navController.navigate("detail/$id") },
                onOpenHistory = { navController.navigate("history") },
                onAddCommitment = { navController.navigate("commitment") },
                onEditCommitment = { id -> navController.navigate("commitment?id=$id") },
                onAddExam = { navController.navigate("exam") },
                onEditExam = { id -> navController.navigate("exam?id=$id") }
            )
        }

        composable(
            route = "editor?postId={postId}",
            arguments = listOf(
                navArgument("postId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { entry ->
            val postId = entry.arguments?.getString("postId")?.toLongOrNull()
            val vm: PostEditorViewModel = viewModel(factory = PostEditorViewModel.factory(app))
            PostEditorScreen(
                viewModel = vm,
                postId = postId,
                onBack = { navController.popBackStack() },
                onSaved = { id ->
                    WidgetUpdater.refresh(app)
                    navController.navigate("detail/$id") {
                        popUpTo("home")
                    }
                }
            )
        }

        composable(
            route = "commitment?id={id}",
            arguments = listOf(
                navArgument("id") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { entry ->
            val commitmentId = entry.arguments?.getString("id")?.toLongOrNull()
            val vm: CommitmentEditorViewModel =
                viewModel(factory = CommitmentEditorViewModel.factory(app))
            CommitmentEditorScreen(
                viewModel = vm,
                commitmentId = commitmentId,
                onBack = { navController.popBackStack() },
                onSaved = {
                    WidgetUpdater.refresh(app)
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = "exam?id={id}",
            arguments = listOf(
                navArgument("id") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { entry ->
            val examId = entry.arguments?.getString("id")?.toLongOrNull()
            val vm: EveningExamEditorViewModel =
                viewModel(factory = EveningExamEditorViewModel.factory(app))
            EveningExamEditorScreen(
                viewModel = vm,
                examId = examId,
                onBack = { navController.popBackStack() },
                onSaved = {
                    WidgetUpdater.refresh(app)
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = "detail/{postId}",
            arguments = listOf(navArgument("postId") { type = NavType.LongType })
        ) { entry ->
            val postId = entry.arguments?.getLong("postId") ?: return@composable
            val vm: PostDetailViewModel = viewModel(
                factory = PostDetailViewModel.factory(app, postId)
            )
            PostDetailScreen(
                viewModel = vm,
                onBack = { navController.popBackStack() },
                onEdit = { id -> navController.navigate("editor?postId=$id") },
                onDeleted = {
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }

        composable("history") {
            val vm: HistoryViewModel = viewModel(factory = HistoryViewModel.factory(app))
            HistoryScreen(
                viewModel = vm,
                onBack = { navController.popBackStack() },
                onOpenPost = { id -> navController.navigate("detail/$id") }
            )
        }
    }
}
