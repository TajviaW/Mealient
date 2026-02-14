package gq.kirmanak.mealient.datasource.models

/**
 * Represents the API version of the Mealie server.
 */
sealed class ApiVersion {
    /**
     * Mealie API v2.x.x
     */
    data class V2(val major: Int, val minor: Int, val patch: Int) : ApiVersion() {
        override fun toString(): String = "v$major.$minor.$patch"
    }

    /**
     * Unknown or unparseable version
     */
    object Unknown : ApiVersion() {
        override fun toString(): String = "Unknown"
    }

    companion object {
        /**
         * Parses a version string like "v2.0.0" or "2.1.3" into an ApiVersion.
         *
         * @param versionString The version string to parse
         * @return ApiVersion.V2 if valid v2.x.x format, ApiVersion.Unknown otherwise
         */
        fun parse(versionString: String): ApiVersion {
            // Remove 'v' prefix if present
            val cleanVersion = versionString.removePrefix("v").trim()

            // Split by dots and parse
            val parts = cleanVersion.split(".")
            if (parts.size != 3) return Unknown

            val major = parts[0].toIntOrNull() ?: return Unknown
            val minor = parts[1].toIntOrNull() ?: return Unknown
            val patch = parts[2].toIntOrNull() ?: return Unknown

            // Only accept v2 or later
            if (major < 2) return Unknown

            return V2(major, minor, patch)
        }

        /**
         * Checks if the version string represents v2.0.0 or later.
         *
         * @param versionString The version string to check
         * @return true if v2.0.0+, false otherwise
         */
        fun isV2OrLater(versionString: String): Boolean {
            return parse(versionString) is V2
        }
    }
}
