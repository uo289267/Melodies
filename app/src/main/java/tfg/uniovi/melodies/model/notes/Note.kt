package tfg.uniovi.melodies.model.notes

import tfg.uniovi.melodies.model.notes.abstractClasses.ScoreElementAbstract
import tfg.uniovi.melodies.model.notes.interfaces.NoteComparable
/**
 * Represents a musical note in a score.
 * Stores its duration, name (pitch class), octave, and whether it is sharp.
 * Implements comparison operations with dominant notes.
 *
 * @param duration The duration of the note in milliseconds.
 * @param _name The pitch class of the note (A–G).
 * @param _octave The octave number of the note.
 * @param _sharp True if the note is sharp (#), false otherwise.
 */
class Note(
    duration: Long,
    private val _name: Char,
    private val _octave: Int,
    private val _sharp: Boolean
) : ScoreElementAbstract(duration), NoteComparable {

    override val name: Char
        get() = _name

    override val octave: Int
        get() = _octave

    override val sharp: Boolean
        get() = _sharp
    /**
     * Checks if the given dominant note matches this note in name and octave.
     *
     * @param noteToCheck The dominant note to compare against, or null.
     * @return True if the names and octaves match, false otherwise.
     */
    override fun check(noteToCheck: NoteDominant?): Boolean {
        if(noteToCheck==null)
            return false
        return (noteToCheck.name == this._name) &&
                (noteToCheck.octave == this._octave) &&
                (noteToCheck.sharp == this._sharp)
    }

    /**
     * Returns the string representation of the note, including sharp sign if applicable.
     *
     * @return A string in the format "Name[#]Octave", e.g., "C#4" or "A3".
     */
    override fun toString(): String {
        var string = ""
        if(_sharp)
            string =  "#"

        return "$_name$string$_octave"
    }
}
