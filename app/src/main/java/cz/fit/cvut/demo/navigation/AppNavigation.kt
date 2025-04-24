package cz.fit.cvut.demo.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import cz.fit.cvut.demo.screens.todo.TodoScreen
import cz.fit.cvut.demo.screens.translations.TranslationsScreen
import cz.fit.cvut.sdk.extensions.registerAsRouteProviderForTolgee

@Composable
fun AppNavigation() {

    val navController = rememberNavController()

    // Register the NavController as route provider
    navController.registerAsRouteProviderForTolgee()
    NavHost(navController = navController, startDestination = "todo") {
        composable("todo") {
            TodoScreen(
                onNavigateToTranslations = {
                    navController.navigate("translations")
                }
            )
        }
        composable("translations") {
            TranslationsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}