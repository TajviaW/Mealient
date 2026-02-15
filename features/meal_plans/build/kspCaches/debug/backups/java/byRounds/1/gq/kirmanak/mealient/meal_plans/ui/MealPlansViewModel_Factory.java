package gq.kirmanak.mealient.meal_plans.ui;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import gq.kirmanak.mealient.datasource.MealieDataSource;
import gq.kirmanak.mealient.logging.Logger;
import gq.kirmanak.mealient.meal_plans.repo.MealPlansRepo;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast",
    "deprecation"
})
public final class MealPlansViewModel_Factory implements Factory<MealPlansViewModel> {
  private final Provider<MealPlansRepo> repoProvider;

  private final Provider<MealieDataSource> mealieDataSourceProvider;

  private final Provider<Logger> loggerProvider;

  public MealPlansViewModel_Factory(Provider<MealPlansRepo> repoProvider,
      Provider<MealieDataSource> mealieDataSourceProvider, Provider<Logger> loggerProvider) {
    this.repoProvider = repoProvider;
    this.mealieDataSourceProvider = mealieDataSourceProvider;
    this.loggerProvider = loggerProvider;
  }

  @Override
  public MealPlansViewModel get() {
    return newInstance(repoProvider.get(), mealieDataSourceProvider.get(), loggerProvider.get());
  }

  public static MealPlansViewModel_Factory create(Provider<MealPlansRepo> repoProvider,
      Provider<MealieDataSource> mealieDataSourceProvider, Provider<Logger> loggerProvider) {
    return new MealPlansViewModel_Factory(repoProvider, mealieDataSourceProvider, loggerProvider);
  }

  public static MealPlansViewModel newInstance(MealPlansRepo repo,
      MealieDataSource mealieDataSource, Logger logger) {
    return new MealPlansViewModel(repo, mealieDataSource, logger);
  }
}
