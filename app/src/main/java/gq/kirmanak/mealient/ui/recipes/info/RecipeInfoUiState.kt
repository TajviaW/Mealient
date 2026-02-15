package gq.kirmanak.mealient.ui.recipes.info

import gq.kirmanak.mealient.database.recipe.entity.RecipeIngredientEntity
import gq.kirmanak.mealient.database.recipe.entity.RecipeInstructionEntity
import gq.kirmanak.mealient.database.recipe.entity.RecipeSummaryEntity
import gq.kirmanak.mealient.datasource.models.GetShoppingListsSummaryResponse

data class RecipeInfoUiState(
    val showIngredients: Boolean = false,
    val showInstructions: Boolean = false,
    val summaryEntity: RecipeSummaryEntity? = null,
    val recipeIngredients: List<RecipeIngredientEntity> = emptyList(),
    val recipeInstructions: Map<RecipeInstructionEntity, List<RecipeIngredientEntity>> = emptyMap(),
    val title: String? = null,
    val description: String? = null,
    val disableAmounts: Boolean = true,
    val imageUrl: String? = null,
    val recipeSlug: String? = null,
    val serverUrl: String? = null,
    val showShoppingListDialog: Boolean = false,
    val availableShoppingLists: List<GetShoppingListsSummaryResponse> = emptyList(),
    val isLoadingShoppingLists: Boolean = false,
)
