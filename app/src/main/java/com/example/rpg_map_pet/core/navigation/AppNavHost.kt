package com.example.rpg_map_pet.core.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.rpg_map_pet.presentation.map.MapScreen
import com.example.rpg_map_pet.presentation.map.MapViewModel
import com.example.rpg_map_pet.presentation.quest.LandmarkDetailScreen
import com.example.rpg_map_pet.presentation.visited.VisitedLandmarksScreen
import com.example.rpg_map_pet.presentation.visited.VisitedLandmarksViewModel

@Composable
fun AppNavHost(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = "map"
    ) {
        composable("map") {
            val viewModel: MapViewModel = hiltViewModel()
            MapScreen(
                onNavigateToLandmark = { landmark ->
                    val encodedId = java.net.URLEncoder.encode(landmark.id, "UTF-8")
                    navController.navigate("landmark/$encodedId")
                },
                onNavigateToVisited = {
                    navController.navigate("visited")
                }
            )
        }

        composable(
            route = "landmark/{landmarkId}"
        ) { backStackEntry ->
            val landmarkId = backStackEntry.arguments?.getString("landmarkId")
                ?.let { java.net.URLDecoder.decode(it, "UTF-8") }
                ?: return@composable
            val viewModel: MapViewModel = hiltViewModel()

            LandmarkDetailScreen(
                landmarkId = landmarkId,
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable("visited") {
            val viewModel: VisitedLandmarksViewModel = hiltViewModel()
            VisitedLandmarksScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                viewModel = viewModel,
                onLandmarkClick = { landmark ->
                    val encodedId = java.net.URLEncoder.encode(landmark.id, "UTF-8")
                    navController.navigate("landmark/$encodedId")
                }
            )
        }
    }
}
