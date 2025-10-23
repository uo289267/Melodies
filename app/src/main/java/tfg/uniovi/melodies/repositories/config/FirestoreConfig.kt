package tfg.uniovi.melodies.repositories.config

import tfg.uniovi.melodies.BuildConfig // ¡Importación corregida! Usa el paquete de la aplicación.

object FirestoreConfig {
    private const val BASE_USERS_COLLECTION = "users"

    /**
     * Devuelve el sufijo del entorno actual: "dev", "test" o "prod".
     * Esta información se lee de la variable BuildConfig generada por Gradle.
     */
    private val environmentSuffix: String
        get() = BuildConfig.ENVIRONMENT

    /**
     * Devuelve el nombre completo de la colección de usuarios (ej: "users_dev").
     * * NOTA IMPORTANTE: Si estás usando la ruta de ejemplo que mencionaste
     * (db/users/MSQUzx0ncX2HVL1ejBFE/...) y la colección 'users' es la principal
     * que quieres modificar, este método te da el nombre correcto.
     */
    fun getUsersCollectionName(): String {
        return "${BASE_USERS_COLLECTION}_$environmentSuffix"
    }
}
