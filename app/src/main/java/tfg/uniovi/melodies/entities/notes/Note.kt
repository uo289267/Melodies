package tfg.uniovi.melodies.entities.notes

import androidx.collection.emptyLongSet
import tfg.uniovi.melodies.entities.notes.abstractClasses.ScoreElementAbstract
import tfg.uniovi.melodies.entities.notes.interfaces.NoteComparable

class Note(
    id: Int,
    duration: Long,
    private val _name: Char,
    private val _octave: Int,
    private val _sharp: Boolean
) : ScoreElementAbstract(id, duration), NoteComparable {

    override val name: Char
        get() = _name

    override val octave: Int
        get() = _octave

    override val sharp: Boolean
        get() = _sharp

    override fun check(noteToCheck: NoteDominant): Boolean {
        return (noteToCheck.name == this.name) && (noteToCheck.octave == this._octave)
    }

    override fun peek(noteToCheck: NoteDominant): Boolean {
        return check(noteToCheck)
    }

    override fun toString(): String {
        var string = ""
        if(_sharp)
            string =  "#"

        return "$_name$string$_octave"
    }
}
