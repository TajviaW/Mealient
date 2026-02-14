package gq.kirmanak.mealient.meal_plans

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import gq.kirmanak.mealient.meal_plans.network.MealPlansDataSource
import gq.kirmanak.mealient.meal_plans.network.MealPlansDataSourceImpl
import gq.kirmanak.mealient.meal_plans.repo.MealPlansRepo
import gq.kirmanak.mealient.meal_plans.repo.MealPlansRepoImpl

@Module
@InstallIn(SingletonComponent::class)
interface MealPlansModule {

    @Binds
    fun bindMealPlansDataSource(impl: MealPlansDataSourceImpl): MealPlansDataSource

    @Binds
    fun bindMealPlansRepo(impl: MealPlansRepoImpl): MealPlansRepo
}
