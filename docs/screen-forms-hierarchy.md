# Screen Forms Hierarchy

Diagram source: [screen-forms-hierarchy.mmd](C:/Projects/NutritionApp/docs/screen-forms-hierarchy.mmd)

Meaning:
- `auth` — authentication and registration screens
- `core` — main application screen
- `tab` — main bottom-navigation tabs
- `support` — nested forms, pickers and detail screens

Built from the actual navigation in:
- [AppNavigation.kt](C:/Projects/NutritionApp/app/src/main/java/com/abdulin/nutritionapp/presentation/navigation/AppNavigation.kt)
- [MainScreen.kt](C:/Projects/NutritionApp/app/src/main/java/com/abdulin/nutritionapp/presentation/navigation/MainScreen.kt)

Structure:
1. LoginScreen — entry point
2. Registration via OnboardingScreen → email verification → back to login
3. MainScreen — 5 main bottom navigation tabs
4. Sub-screens — detail forms, cards, settings

Suggested export flow:
1. Open the `.mmd` file in any Mermaid editor.
2. Export to `PNG` or `SVG`.
3. Use the exported image in the thesis or slides.
