package tfg.uniovi.melodies.tools.pitchdetector

import android.util.Log
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.LifecycleOwner
import be.tarsos.dsp.AudioDispatcher
import be.tarsos.dsp.io.android.AudioDispatcherFactory
import be.tarsos.dsp.pitch.PitchDetectionHandler
import be.tarsos.dsp.pitch.PitchProcessor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.ln
import kotlin.math.log
import kotlin.math.pow
import kotlin.math.round
import kotlin.math.roundToInt

object PitchDetector {
    private const val SAMPLE_RATE = 44100
    private const val BUFFER_SIZE = 2048
    private val NOTES = listOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")
    private const val OCTAVE_MULTIPLIER = 2.0
    private const val KNOWN_NOTE_NAME = "A"
    private const val KNOWN_NOTE_OCTAVE = 4
    private const val KNOWN_NOTE_FREQUENCY = 440.0
    private var dispatcher: AudioDispatcher? = null
    @Volatile private var isRunning = false
    private var lastDetectedNote: String = "None"
     const val MIC_REQ_CODE = 11223344




    fun startListening(scope: CoroutineScope) {

        if (isRunning) return  // Evitar múltiples inicios
        isRunning = true

        dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(SAMPLE_RATE, BUFFER_SIZE, 0)

        val pdh = PitchDetectionHandler { result, _ ->
            if (result.pitch != -1f) {
                val pitchInHz = result.pitch
                lastDetectedNote = convertFrequencyToNote(pitchInHz)
                //Log.d("PITCH","Frecuencia: $pitchInHz Hz, Nota: $lastDetectedNote")
            }
        }

        dispatcher?.addAudioProcessor(
            PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.YIN, SAMPLE_RATE.toFloat(), BUFFER_SIZE, pdh)
        )
        scope.launch(Dispatchers.IO) {
            dispatcher?.run()
        }
    }

    fun stopListening() {
        if (!isRunning) return  // Si no está corriendo, salir

        isRunning = false
        dispatcher?.stop() // Detener el procesamiento de audio
    }

    fun getLastDetectedNote(): String = lastDetectedNote
/*
    private fun convertFrequencyToNote(frequency: Float): String {
        val notes = arrayOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")
        // Reference A4 = 440 Hz
        val A4 = 440
        //val noteIndex = Math.round(12 * ln((frequency / A4).toDouble()) / ln(2.0)).toInt()
        //val noteIndex = (12 * kotlin.math.log(frequency / A4) / kotlin.math.log(2.0)).roundToInt()
        val noteIndex = (12 * log((frequency / A4).toDouble(), 2.0)).roundToInt()

        val noteNumber = (noteIndex + 69) % 12
        val octave = (noteIndex + 69) / 12

        return if (noteNumber >= 0 && noteNumber < notes.size) {
            notes[noteNumber] + octave
        } else {
            "Desconocido"
        }
    }*/

    private fun convertFrequencyToNote(frequency: Float): String {

        val noteMultiplier = OCTAVE_MULTIPLIER.pow(1.0 / NOTES.size)
        val frequencyRelativeToKnownNote = frequency / KNOWN_NOTE_FREQUENCY
        val distanceFromKnownNote = round(log(frequencyRelativeToKnownNote, noteMultiplier)).toInt()

        val knownNoteIndexInOctave = NOTES.indexOf(KNOWN_NOTE_NAME)
        val knownNoteAbsoluteIndex = KNOWN_NOTE_OCTAVE * NOTES.size + knownNoteIndexInOctave
        val noteAbsoluteIndex = knownNoteAbsoluteIndex + distanceFromKnownNote
        val noteOctave = noteAbsoluteIndex / NOTES.size
        val noteIndexInOctave = noteAbsoluteIndex % NOTES.size
        val noteName = NOTES[noteIndexInOctave]

        return "$noteName$noteOctave"
    }


}