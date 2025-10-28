package tfg.uniovi.melodies.model.notes

/**
 * All ScoreElements (Note, Rest) must at least have a duration in milliseconds
 */
abstract class ScoreElementAbstract (
    private val duration : Long
    ) : ScoreElement {
        override fun getDuration():Long{
            return this.duration
        }
    }