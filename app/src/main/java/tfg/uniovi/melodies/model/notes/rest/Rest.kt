package tfg.uniovi.melodies.model.notes.rest

import tfg.uniovi.melodies.model.notes.comparables.NoteDominant
import tfg.uniovi.melodies.model.notes.ScoreElementAbstract
import tfg.uniovi.melodies.model.notes.comparables.Note


/**
 * Represents a rest/silence in a score.
 * Stores its duration
 *
 * @param duration The duration of the note in milliseconds.*/
class Rest(duration: Long)
    : ScoreElementAbstract(duration) {
    private var followingNote : Note? = null //could be null if rest is the last note

    /**
     * Returns true if the noteToCheck is different from the following note with pitch
     * (meaning the silence was not skipped)
     *
     * @param noteToCheck note to check if corresponds with following note with pitch after this rest
     */
    override fun check(noteToCheck: NoteDominant?): Boolean {
        return if(this.followingNote == null || noteToCheck == null)
            true
        else
            this.followingNote!!.name!=noteToCheck.name
                    || this.followingNote!!.octave!=noteToCheck.octave
    }


    /**
     * Sets the following note field with the given note
     *
     * @param followingNote the next note with pitch after the rest
     */
    fun setFollowingNote(followingNote : Note){
        this.followingNote = followingNote
    }

    fun getFollowingNote(): Note? {
        return this.followingNote
    }

    override fun toString(): String {
        return "Rest(${getDuration()} ms)"
    }
}