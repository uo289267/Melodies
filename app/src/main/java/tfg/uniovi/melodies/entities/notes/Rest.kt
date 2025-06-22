package tfg.uniovi.melodies.entities.notes


import tfg.uniovi.melodies.entities.notes.abstractClasses.ScoreElementAbstract

class Rest(id: Int, duration: Long)
    : ScoreElementAbstract(id, duration) {
    private var followingNote : Note? = null //could be null if rest is the last note

    /**
     * Returns true if the noteToCheck is different from the following note with pitch
     * (meaning the silence was not skipped)
     *
     * @param noteToCheck note to check if corresponds with following note with pitch after this rest
     */
    override fun check(noteToCheck: NoteDominant): Boolean {
        if(this.followingNote == null)
            return true
        else
            return this.followingNote!!.name!=noteToCheck.name
                    || this.followingNote!!.octave!=noteToCheck.octave
    }

    override fun peek(noteToCheck: NoteDominant): Boolean {
        return true
    }

    /**
     * Sets the following note field with the given note
     *
     * @param followingNote the next note with pitch after the rest
     */
    fun setFollowingNote(followingNote : Note){
        this.followingNote = followingNote
    }

    override fun toString(): String {
        return "Rest(${getDuration()} ms)"
    }
}