package tfg.uniovi.melodies.model.notes.comparables

import tfg.uniovi.melodies.model.notes.NoteComparable


/**
 * Represents a dominant musical note, typically used for pitch detection or comparison.
 * Stores its name (pitch class), octave, and whether it is sharp.
 *
 * @param name The pitch class of the note (A–G).
 * @param octave The octave number of the note.
 * @param sharp True if the note is sharp (#), false otherwise. Defaults to false.
 */
class NoteDominant(
    override val name: Char,
    override val octave: Int,
    override val sharp: Boolean
) : NoteComparable {
    /**
     * Returns the string representation of the note, including sharp sign if applicable.
     *
     * @return A string in the format "Name[#]Octave", e.g., "C#4" or "A3".
     */
    override fun toString(): String {
        return "$name${if (sharp) "#" else ""}$octave$"
    }
}
