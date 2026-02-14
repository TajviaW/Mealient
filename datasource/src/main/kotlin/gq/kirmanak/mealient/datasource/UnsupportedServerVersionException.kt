package gq.kirmanak.mealient.datasource

/**
 * Exception thrown when the Mealie server version is not supported by this app.
 * This app requires Mealie v2.0.0 or later.
 */
class UnsupportedServerVersionException(message: String) : Exception(message)
