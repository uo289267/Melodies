package tfg.uniovi.melodies.entities.notes.interfaces

import tfg.uniovi.melodies.entities.notes.NoteDominant

interface ScoreElement {
    fun check(noteToCheck: NoteDominant): Boolean
    fun getDuration(): Long
}