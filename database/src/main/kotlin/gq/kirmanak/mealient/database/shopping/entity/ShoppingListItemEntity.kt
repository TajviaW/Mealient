package gq.kirmanak.mealient.database.shopping.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "shopping_list_items",
    foreignKeys = [
        ForeignKey(
            entity = ShoppingListEntity::class,
            parentColumns = ["list_id"],
            childColumns = ["item_list_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("item_list_id")]
)
data class ShoppingListItemEntity(
    @PrimaryKey
    @ColumnInfo(name = "item_id")
    val id: String,

    @ColumnInfo(name = "item_list_id")
    val listId: String,

    @ColumnInfo(name = "item_note")
    val note: String,

    @ColumnInfo(name = "item_quantity")
    val quantity: Double = 1.0,

    @ColumnInfo(name = "item_unit")
    val unit: String? = null,

    @ColumnInfo(name = "item_checked")
    val isChecked: Boolean = false,

    @ColumnInfo(name = "item_position")
    val position: Int = 0,

    @ColumnInfo(name = "item_created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "item_updated_at")
    val updatedAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "item_is_synced")
    val isSynced: Boolean = true,
)
