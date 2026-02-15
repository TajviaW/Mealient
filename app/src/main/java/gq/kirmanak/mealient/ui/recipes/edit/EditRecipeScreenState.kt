package gq.kirmanak.mealient.ui.recipes.edit

internal data class EditRecipeScreenState(
    val isLoading: Boolean = false,
    val recipeSlug: String = "",
    val recipeNameInput: String = "",
    val recipeDescriptionInput: String = "",
    val recipeYieldInput: String = "",
    val isPublicRecipe: Boolean = false,
    val disableComments: Boolean = false,
    val saveButtonEnabled: Boolean = false,
    val selectedParser: IngredientParser = IngredientParser.NLP,
    val ingredients: List<IngredientState> = emptyList(),
    val instructions: List<InstructionState> = emptyList(),
    val errorMessage: String? = null,
    val successMessage: String? = null,
)

internal enum class IngredientParser(val value: String, val displayName: String) {
    NLP("nlp", "NLP"),
    BRUTE("brute", "Brute"),
    OPENAI("openai", "OpenAI"),
}

internal data class IngredientState(
    val id: String,
    val note: String,
    val isParsing: Boolean = false,
)

internal data class InstructionState(
    val id: String,
    val text: String,
    val ingredientReferences: List<String> = emptyList(),
)
