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

object PitchDetector {
    private const val SAMPLE_RATE = 44100
    private const val BUFFER_SIZE = 2048
    private var dispatcher: AudioDispatcher? = null
   // private var audioThread: Thread? = null
    @Volatile private var isRunning = false
    private var lastDetectedNote: String = "None"
    val MIC_REQ_CODE = 11223344




    fun startListening(scope: CoroutineScope) {

        if (isRunning) return  // Evitar múltiples inicios
        isRunning = true

        dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(SAMPLE_RATE, BUFFER_SIZE, 0)

        val pdh = PitchDetectionHandler { result, _ ->
            if (result.pitch != -1f) {
                val pitchInHz = result.pitch
                lastDetectedNote = convertFrequencyToNote(pitchInHz)
                Log.d("PITCH","Frecuencia: $pitchInHz Hz, Nota: $lastDetectedNote")
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

    private fun convertFrequencyToNote(frequency: Float): String {
        val notes = arrayOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")
        // Reference A4 = 440 Hz
        val A4 = 440
        val noteIndex = Math.round(12 * ln((frequency / A4).toDouble()) / ln(2.0)).toInt()
        val noteNumber = (noteIndex + 69) % 12
        val octave = (noteIndex + 69) / 12

        return if (noteNumber >= 0 && noteNumber < notes.size) {
            notes[noteNumber] + octave
        } else {
            "Desconocido"
        }
    }

}