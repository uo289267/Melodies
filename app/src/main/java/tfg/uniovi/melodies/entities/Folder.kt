package tfg.uniovi.melodies.entities

/**
 * Represents a folder entity used to organize scores or other musical sheets.
 *
 * @param name The folder name (maximum 30 characters).
 * @param color The visual color assigned to the folder.
 * @param folderId The unique identifier of the folder.
 */
class Folder(
    val name: String, //max 30 chars
    val color: Colors,
    val folderId: String
){
    override fun toString(): String {
        return this.name
    }
}

/**
 * Enum representing the available folder colors.
 */
enum class Colors(val hex: String) {
    YELLOW("#f8c63d"),
    BLUE("#34a1e4"),
    PINK("#f76592")
}
