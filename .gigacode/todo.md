# Todo List: Улучшение UI рецептов

## Этап 1: Расчёт порций (ViewModel) - ✅
- [x] Добавить `suggestedServings: Int?` в `RecipeDetailUiState`
- [x] Добавить `servingsRange: IntRange` в `RecipeDetailUiState`
- [x] Добавить метод `calculateSuggestedServings()` в `RecipeDetailViewModel`
- [x] Добавить метод `calculateServingsRange()` в `RecipeDetailViewModel`
- [x] Вызвать расчёт при загрузке рецепта

## Этап 2: UI обновление (Screen) - ✅
- [x] Обновить `NutritionBreakdownSection` с новым UI
- [x] Добавить отображение автоматического расчёта
- [x] Использовать `servingsRange` вместо `1..maxPreviewServings`
- [x] Добавить кнопку раскрытия/сворачивания для секций

## Этап 3: Сворачиваемые секции - ✅
- [x] Добавить `expanded` состояния для секций
- [x] Обернуть контент в условные блоки
- [x] Добавить кнопки раскрытия/сворачивания

## Этап 4: Добавление строк (strings.xml) - ✅
- [x] Добавить новые строки для автоматического расчёта порций

## Этап 5: Сборка и тестирование - ✅
- [x] Собрать APK
- [ ] Установить на устройство
- [ ] Протестировать расчёт порций
- [ ] Протестировать сворачивание
