package gq.kirmanak.mealient.meal_plans.network;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import gq.kirmanak.mealient.datasource.MealieDataSource;
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
public final class MealPlansDataSourceImpl_Factory implements Factory<MealPlansDataSourceImpl> {
  private final Provider<MealieDataSource> dataSourceProvider;

  public MealPlansDataSourceImpl_Factory(Provider<MealieDataSource> dataSourceProvider) {
    this.dataSourceProvider = dataSourceProvider;
  }

  @Override
  public MealPlansDataSourceImpl get() {
    return newInstance(dataSourceProvider.get());
  }

  public static MealPlansDataSourceImpl_Factory create(
      Provider<MealieDataSource> dataSourceProvider) {
    return new MealPlansDataSourceImpl_Factory(dataSourceProvider);
  }

  public static MealPlansDataSourceImpl newInstance(MealieDataSource dataSource) {
    return new MealPlansDataSourceImpl(dataSource);
  }
}
