package tfg.uniovi.melodies.entities.notes

abstract class ScoreElementAbstract (
    private val id : Int,
    private val duration : Long
    ) : ScoreElement{
        override fun getDuration():Long{
            return this.duration
        }
    }