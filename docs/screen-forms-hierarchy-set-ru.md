# Иерархия экранных форм

Набор диаграмм разбит на верхний уровень и поддиаграммы по разделам приложения.

Основная диаграмма:
- [screen-forms-main-ru.mmd](C:/Projects/NutritionApp/docs/screen-forms-main-ru.mmd)

Слайд для презентации:
- [screen-forms-hierarchy-slide-ru.mmd](C:/Projects/NutritionApp/docs/screen-forms-hierarchy-slide-ru.mmd)

Поддиаграммы:
- [screen-forms-home-ru.mmd](C:/Projects/NutritionApp/docs/screen-forms-home-ru.mmd)
- [screen-forms-diary-ru.mmd](C:/Projects/NutritionApp/docs/screen-forms-diary-ru.mmd)
- [screen-forms-products-ru.mmd](C:/Projects/NutritionApp/docs/screen-forms-products-ru.mmd)
- [screen-forms-recipes-ru.mmd](C:/Projects/NutritionApp/docs/screen-forms-recipes-ru.mmd)
- [screen-forms-profile-ru.mmd](C:/Projects/NutritionApp/docs/screen-forms-profile-ru.mmd)

Логика построения взята из:
- [AppNavigation.kt](C:/Projects/NutritionApp/app/src/main/java/com/abdulin/nutritionapp/presentation/navigation/AppNavigation.kt)
- [MainScreen.kt](C:/Projects/NutritionApp/app/src/main/java/com/abdulin/nutritionapp/presentation/navigation/MainScreen.kt)

Структура:
1. Авторизация — точка входа в приложение
2. Регистрация через онбординг → подтверждение почты → возврат к авторизации
3. Главный экран — 5 основных вкладок нижней навигации
4. Подэкраны — детальные формы, карточки, настройки

Рекомендация для диплома:
- на слайд выносить только `screen-forms-hierarchy-slide-ru.mmd`
- в приложение или в текст главы добавлять нужные поддиаграммы по разделам
