package gq.kirmanak.mealient.data.household

import gq.kirmanak.mealient.datasource.MealieDataSource
import gq.kirmanak.mealient.logging.Logger
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Provides the household ID for the current user.
 * In Mealie v2, all resources are scoped to households instead of groups.
 */
interface HouseholdProvider {
    /**
     * Gets the household ID for the current user.
     * @return The household ID
     * @throws IllegalArgumentException if household ID is not found in user info
     */
    suspend fun getHouseholdId(): String
}

/**
 * Implementation of [HouseholdProvider] that fetches and caches the household ID.
 */
@Singleton
class HouseholdProviderImpl @Inject constructor(
    private val mealieDataSource: MealieDataSource,
    private val logger: Logger,
) : HouseholdProvider {

    private var cachedHouseholdId: String? = null

    override suspend fun getHouseholdId(): String {
        cachedHouseholdId?.let {
            logger.v { "Returning cached household ID: $it" }
            return it
        }

        logger.d { "Fetching household ID from user info" }
        val userInfo = mealieDataSource.requestUserInfo()
        val householdId = requireNotNull(userInfo.householdId) {
            "Household ID not found in user info. Ensure server is Mealie v2.0+"
        }

        cachedHouseholdId = householdId
        logger.i { "Household ID retrieved and cached: $householdId" }
        return householdId
    }
}
