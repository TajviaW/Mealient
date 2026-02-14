package gq.kirmanak.mealient.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import gq.kirmanak.mealient.data.household.HouseholdProvider
import gq.kirmanak.mealient.data.household.HouseholdProviderImpl

@Module
@InstallIn(SingletonComponent::class)
interface HouseholdModule {

    @Binds
    fun bindHouseholdProvider(impl: HouseholdProviderImpl): HouseholdProvider
}
