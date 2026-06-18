package com.abdulin.nutritionapp.data.remote

import com.abdulin.nutritionapp.core.network.ApiResponse
import com.abdulin.nutritionapp.data.dto.analytics.RecommendationReportDto
import com.abdulin.nutritionapp.data.dto.analytics.MealPlanReportDto
import com.abdulin.nutritionapp.data.dto.auth.UserResponseDto
import com.abdulin.nutritionapp.data.dto.common.PageResponse
import com.abdulin.nutritionapp.data.dto.diary.CreateDiaryEntryDto
import com.abdulin.nutritionapp.data.dto.diary.DiarySummaryDto
import com.abdulin.nutritionapp.data.dto.diary.FoodDiaryEntryDto
import com.abdulin.nutritionapp.data.dto.diary.UpdateDiaryEntryWeightRequestDto
import com.abdulin.nutritionapp.data.dto.home.HomeResponseDto
import com.abdulin.nutritionapp.data.dto.mealplan.MealPlanRequestDto
import com.abdulin.nutritionapp.data.dto.mealplan.MealPlanResponseDto
import com.abdulin.nutritionapp.data.dto.mealplan.AddRecipeToMealPlanRequestDto
import com.abdulin.nutritionapp.data.dto.mealplan.MealPlanMealExplanationDto
import com.abdulin.nutritionapp.data.dto.mealplan.ReplaceMealInPlanRequestDto
import com.abdulin.nutritionapp.data.dto.mealplan.ShoppingListItemDto
import com.abdulin.nutritionapp.data.dto.pantry.PantryItemDto
import com.abdulin.nutritionapp.data.dto.pantry.PantryItemRequestDto
import com.abdulin.nutritionapp.data.dto.product.ProductDto
import com.abdulin.nutritionapp.data.dto.recipe.ProductMatchRecommendationDto
import com.abdulin.nutritionapp.data.dto.recipe.ProductMatchRequestDto
import com.abdulin.nutritionapp.data.dto.recipe.RecipeCompositionDto
import com.abdulin.nutritionapp.data.dto.recipe.RecipeRecommendationDto
import com.abdulin.nutritionapp.data.dto.recipe.RecipeDto
import com.abdulin.nutritionapp.data.dto.user.TodayWaterResponseDto
import com.abdulin.nutritionapp.data.dto.user.UpdateMyProfileRequestDto
import com.abdulin.nutritionapp.data.dto.user.UpdateWeightRequestDto
import com.abdulin.nutritionapp.data.dto.user.WaterLogRequestDto
import com.abdulin.nutritionapp.data.dto.user.WeightLogRequestDto
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.Json
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PATCH
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

@Serializable
data class AnalyticsEventDto(
    val eventType: String,
    val entityType: String? = null,
    val entityId: String? = null,
    val metadata: String? = null
)

@Serializable
data class PushTokenRequestDto(
    val token: String,
    val deviceType: String = "android",
    val deviceId: String
)

@Serializable
data class RecommendationFeedbackRequestDto(
    val recipeId: Long,
    val eventType: String,
    val impressionId: Long? = null,
    val metadataJson: String? = null
)

interface NutritionApi {
    @GET("api/v1/users/me")
    suspend fun getMyProfile(): Response<ApiResponse<UserResponseDto>>

    @PATCH("api/v1/users/me/weight")
    suspend fun updateCurrentWeight(
        @Body request: UpdateWeightRequestDto
    ): Response<ApiResponse<JsonElement>>

    @PATCH("api/v1/users/me")
    suspend fun updateMyProfile(
        @Body request: UpdateMyProfileRequestDto
    ): Response<ApiResponse<UserResponseDto>>

    @POST("api/v1/users/me/weight-history")
    suspend fun logWeight(
        @Body request: WeightLogRequestDto
    ): Response<ApiResponse<JsonElement>>

    @GET("api/v1/home")
    suspend fun getHomeData(): Response<ApiResponse<HomeResponseDto>>

    @GET("api/v1/food-diary")
    suspend fun getDiary(
        @Query("date") date: String
    ): Response<ApiResponse<List<FoodDiaryEntryDto>>>

    @GET("api/v1/food-diary/summary")
    suspend fun getDiarySummary(
        @Query("date") date: String
    ): Response<ApiResponse<DiarySummaryDto>>

    @POST("api/v1/food-diary")
    suspend fun addDiaryEntry(
        @Body body: CreateDiaryEntryDto
    ): Response<ApiResponse<FoodDiaryEntryDto>>

    @PUT("api/v1/food-diary/{id}")
    suspend fun updateDiaryEntry(
        @Path("id") id: Long,
        @Body request: UpdateDiaryEntryWeightRequestDto
    ): Response<ApiResponse<FoodDiaryEntryDto>>

    @DELETE("api/v1/food-diary/{id}")
    suspend fun deleteDiaryEntry(
        @Path("id") id: Long
    ): Response<ApiResponse<JsonElement>>

    @POST("api/v1/water")
    suspend fun logWater(
        @Body request: WaterLogRequestDto
    ): Response<ApiResponse<JsonElement>>

    @GET("api/v1/water/today")
    suspend fun getWaterToday(): Response<ApiResponse<TodayWaterResponseDto>>

    @GET("api/v1/products/search")
    suspend fun searchProducts(
        @Query("q") query: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<ApiResponse<PageResponse<ProductDto>>>

    @GET("api/v1/products")
    suspend fun getProducts(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<ApiResponse<PageResponse<ProductDto>>>

    @GET("api/v1/products/barcode/{barcode}")
    suspend fun getProductByBarcode(
        @Path("barcode") barcode: String
    ): Response<ApiResponse<ProductDto>>

    @POST("api/v1/products/{id}/favorite")
    suspend fun toggleFavoriteProduct(
        @Path("id") id: Long
    ): Response<ApiResponse<JsonElement>>

    @GET("api/v1/recipes/search")
    suspend fun searchRecipes(
        @Query("mealType") mealType: String? = null,
        @Query("maxTime") maxTime: Int? = null,
        @Query("name") query: String? = null
    ): Response<ApiResponse<List<RecipeDto>>>

    @GET("api/v1/recipes/search/page")
    suspend fun searchRecipesPage(
        @Query("name") query: String? = null,
        @Query("mealType") mealType: String? = null,
        @Query("maxTime") maxTime: Int? = null,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<ApiResponse<PageResponse<RecipeDto>>>

    @GET("api/v1/recipes/{id}")
    suspend fun getRecipeById(
        @Path("id") id: Long
    ): Response<ApiResponse<RecipeDto>>

    @GET("api/v1/recipes/{id}/composition")
    suspend fun getRecipeComposition(
        @Path("id") id: Long,
        @Query("sideDishRecipeId") sideDishRecipeId: Long? = null
    ): Response<ApiResponse<RecipeCompositionDto>>

    @GET("api/v1/pantry")
    suspend fun getPantry(): Response<ApiResponse<List<PantryItemDto>>>

    @POST("api/v1/pantry/items")
    suspend fun addPantryItem(
        @Body request: PantryItemRequestDto
    ): Response<ApiResponse<PantryItemDto>>

    @DELETE("api/v1/pantry/items/{id}")
    suspend fun deletePantryItem(
        @Path("id") id: Long
    ): Response<ApiResponse<JsonElement>>

    @GET("api/v1/recommendations/recipes")
    suspend fun getSmartRecommendations(
        @Query("limit") limit: Int = 5,
        @Query("mealType") mealType: String? = null
    ): Response<ApiResponse<List<RecipeRecommendationDto>>>

    @POST("api/v1/recommendations/recipes/by-products")
    suspend fun getRecommendationsByProducts(
        @Body request: ProductMatchRequestDto
    ): Response<ApiResponse<List<ProductMatchRecommendationDto>>>

    @POST("api/v1/recommendations/feedback")
    suspend fun sendRecommendationFeedback(
        @Body request: RecommendationFeedbackRequestDto
    ): Response<Unit>

    @GET("api/v1/recommendations/report")
    suspend fun getRecommendationReport(): Response<ApiResponse<RecommendationReportDto>>

    @GET("api/v1/meal-plans/report")
    suspend fun getMealPlanReport(): Response<ApiResponse<MealPlanReportDto>>

    @POST("api/v1/meal-plans/generate")
    suspend fun generateMealPlan(
        @Body request: MealPlanRequestDto
    ): Response<ApiResponse<MealPlanResponseDto>>

    @GET("api/v1/meal-plans/latest")
    suspend fun getLatestMealPlan(): Response<ApiResponse<MealPlanResponseDto>>

    @GET("api/v1/meal-plans/meals/{planRecipeId}/composition")
    suspend fun getMealPlanMealComposition(
        @Path("planRecipeId") planRecipeId: Long
    ): Response<ApiResponse<RecipeCompositionDto>>

    @GET("api/v1/meal-plans/meals/{planRecipeId}/explanation")
    suspend fun getMealPlanMealExplanation(
        @Path("planRecipeId") planRecipeId: Long
    ): Response<ApiResponse<MealPlanMealExplanationDto>>

    @DELETE("api/v1/meal-plans/meals/{planRecipeId}")
    suspend fun removeMealFromPlan(
        @Path("planRecipeId") planRecipeId: Long
    ): Response<ApiResponse<MealPlanResponseDto>>

    @POST("api/v1/meal-plans/meals/{planRecipeId}/replace")
    suspend fun replaceMealInPlan(
        @Path("planRecipeId") planRecipeId: Long,
        @Body request: ReplaceMealInPlanRequestDto
    ): Response<ApiResponse<MealPlanResponseDto>>

    @POST("api/v1/meal-plans/meals/{planRecipeId}/dislike")
    suspend fun dislikeMealFromPlan(
        @Path("planRecipeId") planRecipeId: Long
    ): Response<ApiResponse<MealPlanResponseDto>>

    @POST("api/v1/meal-plans/meals/{planRecipeId}/pin")
    suspend fun toggleMealPin(
        @Path("planRecipeId") planRecipeId: Long
    ): Response<ApiResponse<MealPlanResponseDto>>

    @POST("api/v1/meal-plans/meals/manual")
    suspend fun addRecipeToMealPlan(
        @Body request: AddRecipeToMealPlanRequestDto
    ): Response<ApiResponse<MealPlanResponseDto>>

    @POST("api/v1/meal-plans/replan/today")
    suspend fun replanRemainingDay(): Response<ApiResponse<MealPlanResponseDto>>

    @POST("api/v1/meal-plans/replan/day/{planDate}")
    suspend fun fillEmptySlotsForDay(
        @Path("planDate") planDate: String
    ): Response<ApiResponse<MealPlanResponseDto>>

    @POST("api/v1/meal-plans/replan/day/{planDate}/all")
    suspend fun replanDay(
        @Path("planDate") planDate: String
    ): Response<ApiResponse<MealPlanResponseDto>>

    @POST("api/v1/meal-plans/replan/upcoming/{days}")
    suspend fun replanUpcomingDays(
        @Path("days") days: Int
    ): Response<ApiResponse<MealPlanResponseDto>>

    @POST("api/v1/meal-plans/days/{planDate}/repeat-next")
    suspend fun repeatDayToNext(
        @Path("planDate") planDate: String
    ): Response<ApiResponse<MealPlanResponseDto>>

    @GET("api/v1/meal-plans/shopping-list/{id}")
    suspend fun getShoppingList(
        @Path("id") id: Long
    ): Response<ApiResponse<List<ShoppingListItemDto>>>

    @GET("api/v1/meal-plans/shopping-list/latest")
    suspend fun getLatestShoppingList(): Response<ApiResponse<List<ShoppingListItemDto>>>

    @POST("api/v1/analytics/events")
    suspend fun sendAnalyticsEvent(
        @Body event: AnalyticsEventDto
    ): Response<Unit>

    @POST("api/v1/notifications/token")
    suspend fun updatePushToken(
        @Body request: PushTokenRequestDto
    ): Response<ApiResponse<JsonElement>>
}
