package gq.kirmanak.mealient.meal_plans.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import gq.kirmanak.mealient.datasource.MealieDataSource
import gq.kirmanak.mealient.datasource.models.GetMealPlanResponse
import gq.kirmanak.mealient.datasource.models.GetRecipeSummaryResponse
import gq.kirmanak.mealient.logging.Logger
import gq.kirmanak.mealient.meal_plans.repo.MealPlansRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class MealPlansViewModel @Inject constructor(
    private val repo: MealPlansRepo,
    private val mealieDataSource: MealieDataSource,
    private val logger: Logger,
) : ViewModel() {

    private val _state = MutableStateFlow(MealPlansState())
    val state: StateFlow<MealPlansState> = _state.asStateFlow()

    init {
        loadMealPlans()
    }

    private fun loadMealPlans() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loadingState = MealPlansLoadingState.Loading)
            try {
                // Load meal plans for the current week
                val today = LocalDate.now()
                val startDate = today.minusDays(today.dayOfWeek.value.toLong() - 1) // Start of week (Monday)
                val endDate = startDate.plusDays(6) // End of week (Sunday)

                val mealPlans = repo.getMealPlansForDateRange(
                    startDate = startDate.toString(),
                    endDate = endDate.toString()
                )

                _state.value = _state.value.copy(
                    loadingState = MealPlansLoadingState.Success(mealPlans),
                    startDate = startDate.toString(),
                    endDate = endDate.toString()
                )
            } catch (e: Exception) {
                logger.e(e) { "Failed to load meal plans" }
                _state.value = _state.value.copy(
                    loadingState = MealPlansLoadingState.Error(e.message ?: "Unknown error")
                )
            }
        }
    }

    fun onRefresh() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isRefreshing = true)
            loadMealPlans()
            _state.value = _state.value.copy(isRefreshing = false)
        }
    }

    fun onDeleteMealPlan(id: String) {
        viewModelScope.launch {
            try {
                repo.deleteMealPlan(id)
                loadMealPlans() // Reload after delete
            } catch (e: Exception) {
                logger.e(e) { "Failed to delete meal plan" }
                _state.value = _state.value.copy(
                    loadingState = MealPlansLoadingState.Error("Failed to delete: ${e.message}")
                )
            }
        }
    }

    fun onAddMealPlanClick() {
        _state.value = _state.value.copy(
            showMealPlanDialog = true,
            editingMealPlanId = null
        )
        loadRecipes()
    }

    fun onMealPlanClick(mealPlan: GetMealPlanResponse) {
        _state.value = _state.value.copy(
            showMealPlanOptionsDialog = true,
            selectedMealPlanForOptions = mealPlan
        )
    }

    fun onDismissOptionsDialog() {
        _state.value = _state.value.copy(
            showMealPlanOptionsDialog = false,
            selectedMealPlanForOptions = null
        )
    }

    fun onEditFromOptions() {
        val mealPlan = _state.value.selectedMealPlanForOptions
        if (mealPlan != null) {
            _state.value = _state.value.copy(
                showMealPlanDialog = true,
                editingMealPlanId = mealPlan.id,
                showMealPlanOptionsDialog = false,
                selectedMealPlanForOptions = null
            )
            loadRecipes()
        }
    }

    fun onEditMealPlanClick(id: Int) {
        _state.value = _state.value.copy(
            showMealPlanDialog = true,
            editingMealPlanId = id
        )
        loadRecipes()
    }

    fun onDismissMealPlanDialog() {
        _state.value = _state.value.copy(
            showMealPlanDialog = false,
            editingMealPlanId = null
        )
    }

    private fun loadRecipes() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoadingRecipes = true)
            try {
                val recipes = mealieDataSource.requestRecipes(page = 1, perPage = 100)
                _state.value = _state.value.copy(
                    availableRecipes = recipes,
                    isLoadingRecipes = false
                )
            } catch (e: Exception) {
                logger.e(e) { "Failed to load recipes" }
                _state.value = _state.value.copy(isLoadingRecipes = false)
            }
        }
    }

    fun onSaveMealPlan(
        date: String,
        entryType: String,
        recipeId: String?,
        title: String?,
        text: String?
    ) {
        viewModelScope.launch {
            try {
                val editingId = _state.value.editingMealPlanId
                if (editingId != null) {
                    // Edit existing meal plan
                    repo.updateMealPlan(
                        id = editingId.toString(),
                        date = date,
                        entryType = entryType,
                        recipeId = recipeId,
                        title = title,
                        text = text
                    )
                } else {
                    // Create new meal plan
                    repo.createMealPlan(
                        date = date,
                        entryType = entryType,
                        recipeId = recipeId,
                        title = title,
                        text = text
                    )
                }
                onDismissMealPlanDialog()
                loadMealPlans() // Reload after save
            } catch (e: Exception) {
                logger.e(e) { "Failed to save meal plan" }
                _state.value = _state.value.copy(
                    loadingState = MealPlansLoadingState.Error("Failed to save: ${e.message}")
                )
            }
        }
    }
}
