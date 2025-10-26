package tfg.uniovi.melodies.model.notes.interfaces

import tfg.uniovi.melodies.model.notes.NoteDominant

interface ScoreElement {
    fun check(noteToCheck: NoteDominant?): Boolean
    fun getDuration(): Long
}