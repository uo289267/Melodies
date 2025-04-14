package tfg.uniovi.melodies.entities

import com.google.firebase.Timestamp
import java.util.UUID

class Folder(
    val name: String, //max 30 chars
    val color: Int,
    val creationTime: Timestamp,
    val folderId: String
) {

}