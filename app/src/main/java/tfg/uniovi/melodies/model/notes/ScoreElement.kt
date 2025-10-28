package tfg.uniovi.melodies.model.notes

import tfg.uniovi.melodies.model.notes.comparables.NoteDominant

interface ScoreElement {
    fun check(noteToCheck: NoteDominant?): Boolean
    fun getDuration(): Long
}