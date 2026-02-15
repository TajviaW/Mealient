package gq.kirmanak.mealient.ui.recipes.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import gq.kirmanak.mealient.data.recipes.RecipeRepo
import gq.kirmanak.mealient.datasource.MealieDataSource
import gq.kirmanak.mealient.datasource.models.AddRecipeIngredient
import gq.kirmanak.mealient.datasource.models.AddRecipeInstruction
import gq.kirmanak.mealient.datasource.models.AddRecipeSettings
import gq.kirmanak.mealient.datasource.models.ParseIngredientRequest
import gq.kirmanak.mealient.datasource.models.UpdateRecipeRequest
import gq.kirmanak.mealient.datasource.runCatchingExceptCancel
import gq.kirmanak.mealient.logging.Logger
import gq.kirmanak.mealient.ui.navArgs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
internal class EditRecipeViewModel @Inject constructor(
    private val recipeRepo: RecipeRepo,
    private val mealieDataSource: MealieDataSource,
    private val logger: Logger,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val args = savedStateHandle.navArgs<EditRecipeScreenArgs>()
    private val _screenState = MutableStateFlow(EditRecipeScreenState())
    val screenState: StateFlow<EditRecipeScreenState> = _screenState.asStateFlow()

    init {
        loadRecipe()
    }

    private fun loadRecipe() {
        logger.v { "loadRecipe() called with recipeId = ${args.recipeId}" }
        viewModelScope.launch {
            _screenState.update { it.copy(isLoading = true) }

            val result = runCatchingExceptCancel {
                mealieDataSource.requestRecipeInfo(args.recipeId)
            }

            result.fold(
                onSuccess = { recipe ->
                    _screenState.update { state ->
                        state.copy(
                            isLoading = false,
                            recipeSlug = args.recipeId,
                            recipeNameInput = recipe.name,
                            recipeDescriptionInput = "",
                            recipeYieldInput = recipe.recipeYield,
                            isPublicRecipe = false,
                            disableComments = false,
                            ingredients = recipe.ingredients.map { ingredient ->
                                IngredientState(
                                    id = ingredient.referenceId,
                                    note = ingredient.note,
                                )
                            },
                            instructions = recipe.instructions.map { instruction ->
                                InstructionState(
                                    id = instruction.id,
                                    text = instruction.text,
                                    ingredientReferences = instruction.ingredientReferences.map { it.referenceId }
                                )
                            },
                            saveButtonEnabled = recipe.name.isNotBlank(),
                        )
                    }
                },
                onFailure = { error ->
                    logger.e(error) { "Failed to load recipe" }
                    _screenState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Failed to load recipe"
                        )
                    }
                }
            )
        }
    }

    fun onEvent(event: EditRecipeScreenEvent) {
        logger.v { "onEvent() called with: event = $event" }
        when (event) {
            is EditRecipeScreenEvent.RecipeNameInput -> {
                _screenState.update {
                    it.copy(
                        recipeNameInput = event.input,
                        saveButtonEnabled = event.input.isNotBlank(),
                    )
                }
            }

            is EditRecipeScreenEvent.RecipeDescriptionInput -> {
                _screenState.update {
                    it.copy(recipeDescriptionInput = event.input)
                }
            }

            is EditRecipeScreenEvent.RecipeYieldInput -> {
                _screenState.update {
                    it.copy(recipeYieldInput = event.input)
                }
            }

            is EditRecipeScreenEvent.PublicRecipeToggle -> {
                _screenState.update {
                    it.copy(isPublicRecipe = !it.isPublicRecipe)
                }
            }

            is EditRecipeScreenEvent.DisableCommentsToggle -> {
                _screenState.update {
                    it.copy(disableComments = !it.disableComments)
                }
            }

            is EditRecipeScreenEvent.ParserSelection -> {
                _screenState.update {
                    it.copy(selectedParser = event.parser)
                }
            }

            is EditRecipeScreenEvent.IngredientInput -> {
                _screenState.update { state ->
                    val updatedIngredients = state.ingredients.toMutableList()
                    updatedIngredients[event.ingredientIndex] = updatedIngredients[event.ingredientIndex].copy(
                        note = event.input
                    )
                    state.copy(ingredients = updatedIngredients)
                }
            }

            is EditRecipeScreenEvent.ParseIngredient -> {
                parseIngredient(event.ingredientIndex)
            }

            is EditRecipeScreenEvent.ParseAllIngredients -> {
                parseAllIngredients()
            }

            is EditRecipeScreenEvent.AddIngredientClick -> {
                _screenState.update { state ->
                    state.copy(
                        ingredients = state.ingredients + IngredientState(
                            id = UUID.randomUUID().toString(),
                            note = ""
                        )
                    )
                }
            }

            is EditRecipeScreenEvent.RemoveIngredientClick -> {
                _screenState.update { state ->
                    val updatedIngredients = state.ingredients.toMutableList()
                    updatedIngredients.removeAt(event.ingredientIndex)
                    state.copy(ingredients = updatedIngredients)
                }
            }

            is EditRecipeScreenEvent.InstructionInput -> {
                _screenState.update { state ->
                    val updatedInstructions = state.instructions.toMutableList()
                    updatedInstructions[event.instructionIndex] = updatedInstructions[event.instructionIndex].copy(
                        text = event.input
                    )
                    state.copy(instructions = updatedInstructions)
                }
            }

            is EditRecipeScreenEvent.AddInstructionClick -> {
                _screenState.update { state ->
                    state.copy(
                        instructions = state.instructions + InstructionState(
                            id = UUID.randomUUID().toString(),
                            text = ""
                        )
                    )
                }
            }

            is EditRecipeScreenEvent.RemoveInstructionClick -> {
                _screenState.update { state ->
                    val updatedInstructions = state.instructions.toMutableList()
                    updatedInstructions.removeAt(event.instructionIndex)
                    state.copy(instructions = updatedInstructions)
                }
            }

            is EditRecipeScreenEvent.SaveRecipeClick -> {
                saveRecipe()
            }

            is EditRecipeScreenEvent.SnackbarShown -> {
                _screenState.update {
                    it.copy(errorMessage = null, successMessage = null)
                }
            }
        }
    }

    private fun parseIngredient(index: Int) {
        logger.v { "parseIngredient() called with index = $index" }
        val state = _screenState.value
        if (index >= state.ingredients.size) return

        val ingredient = state.ingredients[index]
        if (ingredient.note.isBlank()) return

        viewModelScope.launch {
            // Mark ingredient as parsing
            _screenState.update { currentState ->
                val updatedIngredients = currentState.ingredients.toMutableList()
                updatedIngredients[index] = updatedIngredients[index].copy(isParsing = true)
                currentState.copy(ingredients = updatedIngredients)
            }

            val result = runCatchingExceptCancel {
                mealieDataSource.parseIngredient(
                    ParseIngredientRequest(
                        parser = _screenState.value.selectedParser.value,
                        ingredient = ingredient.note
                    )
                )
            }

            result.fold(
                onSuccess = { response ->
                    logger.d { "Successfully parsed ingredient: ${response.ingredient}" }
                    _screenState.update { currentState ->
                        val updatedIngredients = currentState.ingredients.toMutableList()
                        updatedIngredients[index] = updatedIngredients[index].copy(
                            note = response.ingredient.display,
                            isParsing = false
                        )
                        currentState.copy(ingredients = updatedIngredients)
                    }
                },
                onFailure = { error ->
                    logger.e(error) { "Failed to parse ingredient" }
                    _screenState.update { currentState ->
                        val updatedIngredients = currentState.ingredients.toMutableList()
                        updatedIngredients[index] = updatedIngredients[index].copy(isParsing = false)
                        currentState.copy(
                            ingredients = updatedIngredients,
                            errorMessage = "Failed to parse ingredient"
                        )
                    }
                }
            )
        }
    }

    private fun parseAllIngredients() {
        logger.v { "parseAllIngredients() called" }
        val state = _screenState.value
        state.ingredients.forEachIndexed { index, ingredient ->
            if (ingredient.note.isNotBlank()) {
                parseIngredient(index)
            }
        }
    }

    private fun saveRecipe() {
        logger.v { "saveRecipe() called" }
        val state = _screenState.value

        _screenState.update {
            it.copy(
                isLoading = true,
                saveButtonEnabled = false,
            )
        }

        viewModelScope.launch {
            val request = UpdateRecipeRequest(
                description = state.recipeDescriptionInput,
                recipeYield = state.recipeYieldInput,
                recipeIngredient = state.ingredients.map { ingredient ->
                    AddRecipeIngredient(
                        id = ingredient.id,
                        note = ingredient.note
                    )
                },
                recipeInstructions = state.instructions.map { instruction ->
                    AddRecipeInstruction(
                        id = instruction.id,
                        text = instruction.text,
                        ingredientReferences = instruction.ingredientReferences
                    )
                },
                settings = AddRecipeSettings(
                    disableComments = state.disableComments,
                    public = state.isPublicRecipe,
                )
            )

            val result = runCatchingExceptCancel {
                mealieDataSource.updateRecipe(state.recipeSlug, request)
            }

            result.fold(
                onSuccess = {
                    logger.d { "Successfully updated recipe" }
                    _screenState.update {
                        it.copy(
                            isLoading = false,
                            saveButtonEnabled = true,
                            successMessage = "Recipe saved successfully"
                        )
                    }
                },
                onFailure = { error ->
                    logger.e(error) { "Failed to update recipe" }
                    _screenState.update {
                        it.copy(
                            isLoading = false,
                            saveButtonEnabled = true,
                            errorMessage = "Failed to save recipe"
                        )
                    }
                }
            )
        }
    }
}

data class EditRecipeScreenArgs(
    val recipeId: String,
)
