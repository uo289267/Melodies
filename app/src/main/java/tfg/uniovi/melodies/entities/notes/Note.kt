package tfg.uniovi.melodies.entities.notes

class Note(id: Int, duration: Long,
        private val name : Char,
        private val sharp: Boolean,
        private val octave : Int)
    : ScoreElementAbstract(id, duration)
{
    /**
     * Returns true if the noteToCheck and this note are the same
     *
     * @param noteToCheck note that needs to be checked with this note
      */
    override fun check(noteToCheck: NoteDominant): Boolean {
        return noteToCheck.getName().compareTo(this.name) == 0
    }

    fun getName(): Char{
        return this.name
    }

}