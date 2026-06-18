# Import Into dbdiagram.io

1. Open `https://dbdiagram.io/`.
2. Create a new diagram.
3. Choose `Import` or replace the editor contents.
4. Paste the contents of [nutrition-db.dbml](C:/Projects/NutritionApp/docs/nutrition-db.dbml).
5. Save the diagram and, if needed, rearrange blocks visually.

Source files used to build the schema:
- `C:\Projects\api\src\main\java\com\rest\api\entity\`
- existing диплом figure: [figure-2-1-er-model.svg](C:/Projects/NutritionApp/docs/figure-2-1-er-model.svg)

Notes:
- The DBML reflects the current backend model, including recent meal-plan and recommendation tables.
- Some column names were inferred from Spring/JPA snake_case naming for fields without explicit `@Column`.
