package tfg.uniovi.melodies.entities.notes.abstractClasses

import tfg.uniovi.melodies.entities.notes.interfaces.ScoreElement

abstract class ScoreElementAbstract (
    private val duration : Long
    ) : ScoreElement {
        override fun getDuration():Long{
            return this.duration
        }
    }