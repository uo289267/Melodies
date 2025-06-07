package tfg.uniovi.melodies.fragments.viewmodels


import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import tfg.uniovi.melodies.entities.MusicXMLSheet
import tfg.uniovi.melodies.entities.notes.Note
import tfg.uniovi.melodies.entities.notes.interfaces.ScoreElement
import tfg.uniovi.melodies.repositories.FoldersAndSheetsFirestore
import tfg.uniovi.melodies.tools.pitchdetector.SheetChecker2
import tfg.uniovi.melodies.utils.parser.XMLParser
import java.io.Serializable

class SheetVisualizationViewModelFactory(
    private val currentUserUUID: String
):
    ViewModelProvider.Factory{
        override fun <T: ViewModel> create(modelClass: Class<T>): T {
            val sheetBD = FoldersAndSheetsFirestore(currentUserUUID)
            return SheetVisualizationViewModel(sheetBD) as T
        }
    }
class SheetVisualizationViewModel(
    private val sheetBD: FoldersAndSheetsFirestore
) : ViewModel(){
    private val _musicXML = MutableLiveData<MusicXMLSheet>()
    val musicXMLSheet: LiveData<MusicXMLSheet>
        get()= _musicXML

    private val _svg = MutableLiveData<String>()
    val svg : LiveData<String>
        get() = _svg

    private val _parsingFinished = MutableLiveData(false)
    val parsingFinished: LiveData<Boolean> get() = _parsingFinished

    //private val sheetChecker: SheetChecker = SheetChecker() TODO
    private val sheetChecker: SheetChecker2 = SheetChecker2()
    private var currentNoteIndex = 0
    private var isCheckingNotes = false
    private var _noteList : MutableList<ScoreElement> = mutableListOf()

    private val _noteCheckingState = MutableLiveData<NoteCheckingState>()
    val noteCheckingState: LiveData<NoteCheckingState> get() = _noteCheckingState
    /**
     * Loads the musicxml given the sheedId and the folderId where the sheet resides
     */
    fun loadMusicSheet(dto: SheetVisualizationDto){
        viewModelScope.launch {
            _musicXML.postValue(sheetBD.getSheetById(dto.sheetId, dto.folderId))
        }

    }

    fun parseMusicXML(){
        _musicXML.value?.let {
            val parser = XMLParser(it.musicxml)
            parser.parseAllNotes()
            _noteList = parser.getAllNotes().toMutableList()
            _parsingFinished.postValue(true)
        }
    }
    fun startNoteChecking() {
        if (!isCheckingNotes) {
            isCheckingNotes = true
            currentNoteIndex = 0
            checkNextNote()
        }
    }
    private fun checkNextNote() {
        if (currentNoteIndex < _noteList.size) {
            val currentNote = _noteList[currentNoteIndex]
            val isRepeatedNote = if (currentNoteIndex > 0) {
                val previousNote = _noteList[currentNoteIndex - 1]
                // Comparar si son iguales (necesitarás implementar equals en tus clases Note)
                areNotesEqual(currentNote, previousNote)
            } else {
                false
            }
            // Resaltar nota actual
            //highlightNoteByIndex(currentNoteIndex, "#0000FF")
            _noteCheckingState.postValue(NoteCheckingState.CHECKING)

            viewModelScope.launch {
                //val isCorrect = sheetChecker.isNotePlayedCorrectly(currentNote)
                // Usar el nuevo método que maneja notas repetidas
                val isCorrect = sheetChecker.isNotePlayedCorrectlyWithOnset(
                    noteToCheck = currentNote,
                    dominancePercentage = 0.95, // 95% de dominancia
                    onsetTimeoutMs = 15000L,     // 15 segundos máximo esperando onset
                    isRepeatedNote = isRepeatedNote
                )
                when (isCorrect) {
                    true -> {
                        highlightNoteByIndex(currentNoteIndex, "#00FF00")
                        //_noteCheckingState.postValue(NoteCheckingState.NoteCorrect(currentNoteIndex))
                        currentNoteIndex++
                        delay(200)
                        checkNextNote()
                    }
                    false -> {
                        highlightNoteByIndex(currentNoteIndex, "#FF0000")
                        //_noteCheckingState.postValue(NoteCheckingState.NoteIncorrect(currentNoteIndex))
                        delay(200)
                        checkNextNote()
                    }
                    null -> {
                        highlightNoteByIndex(currentNoteIndex, "#FF0000")
                        //_noteCheckingState.postValue(NoteCheckingState.NoNoteDetected)
                        checkNextNote()
                    }
                }
            }
        } else {
            isCheckingNotes = false
            _noteCheckingState.postValue(NoteCheckingState.FINISHED)
        }
    }

    private fun areNotesEqual(note1: ScoreElement, note2: ScoreElement): Boolean {
        if (note1 is Note && note2 is Note) {
            return note1.name == note2.name &&
                    note1.octave == note2.octave
        }
        return false
    }

    private fun highlightNoteByIndex(index: Int, color: String) {
        // Find the notes
        val regex = Regex("""<g[^>]*class="[^"]*note[^"]*"[^>]*>.*?</g>""", RegexOption.DOT_MATCHES_ALL)
        val matches = regex.findAll(_svg.value!!).toList()

        if (index >= matches.size) {
            Log.d("SheetVisualizer","Índice fuera de rango: ${matches.size} notas encontradas")
        }

        val targetGroup = matches[index].value

        // Añade o reemplaza fill="color" en los elementos internos
        val modifiedGroup = targetGroup.replace(
            Regex("""(fill="[^"]*")"""),
            """fill="$color""""
        ).ifEmpty {
            // Si no tiene atributo fill, lo añadimos a cada elemento gráfico
            targetGroup.replace("<path", """<path fill="$color"""")
        }
        _svg.postValue(_svg.value!!.replace(targetGroup, modifiedGroup))
    }


    fun updateSVGValue(svgClean: String) {
        _svg.postValue(svgClean)
    }
}


enum class NoteCheckingState {
    CHECKING, FINISHED
}

class SheetVisualizationDto (
    val sheetId : String,
    val folderId : String
): Serializable

