package tfg.uniovi.melodies.model.notes

/**
 * Interface that allows to compare Notes data to check it is the same note
 */
interface NoteComparable {
    val name: Char
    val octave: Int
    val sharp: Boolean
}
