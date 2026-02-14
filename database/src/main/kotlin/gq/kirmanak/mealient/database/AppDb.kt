package gq.kirmanak.mealient.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import gq.kirmanak.mealient.database.mealplan.MealPlanDao
import gq.kirmanak.mealient.database.mealplan.entity.MealPlanEntity
import gq.kirmanak.mealient.database.recipe.RecipeDao
import gq.kirmanak.mealient.database.recipe.entity.RecipeEntity
import gq.kirmanak.mealient.database.recipe.entity.RecipeIngredientEntity
import gq.kirmanak.mealient.database.recipe.entity.RecipeIngredientToInstructionEntity
import gq.kirmanak.mealient.database.recipe.entity.RecipeInstructionEntity
import gq.kirmanak.mealient.database.recipe.entity.RecipeSummaryEntity
import gq.kirmanak.mealient.database.shopping.ShoppingListDao
import gq.kirmanak.mealient.database.shopping.entity.ShoppingListEntity
import gq.kirmanak.mealient.database.shopping.entity.ShoppingListItemEntity

@Database(
    version = 14,
    entities = [
        RecipeSummaryEntity::class,
        RecipeEntity::class,
        RecipeIngredientEntity::class,
        RecipeInstructionEntity::class,
        RecipeIngredientToInstructionEntity::class,
        ShoppingListEntity::class,
        ShoppingListItemEntity::class,
        MealPlanEntity::class,
    ]
)
@TypeConverters(RoomTypeConverters::class)
internal abstract class AppDb : RoomDatabase() {

    abstract fun recipeDao(): RecipeDao
    abstract fun shoppingListDao(): ShoppingListDao
    abstract fun mealPlanDao(): MealPlanDao
}