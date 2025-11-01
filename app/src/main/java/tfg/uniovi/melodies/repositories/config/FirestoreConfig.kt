package tfg.uniovi.melodies.repositories.config

import tfg.uniovi.melodies.BuildConfig

object FirestoreConfig {
    private const val BASE_USERS_COLLECTION = "users"

    /**
     * Returns the environment suffix "dev", "test" o "prod".
     */
    private val environmentSuffix: String
        get() = BuildConfig.ENVIRONMENT

    /**
     * Returns the complete name of the users collection ("users_dev", "users_test"...)
     */
    fun getUsersCollectionName(): String {
        return "${BASE_USERS_COLLECTION}_$environmentSuffix"
    }
}
