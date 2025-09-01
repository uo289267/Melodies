package tfg.uniovi.melodies.tools.pitchdetector

import be.tarsos.dsp.AudioDispatcher
import be.tarsos.dsp.io.android.AudioDispatcherFactory
import be.tarsos.dsp.pitch.PitchDetectionHandler
import be.tarsos.dsp.pitch.PitchProcessor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tfg.uniovi.melodies.tools.pitchdetector.PitchDetector.getLastDetectedNote
import kotlin.math.log
import kotlin.math.pow
import kotlin.math.round

/**
 * Singleton object for real-time pitch detection using the microphone.
 *
 * This class uses **TarsosDSP** to analyze audio input from the device’s
 * microphone and detect musical notes (e.g., "A4", "C#5").
 *
 * - The note detection is based on the A4 = 440 Hz reference.
 * - Only one instance can run at a time.
 * - Results can be retrieved using [getLastDetectedNote].
 */
object PitchDetector {
    /** Audio sampling rate in Hz. */
    private const val SAMPLE_RATE = 44100
    /** Buffer size for audio frames. */
    private const val BUFFER_SIZE = 2048
    /** Names of the chromatic scale notes. */
    private val NOTES = listOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")
    /** Multiplier used to calculate frequency changes across octaves. */
    private const val OCTAVE_MULTIPLIER = 2.0
    /** Reference note name used for calculations (A4). */
    private const val KNOWN_NOTE_NAME = "A"
    private const val KNOWN_NOTE_OCTAVE = 4
    private const val KNOWN_NOTE_FREQUENCY = 440.0

    const val SILENCE ="Unknown"
    /** Request code for microphone permission. */
    const val MIC_REQ_CODE = 11223344
    /** Dispatcher that processes audio input. */
    private var dispatcher: AudioDispatcher? = null
    /** Flag indicating whether pitch detection is active. */
    @Volatile private var isRunning = false
    /** Stores the last detected note (or [SILENCE] if none). */
    private var lastDetectedNote: String = SILENCE

    /**
     * Starts listening to the microphone and detecting pitch.
     *
     * - Uses a coroutine to process audio in the background.
     * - Only one instance of the listener can run at a time.
     *
     * @param scope The [CoroutineScope] where the audio dispatcher will run.
     */
    fun startListening(scope: CoroutineScope) {

        if (isRunning) return  // Evitar múltiples inicios
        isRunning = true

        dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(SAMPLE_RATE, BUFFER_SIZE, 0)

        val pdh = PitchDetectionHandler { result, _ ->
            if (result.pitch != -1f) {
                val pitchInHz = result.pitch
                lastDetectedNote = convertFrequencyToNote(pitchInHz)
                //Log.d("PITCH","Frecuencia: $pitchInHz Hz, Nota: $lastDetectedNote")
            }else{
                lastDetectedNote = SILENCE
                //Log.d("PITCH","SILENCE...")
            }
        }

        dispatcher?.addAudioProcessor(
            PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.YIN, SAMPLE_RATE.toFloat(), BUFFER_SIZE, pdh)
        )
        scope.launch(Dispatchers.IO) {
            dispatcher?.run()
        }
    }
    /**
     * Stops listening to the microphone and halts pitch detection.
     */
    fun stopListening() {
        if (!isRunning) return  // If it is not running return

        isRunning = false
        dispatcher?.stop() // Stop audio processing
    }
    /**
     * Returns the last detected note.
     *
     * @return A string representing the last detected note (e.g., `"C#4"`) or [SILENCE] if no note was detected.
     */
    fun getLastDetectedNote(): String = lastDetectedNote
    /**
     * Converts a frequency in Hz to its closest musical note name and octave.
     *
     * Uses the equal-tempered chromatic scale with A4 = 440 Hz as the reference.
     *
     * @param frequency The frequency in Hz.
     * @return The corresponding note name and octave (e.g., `"F#3"`).
     */
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