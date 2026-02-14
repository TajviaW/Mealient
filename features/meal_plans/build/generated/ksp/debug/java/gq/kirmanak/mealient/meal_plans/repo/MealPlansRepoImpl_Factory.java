package gq.kirmanak.mealient.meal_plans.repo;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import gq.kirmanak.mealient.logging.Logger;
import gq.kirmanak.mealient.meal_plans.network.MealPlansDataSource;
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
public final class MealPlansRepoImpl_Factory implements Factory<MealPlansRepoImpl> {
  private final Provider<MealPlansDataSource> dataSourceProvider;

  private final Provider<Logger> loggerProvider;

  public MealPlansRepoImpl_Factory(Provider<MealPlansDataSource> dataSourceProvider,
      Provider<Logger> loggerProvider) {
    this.dataSourceProvider = dataSourceProvider;
    this.loggerProvider = loggerProvider;
  }

  @Override
  public MealPlansRepoImpl get() {
    return newInstance(dataSourceProvider.get(), loggerProvider.get());
  }

  public static MealPlansRepoImpl_Factory create(Provider<MealPlansDataSource> dataSourceProvider,
      Provider<Logger> loggerProvider) {
    return new MealPlansRepoImpl_Factory(dataSourceProvider, loggerProvider);
  }

  public static MealPlansRepoImpl newInstance(MealPlansDataSource dataSource, Logger logger) {
    return new MealPlansRepoImpl(dataSource, logger);
  }
}
