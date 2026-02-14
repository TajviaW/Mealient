package gq.kirmanak.mealient.database.shopping.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shopping_lists")
data class ShoppingListEntity(
    @PrimaryKey
    @ColumnInfo(name = "list_id")
    val id: String,

    @ColumnInfo(name = "list_name")
    val name: String,

    @ColumnInfo(name = "list_created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "list_updated_at")
    val updatedAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "list_is_synced")
    val isSynced: Boolean = true,
)
