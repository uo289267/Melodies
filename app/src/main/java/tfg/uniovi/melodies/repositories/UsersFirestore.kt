package tfg.uniovi.melodies.repositories

import android.content.Context
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await
import tfg.uniovi.melodies.preferences.PreferenceManager
import tfg.uniovi.melodies.utils.parser.String2MusicXML
import tfg.uniovi.melodies.utils.parser.XMLParser
import java.io.IOException

class UsersFirestore {
    private val db = Firebase.firestore

    suspend fun userExists(userId: String): Boolean {
        return try {
            val snapshot = db.collection("users").document(userId).get().await()
            snapshot.exists()
        } catch (e: Exception) {
            Log.e("UserRepository", "Error checking user existence", e)
            false
        }
    }
    suspend fun setupUserDataIfNeeded(context: Context): String? {
        val storedUserId = PreferenceManager.getUserId(context)
        if (storedUserId != null) {
            Log.d("UserRepository", "Usuario ya existente con ID $storedUserId")
            return null // Ya hay usuario creado
        }

        return try {
            val userRef = db.collection("users").document() // ID automático
            val userId = userRef.id
            // Crear documento de usuario (con algún dato, o vacío si no tienes más info)
            val userData = mapOf("createdAt" to FieldValue.serverTimestamp())
            userRef.set(userData).await()
            // Crear carpeta "Basic Sheets"
            val folderRef = userRef.collection("folders").document()
            val folderData = mapOf(
                "name" to "Basic Sheets",
                "color" to "YELLOW",
                "creationTime" to FieldValue.serverTimestamp()
            )
            folderRef.set(folderData).await()

            // Obtener partituras por defecto
            val result = db.collection("defaultSheets").get().await()
            for (sheetDoc in result) {
                val sheetData = sheetDoc.data
                folderRef.collection("sheets").add(sheetData).await()
            }

            PreferenceManager.saveUserId(context, userId)
            Log.d("UserRepository", "Partituras copiadas para usuario $userId")
            userId
        } catch (e: Exception) {
            Log.e("UserRepository", "Error al configurar el usuario", e)
            null
        }
    }

    /*
        suspend fun setupUserDataIfNeeded(context: Context) {

            /*val storedUserId = PreferenceManager.getUserId(context)
            if (storedUserId != null) {
                Log.d("UserRepository", "Usuario ya existente con ID $storedUserId")
                return  // Ya hay usuario creado, no repetir
            }

            val userRef = db.collection("users").document() // ID automático
            val userId = userRef.id

            // Crear documento de usuario
           // userRef.set(mapOf("createdAt" to FieldValue.serverTimestamp()))
            //    .addOnSuccessListener {
                    // Crear carpeta "Basic Sheets"
                    val folderRef = userRef.collection("folders").document()
                    val folderData = mapOf(
                        "name" to "Basic Sheets",
                        "color" to "YELLOW",
                        "creationTime" to FieldValue.serverTimestamp()
                    )
                    folderRef.set(folderData).addOnSuccessListener {
                        // Cargar partituras desde Firestore
                        db.collection("defaultSheets").get()
                            .addOnSuccessListener { result ->
                                for (sheetDoc in result) {
                                    val sheetData = sheetDoc.data
                                    folderRef.collection("sheets").add(sheetData)
                                }
                                Log.d("UserRepository", "Partituras copiadas para usuario $userId")
                                PreferenceManager.saveUserId(context, userId)
                            }
                            .addOnFailureListener { e ->
                                Log.e("UserRepository", "Error cargando partituras por defecto", e)
                            }
                    }
                /*}
                .addOnFailureListener { e ->
                    Log.e("UserRepository", "Error creando usuario", e)
                }*/*/
        }*/

}