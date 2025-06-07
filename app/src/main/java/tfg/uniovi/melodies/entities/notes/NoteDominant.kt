package tfg.uniovi.melodies.entities.notes


import tfg.uniovi.melodies.entities.notes.interfaces.NoteComparable

class NoteDominant(
    override val name: Char,
    override val octave: Int,
    override val sharp: Boolean = false
) : NoteComparable {

    override fun toString(): String {
        return "$name$octave${if (sharp) "#" else ""}"
    }
}
