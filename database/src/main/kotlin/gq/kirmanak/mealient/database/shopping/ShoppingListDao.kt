package gq.kirmanak.mealient.database.shopping

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import gq.kirmanak.mealient.database.shopping.entity.ShoppingListEntity
import gq.kirmanak.mealient.database.shopping.entity.ShoppingListItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ShoppingListDao {

    // Shopping Lists
    @Query("SELECT * FROM shopping_lists ORDER BY list_created_at DESC")
    fun observeShoppingLists(): Flow<List<ShoppingListEntity>>

    @Query("SELECT * FROM shopping_lists WHERE list_id = :listId")
    suspend fun getShoppingList(listId: String): ShoppingListEntity?

    @Query("SELECT * FROM shopping_lists WHERE list_id = :listId")
    fun observeShoppingList(listId: String): Flow<ShoppingListEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShoppingList(list: ShoppingListEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShoppingLists(lists: List<ShoppingListEntity>)

    @Update
    suspend fun updateShoppingList(list: ShoppingListEntity)

    @Delete
    suspend fun deleteShoppingList(list: ShoppingListEntity)

    @Query("DELETE FROM shopping_lists WHERE list_id = :listId")
    suspend fun deleteShoppingListById(listId: String)

    @Query("SELECT * FROM shopping_lists WHERE list_is_synced = 0")
    suspend fun getUnsyncedShoppingLists(): List<ShoppingListEntity>

    // Shopping List Items
    @Query("SELECT * FROM shopping_list_items WHERE item_list_id = :listId ORDER BY item_position ASC, item_created_at ASC")
    fun observeShoppingListItems(listId: String): Flow<List<ShoppingListItemEntity>>

    @Query("SELECT * FROM shopping_list_items WHERE item_list_id = :listId ORDER BY item_position ASC, item_created_at ASC")
    suspend fun getShoppingListItems(listId: String): List<ShoppingListItemEntity>

    @Query("SELECT * FROM shopping_list_items WHERE item_id = :itemId")
    suspend fun getShoppingListItem(itemId: String): ShoppingListItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShoppingListItem(item: ShoppingListItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShoppingListItems(items: List<ShoppingListItemEntity>)

    @Update
    suspend fun updateShoppingListItem(item: ShoppingListItemEntity)

    @Delete
    suspend fun deleteShoppingListItem(item: ShoppingListItemEntity)

    @Query("DELETE FROM shopping_list_items WHERE item_id = :itemId")
    suspend fun deleteShoppingListItemById(itemId: String)

    @Query("SELECT * FROM shopping_list_items WHERE item_is_synced = 0")
    suspend fun getUnsyncedShoppingListItems(): List<ShoppingListItemEntity>

    @Transaction
    @Query("DELETE FROM shopping_lists")
    suspend fun deleteAllShoppingLists()

    @Transaction
    @Query("DELETE FROM shopping_list_items")
    suspend fun deleteAllShoppingListItems()
}
