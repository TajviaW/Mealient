package gq.kirmanak.mealient.ui.recipes.edit

internal sealed interface EditRecipeScreenEvent {
    data class RecipeNameInput(val input: String) : EditRecipeScreenEvent
    data class RecipeDescriptionInput(val input: String) : EditRecipeScreenEvent
    data class RecipeYieldInput(val input: String) : EditRecipeScreenEvent
    data object PublicRecipeToggle : EditRecipeScreenEvent
    data object DisableCommentsToggle : EditRecipeScreenEvent
    data class ParserSelection(val parser: IngredientParser) : EditRecipeScreenEvent
    data class IngredientInput(val ingredientIndex: Int, val input: String) : EditRecipeScreenEvent
    data class ParseIngredient(val ingredientIndex: Int) : EditRecipeScreenEvent
    data object ParseAllIngredients : EditRecipeScreenEvent
    data class RemoveIngredientClick(val ingredientIndex: Int) : EditRecipeScreenEvent
    data object AddIngredientClick : EditRecipeScreenEvent
    data class InstructionInput(val instructionIndex: Int, val input: String) : EditRecipeScreenEvent
    data class RemoveInstructionClick(val instructionIndex: Int) : EditRecipeScreenEvent
    data object AddInstructionClick : EditRecipeScreenEvent
    data object SaveRecipeClick : EditRecipeScreenEvent
    data object SnackbarShown : EditRecipeScreenEvent
}
