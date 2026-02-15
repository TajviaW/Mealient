package gq.kirmanak.mealient.ui.recipes.info

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import gq.kirmanak.mealient.data.baseurl.ServerInfoRepo
import gq.kirmanak.mealient.data.recipes.RecipeRepo
import gq.kirmanak.mealient.data.recipes.impl.RecipeImageUrlProvider
import gq.kirmanak.mealient.database.recipe.entity.RecipeIngredientEntity
import gq.kirmanak.mealient.database.recipe.entity.RecipeInstructionEntity
import gq.kirmanak.mealient.database.recipe.entity.RecipeWithSummaryAndIngredientsAndInstructions
import gq.kirmanak.mealient.datasource.MealieDataSource
import gq.kirmanak.mealient.datasource.models.CreateShoppingListItemRequest
import gq.kirmanak.mealient.logging.Logger
import gq.kirmanak.mealient.ui.navArgs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class RecipeInfoViewModel @Inject constructor(
    private val recipeRepo: RecipeRepo,
    private val serverInfoRepo: ServerInfoRepo,
    private val mealieDataSource: MealieDataSource,
    private val logger: Logger,
    private val recipeImageUrlProvider: RecipeImageUrlProvider,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val args = savedStateHandle.navArgs<RecipeScreenArgs>()

    private val _uiState = MutableStateFlow(RecipeInfoUiState())
    val uiState: StateFlow<RecipeInfoUiState> = _uiState.asStateFlow()

    init {
        loadRecipeInfo()
    }

    private fun loadRecipeInfo() {
        viewModelScope.launch {
            logger.v { "Initializing UI state with args = $args" }
            val recipeInfo = recipeRepo.loadRecipeInfo(args.recipeId)
            logger.v { "Loaded recipe info = $recipeInfo" }
            val slug = recipeInfo?.recipeSummaryEntity?.imageId
            val imageUrl = slug?.let { recipeImageUrlProvider.generateImageUrl(slug) }
            val serverUrl = serverInfoRepo.getUrl()
            val state = recipeInfo?.let { entity ->
                RecipeInfoUiState(
                    showIngredients = entity.recipeIngredients.isNotEmpty(),
                    showInstructions = entity.recipeInstructions.isNotEmpty(),
                    summaryEntity = entity.recipeSummaryEntity,
                    recipeIngredients = entity.recipeIngredients,
                    recipeInstructions = associateInstructionsToIngredients(entity),
                    title = entity.recipeSummaryEntity.name,
                    description = entity.recipeSummaryEntity.description,
                    imageUrl = imageUrl,
                    recipeSlug = slug,
                    serverUrl = serverUrl,
                )
            } ?: RecipeInfoUiState()
            _uiState.value = state
        }
    }

    fun onAddToShoppingListClick() {
        addIngredientsToShoppingList()
    }

    private fun addIngredientsToShoppingList() {
        viewModelScope.launch {
            try {
                // Get the first shopping list
                val lists = mealieDataSource.getShoppingLists(page = 1, perPage = 1).items
                if (lists.isEmpty()) {
                    logger.e { "No shopping lists found" }
                    return@launch
                }

                val shoppingListId = lists.first().id
                val ingredients = _uiState.value.recipeIngredients

                ingredients.forEachIndexed { index, ingredient ->
                    val request = CreateShoppingListItemRequest(
                        shoppingListId = shoppingListId,
                        checked = false,
                        position = index,
                        isFood = ingredient.isFood,
                        note = ingredient.display.ifBlank { ingredient.note },
                        quantity = ingredient.quantity ?: 1.0,
                        foodId = null,
                        unitId = null
                    )
                    mealieDataSource.addShoppingListItem(request)
                }

                logger.d { "Successfully added ${ingredients.size} ingredients to shopping list" }
            } catch (e: Exception) {
                logger.e(e) { "Failed to add ingredients to shopping list" }
            }
        }
    }

    fun onEvent(event: RecipeInfoEvent) {
        when (event) {
            is RecipeInfoEvent.DismissShoppingListDialog -> {
                _uiState.update { it.copy(showShoppingListDialog = false) }
            }
            is RecipeInfoEvent.AddToShoppingList -> {
                // No longer used, keeping for compatibility
            }
        }
    }

}

private fun associateInstructionsToIngredients(
    recipe: RecipeWithSummaryAndIngredientsAndInstructions,
): Map<RecipeInstructionEntity, List<RecipeIngredientEntity>> {
    return recipe.recipeInstructions.associateWith { instruction ->
        recipe.recipeIngredientToInstructionEntity
            .filter { it.instructionId == instruction.id }
            .flatMap { mapping ->
                recipe.recipeIngredients.filter { ingredient ->
                    ingredient.id == mapping.ingredientId
                }
            }
    }
}
