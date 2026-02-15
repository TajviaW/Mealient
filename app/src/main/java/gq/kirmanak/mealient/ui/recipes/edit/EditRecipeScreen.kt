package gq.kirmanak.mealient.ui.recipes.edit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.hilt.navigation.compose.hiltViewModel
import com.ramcosta.composedestinations.annotation.Destination
import gq.kirmanak.mealient.R
import gq.kirmanak.mealient.ui.Dimens
import gq.kirmanak.mealient.ui.components.BaseScreenState
import gq.kirmanak.mealient.ui.components.BaseScreenWithNavigation
import gq.kirmanak.mealient.ui.components.TopProgressIndicator

@Destination(
    navArgsDelegate = EditRecipeScreenArgs::class,
)
@Composable
internal fun EditRecipeScreen(
    baseScreenState: BaseScreenState,
    viewModel: EditRecipeViewModel = hiltViewModel()
) {
    val screenState by viewModel.screenState.collectAsState()

    EditRecipeScreen(
        baseScreenState = baseScreenState,
        state = screenState,
        onEvent = viewModel::onEvent,
    )
}

@Composable
internal fun EditRecipeScreen(
    baseScreenState: BaseScreenState,
    state: EditRecipeScreenState,
    onEvent: (EditRecipeScreenEvent) -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    state.errorMessage?.let { message ->
        LaunchedEffect(message) {
            snackbarHostState.showSnackbar(message)
            onEvent(EditRecipeScreenEvent.SnackbarShown)
        }
    }

    state.successMessage?.let { message ->
        LaunchedEffect(message) {
            snackbarHostState.showSnackbar(message)
            onEvent(EditRecipeScreenEvent.SnackbarShown)
        }
    }

    BaseScreenWithNavigation(
        baseScreenState = baseScreenState,
        snackbarHostState = snackbarHostState,
    ) { modifier ->
        TopProgressIndicator(
            modifier = modifier,
            isLoading = state.isLoading,
        ) {
            EditRecipeScreenContent(
                state = state,
                onEvent = onEvent,
            )
        }
    }
}

@Composable
private fun EditRecipeScreenContent(
    state: EditRecipeScreenState,
    onEvent: (EditRecipeScreenEvent) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(Dimens.Medium),
        verticalArrangement = Arrangement.spacedBy(Dimens.Medium),
        horizontalAlignment = Alignment.End,
    ) {
        item {
            EditRecipeInputField(
                input = state.recipeNameInput,
                label = stringResource(id = R.string.fragment_add_recipe_recipe_name),
                isLast = false,
                onValueChange = { onEvent(EditRecipeScreenEvent.RecipeNameInput(it)) },
            )
        }

        item {
            EditRecipeInputField(
                input = state.recipeDescriptionInput,
                label = stringResource(id = R.string.fragment_add_recipe_recipe_description),
                isLast = false,
                onValueChange = { onEvent(EditRecipeScreenEvent.RecipeDescriptionInput(it)) },
            )
        }

        item {
            EditRecipeInputField(
                input = state.recipeYieldInput,
                label = stringResource(id = R.string.fragment_add_recipe_recipe_yield),
                isLast = state.ingredients.isEmpty() && state.instructions.isEmpty(),
                onValueChange = { onEvent(EditRecipeScreenEvent.RecipeYieldInput(it)) },
            )
        }

        item {
            Text(text = "Parser")
        }

        item {
            ParserSelector(
                selectedParser = state.selectedParser,
                onParserSelected = { onEvent(EditRecipeScreenEvent.ParserSelection(it)) },
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = "Ingredients")
                Button(
                    onClick = {
                        onEvent(EditRecipeScreenEvent.ParseAllIngredients)
                    },
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoFixHigh,
                        contentDescription = null,
                    )
                    Text(text = "Parse All")
                }
            }
        }

        itemsIndexed(state.ingredients) { index, ingredient ->
            EditRecipeIngredientField(
                ingredient = ingredient,
                label = stringResource(id = R.string.fragment_add_recipe_ingredient_hint),
                isLast = state.ingredients.lastIndex == index && state.instructions.isEmpty(),
                onValueChange = {
                    onEvent(EditRecipeScreenEvent.IngredientInput(index, it))
                },
                onRemoveClick = {
                    onEvent(EditRecipeScreenEvent.RemoveIngredientClick(index))
                },
                onParseClick = {
                    onEvent(EditRecipeScreenEvent.ParseIngredient(index))
                },
            )
        }

        item {
            Button(
                onClick = {
                    onEvent(EditRecipeScreenEvent.AddIngredientClick)
                },
            ) {
                Text(
                    text = stringResource(id = R.string.fragment_add_recipe_new_ingredient),
                )
            }
        }

        itemsIndexed(state.instructions) { index, instruction ->
            EditRecipeInputField(
                input = instruction.text,
                label = stringResource(id = R.string.fragment_add_recipe_instruction_hint),
                isLast = state.instructions.lastIndex == index,
                onValueChange = {
                    onEvent(
                        EditRecipeScreenEvent.InstructionInput(index, it)
                    )
                },
                onRemoveClick = {
                    onEvent(EditRecipeScreenEvent.RemoveInstructionClick(index))
                },
            )
        }

        item {
            Button(
                onClick = {
                    onEvent(EditRecipeScreenEvent.AddInstructionClick)
                },
            ) {
                Text(
                    text = stringResource(id = R.string.fragment_add_recipe_new_instruction),
                )
            }
        }

        item {
            EditRecipeSwitch(
                name = R.string.fragment_add_recipe_public_recipe,
                checked = state.isPublicRecipe,
                onCheckedChange = { onEvent(EditRecipeScreenEvent.PublicRecipeToggle) },
            )
        }

        item {
            EditRecipeSwitch(
                name = R.string.fragment_add_recipe_disable_comments,
                checked = state.disableComments,
                onCheckedChange = { onEvent(EditRecipeScreenEvent.DisableCommentsToggle) },
            )
        }

        item {
            Button(
                enabled = state.saveButtonEnabled,
                onClick = {
                    onEvent(EditRecipeScreenEvent.SaveRecipeClick)
                },
            ) {
                Text(
                    text = stringResource(id = R.string.fragment_add_recipe_save_button),
                )
            }
        }
    }
}

@Composable
private fun EditRecipeSwitch(
    name: Int,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Dimens.Medium),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(id = name),
        )

        Spacer(modifier = Modifier.weight(1f))

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
    }
}

@Composable
private fun EditRecipeInputField(
    input: String,
    label: String,
    isLast: Boolean,
    onValueChange: (String) -> Unit,
    onRemoveClick: (() -> Unit)? = null,
) {
    OutlinedTextField(
        modifier = Modifier
            .fillMaxWidth(),
        value = input,
        onValueChange = onValueChange,
        label = {
            Text(text = label)
        },
        trailingIcon = {
            if (onRemoveClick != null) {
                IconButton(onClick = onRemoveClick) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                    )
                }
            }
        },
        keyboardOptions = KeyboardOptions(
            imeAction = if (isLast) ImeAction.Done else ImeAction.Next,
        )
    )
}

@Composable
private fun EditRecipeIngredientField(
    ingredient: IngredientState,
    label: String,
    isLast: Boolean,
    onValueChange: (String) -> Unit,
    onRemoveClick: () -> Unit,
    onParseClick: () -> Unit,
) {
    OutlinedTextField(
        modifier = Modifier
            .fillMaxWidth(),
        value = ingredient.note,
        onValueChange = onValueChange,
        label = {
            Text(text = label)
        },
        trailingIcon = {
            Row {
                if (ingredient.isParsing) {
                    CircularProgressIndicator()
                } else {
                    IconButton(onClick = onParseClick) {
                        Icon(
                            imageVector = Icons.Default.AutoFixHigh,
                            contentDescription = "Parse ingredient",
                        )
                    }
                }
                IconButton(onClick = onRemoveClick) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                    )
                }
            }
        },
        keyboardOptions = KeyboardOptions(
            imeAction = if (isLast) ImeAction.Done else ImeAction.Next,
        )
    )
}

@Composable
private fun ParserSelector(
    selectedParser: IngredientParser,
    onParserSelected: (IngredientParser) -> Unit,
) {
    val parsers = IngredientParser.entries
    SingleChoiceSegmentedButtonRow(
        modifier = Modifier.fillMaxWidth()
    ) {
        parsers.forEachIndexed { index, parser ->
            SegmentedButton(
                selected = selectedParser == parser,
                onClick = { onParserSelected(parser) },
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = parsers.size
                ),
            ) {
                Text(text = parser.displayName)
            }
        }
    }
}
