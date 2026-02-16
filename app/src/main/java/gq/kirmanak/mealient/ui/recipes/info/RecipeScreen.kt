package gq.kirmanak.mealient.ui.recipes.info

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.navigate
import gq.kirmanak.mealient.R
import gq.kirmanak.mealient.ui.AppTheme
import gq.kirmanak.mealient.ui.Dimens
import gq.kirmanak.mealient.ui.components.BaseScreenState
import gq.kirmanak.mealient.ui.components.BaseScreenWithNavigation
import gq.kirmanak.mealient.ui.components.OpenDrawerIconButton
import gq.kirmanak.mealient.ui.destinations.EditRecipeScreenDestination
import gq.kirmanak.mealient.ui.preview.ColorSchemePreview

data class RecipeScreenArgs(
    val recipeId: String,
)

@Destination(
    navArgsDelegate = RecipeScreenArgs::class,
)
@Composable
internal fun RecipeScreen(
    navController: NavController,
    baseScreenState: BaseScreenState,
    viewModel: RecipeInfoViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    BaseScreenWithNavigation(
        baseScreenState = baseScreenState,
        drawerState = drawerState,
        topAppBar = {
            RecipeTopAppBar(
                title = state.title ?: "",
                drawerState = drawerState,
                onEditClick = {
                    navController.navigate(EditRecipeScreenDestination(state.summaryEntity?.imageId ?: ""))
                },
                onAddToShoppingListClick = viewModel::onAddToShoppingListClick
            )
        }
    ) { modifier ->
        RecipeScreen(
            modifier = modifier,
            state = state,
        )
    }
}

@Composable
private fun RecipeScreen(
    state: RecipeInfoUiState,
    modifier: Modifier = Modifier,
) {
    KeepScreenOn()

    Column(
        modifier = modifier
            .verticalScroll(
                state = rememberScrollState(),
            ),
        verticalArrangement = Arrangement.spacedBy(Dimens.Small, Alignment.Top),
    ) {
        HeaderSection(
            imageUrl = state.imageUrl,
            title = state.title,
            description = state.description,
        )

        if (state.showIngredients) {
            IngredientsSection(
                ingredients = state.recipeIngredients,
            )
        }

        if (state.showInstructions) {
            InstructionsSection(
                instructions = state.recipeInstructions,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecipeTopAppBar(
    title: String,
    drawerState: androidx.compose.material3.DrawerState,
    onEditClick: () -> Unit,
    onAddToShoppingListClick: () -> Unit,
) {
    TopAppBar(
        title = { Text(text = title) },
        navigationIcon = {
            OpenDrawerIconButton(drawerState = drawerState)
        },
        actions = {
            IconButton(onClick = onAddToShoppingListClick) {
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = stringResource(R.string.add_to_shopping_list),
                )
            }
            IconButton(onClick = onEditClick) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = stringResource(R.string.edit_recipe),
                )
            }
        }
    )
}

@ColorSchemePreview
@Composable
private fun RecipeScreenPreview() {
    AppTheme {
        RecipeScreen(
            state = RecipeInfoUiState(
                showIngredients = true,
                showInstructions = true,
                summaryEntity = SUMMARY_ENTITY,
                recipeIngredients = INGREDIENTS,
                recipeInstructions = INSTRUCTIONS,
                title = "Recipe title",
                description = "Recipe description",
            )
        )
    }
}


@Composable
private fun ShoppingListSelectorDialog(
    shoppingLists: List<gq.kirmanak.mealient.datasource.models.GetShoppingListsSummaryResponse>,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onSelectList: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.select_shopping_list)) },
        text = {
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.select_shopping_list_message),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    shoppingLists.forEach { list ->
                        TextButton(
                            onClick = { onSelectList(list.id) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = list.name ?: "Unnamed List")
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.activity_main_logout_confirmation_negative))
            }
        }
    )
}
