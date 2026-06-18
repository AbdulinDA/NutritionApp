package com.abdulin.nutritionapp.core.utils

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.abdulin.nutritionapp.R
import com.abdulin.nutritionapp.data.dto.auth.UserResponseDto
import com.abdulin.nutritionapp.domain.model.ShoppingListItem
import com.abdulin.nutritionapp.presentation.profile.FavoriteRecipeItem
import com.abdulin.nutritionapp.presentation.profile.FoodPreferenceProduct
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "auth_prefs")

class TokenManager(private val context: Context) {

    companion object {
        private const val SECURE_AUTH_PREFS = "secure_auth_prefs"
        private const val UI_PREFS = "ui_prefs"
        private const val ACCESS_TOKEN_KEY = "access_token"
        private const val REFRESH_TOKEN_KEY = "refresh_token"
        private const val APP_LOCALE_PREF_KEY = "app_locale_code"
        private val ACCESS_TOKEN = stringPreferencesKey("access_token")
        private val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        private val LAST_MEAL_PLAN_ID = longPreferencesKey("last_meal_plan_id")
        private val THEME_PRESET_ORDINAL = intPreferencesKey("theme_preset_ordinal")
        private val APP_LOCALE_CODE = stringPreferencesKey("app_locale_code")
        private val SHOPPING_LIST_PLAN_ID = longPreferencesKey("shopping_list_plan_id")
        private val PURCHASED_SHOPPING_ITEM_IDS = stringSetPreferencesKey("purchased_shopping_item_ids")
        private val PURCHASED_SHOPPING_ITEM_KEYS = stringSetPreferencesKey("purchased_shopping_item_keys")
        private val CHECKED_SHOPPING_ITEM_KEYS = stringSetPreferencesKey("checked_shopping_item_keys")
        private val AT_HOME_SHOPPING_ITEM_KEYS = stringSetPreferencesKey("at_home_shopping_item_keys")
        private val HIDDEN_SHOPPING_ITEM_KEYS = stringSetPreferencesKey("hidden_shopping_item_keys")
        private val MANUAL_SHOPPING_ITEM_ENTRIES = stringSetPreferencesKey("manual_shopping_item_entries")
        private val FAVORITE_PRODUCT_IDS = stringSetPreferencesKey("favorite_product_ids")
        private val FAVORITE_RECIPE_IDS = stringSetPreferencesKey("favorite_recipe_ids")
        private val FAVORITE_PRODUCT_ENTRIES = stringSetPreferencesKey("favorite_product_entries")
        private val FAVORITE_RECIPE_ENTRIES = stringSetPreferencesKey("favorite_recipe_entries")
        private val EXCLUDED_PRODUCT_ENTRIES = stringSetPreferencesKey("excluded_product_entries")
        private val ALLERGY_PRODUCT_ENTRIES = stringSetPreferencesKey("allergy_product_entries")
        private val FRIDGE_PRODUCT_ENTRIES = stringSetPreferencesKey("fridge_product_entries")
        private val FAVORITE_CUISINE_ENTRIES = stringSetPreferencesKey("favorite_cuisine_entries")
        private val DISLIKED_CUISINE_ENTRIES = stringSetPreferencesKey("disliked_cuisine_entries")
    }

    private val securePrefs by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            context,
            SECURE_AUTH_PREFS,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    suspend fun saveTokens(access: String, refresh: String) {
        securePrefs.edit()
            .putString(ACCESS_TOKEN_KEY, access)
            .putString(REFRESH_TOKEN_KEY, refresh)
            .apply()
        clearLegacyTokens()
    }

    val accessTokenFlow: Flow<String?> = flow {
        emit(getAccessToken())
    }

    val refreshTokenFlow: Flow<String?> = flow {
        emit(getRefreshToken())
    }

    suspend fun getAccessToken(): String? {
        securePrefs.getString(ACCESS_TOKEN_KEY, null)?.let { return it }
        return migrateLegacyTokens()?.first
    }

    fun peekAccessToken(): String? = securePrefs.getString(ACCESS_TOKEN_KEY, null)
    
    suspend fun getRefreshToken(): String? {
        securePrefs.getString(REFRESH_TOKEN_KEY, null)?.let { return it }
        return migrateLegacyTokens()?.second
    }

    suspend fun saveLastMealPlanId(id: Long) {
        context.dataStore.edit { prefs ->
            prefs[LAST_MEAL_PLAN_ID] = id
        }
    }

    val lastMealPlanId: Flow<Long?> = context.dataStore.data
        .map { prefs -> prefs[LAST_MEAL_PLAN_ID] }

    // Темы
    suspend fun saveThemePreset(ordinal: Int) {
        context.dataStore.edit { it[THEME_PRESET_ORDINAL] = ordinal }
    }

    val themePresetOrdinal: Flow<Int> = context.dataStore.data
        .map { it[THEME_PRESET_ORDINAL] ?: 0 }

    suspend fun saveAppLocaleCode(localeCode: String?) {
        val uiPrefs = context.getSharedPreferences(UI_PREFS, Context.MODE_PRIVATE)
        uiPrefs.edit().apply {
            if (localeCode.isNullOrBlank()) {
                remove(APP_LOCALE_PREF_KEY)
            } else {
                putString(APP_LOCALE_PREF_KEY, localeCode)
            }
        }.apply()
        context.dataStore.edit {
            if (localeCode.isNullOrBlank()) {
                it.remove(APP_LOCALE_CODE)
            } else {
                it[APP_LOCALE_CODE] = localeCode
            }
        }
    }

    val appLocaleCode: Flow<String?> = context.dataStore.data
        .map { it[APP_LOCALE_CODE] }

    fun peekSavedAppLocaleCode(): String? {
        return context.getSharedPreferences(UI_PREFS, Context.MODE_PRIVATE)
            .getString(APP_LOCALE_PREF_KEY, null)
    }

    val favoriteProductIds: Flow<Set<Long>> = context.dataStore.data
        .map { prefs ->
            prefs[FAVORITE_PRODUCT_IDS]
                .orEmpty()
                .mapNotNull { it.toLongOrNull() }
                .toSet()
        }

    val favoriteRecipeIds: Flow<Set<Long>> = context.dataStore.data
        .map { prefs ->
            prefs[FAVORITE_RECIPE_IDS]
                .orEmpty()
                .mapNotNull { it.toLongOrNull() }
                .toSet()
        }

    val favoriteProducts: Flow<List<FoodPreferenceProduct>> = context.dataStore.data
        .map { prefs -> decodePreferenceProducts(prefs[FAVORITE_PRODUCT_ENTRIES]) }

    val favoriteRecipes: Flow<List<FavoriteRecipeItem>> = context.dataStore.data
        .map { prefs -> decodeFavoriteRecipes(prefs[FAVORITE_RECIPE_ENTRIES]) }

    val excludedProducts: Flow<List<FoodPreferenceProduct>> = context.dataStore.data
        .map { prefs -> decodePreferenceProducts(prefs[EXCLUDED_PRODUCT_ENTRIES]) }

    val allergyProducts: Flow<List<FoodPreferenceProduct>> = context.dataStore.data
        .map { prefs -> decodePreferenceProducts(prefs[ALLERGY_PRODUCT_ENTRIES]) }

    val fridgeProducts: Flow<List<FoodPreferenceProduct>> = context.dataStore.data
        .map { prefs -> decodePreferenceProducts(prefs[FRIDGE_PRODUCT_ENTRIES]) }

    val favoriteCuisines: Flow<List<String>> = context.dataStore.data
        .map { prefs -> decodePreferenceStrings(prefs[FAVORITE_CUISINE_ENTRIES]) }

    val dislikedCuisines: Flow<List<String>> = context.dataStore.data
        .map { prefs -> decodePreferenceStrings(prefs[DISLIKED_CUISINE_ENTRIES]) }

    suspend fun getPurchasedShoppingItemIds(planId: Long): Set<Long> {
        val prefs = context.dataStore.data.first()
        val savedPlanId = prefs[SHOPPING_LIST_PLAN_ID]
        if (savedPlanId != planId) {
            clearPurchasedShoppingItemIds()
            context.dataStore.edit { it[SHOPPING_LIST_PLAN_ID] = planId }
            return emptySet()
        }

        return prefs[PURCHASED_SHOPPING_ITEM_IDS]
            .orEmpty()
            .mapNotNull { it.toLongOrNull() }
            .toSet()
    }

    suspend fun getPurchasedShoppingItemKeys(planId: Long): Set<String> {
        val prefs = context.dataStore.data.first()
        val savedPlanId = prefs[SHOPPING_LIST_PLAN_ID]
        if (savedPlanId != planId) {
            clearPurchasedShoppingItemIds()
            context.dataStore.edit { it[SHOPPING_LIST_PLAN_ID] = planId }
            return emptySet()
        }

        return prefs[PURCHASED_SHOPPING_ITEM_KEYS].orEmpty()
    }

    suspend fun getCheckedShoppingItemKeys(planId: Long): Set<String> {
        val prefs = context.dataStore.data.first()
        val savedPlanId = prefs[SHOPPING_LIST_PLAN_ID]
        if (savedPlanId != planId) {
            clearPurchasedShoppingItemIds()
            context.dataStore.edit { it[SHOPPING_LIST_PLAN_ID] = planId }
            return emptySet()
        }

        return prefs[CHECKED_SHOPPING_ITEM_KEYS].orEmpty()
    }

    suspend fun getAtHomeShoppingItemKeys(planId: Long): Set<String> {
        val prefs = context.dataStore.data.first()
        val savedPlanId = prefs[SHOPPING_LIST_PLAN_ID]
        if (savedPlanId != planId) {
            clearPurchasedShoppingItemIds()
            context.dataStore.edit { it[SHOPPING_LIST_PLAN_ID] = planId }
            return emptySet()
        }
        return prefs[AT_HOME_SHOPPING_ITEM_KEYS].orEmpty()
    }

    suspend fun getHiddenShoppingItemKeys(planId: Long): Set<String> {
        val prefs = context.dataStore.data.first()
        val savedPlanId = prefs[SHOPPING_LIST_PLAN_ID]
        if (savedPlanId != planId) {
            clearPurchasedShoppingItemIds()
            context.dataStore.edit { it[SHOPPING_LIST_PLAN_ID] = planId }
            return emptySet()
        }
        return prefs[HIDDEN_SHOPPING_ITEM_KEYS].orEmpty()
    }

    suspend fun getManualShoppingItems(planId: Long): List<ShoppingListItem> {
        val prefs = context.dataStore.data.first()
        val savedPlanId = prefs[SHOPPING_LIST_PLAN_ID]
        if (savedPlanId != planId) {
            clearPurchasedShoppingItemIds()
            context.dataStore.edit { it[SHOPPING_LIST_PLAN_ID] = planId }
            return emptyList()
        }
        return decodeManualShoppingItems(prefs[MANUAL_SHOPPING_ITEM_ENTRIES])
    }

    suspend fun markShoppingItemPurchased(planId: Long, productId: Long) {
        val currentIds = getPurchasedShoppingItemIds(planId).toMutableSet()
        currentIds += productId
        context.dataStore.edit {
            it[SHOPPING_LIST_PLAN_ID] = planId
            it[PURCHASED_SHOPPING_ITEM_IDS] = currentIds.map(Long::toString).toSet()
        }
    }

    suspend fun markShoppingItemsPurchased(planId: Long, productIds: Set<Long>) {
        if (productIds.isEmpty()) return

        val currentIds = getPurchasedShoppingItemIds(planId).toMutableSet()
        currentIds += productIds
        context.dataStore.edit {
            it[SHOPPING_LIST_PLAN_ID] = planId
            it[PURCHASED_SHOPPING_ITEM_IDS] = currentIds.map(Long::toString).toSet()
        }
    }

    suspend fun markShoppingItemKeysPurchased(planId: Long, itemKeys: Set<String>) {
        if (itemKeys.isEmpty()) return

        val currentKeys = getPurchasedShoppingItemKeys(planId).toMutableSet()
        currentKeys += itemKeys
        context.dataStore.edit {
            it[SHOPPING_LIST_PLAN_ID] = planId
            it[PURCHASED_SHOPPING_ITEM_KEYS] = currentKeys
            it[CHECKED_SHOPPING_ITEM_KEYS] = emptySet()
        }
    }

    suspend fun markShoppingItemKeysAtHome(planId: Long, itemKeys: Set<String>) {
        if (itemKeys.isEmpty()) return

        val currentKeys = getAtHomeShoppingItemKeys(planId).toMutableSet()
        currentKeys += itemKeys
        context.dataStore.edit {
            it[SHOPPING_LIST_PLAN_ID] = planId
            it[AT_HOME_SHOPPING_ITEM_KEYS] = currentKeys
            it[CHECKED_SHOPPING_ITEM_KEYS] = emptySet()
        }
    }

    suspend fun hideShoppingItemKeys(planId: Long, itemKeys: Set<String>) {
        if (itemKeys.isEmpty()) return

        val currentKeys = getHiddenShoppingItemKeys(planId).toMutableSet()
        currentKeys += itemKeys
        context.dataStore.edit {
            it[SHOPPING_LIST_PLAN_ID] = planId
            it[HIDDEN_SHOPPING_ITEM_KEYS] = currentKeys
            it[CHECKED_SHOPPING_ITEM_KEYS] = emptySet()
        }
    }

    suspend fun saveManualShoppingItem(planId: Long, item: ShoppingListItem) {
        val currentItems = getManualShoppingItems(planId).toMutableList()
        currentItems.removeAll { it.purchaseKey() == item.purchaseKey() }
        currentItems += item.copy(isManual = true)
        context.dataStore.edit {
            it[SHOPPING_LIST_PLAN_ID] = planId
            it[MANUAL_SHOPPING_ITEM_ENTRIES] = currentItems.map(::encodeManualShoppingItem).toSet()
        }
    }

    suspend fun saveCheckedShoppingItemKeys(planId: Long, itemKeys: Set<String>) {
        context.dataStore.edit {
            it[SHOPPING_LIST_PLAN_ID] = planId
            it[CHECKED_SHOPPING_ITEM_KEYS] = itemKeys
        }
    }

    suspend fun clearPurchasedShoppingItemIds() {
        context.dataStore.edit {
            it.remove(SHOPPING_LIST_PLAN_ID)
            it.remove(PURCHASED_SHOPPING_ITEM_IDS)
            it.remove(PURCHASED_SHOPPING_ITEM_KEYS)
            it.remove(CHECKED_SHOPPING_ITEM_KEYS)
            it.remove(AT_HOME_SHOPPING_ITEM_KEYS)
            it.remove(HIDDEN_SHOPPING_ITEM_KEYS)
            it.remove(MANUAL_SHOPPING_ITEM_ENTRIES)
        }
    }

    private fun encodeManualShoppingItem(item: ShoppingListItem): String {
        val safeName = item.productName.replace("|", " ").trim()
        val safeCategory = item.category.replace("|", " ").trim()
        val safeUnit = item.unit.replace("|", " ").trim()
        return listOf(
            item.productId.toString(),
            safeName,
            safeCategory,
            item.totalQuantity.toString(),
            safeUnit
        ).joinToString("|")
    }

    private fun decodeManualShoppingItems(entries: Set<String>?): List<ShoppingListItem> {
        return entries.orEmpty().mapNotNull { entry ->
            val parts = entry.split("|")
            if (parts.size != 5) return@mapNotNull null
            val productId = parts[0].toLongOrNull() ?: return@mapNotNull null
            val quantity = parts[3].toDoubleOrNull() ?: return@mapNotNull null
            ShoppingListItem(
                productId = productId,
                productName = parts[1],
                category = parts[2],
                totalQuantity = quantity,
                unit = parts[4],
                isManual = true
            )
        }.sortedBy { it.productName.lowercase() }
    }

    private fun ShoppingListItem.purchaseKey(): String {
        return listOf(
            productId.toString(),
            productName.trim().lowercase(),
            category.trim().lowercase(),
            totalQuantity.toString(),
            unit.trim().lowercase()
        ).joinToString("|")
    }

    suspend fun toggleFavoriteProductId(productId: Long, productName: String? = null): Boolean {
        val updated = favoriteProductIds.first().toMutableSet().apply {
            if (!add(productId)) {
                remove(productId)
            }
        }
        context.dataStore.edit {
            it[FAVORITE_PRODUCT_IDS] = updated.map(Long::toString).toSet()
        }
        productName?.let { name ->
            updateNamedProductEntries(
                key = FAVORITE_PRODUCT_ENTRIES,
                current = favoriteProducts.first(),
                product = FoodPreferenceProduct(productId, name),
                shouldContain = updated.contains(productId)
            )
        }
        return updated.contains(productId)
    }

    suspend fun isFavoriteProduct(productId: Long): Boolean {
        return favoriteProductIds.first().contains(productId)
    }

    suspend fun toggleFavoriteRecipeId(recipeId: Long, recipeTitle: String? = null): Boolean {
        val updated = favoriteRecipeIds.first().toMutableSet().apply {
            if (!add(recipeId)) {
                remove(recipeId)
            }
        }
        context.dataStore.edit {
            it[FAVORITE_RECIPE_IDS] = updated.map(Long::toString).toSet()
        }
        recipeTitle?.let { title ->
            updateNamedRecipeEntries(
                current = favoriteRecipes.first(),
                recipe = FavoriteRecipeItem(recipeId, title),
                shouldContain = updated.contains(recipeId)
            )
        }
        return updated.contains(recipeId)
    }

    suspend fun isFavoriteRecipe(recipeId: Long): Boolean {
        return favoriteRecipeIds.first().contains(recipeId)
    }

    suspend fun toggleExcludedProduct(product: FoodPreferenceProduct): Boolean {
        return togglePreferenceProduct(
            key = EXCLUDED_PRODUCT_ENTRIES,
            current = excludedProducts.first(),
            product = product,
            conflictKey = ALLERGY_PRODUCT_ENTRIES
        )
    }

    suspend fun toggleAllergyProduct(product: FoodPreferenceProduct): Boolean {
        return togglePreferenceProduct(
            key = ALLERGY_PRODUCT_ENTRIES,
            current = allergyProducts.first(),
            product = product,
            conflictKey = EXCLUDED_PRODUCT_ENTRIES
        )
    }

    suspend fun toggleFridgeProduct(product: FoodPreferenceProduct): Boolean {
        return togglePreferenceProduct(FRIDGE_PRODUCT_ENTRIES, fridgeProducts.first(), product)
    }

    suspend fun toggleFavoriteCuisine(cuisine: String): Boolean {
        val normalizedCuisine = cuisine.trim()
        if (normalizedCuisine.isBlank()) {
            return false
        }

        val updated = favoriteCuisines.first().toMutableList().apply {
            val existingIndex = indexOfFirst { it.equals(normalizedCuisine, ignoreCase = true) }
            if (existingIndex >= 0) {
                removeAt(existingIndex)
            } else {
                add(normalizedCuisine)
            }
        }
            .distinctBy { it.lowercase() }
            .sortedBy { it.lowercase() }

        context.dataStore.edit {
            it[FAVORITE_CUISINE_ENTRIES] = updated.toSet()
            val disliked = decodePreferenceStrings(it[DISLIKED_CUISINE_ENTRIES])
                .filterNot { entry -> entry.equals(normalizedCuisine, ignoreCase = true) }
            it[DISLIKED_CUISINE_ENTRIES] = disliked.toSet()
        }
        return updated.any { it.equals(normalizedCuisine, ignoreCase = true) }
    }

    suspend fun toggleDislikedCuisine(cuisine: String): Boolean {
        val normalizedCuisine = cuisine.trim()
        if (normalizedCuisine.isBlank()) {
            return false
        }

        val updated = dislikedCuisines.first().toMutableList().apply {
            val existingIndex = indexOfFirst { it.equals(normalizedCuisine, ignoreCase = true) }
            if (existingIndex >= 0) {
                removeAt(existingIndex)
            } else {
                add(normalizedCuisine)
            }
        }
            .distinctBy { it.lowercase() }
            .sortedBy { it.lowercase() }

        context.dataStore.edit {
            it[DISLIKED_CUISINE_ENTRIES] = updated.toSet()
            val favorites = decodePreferenceStrings(it[FAVORITE_CUISINE_ENTRIES])
                .filterNot { entry -> entry.equals(normalizedCuisine, ignoreCase = true) }
            it[FAVORITE_CUISINE_ENTRIES] = favorites.toSet()
        }
        return updated.any { it.equals(normalizedCuisine, ignoreCase = true) }
    }

    private suspend fun togglePreferenceProduct(
        key: androidx.datastore.preferences.core.Preferences.Key<Set<String>>,
        current: List<FoodPreferenceProduct>,
        product: FoodPreferenceProduct,
        conflictKey: androidx.datastore.preferences.core.Preferences.Key<Set<String>>? = null,
        matches: (FoodPreferenceProduct, FoodPreferenceProduct) -> Boolean = { left, right ->
            left.id == right.id || normalizePreferenceName(left.name) == normalizePreferenceName(right.name)
        }
    ): Boolean {
        val updated = normalizePreferenceProducts(current).toMutableList().apply {
            val existingIndex = indexOfFirst { matches(it, product) }
            if (existingIndex >= 0) {
                removeAt(existingIndex)
            } else {
                add(product)
            }
        }
        context.dataStore.edit {
            it[key] = updated.map(::encodePreferenceProduct).toSet()
            if (conflictKey != null) {
                val conflictCurrent = decodePreferenceProducts(it[conflictKey])
                val conflictUpdated = conflictCurrent
                    .filterNot { matches(it, product) }
                    .let(::normalizePreferenceProducts)
                it[conflictKey] = conflictUpdated.map(::encodePreferenceProduct).toSet()
            }
        }
        return updated.any { matches(it, product) }
    }

    suspend fun syncFoodPreferencesFromProfile(profile: UserResponseDto) {
        syncExcludedProductsFromServer(profile.excludedProductsIds)
        syncAllergyProductsFromServer(profile.allergies)
        syncFavoriteCuisinesFromServer(profile.favoriteCuisines)
        syncDislikedCuisinesFromServer(profile.dislikedCuisines)
    }

    suspend fun normalizeStoredFoodPreferences() {
        val normalizedAllergies = normalizePreferenceProducts(allergyProducts.first())
        val normalizedExcluded = normalizePreferenceProducts(excludedProducts.first())

        val allergyKeys = normalizedAllergies
            .map(::preferenceIdentity)
            .toSet()
        val cleanedExcluded = normalizedExcluded
            .filterNot { allergyKeys.contains(preferenceIdentity(it)) }
            .let(::normalizePreferenceProducts)

        context.dataStore.edit {
            it[ALLERGY_PRODUCT_ENTRIES] = normalizedAllergies.map(::encodePreferenceProduct).toSet()
            it[EXCLUDED_PRODUCT_ENTRIES] = cleanedExcluded.map(::encodePreferenceProduct).toSet()
        }
    }

    suspend fun syncExcludedProductsFromServer(productIds: List<Long>) {
        val currentProducts = excludedProducts.first()
        val currentById = currentProducts.associateBy { it.id }
        val updated = productIds
            .distinct()
            .map { productId ->
                currentById[productId] ?: FoodPreferenceProduct(
                    id = productId,
                    name = context.getString(R.string.food_preferences_product_fallback, productId)
                )
            }
            .sortedBy { it.name.lowercase() }

        context.dataStore.edit {
            it[EXCLUDED_PRODUCT_ENTRIES] = updated.map(::encodePreferenceProduct).toSet()
        }
    }

    suspend fun syncAllergyProductsFromServer(allergies: List<String>) {
        val currentProducts = allergyProducts.first()
        val currentByName = currentProducts.associateBy { it.name.trim().lowercase() }
        val updated = allergies
            .mapNotNull { it.trim().takeIf(String::isNotBlank) }
            .distinctBy { it.lowercase() }
            .map { allergyName ->
                currentByName[allergyName.lowercase()] ?: FoodPreferenceProduct(
                    id = stableGeneratedProductId(allergyName),
                    name = allergyName
                )
            }
            .sortedBy { it.name.lowercase() }

        context.dataStore.edit {
            it[ALLERGY_PRODUCT_ENTRIES] = updated.map(::encodePreferenceProduct).toSet()
        }
    }

    suspend fun syncFavoriteCuisinesFromServer(cuisines: List<String>) {
        val updated = cuisines
            .mapNotNull { it.trim().takeIf(String::isNotBlank) }
            .distinctBy { it.lowercase() }
            .sortedBy { it.lowercase() }

        context.dataStore.edit {
            it[FAVORITE_CUISINE_ENTRIES] = updated.toSet()
        }
    }

    suspend fun syncDislikedCuisinesFromServer(cuisines: List<String>) {
        val favorites = favoriteCuisines.first()
        val updated = cuisines
            .mapNotNull { it.trim().takeIf(String::isNotBlank) }
            .distinctBy { it.lowercase() }
            .filterNot { disliked -> favorites.any { it.equals(disliked, ignoreCase = true) } }
            .sortedBy { it.lowercase() }

        context.dataStore.edit {
            it[DISLIKED_CUISINE_ENTRIES] = updated.toSet()
        }
    }

    private fun encodePreferenceProduct(product: FoodPreferenceProduct): String {
        return "${product.id}|${product.name.replace("|", " ")}"
    }

    private fun encodeFavoriteRecipe(recipe: FavoriteRecipeItem): String {
        return "${recipe.id}|${recipe.title.replace("|", " ")}"
    }

    private fun decodePreferenceProducts(entries: Set<String>?): List<FoodPreferenceProduct> {
        val deduped = entries
            .orEmpty()
            .mapNotNull { entry ->
                val separatorIndex = entry.indexOf('|')
                if (separatorIndex <= 0 || separatorIndex >= entry.lastIndex) {
                    return@mapNotNull null
                }
                val id = entry.substring(0, separatorIndex).toLongOrNull() ?: return@mapNotNull null
                val name = entry.substring(separatorIndex + 1).trim()
                if (name.isBlank()) return@mapNotNull null
                FoodPreferenceProduct(id = id, name = name)
            }
        return normalizePreferenceProducts(deduped)
    }

    private fun decodeFavoriteRecipes(entries: Set<String>?): List<FavoriteRecipeItem> {
        return entries
            .orEmpty()
            .mapNotNull { entry ->
                val separatorIndex = entry.indexOf('|')
                if (separatorIndex <= 0 || separatorIndex >= entry.lastIndex) return@mapNotNull null
                val id = entry.substring(0, separatorIndex).toLongOrNull() ?: return@mapNotNull null
                val title = entry.substring(separatorIndex + 1).trim()
                if (title.isBlank()) return@mapNotNull null
                FavoriteRecipeItem(id = id, title = title)
            }
            .sortedBy { it.title.lowercase() }
    }

    private fun decodePreferenceStrings(entries: Set<String>?): List<String> {
        return entries
            .orEmpty()
            .mapNotNull { it.trim().takeIf(String::isNotBlank) }
            .distinctBy { it.lowercase() }
            .sortedBy { it.lowercase() }
    }

    private suspend fun updateNamedProductEntries(
        key: androidx.datastore.preferences.core.Preferences.Key<Set<String>>,
        current: List<FoodPreferenceProduct>,
        product: FoodPreferenceProduct,
        shouldContain: Boolean
    ) {
        val updated = current.toMutableList().apply {
            removeAll { it.id == product.id }
            if (shouldContain) add(product)
        }
        context.dataStore.edit {
            it[key] = updated.map(::encodePreferenceProduct).toSet()
        }
    }

    private suspend fun updateNamedRecipeEntries(
        current: List<FavoriteRecipeItem>,
        recipe: FavoriteRecipeItem,
        shouldContain: Boolean
    ) {
        val updated = current.toMutableList().apply {
            removeAll { it.id == recipe.id }
            if (shouldContain) add(recipe)
        }
        context.dataStore.edit {
            it[FAVORITE_RECIPE_ENTRIES] = updated.map(::encodeFavoriteRecipe).toSet()
        }
    }

    private fun stableGeneratedProductId(name: String): Long {
        return -name.trim().lowercase().hashCode().toLong().let { if (it == 0L) 1L else kotlin.math.abs(it) }
    }

    private fun normalizePreferenceProducts(products: List<FoodPreferenceProduct>): List<FoodPreferenceProduct> {
        return products
            .asSequence()
            .filter { it.name.isNotBlank() }
            .distinctBy { preferenceIdentity(it) }
            .sortedBy { it.name.lowercase() }
            .toList()
    }

    private fun preferenceIdentity(product: FoodPreferenceProduct): String {
        val normalizedName = normalizePreferenceName(product.name)
        return if (normalizedName.isNotBlank()) {
            normalizedName
        } else {
            "id:${product.id}"
        }
    }

    private fun normalizePreferenceName(name: String?): String {
        return name?.trim()?.lowercase().orEmpty()
    }

    suspend fun clearTokens() {
        securePrefs.edit().clear().apply()
        clearLegacyTokens()
    }

    private suspend fun migrateLegacyTokens(): Pair<String, String>? {
        val prefs = context.dataStore.data.first()
        val legacyAccessToken = prefs[ACCESS_TOKEN]
        val legacyRefreshToken = prefs[REFRESH_TOKEN]

        if (legacyAccessToken.isNullOrBlank() || legacyRefreshToken.isNullOrBlank()) {
            clearLegacyTokens()
            return null
        }

        securePrefs.edit()
            .putString(ACCESS_TOKEN_KEY, legacyAccessToken)
            .putString(REFRESH_TOKEN_KEY, legacyRefreshToken)
            .apply()
        clearLegacyTokens()
        return legacyAccessToken to legacyRefreshToken
    }

    private suspend fun clearLegacyTokens() {
        context.dataStore.edit { prefs ->
            prefs.remove(ACCESS_TOKEN)
            prefs.remove(REFRESH_TOKEN)
        }
    }
}
