package tfg.uniovi.melodies.entities.notes

interface ScoreElement {
    fun check(noteToCheck: NoteDominant): Boolean
    fun getDuration(): Long
}