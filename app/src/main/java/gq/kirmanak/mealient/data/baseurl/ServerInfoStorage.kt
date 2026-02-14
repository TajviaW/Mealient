package gq.kirmanak.mealient.data.baseurl

import kotlinx.coroutines.flow.Flow

interface ServerInfoStorage {

    val baseUrlFlow: Flow<String?>

    val versionFlow: Flow<String?>

    suspend fun getBaseURL(): String?

    suspend fun storeBaseURL(baseURL: String?)

    suspend fun getVersion(): String?

    suspend fun storeVersion(version: String?)

}