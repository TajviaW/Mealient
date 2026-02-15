package gq.kirmanak.mealient.ui.recipes.info

sealed interface RecipeInfoEvent {
    data object DismissShoppingListDialog : RecipeInfoEvent
    data class AddToShoppingList(val shoppingListId: String) : RecipeInfoEvent
}
