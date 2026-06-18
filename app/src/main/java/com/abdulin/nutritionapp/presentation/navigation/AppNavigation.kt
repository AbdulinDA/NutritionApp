package com.abdulin.nutritionapp.presentation.navigation

import android.net.Uri
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.abdulin.nutritionapp.presentation.analytics.AnalyticsScreen
import com.abdulin.nutritionapp.presentation.auth.LoginScreen
import com.abdulin.nutritionapp.presentation.auth.OnboardingScreen
import com.abdulin.nutritionapp.presentation.auth.EmailVerificationPendingScreen
import com.abdulin.nutritionapp.presentation.auth.RegisterScreen
import com.abdulin.nutritionapp.presentation.diary.AddFoodScreen
import com.abdulin.nutritionapp.presentation.diary.BarcodeScannerScreen
import com.abdulin.nutritionapp.presentation.diary.ProductSearchScreen
import com.abdulin.nutritionapp.presentation.diary.ProductSearchViewModel
import com.abdulin.nutritionapp.presentation.products.ProductDetailScreen
import com.abdulin.nutritionapp.presentation.profile.AppSettingsScreen
import com.abdulin.nutritionapp.presentation.profile.EditProfileScreen
import com.abdulin.nutritionapp.presentation.profile.FastingScreen
import com.abdulin.nutritionapp.presentation.profile.FoodPreferencesScreen
import com.abdulin.nutritionapp.presentation.profile.FridgeScreen
import com.abdulin.nutritionapp.presentation.profile.ShoppingListScreen
import com.abdulin.nutritionapp.presentation.recipe.GeneratePlanScreen
import com.abdulin.nutritionapp.presentation.recipe.MealPlanScreen
import com.abdulin.nutritionapp.presentation.recipe.PlanRecipePickerViewModel
import com.abdulin.nutritionapp.presentation.recipe.RecipeDetailScreen
import com.abdulin.nutritionapp.presentation.recipe.RecipeSearchScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(startDestination: String) {
    val navController = rememberNavController()

    fun recipeRoute(recipeId: Long, source: String?, reason: String?, impressionId: Long?, initialServings: Double?): String {
        val sourceArg = Uri.encode(source ?: "")
        val reasonArg = Uri.encode(reason ?: "")
        val impressionArg = impressionId?.toString() ?: ""
        val servingsArg = initialServings?.toString() ?: ""
        return "recipe_detail/$recipeId?source=$sourceArg&reason=$reasonArg&impressionId=$impressionArg&servings=$servingsArg"
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable("login") {
            LoginScreen(
                onLoginSuccess = { destination ->
                    navController.navigate(destination) {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate("register")
                }
            )
        }

        composable("register") {
            RegisterScreen(
                onRegisterSuccess = { destination ->
                    navController.navigate(destination) {
                        popUpTo("register") { inclusive = true }
                    }
                },
                onBackToLogin = { navController.popBackStack() }
            )
        }

        composable("onboarding") {
            OnboardingScreen(
                onFinish = {
                    navController.navigate("main") {
                        popUpTo("onboarding") { inclusive = true }
                    }
                },
                onBack = {
                    navController.navigate("main") {
                        popUpTo("onboarding") { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = "email_verification_pending/{email}",
            arguments = listOf(navArgument("email") { type = NavType.StringType })
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            EmailVerificationPendingScreen(
                email = email,
                onBackToLogin = {
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable("main") {
            MainScreen(
                onNavigateToAddFood = { navController.navigate("add_food") },
                onNavigateToRecipe = { recipeId, source, reason, impressionId, initialServings ->
                    navController.navigate(recipeRoute(recipeId, source, reason, impressionId, initialServings))
                },
                onNavigateToShoppingList = { navController.navigate("shopping_list") },
                onNavigateToGeneratePlan = { navController.navigate("generate_plan") },
                onNavigateToMealPlan = { navController.navigate("meal_plan") },
                onNavigateToAnalytics = { navController.navigate("analytics") },
                onNavigateToScanner = { navController.navigate("barcode_scanner") },
                onNavigateToFasting = { navController.navigate("fasting") },
                onNavigateToEditProfile = { navController.navigate("edit_profile") },
                onNavigateToFoodPreferences = { navController.navigate("food_preferences") },
                onNavigateToSettings = { navController.navigate("app_settings") },
                onLogout = {
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = "recipe_detail/{recipeId}?source={source}&reason={reason}&impressionId={impressionId}&servings={servings}",
            arguments = listOf(
                navArgument("recipeId") { type = NavType.LongType },
                navArgument("source") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("reason") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("impressionId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("servings") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val recipeId = backStackEntry.arguments?.getLong("recipeId") ?: 0L
            val recommendationSource = backStackEntry.arguments?.getString("source")
            val recommendationReason = backStackEntry.arguments?.getString("reason")
            val recommendationImpressionId = backStackEntry.arguments
                ?.getString("impressionId")
                ?.toLongOrNull()
            val initialServings = backStackEntry.arguments
                ?.getString("servings")
                ?.toDoubleOrNull()
            RecipeDetailScreen(
                recipeId = recipeId,
                recommendationSource = recommendationSource,
                recommendationReason = recommendationReason,
                recommendationImpressionId = recommendationImpressionId,
                initialServings = initialServings,
                onBack = { navController.popBackStack() },
                onAddtoDiary = { navController.popBackStack() }
            )
        }

        composable(
            route = "add_food?productId={productId}&productName={productName}&recipeId={recipeId}&recipeName={recipeName}",
            arguments = listOf(
                navArgument("productId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("productName") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("recipeId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("recipeName") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val selectedProductId by backStackEntry.savedStateHandle
                .getStateFlow<String?>("selected_product_id", backStackEntry.arguments?.getString("productId"))
                .collectAsState()
            val selectedProductName by backStackEntry.savedStateHandle
                .getStateFlow<String?>("selected_product_name", backStackEntry.arguments?.getString("productName"))
                .collectAsState()
            val selectedRecipeId by backStackEntry.savedStateHandle
                .getStateFlow<String?>("selected_recipe_id", backStackEntry.arguments?.getString("recipeId"))
                .collectAsState()
            val selectedRecipeName by backStackEntry.savedStateHandle
                .getStateFlow<String?>("selected_recipe_name", backStackEntry.arguments?.getString("recipeName"))
                .collectAsState()

            AddFoodScreen(
                initialProductId = selectedProductId,
                initialProductName = selectedProductName,
                initialRecipeId = selectedRecipeId,
                initialRecipeName = selectedRecipeName,
                onNavigateToSearch = { navController.navigate("product_search") },
                onNavigateToRecipeSearch = { navController.navigate("recipe_search_picker") },
                onBack = { navController.popBackStack() }
            )
        }

        composable("product_search") {
            val viewModel: ProductSearchViewModel = hiltViewModel()
            ProductSearchScreen(
                viewModel = viewModel,
                navController = navController,
                onProductSelected = { product ->
                    navController.previousBackStackEntry?.savedStateHandle?.set(
                        "selected_product_id",
                        product.id.toString()
                    )
                    navController.previousBackStackEntry?.savedStateHandle?.set(
                        "selected_product_name",
                        product.name
                    )
                    navController.popBackStack()
                },
                onNavigateToScanner = { navController.navigate("barcode_scanner") },
                onBack = { navController.popBackStack() }
            )
        }

        composable("recipe_search_picker") {
            RecipeSearchScreen(
                onRecipeClick = { recipeId, _, _, _, _ ->
                    navController.previousBackStackEntry?.savedStateHandle?.set(
                        "selected_recipe_id",
                        recipeId.toString()
                    )
                    navController.popBackStack()
                },
                onRecipeSelected = { recipe ->
                    navController.previousBackStackEntry?.savedStateHandle?.set(
                        "selected_recipe_id",
                        recipe.id.toString()
                    )
                    navController.previousBackStackEntry?.savedStateHandle?.set(
                        "selected_recipe_name",
                        recipe.title
                    )
                    navController.popBackStack()
                },
                onGeneratePlanClick = {}
            )
        }

        composable(
            route = "product_detail/{productId}",
            arguments = listOf(navArgument("productId") { type = NavType.LongType })
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getLong("productId") ?: 0L
            val productSearchBackStackEntry = remember(backStackEntry) {
                navController.getBackStackEntry("product_search")
            }
            val viewModel: ProductSearchViewModel = hiltViewModel(productSearchBackStackEntry)
            val state by viewModel.state.collectAsState()
            val product = state.selectedProduct?.takeIf { it.id == productId }
                ?: state.scannedProduct?.takeIf { it.id == productId }

            if (product != null) {
                ProductDetailScreen(
                    product = product,
                    onBack = { navController.popBackStack() },
                    onAlternativeClick = { alternative ->
                        viewModel.selectProduct(alternative)
                        navController.navigate("product_detail/${alternative.id}")
                    }
                )
            }
        }

        composable("barcode_scanner") {
            BarcodeScannerScreen(
                onBarcodeScanned = { barcode ->
                    navController.previousBackStackEntry?.savedStateHandle?.set("scanned_barcode", barcode)
                    navController.popBackStack()
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable("shopping_list") {
            ShoppingListScreen(
                onBack = { navController.popBackStack() },
                onNavigateToGeneratePlan = { navController.navigate("generate_plan") }
            )
        }

        composable("meal_plan") {
            MealPlanScreen(
                onBack = { navController.popBackStack() },
                onNavigateToRecipe = { recipeId, source, reason, impressionId, initialServings ->
                    navController.navigate(recipeRoute(recipeId, source, reason, impressionId, initialServings))
                },
                onNavigateToShoppingList = { navController.navigate("shopping_list") },
                onNavigateToGeneratePlan = { navController.navigate("generate_plan") },
                onAddMealToSlot = { planDate, mealType ->
                    navController.navigate(
                        "recipe_search_plan_picker?planDate=${Uri.encode(planDate)}&mealType=${Uri.encode(mealType)}"
                    )
                }
            )
        }

        composable(
            route = "recipe_search_plan_picker?planDate={planDate}&mealType={mealType}",
            arguments = listOf(
                navArgument("planDate") {
                    type = NavType.StringType
                },
                navArgument("mealType") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val planDate = backStackEntry.arguments?.getString("planDate").orEmpty()
            val mealType = backStackEntry.arguments?.getString("mealType").orEmpty()
            val pickerViewModel: PlanRecipePickerViewModel = hiltViewModel()
            val pickerState by pickerViewModel.state.collectAsState()
            val snackbarHostState = remember { SnackbarHostState() }

            LaunchedEffect(pickerState.isSaved) {
                if (pickerState.isSaved) {
                    pickerViewModel.consumeResult()
                    navController.popBackStack()
                }
            }

            LaunchedEffect(pickerState.error) {
                pickerState.error?.let { error ->
                    snackbarHostState.showSnackbar(error)
                    pickerViewModel.consumeResult()
                }
            }

            androidx.compose.material3.Scaffold(
                snackbarHost = { SnackbarHost(snackbarHostState) }
            ) { innerPadding ->
                RecipeSearchScreen(
                    onRecipeClick = { recipeId, _, _, _, _ ->
                        pickerViewModel.addRecipeToPlan(recipeId, planDate, mealType)
                    },
                    onRecipeSelected = { recipe ->
                        pickerViewModel.addRecipeToPlan(recipe.id, planDate, mealType)
                    },
                    onGeneratePlanClick = {},
                    bottomBarPadding = innerPadding.calculateBottomPadding(),
                    initialMealTypeFilter = mealType,
                    onBack = { navController.popBackStack() }
                )
            }
        }

        composable("edit_profile") {
            EditProfileScreen(
                onBack = { navController.popBackStack() },
                onOpenFoodPreferences = { navController.navigate("food_preferences") }
            )
        }

        composable("app_settings") {
            AppSettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable("fasting") {
            FastingScreen(onBack = { navController.popBackStack() })
        }

        composable("food_preferences") {
            FoodPreferencesScreen(
                onBack = { navController.popBackStack() },
                onOpenFridge = { navController.navigate("fridge_preferences") }
            )
        }

        composable("fridge_preferences") {
            FridgeScreen(
                onBack = { navController.popBackStack() },
                onOpenRecipes = {
                    navController.navigate("main") {
                        popUpTo("main") { inclusive = false }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable("generate_plan") {
            GeneratePlanScreen(
                onPlanGenerated = {
                    navController.navigate("meal_plan") {
                        popUpTo("generate_plan") { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable("analytics") {
            AnalyticsScreen(onBack = { navController.popBackStack() })
        }
    }
}
