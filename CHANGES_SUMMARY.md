# Резюме изменений для исправления масштабирования количества продуктов в рецептах

## Проблема
Когда пользователь менял количество порций в рецепте, калории и БЖУ пересчитывались корректно, но **количество продуктов (quantity) не менялось**.

## Причина
1. API возвращает `quantity` для каждого ингредиента как есть (для одной порции)
2. API **не** масштабирует `quantity` на `selectedServings` (количество порций, выбранных пользователем)
3. В `RecipeService.getRecipeComposition()` масштабирование происходит только на `portionMultiplier` для гарнира (по умолчанию `1.0`)

### Ключевая строка в `RecipeService.java`:
```java
composition.setIngredientBreakdown(buildCompositionBreakdown(mainRecipe, "main_recipe", 1.0, sideDishRecipe, multiplier));
```
Здесь `mainMultiplier = 1.0` — это фиксированное значение, которое **не учитывает** количество порций, выбранных пользователем.

## Решение
Масштабирование количества продуктов реализовано на стороне Android-клиента в `RecipeDetailScreen.kt`.

### Измененные файлы
- `app/src/main/java/com/abdulin/nutritionapp/presentation/recipe/RecipeDetailScreen.kt`

### Изменения

#### 1. Улучшенная функция `scaleForServings` с логированием
```kotlin
private fun RecipeIngredientModel.scaleForServings(multiplier: Double): RecipeIngredientModel {
    if (multiplier == 1.0) return this
    
    println("scaleForServings: multiplier=$multiplier, before: quantity=$quantity, calories=$calories")

    val scaledQuantity = quantity?.times(multiplier)
    val scaledCalories = calories * multiplier
    val scaledProtein = protein * multiplier
    val scaledFat = fat * multiplier
    val scaledCarbs = carbs * multiplier
    
    println("scaleForServings: after: quantity=$scaledQuantity, calories=$scaledCalories")

    return copy(
        quantity = scaledQuantity,
        calories = scaledCalories,
        protein = scaledProtein,
        fat = scaledFat,
        carbs = scaledCarbs
    )
}
```

#### 2. Улучшенная функция `formatIngredientAmount` для более точного отображения
```kotlin
private fun formatIngredientAmount(ingredient: RecipeIngredientModel): String {
    val quantity = ingredient.quantity ?: return ingredient.unit.orEmpty()
    val formatted = if (quantity % 1.0 == 0.0) {
        quantity.toInt().toString()
    } else {
        String.format(Locale.getDefault(), "%.2f", quantity).removeSuffix("0").removeSuffix(".")
    }
    return listOf(formatted, ingredient.unit)
        .filter { !it.isNullOrBlank() }
        .joinToString(" ")
}
```

Изменено с `%.1f` на `%.2f` для более точного отображения дробных чисел, с автоматическим удалением лишних нулей.

#### 3. Добавлено логирование в `IngredientNutritionSection`
```kotlin
println("IngredientNutritionSection: multiplier=$multiplier, ingredientsCount=${ingredients.size}")
ingredients.forEach { ingredient ->
    println("  Ingredient: ${ingredient.name}, quantity=${ingredient.quantity}, scaled=${ingredient.quantity?.times(multiplier)}")
}
```

#### 4. Добавлено логирование в `NutritionBreakdownSection`
```kotlin
println("NutritionBreakdownSection: servingsBase=$servingsBase, selectedServings=$selectedServings, ingredientMultiplier=$ingredientMultiplier")
println("  mainRecipe ingredients count: ${effectiveComposition.mainRecipe.ingredients.size}")
effectiveComposition.mainRecipe.ingredients.take(3).forEach { ingredient ->
    println("    Ingredient: ${ingredient.name}, quantity=${ingredient.quantity}, calories=${ingredient.calories}")
}
```

## Как это работает

Когда пользователь выбирает другое количество порций в `NutritionBreakdownSection`:

1. Вычисляется `ingredientMultiplier = selectedServings / servingsBase`
2. Для каждого ингредиента вызывается `scaleForServings(multiplier)`, который масштабирует:
   - `quantity` (количество продукта)
   - `calories` (калории)
   - `protein` (белки)
   - `fat` (жиры)
   - `carbs` (углеводы)
3. Масштабированный ингредиент передаётся в `IngredientNutritionRow`, который отображает масштабированное количество

## Тестирование

### Сборка приложения
```bash
cd C:/Projects/NutritionApp
.\gradlew.bat assembleDebug
```

### Установка APK
```bash
# Подключите Android устройство или эмулятор
adb install app/build/outputs/apk/debug/app-debug.apk
```

### Проверка масштабирования
1. Откройте любой рецепт
2. Прокрутите до раздела "Сводка питания" (Nutrition Breakdown)
3. Выберите другое количество порций (например, 2 или 3)
4. Проверьте, что количество продуктов (quantity) меняется пропорционально

### Проверка логов
Если проблема всё ещё будет наблюдаться, проверьте логи через `logcat`:

```bash
# Фильтруем по ключевым словам
adb logcat | Select-String "scaleForServings|NutritionBreakdownSection|IngredientNutritionSection"
```

Ожидаемые строки в логах:
- `NutritionBreakdownSection: servingsBase=X, selectedServings=Y, ingredientMultiplier=Z`
- `IngredientNutritionSection: multiplier=Z, ingredientsCount=N`
- `scaleForServings: multiplier=Z, before: quantity=Q, calories=C`
- `scaleForServings: after: quantity=Q', calories=C'`

## Возможные проблемы и диагностика

### Проблема: quantity не масштабируется
**Проверить:**
1. В логах `NutritionBreakdownSection` увидеть правильный `ingredientMultiplier`
2. В логах `IngredientNutritionSection` увидеть правильный `multiplier`
3. В логах `scaleForServings` увидеть, что `multiplier != 1.0`

### Проблема: quantity масштабируется, но отображается неправильно
**Проверить:**
1. `formatIngredientAmount` использует `%.2f` для более точного отображения
2. Проверить, что `quantity` не равен `null`

### Проблем��: API возвращает null quantity
**Причина:** API не сохраняет `quantity` для ингредиентов
**Решение:** Убедиться, что в БД есть значения `quantity` для ингредиентов

## Файлы, которые не требуют изменений

### API (`C:/Projects/api`)
Изменения в API не требуются, так как:
- Масштабирование реализовано на стороне клиента
- API уже возвращает `quantity` для каждого ингредиента

**НО** если вы хотите улучшить API в будущем, можно добавить параметр `servings` в эндпоинт `/api/v1/recipes/{id}/composition`, который будет масштабировать `quantity` на стороне сервера.

### DTO (`RecipeIngredientDto`, `RecipeCompositionDto`)
Не требуют изменений, так как они уже содержат поле `quantity`.

### Mapper (`RecipeMapper.kt`)
Не требует изменений, так как `quantity` уже маппится из DTO в Domain модель.

## APK файл
```
app/build/outputs/apk/debug/app-debug.apk
Размер: ~52 МБ
```

## Дата изменений
2026-06-16
