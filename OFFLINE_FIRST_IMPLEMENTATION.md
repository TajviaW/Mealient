# Offline-First Implementation with Room Database

**Implementation Date**: 2026-02-14
**Status**: Foundation Complete, Repository Layer Pending

---

## ✅ Completed: Database Layer

### New Entities Added

#### 1. Shopping Lists
- **ShoppingListEntity**: Core shopping list with sync tracking
  - Fields: id, name, createdAt, updatedAt, isSynced
  - Table: `shopping_lists`

- **ShoppingListItemEntity**: Individual items in shopping lists
  - Fields: id, listId, note, quantity, unit, isChecked, position, createdAt, updatedAt, isSynced
  - Table: `shopping_list_items`
  - Foreign key: CASCADE delete on parent list

#### 2. Meal Plans
- **MealPlanEntity**: Meal planning entries
  - Fields: id, date, entryType, title, text, recipeId, recipeName, recipeSlug, createdAt, updatedAt, isSynced
  - Table: `meal_plans`
  - Supports: breakfast, lunch, dinner, snack entry types

### DAOs Implemented

#### ShoppingListDao
```kotlin
// Lists
fun observeShoppingLists(): Flow<List<ShoppingListEntity>>
suspend fun getShoppingList(listId: String): ShoppingListEntity?
suspend fun insertShoppingLists(lists: List<ShoppingListEntity>)
suspend fun getUnsyncedShoppingLists(): List<ShoppingListEntity>

// Items
fun observeShoppingListItems(listId: String): Flow<List<ShoppingListItemEntity>>
suspend fun insertShoppingListItems(items: List<ShoppingListItemEntity>)
suspend fun getUnsyncedShoppingListItems(): List<ShoppingListItemEntity>
```

#### MealPlanDao
```kotlin
fun observeMealPlans(startDate: String, endDate: String): Flow<List<MealPlanEntity>>
suspend fun getMealPlans(startDate: String, endDate: String): List<MealPlanEntity>
suspend fun insertMealPlans(plans: List<MealPlanEntity>)
suspend fun getUnsyncedMealPlans(): List<MealPlanEntity>
suspend fun deleteOldMealPlans(beforeDate: String)
```

### Database Schema
- **Version**: 13 → 14
- **Migration Strategy**: Currently using `fallbackToDestructiveMigration()`
- **Total Entities**: 8 (5 recipe, 2 shopping, 1 meal plan)

---

## 🔄 Next Steps: Repository Layer

### Phase 1: Update Shopping Lists Repository

**Current**: Direct API calls to `MealieDataSource`
**Target**: Offline-first pattern with local cache

```kotlin
class ShoppingListsRepoOfflineFirst @Inject constructor(
    private val shoppingListDao: ShoppingListDao,
    private val mealieDataSource: MealieDataSource,
    private val logger: Logger,
) : ShoppingListsRepo {

    override suspend fun getShoppingLists(): Result<List<ShoppingListEntity>> {
        // 1. Return cached data immediately
        val cached = shoppingListDao.observeShoppingLists().first()
        if (cached.isNotEmpty()) return Result.success(cached)

        // 2. Fetch from API in background
        return try {
            val remote = mealieDataSource.getShoppingLists()
            val entities = remote.items.map { it.toEntity() }
            shoppingListDao.insertShoppingLists(entities)
            Result.success(entities)
        } catch (e: Exception) {
            if (cached.isEmpty()) Result.failure(e)
            else Result.success(cached) // Return stale cache on error
        }
    }

    override suspend fun createShoppingList(name: String): Result<Unit> {
        // 1. Create locally with isSynced = false
        val entity = ShoppingListEntity(
            id = UUID.randomUUID().toString(),
            name = name,
            isSynced = false
        )
        shoppingListDao.insertShoppingList(entity)

        // 2. Sync to server
        return try {
            val request = CreateShoppingListRequest(name)
            val response = mealieDataSource.createShoppingList(request)
            // Update with server ID and mark synced
            shoppingListDao.insertShoppingList(entity.copy(id = response.id, isSynced = true))
            Result.success(Unit)
        } catch (e: Exception) {
            logger.w(e) { "Failed to sync shopping list, will retry later" }
            Result.success(Unit) // Return success, sync will happen later
        }
    }
}
```

### Phase 2: Update Meal Plans Repository

**Current**: `MealPlansRepoImpl` with direct API calls
**Target**: Offline-first with local cache

```kotlin
class MealPlansRepoOfflineFirst @Inject constructor(
    private val mealPlanDao: MealPlanDao,
    private val mealieDataSource: MealieDataSource,
    private val logger: Logger,
) : MealPlansRepo {

    override suspend fun getMealPlansForDateRange(
        startDate: String,
        endDate: String
    ): List<GetMealPlanResponse> {
        // 1. Return cached data
        val cached = mealPlanDao.getMealPlans(startDate, endDate)

        // 2. Fetch from API and update cache
        try {
            val remote = mealieDataSource.getMealPlans(startDate, endDate)
            val entities = remote.items.map { it.toEntity() }
            mealPlanDao.insertMealPlans(entities)
            return remote.items
        } catch (e: Exception) {
            logger.w(e) { "Failed to fetch meal plans from API" }
            // Return cached data on error
            return cached.map { it.toResponse() }
        }
    }
}
```

### Phase 3: Background Sync Worker

**Purpose**: Sync unsynced changes when network is available

```kotlin
class SyncWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        // 1. Sync unsynced shopping lists
        val unsyncedLists = shoppingListDao.getUnsyncedShoppingLists()
        unsyncedLists.forEach { list ->
            try {
                mealieDataSource.updateShoppingList(list.id, list.toRequest())
                shoppingListDao.updateShoppingList(list.copy(isSynced = true))
            } catch (e: Exception) {
                logger.e(e) { "Failed to sync shopping list ${list.id}" }
            }
        }

        // 2. Sync unsynced shopping list items
        val unsyncedItems = shoppingListDao.getUnsyncedShoppingListItems()
        unsyncedItems.forEach { item ->
            try {
                mealieDataSource.updateShoppingListItem(item.id, item.toRequest())
                shoppingListDao.updateShoppingListItem(item.copy(isSynced = true))
            } catch (e: Exception) {
                logger.e(e) { "Failed to sync shopping list item ${item.id}" }
            }
        }

        // 3. Sync unsynced meal plans
        val unsyncedPlans = mealPlanDao.getUnsyncedMealPlans()
        unsyncedPlans.forEach { plan ->
            try {
                mealieDataSource.updateMealPlan(plan.id, plan.toRequest())
                mealPlanDao.updateMealPlan(plan.copy(isSynced = true))
            } catch (e: Exception) {
                logger.e(e) { "Failed to sync meal plan ${plan.id}" }
            }
        }

        return Result.success()
    }
}
```

### Phase 4: Conflict Resolution

**Strategy**: Last-write-wins with server authority

1. **Server wins on pull**: Always overwrite local with server data when fetching
2. **Local changes marked unsynced**: Track with `isSynced = false` flag
3. **Retry logic**: Use WorkManager with exponential backoff
4. **User notification**: Show sync status in UI

---

## 📊 Database Schema Summary

### Tables Created

| Table | Entities | Purpose |
|-------|----------|---------|
| `shopping_lists` | ShoppingListEntity | Shopping list metadata |
| `shopping_list_items` | ShoppingListItemEntity | Individual items with quantities |
| `meal_plans` | MealPlanEntity | Meal planning entries by date |

### Key Features

✅ **Foreign Keys**: CASCADE delete for shopping list items
✅ **Indexes**: On foreign keys for query performance
✅ **Sync Tracking**: `isSynced` flag on all entities
✅ **Timestamps**: `createdAt` and `updatedAt` for conflict resolution
✅ **Flow Support**: Real-time UI updates with `Flow<T>` queries

---

## 🎯 Benefits of Offline-First

1. **Instant UI**: No loading spinners for cached data
2. **Works offline**: Users can view and edit data without connectivity
3. **Better UX**: Optimistic updates with background sync
4. **Resilience**: App works even with poor network
5. **Performance**: Reduced API calls and faster response times

---

## 🔨 Implementation Checklist

### Database Layer ✅
- [x] Create ShoppingListEntity and ShoppingListItemEntity
- [x] Create MealPlanEntity
- [x] Implement ShoppingListDao with Flow support
- [x] Implement MealPlanDao with date range queries
- [x] Update AppDb to version 14
- [x] Add DAOs to DatabaseModule

### Repository Layer ⏳
- [ ] Update ShoppingListsRepo to use offline-first pattern
- [ ] Update MealPlansRepo to use offline-first pattern
- [ ] Create mapper extensions (toEntity/toResponse)
- [ ] Add sync error handling
- [ ] Implement retry logic

### Sync Layer ⏳
- [ ] Create SyncWorker with WorkManager
- [ ] Schedule periodic sync (every 15 minutes)
- [ ] Add network constraint to sync worker
- [ ] Implement conflict resolution strategy
- [ ] Add sync status indicator in UI

### Testing ⏳
- [ ] Unit tests for DAOs
- [ ] Unit tests for offline-first repositories
- [ ] Integration tests for sync worker
- [ ] Manual testing: airplane mode scenarios
- [ ] Performance testing: large datasets

---

## 📝 Migration Strategy

### For Existing Users

When users update to this version:
1. Database is recreated (using `fallbackToDestructiveMigration`)
2. On first launch, app fetches all data from server
3. Data is cached locally for offline access
4. No data loss (data still on server)

### For Production

Replace `fallbackToDestructiveMigration()` with proper migrations:

```kotlin
Room.databaseBuilder(context, AppDb::class.java, "app.db")
    .addMigrations(MIGRATION_13_14)
    .build()

val MIGRATION_13_14 = object : Migration(13, 14) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Create shopping lists table
        database.execSQL("""
            CREATE TABLE shopping_lists (
                list_id TEXT PRIMARY KEY NOT NULL,
                list_name TEXT NOT NULL,
                list_created_at INTEGER NOT NULL,
                list_updated_at INTEGER NOT NULL,
                list_is_synced INTEGER NOT NULL
            )
        """)

        // Create shopping list items table
        database.execSQL("""
            CREATE TABLE shopping_list_items (
                item_id TEXT PRIMARY KEY NOT NULL,
                item_list_id TEXT NOT NULL,
                item_note TEXT NOT NULL,
                item_quantity REAL NOT NULL,
                item_unit TEXT,
                item_checked INTEGER NOT NULL,
                item_position INTEGER NOT NULL,
                item_created_at INTEGER NOT NULL,
                item_updated_at INTEGER NOT NULL,
                item_is_synced INTEGER NOT NULL,
                FOREIGN KEY(item_list_id) REFERENCES shopping_lists(list_id) ON DELETE CASCADE
            )
        """)

        // Create meal plans table
        database.execSQL("""
            CREATE TABLE meal_plans (
                plan_id TEXT PRIMARY KEY NOT NULL,
                plan_date TEXT NOT NULL,
                plan_entry_type TEXT NOT NULL,
                plan_title TEXT,
                plan_text TEXT,
                plan_recipe_id TEXT,
                plan_recipe_name TEXT,
                plan_recipe_slug TEXT,
                plan_created_at INTEGER NOT NULL,
                plan_updated_at INTEGER NOT NULL,
                plan_is_synced INTEGER NOT NULL
            )
        """)

        // Create indexes
        database.execSQL("CREATE INDEX index_shopping_list_items_item_list_id ON shopping_list_items(item_list_id)")
    }
}
```

---

## 🚀 Estimated Remaining Work

- **Repository Layer**: 2-3 days
- **Sync Worker**: 1-2 days
- **Testing**: 1-2 days
- **Total**: 4-7 days

---

**Last Updated**: 2026-02-14
**Status**: Database foundation complete, ready for repository implementation
