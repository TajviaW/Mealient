package gq.kirmanak.mealient.meal_plans.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.navigate
import gq.kirmanak.mealient.datasource.models.GetMealPlanResponse
import gq.kirmanak.mealient.meal_plans.R
import gq.kirmanak.mealient.ui.components.BaseScreenState
import gq.kirmanak.mealient.ui.components.BaseScreenWithNavigation

@Destination
@Composable
fun MealPlansScreen(
    navController: NavController,
    baseScreenState: BaseScreenState,
    viewModel: MealPlansViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    BaseScreenWithNavigation(
        baseScreenState = baseScreenState,
    ) { modifier ->
        Box(modifier = modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
            when (val loadingState = state.loadingState) {
                is MealPlansLoadingState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is MealPlansLoadingState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = loadingState.message,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                is MealPlansLoadingState.Success -> {
                    MealPlansContent(
                        modifier = Modifier,
                        mealPlans = loadingState.mealPlans,
                        startDate = state.startDate,
                        endDate = state.endDate,
                        isRefreshing = state.isRefreshing,
                        onRefresh = viewModel::onRefresh,
                        onMealPlanClick = viewModel::onMealPlanClick,
                        onDeleteMealPlan = viewModel::onDeleteMealPlan
                    )
                }
            }
            }

            FloatingActionButton(
                onClick = { viewModel.onAddMealPlanClick() },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.meal_plans_add_meal_plan)
                )
            }
        }

        val selectedMealPlan = state.selectedMealPlanForOptions
        if (state.showMealPlanOptionsDialog && selectedMealPlan != null) {
            MealPlanOptionsDialog(
                mealPlan = selectedMealPlan,
                onDismiss = viewModel::onDismissOptionsDialog,
                onEdit = viewModel::onEditFromOptions,
                onViewRecipe = {
                    selectedMealPlan.recipe?.slug?.let { slug ->
                        navController.navigate("recipe_screen/$slug")
                    }
                    viewModel.onDismissOptionsDialog()
                }
            )
        }

        if (state.showMealPlanDialog) {
            val editingMealPlan = if (state.editingMealPlanId != null) {
                (state.loadingState as? MealPlansLoadingState.Success)?.mealPlans
                    ?.find { it.id == state.editingMealPlanId }
            } else null

            MealPlanDialog(
                mealPlan = editingMealPlan,
                availableRecipes = state.availableRecipes,
                isLoadingRecipes = state.isLoadingRecipes,
                onDismiss = viewModel::onDismissMealPlanDialog,
                onSave = viewModel::onSaveMealPlan
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MealPlansContent(
    modifier: Modifier = Modifier,
    mealPlans: List<GetMealPlanResponse>,
    startDate: String,
    endDate: String,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onMealPlanClick: (GetMealPlanResponse) -> Unit,
    onDeleteMealPlan: (String) -> Unit,
) {
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier.fillMaxSize(),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header showing date range
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                    Icons.Default.CalendarToday,
                    contentDescription = stringResource(R.string.meal_plans_content_desc_calendar_icon)
                )
                    Text(
                        text = stringResource(R.string.meal_plans_date_range_format, startDate, endDate),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            if (mealPlans.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.meal_plans_screen_empty),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = stringResource(R.string.meal_plans_screen_empty_subtitle),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(mealPlans) { mealPlan ->
                        MealPlanCard(
                            mealPlan = mealPlan,
                            onClick = { onMealPlanClick(mealPlan) },
                            onDelete = { onDeleteMealPlan(mealPlan.id.toString()) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MealPlanCard(
    mealPlan: GetMealPlanResponse,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp)
            ) {
                Text(
                    text = mealPlan.date,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = mealPlan.entryType.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.titleMedium
                )
                mealPlan.recipe?.let { recipe ->
                    Text(
                        text = recipe.name,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                mealPlan.title?.let { title ->
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                mealPlan.text?.let { text ->
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            androidx.compose.material3.IconButton(
                onClick = onDelete,
                modifier = Modifier.padding(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.meal_plans_delete_meal_plan),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MealPlanDialog(
    mealPlan: GetMealPlanResponse?,
    availableRecipes: List<gq.kirmanak.mealient.datasource.models.GetRecipeSummaryResponse>,
    isLoadingRecipes: Boolean,
    onDismiss: () -> Unit,
    onSave: (date: String, entryType: String, recipeId: String?, title: String?, text: String?) -> Unit
) {
    var date by remember { mutableStateOf(mealPlan?.date ?: java.time.LocalDate.now().toString()) }
    var entryType by remember { mutableStateOf(mealPlan?.entryType ?: "dinner") }
    var selectedRecipe by remember { mutableStateOf(mealPlan?.recipe) }
    var title by remember { mutableStateOf(mealPlan?.title ?: "") }
    var notes by remember { mutableStateOf(mealPlan?.text ?: "") }
    var entryTypeExpanded by remember { mutableStateOf(false) }
    var recipeExpanded by remember { mutableStateOf(false) }

    val entryTypes = listOf("breakfast", "lunch", "dinner", "snack")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (mealPlan != null) {
                    stringResource(R.string.meal_plans_dialog_edit_title)
                } else {
                    stringResource(R.string.meal_plans_dialog_create_title)
                }
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text(stringResource(R.string.meal_plans_field_date)) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("YYYY-MM-DD") }
                )

                ExposedDropdownMenuBox(
                    expanded = entryTypeExpanded,
                    onExpandedChange = { entryTypeExpanded = !entryTypeExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = entryType.replaceFirstChar { it.uppercase() },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.meal_plans_field_meal_type)) },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = entryTypeExpanded)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = entryTypeExpanded,
                        onDismissRequest = { entryTypeExpanded = false }
                    ) {
                        entryTypes.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.replaceFirstChar { it.uppercase() }) },
                                onClick = {
                                    entryType = type
                                    entryTypeExpanded = false
                                }
                            )
                        }
                    }
                }

                ExposedDropdownMenuBox(
                    expanded = recipeExpanded,
                    onExpandedChange = { recipeExpanded = !recipeExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedRecipe?.name ?: stringResource(R.string.meal_plans_hint_select_recipe),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.meal_plans_field_recipe)) },
                        trailingIcon = {
                            Row {
                                if (selectedRecipe != null) {
                                    androidx.compose.material3.IconButton(
                                        onClick = { selectedRecipe = null }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Clear recipe"
                                        )
                                    }
                                }
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = recipeExpanded)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        enabled = !isLoadingRecipes
                    )

                    ExposedDropdownMenu(
                        expanded = recipeExpanded,
                        onDismissRequest = { recipeExpanded = false }
                    ) {
                        if (isLoadingRecipes) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        } else {
                            availableRecipes.forEach { recipe ->
                                DropdownMenuItem(
                                    text = { Text(recipe.name) },
                                    onClick = {
                                        selectedRecipe = recipe
                                        recipeExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(stringResource(R.string.meal_plans_field_title)) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(stringResource(R.string.meal_plans_hint_title)) }
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(stringResource(R.string.meal_plans_field_notes)) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(stringResource(R.string.meal_plans_hint_notes)) },
                    minLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        date,
                        entryType,
                        selectedRecipe?.slug,
                        title.ifBlank { null },
                        notes.ifBlank { null }
                    )
                }
            ) {
                Text(
                    if (mealPlan != null) {
                        stringResource(R.string.meal_plans_dialog_button_save)
                    } else {
                        stringResource(R.string.meal_plans_dialog_button_create)
                    }
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.meal_plans_dialog_button_cancel))
            }
        }
    )
}

@Composable
private fun MealPlanOptionsDialog(
    mealPlan: GetMealPlanResponse,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onViewRecipe: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = stringResource(R.string.meal_plans_options_title))
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(
                    onClick = {
                        onEdit()
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Text(
                            text = stringResource(R.string.meal_plans_option_edit),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                if (mealPlan.recipe != null) {
                    TextButton(
                        onClick = {
                            onViewRecipe()
                            onDismiss()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Text(
                                text = stringResource(R.string.meal_plans_option_view_recipe),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.meal_plans_dialog_button_cancel))
            }
        }
    )
}
