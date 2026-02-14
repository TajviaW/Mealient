package gq.kirmanak.mealient.database.mealplan.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "meal_plans")
data class MealPlanEntity(
    @PrimaryKey
    @ColumnInfo(name = "plan_id")
    val id: String,

    @ColumnInfo(name = "plan_date")
    val date: String, // ISO 8601 date format: "2024-02-14"

    @ColumnInfo(name = "plan_entry_type")
    val entryType: String, // "breakfast", "lunch", "dinner", "snack"

    @ColumnInfo(name = "plan_title")
    val title: String? = null,

    @ColumnInfo(name = "plan_text")
    val text: String? = null,

    @ColumnInfo(name = "plan_recipe_id")
    val recipeId: String? = null,

    @ColumnInfo(name = "plan_recipe_name")
    val recipeName: String? = null,

    @ColumnInfo(name = "plan_recipe_slug")
    val recipeSlug: String? = null,

    @ColumnInfo(name = "plan_created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "plan_updated_at")
    val updatedAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "plan_is_synced")
    val isSynced: Boolean = true,
)
