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

    private val sheetChecker: SheetChecker2 = SheetChecker2()
    private var currentNoteIndex = 0  // Índice global absoluto
    private var isCheckingNotes = false
    private var _noteList : MutableList<ScoreElement> = mutableListOf()

    private var totalPages = 1
    private var notesPerPage = mutableMapOf<Int, Pair<Int, Int>>() // Página -> (startIndex, endIndex)

    private val _currentPage = MutableLiveData(0)
    val currentPage: LiveData<Int>
        get() = _currentPage

    private val _shouldNavigateToNextPage = MutableLiveData(false)
    val shouldNavigateToNextPage: LiveData<Boolean> get() = _shouldNavigateToNextPage

    private val _noteCheckingState = MutableLiveData<NoteCheckingState>()
    val noteCheckingState: LiveData<NoteCheckingState> get() = _noteCheckingState


    private var isCurrentPageAllSetUp = false
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
            currentNoteIndex = 0 // Reset al parsear
            //_parsingFinished.postValue(true)
        }
    }

    fun setTotalPages(pages: Int) {
        totalPages = pages
        Log.d("PAGES", "Total pages set to: $totalPages")
    }

    fun updateCurrentPage(){
        if(!isCurrentPageAllSetUp){
            isCurrentPageAllSetUp = true
            // Calcular notas en la página actual después de que se haya renderizado
            calculateNotesInCurrentPage()
            currentNoteIndex= notesPerPage[currentPage.value]?.first ?: currentNoteIndex
            _shouldNavigateToNextPage.postValue(false)
            if(noteCheckingState.value!= NoteCheckingState.CHECKING)
                checkNextNote()
        }
    }

    /**
     * Calcula y mapea las notas de la página actual
     */
    private fun calculateNotesInCurrentPage() {
        val page = currentPage.value?:1
        if (_svg.value != null) {
            val regex = Regex("""<g\b[^>]*\bclass="(note|rest)"[^>]*>""")
            val notesInPage = regex.findAll(_svg.value!!).count()

            // Si es la primera vez que visitamos esta página, mapearla
            if (!notesPerPage.containsKey(page)) {
                val startIndex = (notesPerPage[page-1]?.second ?: -1) +1
                val endIndex = startIndex + notesInPage - 1
                notesPerPage[page] = Pair(startIndex, endIndex)

                Log.d("PAGE_MAPPING", "Página $page mapeada: notas $startIndex-$endIndex (total: $notesInPage)")
            }
        }
    }

    fun moveForward(){
        Log.d("PAGING", "MOVE FORWARD")
        //_shouldNavigateToNextPage.value = false
        val currentPage = _currentPage.value!!
        val pageToBeUpdated = currentPage + 1 //pasamos a la sgte página
        if(pageToBeUpdated <= totalPages){// la pág a la que pasamos está dentro de las págs existentes
            //calculateNotesInCurrentPage(pageToBeUpdated)
            isCurrentPageAllSetUp=false
            _currentPage.value= pageToBeUpdated

            //currentNoteIndex = notesPerPage[pageToBeUpdated]!!.first
            //checkNextNote()
        }
        else
            Log.d("PAGING", "Se intentó ir de ${_currentPage.value} a " +
                    "$pageToBeUpdated when there are $totalPages num of pages")
    }

    fun moveBack(){
        //_shouldNavigateToNextPage.value = false
        val currentPage = _currentPage.value!!
        val pageToBeUpdated = currentPage - 1
        if(pageToBeUpdated > 1){
            _currentPage.value= pageToBeUpdated
            isCurrentPageAllSetUp=false
           // currentNoteIndex= notesPerPage[pageToBeUpdated]!!.first
        }

        else
            Log.d("PAGING", "Se intentó ir de ${_currentPage.value} a " +
                    "$pageToBeUpdated when there are $totalPages num of pages")
    }


    fun startNoteChecking() {
        if (!isCheckingNotes) {
            Log.d("CHECK", "STARTED NOTE CHECKING")
            isCheckingNotes = true
            currentNoteIndex = 0 // Empezar desde la primera nota
            checkNextNote()
        }
    }

    private fun checkNextNote() {

        viewModelScope.launch {
            _noteCheckingState.postValue(NoteCheckingState.CHECKING)
        while (currentNoteIndex < _noteList.size) {
            if(_shouldNavigateToNextPage.value == false || isCurrentPageAllSetUp){
                val currentNote = _noteList[currentNoteIndex]
                val isRepeatedNote = if (currentNoteIndex > 0) {
                    val previousNote = _noteList[currentNoteIndex - 1]
                    areNotesEqual(currentNote, previousNote)
                } else {
                    false
                }

                Log.d("CHECK", "Checking note index: $currentNoteIndex")


                    val isCorrect = sheetChecker.isNotePlayedCorrectlyWithOnset(
                        noteToCheck = currentNote,
                        dominancePercentage = 0.95,
                        onsetTimeoutMs = 15000L,
                        isRepeatedNote = isRepeatedNote
                    )
                    val relativeIndex = currentNoteIndex - (notesPerPage[_currentPage.value]?.first ?: 0)
                    Log.d("PAGING", "Absolute Index: $currentNoteIndex and relative Index: $relativeIndex")
                    when (isCorrect) {
                        true -> {
                            highlightNoteByIndex(relativeIndex, "#00FF00")

                            Log.d("CHECK", "Note $currentNoteIndex correct, relative index: $relativeIndex")

                            // Verificar si necesitamos cambFiar de página
                            if (needsPageChange()) {
                                handlePageCompletion()
                                currentNoteIndex++
                            } else {
                                delay(200)
                                currentNoteIndex++
                               // checkNextNote()
                            }
                        }
                        false -> {
                            highlightNoteByIndex(relativeIndex, "#FF0000")
                            delay(200)
                            //checkNextNote()
                        }
                        null -> {
                            highlightNoteByIndex(relativeIndex, "#FF0000")
                            //checkNextNote()
                        }
                    }
                }
            }
            _noteCheckingState.postValue(NoteCheckingState.FINISHED)
            Log.d("CHECK", "All notes finished!")
        }
    }

    /**
     * Verifica si necesitamos cambiar de página basado en el índice actual
     */
    private fun needsPageChange(): Boolean {
        return currentNoteIndex == notesPerPage[_currentPage.value]?.second
    }

    /**
     * Maneja la completación de una página
     */
    private fun handlePageCompletion() {
        val currentPageValue = _currentPage.value ?: 1

        if (currentPageValue < totalPages) {
            Log.d("PAGE_COMPLETION", "Página $currentPageValue completada, navegando a ${currentPageValue + 1}")
            _shouldNavigateToNextPage.postValue(true)

            //checkNextNote()
        } else {
            // Hemos terminado todas las páginas
            isCheckingNotes = false
            _noteCheckingState.postValue(NoteCheckingState.FINISHED)
            Log.d("PAGE_COMPLETION", "Todas las páginas completadas!")
        }

    }



    private fun areNotesEqual(note1: ScoreElement, note2: ScoreElement): Boolean {
        if (note1 is Note && note2 is Note) {
            return note1.name == note2.name
        }
        return false
    }

    private fun highlightNoteByIndex(index: Int, color: String) {
        val regex = Regex("""<g\s+[^>]*class="(note|rest)"[^>]*>""")
        val matches = regex.findAll(_svg.value!!).toList()

        if (index >= matches.size || index < 0) {
            Log.w("SheetVisualizer", "Índice fuera de rango: $index, notas disponibles: ${matches.size}")
            return
        }

        val targetGroup = matches[index].value

        val cleanedGroup = targetGroup
            .replace(Regex("""\s*color="#[0-9A-Fa-f]{6}""""), "")
            .replace(Regex("""\s*fill="#[0-9A-Fa-f]{6}""""), "")

        val modifiedGroup = cleanedGroup.replaceFirst(
            Regex(""">"""),
            """ color="$color" fill="$color">"""
        )
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