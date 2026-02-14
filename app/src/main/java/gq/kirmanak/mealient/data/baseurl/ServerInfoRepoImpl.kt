package gq.kirmanak.mealient.data.baseurl

import gq.kirmanak.mealient.datasource.ServerUrlProvider
import gq.kirmanak.mealient.datasource.UnsupportedServerVersionException
import gq.kirmanak.mealient.datasource.models.ApiVersion
import gq.kirmanak.mealient.datasource.models.VersionResponse
import gq.kirmanak.mealient.logging.Logger
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ServerInfoRepoImpl @Inject constructor(
    private val serverInfoStorage: ServerInfoStorage,
    private val versionDataSource: VersionDataSource,
    private val logger: Logger,
) : ServerInfoRepo, ServerUrlProvider {

    override val baseUrlFlow: Flow<String?>
        get() = serverInfoStorage.baseUrlFlow

    override suspend fun getUrl(): String? {
        val result = serverInfoStorage.getBaseURL()
        logger.v { "getUrl() returned: $result" }
        return result
    }

    override suspend fun tryBaseURL(baseURL: String): Result<VersionResponse> {
        return versionDataSource.runCatching {
            requestVersion(baseURL)
        }.onSuccess { versionResponse ->
            // Validate that server is running Mealie v2.0+
            if (!ApiVersion.isV2OrLater(versionResponse.version)) {
                throw UnsupportedServerVersionException(
                    "This app requires Mealie v2.0 or later. Server version: ${versionResponse.version}"
                )
            }
            logger.i { "Server version validated: ${versionResponse.version}" }
            serverInfoStorage.storeBaseURL(baseURL)
            serverInfoStorage.storeVersion(versionResponse.version)
        }
    }
}