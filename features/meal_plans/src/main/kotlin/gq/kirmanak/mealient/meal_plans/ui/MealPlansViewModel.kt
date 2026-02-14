package gq.kirmanak.mealient.meal_plans.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
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
        loadMealPlans()
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
}
