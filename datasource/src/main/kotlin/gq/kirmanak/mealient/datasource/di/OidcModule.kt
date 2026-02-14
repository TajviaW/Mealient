package gq.kirmanak.mealient.datasource.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import gq.kirmanak.mealient.datasource.oidc.OidcDiscoveryDataSource
import gq.kirmanak.mealient.datasource.oidc.OidcDiscoveryDataSourceImpl

/**
 * Dagger module for OIDC-related data source bindings.
 */
@Module
@InstallIn(SingletonComponent::class)
internal interface OidcModule {

    @Binds
    fun bindOidcDiscoveryDataSource(
        impl: OidcDiscoveryDataSourceImpl
    ): OidcDiscoveryDataSource
}
