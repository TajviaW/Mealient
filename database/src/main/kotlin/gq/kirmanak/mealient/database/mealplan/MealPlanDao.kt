package gq.kirmanak.mealient.database.mealplan

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import gq.kirmanak.mealient.database.mealplan.entity.MealPlanEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MealPlanDao {

    @Query("SELECT * FROM meal_plans WHERE plan_date BETWEEN :startDate AND :endDate ORDER BY plan_date ASC, plan_entry_type ASC")
    fun observeMealPlans(startDate: String, endDate: String): Flow<List<MealPlanEntity>>

    @Query("SELECT * FROM meal_plans WHERE plan_date BETWEEN :startDate AND :endDate ORDER BY plan_date ASC, plan_entry_type ASC")
    suspend fun getMealPlans(startDate: String, endDate: String): List<MealPlanEntity>

    @Query("SELECT * FROM meal_plans WHERE plan_id = :planId")
    suspend fun getMealPlan(planId: String): MealPlanEntity?

    @Query("SELECT * FROM meal_plans WHERE plan_id = :planId")
    fun observeMealPlan(planId: String): Flow<MealPlanEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMealPlan(plan: MealPlanEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMealPlans(plans: List<MealPlanEntity>)

    @Update
    suspend fun updateMealPlan(plan: MealPlanEntity)

    @Delete
    suspend fun deleteMealPlan(plan: MealPlanEntity)

    @Query("DELETE FROM meal_plans WHERE plan_id = :planId")
    suspend fun deleteMealPlanById(planId: String)

    @Query("SELECT * FROM meal_plans WHERE plan_is_synced = 0")
    suspend fun getUnsyncedMealPlans(): List<MealPlanEntity>

    @Query("DELETE FROM meal_plans")
    suspend fun deleteAllMealPlans()

    @Query("DELETE FROM meal_plans WHERE plan_date < :beforeDate")
    suspend fun deleteOldMealPlans(beforeDate: String)
}
