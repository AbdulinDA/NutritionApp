package com.abdulin.nutritionapp.presentation.navigation

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.abdulin.nutritionapp.R
import com.abdulin.nutritionapp.presentation.diary.DiaryScreen
import com.abdulin.nutritionapp.presentation.diary.ProductSearchScreen
import com.abdulin.nutritionapp.presentation.diary.ProductSearchViewModel
import com.abdulin.nutritionapp.presentation.home.HomeScreen
import com.abdulin.nutritionapp.presentation.products.ProductDetailScreen
import com.abdulin.nutritionapp.presentation.profile.ProfileScreen
import com.abdulin.nutritionapp.presentation.profile.FavoritesScreen
import com.abdulin.nutritionapp.presentation.profile.FridgeScreen
import com.abdulin.nutritionapp.presentation.recipe.RecipeSearchScreen

sealed class BottomBarScreen(
    val route: String,
    val titleRes: Int,
    val icon: ImageVector
) {
    object Home : BottomBarScreen("home_tab", R.string.nav_home, Icons.Default.Home)
    object Diary : BottomBarScreen("diary_tab", R.string.nav_diary, Icons.AutoMirrored.Filled.List)
    object Products : BottomBarScreen("products_tab", R.string.nav_products, Icons.Default.ShoppingCart)
    object Recipes : BottomBarScreen("recipes_tab", R.string.nav_recipes, Icons.Default.RestaurantMenu)
    object Profile : BottomBarScreen("profile_tab", R.string.nav_profile, Icons.Default.Person)
}

@Composable
fun MainScreen(
    onNavigateToAddFood: () -> Unit,
    onNavigateToRecipe: (Long, String?, String?, Long?, Double?) -> Unit,
    onNavigateToShoppingList: () -> Unit,
    onNavigateToGeneratePlan: () -> Unit,
    onNavigateToMealPlan: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToScanner: () -> Unit,
    onNavigateToFasting: () -> Unit,
    onNavigateToEditProfile: () -> Unit,
    onNavigateToFoodPreferences: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onLogout: () -> Unit
) {
    val navController = rememberNavController()
    val screens = listOf(
        BottomBarScreen.Home,
        BottomBarScreen.Diary,
        BottomBarScreen.Products,
        BottomBarScreen.Recipes,
        BottomBarScreen.Profile
    )
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    fun navigateToTab(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    BoxWithConstraints {
        val isWideScreen = maxWidth > 600.dp

        Row(modifier = Modifier.fillMaxSize()) {
            if (isWideScreen) {
                NavigationRail(
                    modifier = Modifier.fillMaxHeight(),
                    containerColor = MaterialTheme.colorScheme.surface,
                    header = {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    }
                ) {
                    Spacer(Modifier.weight(1f))
                    screens.forEach { screen ->
                        NavigationRailItem(
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = { navigateToTab(screen.route) },
                            icon = { Icon(screen.icon, contentDescription = null) },
                            label = { Text(stringResource(screen.titleRes)) }
                        )
                    }
                    Spacer(Modifier.weight(1f))
                }
            }

            Scaffold(
                containerColor = MaterialTheme.colorScheme.background,
                bottomBar = {
                    if (!isWideScreen) {
                        NavigationBar(
                            modifier = Modifier,
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 0.dp
                        ) {
                            screens.forEach { screen ->
                                NavigationBarItem(
                                    label = {
                                        Text(
                                            text = stringResource(screen.titleRes),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Medium
                                        )
                                    },
                                    icon = {
                                        Icon(screen.icon, contentDescription = null)
                                    },
                                    selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                                    onClick = { navigateToTab(screen.route) },
                                    colors = NavigationBarItemDefaults.colors(
                                        indicatorColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
                                    )
                                )
                            }
                        }
                    }
                }
            ) { innerPadding ->
                NavHost(
                    navController = navController,
                    startDestination = BottomBarScreen.Home.route,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    composable(BottomBarScreen.Home.route) {
                        HomeScreen(
                            onNavigateToDiary = { navigateToTab(BottomBarScreen.Diary.route) },
                            onNavigateToProfile = { navigateToTab(BottomBarScreen.Profile.route) },
                            onNavigateToFridge = { navController.navigate("fridge_screen") },
                            onNavigateToFavorites = { navController.navigate("favorites_screen") },
                            onNavigateToRecipe = onNavigateToRecipe,
                            onNavigateToAnalytics = onNavigateToAnalytics,
                            onNavigateToFasting = onNavigateToFasting
                        )
                    }
                    composable(BottomBarScreen.Diary.route) {
                        DiaryScreen(
                            onNavigateToAddFood = onNavigateToAddFood,
                            onBack = {},
                            showBackButton = false
                        )
                    }
                    composable(BottomBarScreen.Products.route) {
                        ProductSearchScreen(
                            navController = navController,
                            onProductSelected = { product ->
                                navController.currentBackStackEntry?.savedStateHandle?.set(
                                    "selected_product_id_for_detail",
                                    product.id
                                )
                                navController.navigate("products_detail/${product.id}")
                            },
                            onNavigateToScanner = onNavigateToScanner,
                            onBack = {},
                            showBackButton = false
                        )
                    }
                    composable("products_detail/{productId}") { backStackEntry ->
                        val productId = backStackEntry.arguments?.getString("productId")?.toLongOrNull() ?: 0L
                        val productListBackStackEntry = remember(backStackEntry) {
                            navController.getBackStackEntry(BottomBarScreen.Products.route)
                        }
                        val viewModel: ProductSearchViewModel = hiltViewModel(productListBackStackEntry)
                        val productState by viewModel.state.collectAsState()
                        val product = productState.selectedProduct?.takeIf { it.id == productId }
                            ?: productState.scannedProduct?.takeIf { it.id == productId }

                        if (product != null) {
                            ProductDetailScreen(
                                product = product,
                                onBack = { navController.popBackStack() },
                                onAlternativeClick = { alternative ->
                                    viewModel.selectProduct(alternative)
                                    navController.navigate("products_detail/${alternative.id}")
                                }
                            )
                        }
                    }
                    composable(BottomBarScreen.Recipes.route) {
                        RecipeSearchScreen(
                            onRecipeClick = onNavigateToRecipe,
                            onGeneratePlanClick = onNavigateToGeneratePlan
                        )
                    }
                    composable(BottomBarScreen.Profile.route) {
                        ProfileScreen(
                            onNavigateToShoppingList = onNavigateToShoppingList,
                            onNavigateToEditProfile = onNavigateToEditProfile,
                            onNavigateToSettings = onNavigateToSettings,
                            onNavigateToMealPlan = onNavigateToMealPlan,
                            onNavigateToFridge = { navController.navigate("fridge_screen") },
                            onNavigateToFavorites = { navController.navigate("favorites_screen") },
                            onNavigateToFoodPreferences = onNavigateToFoodPreferences,
                            onNavigateToFasting = onNavigateToFasting,
                            onBack = {},
                            onLogout = onLogout,
                            showBackButton = false
                        )
                    }
                    composable("fridge_screen") {
                        FridgeScreen(
                            onBack = { navController.popBackStack() },
                            onOpenRecipes = {
                                navController.popBackStack()
                                navigateToTab(BottomBarScreen.Recipes.route)
                            }
                        )
                    }
                    composable("favorites_screen") {
                        FavoritesScreen(
                            onBack = { navController.popBackStack() },
                            onOpenRecipe = { recipeId ->
                                onNavigateToRecipe(recipeId, "favorite_recipe", null, null, null)
                            }
                        )
                    }
                }
            }
        }
    }
}
