package tfg.uniovi.melodies.fragments.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import tfg.uniovi.melodies.model.HistoryEntry
import tfg.uniovi.melodies.model.MusicXMLSheet
import tfg.uniovi.melodies.model.notes.ScoreElement
import tfg.uniovi.melodies.repositories.FoldersAndSheetsFirestore
import tfg.uniovi.melodies.processing.SheetChecker
import tfg.uniovi.melodies.processing.parser.XMLParser
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

private const val PAGE_MAPPING = "PAGE_MAPPING"
private const val PAGES = "PAGES"
const val PAGING = "PAGING"
const val CHECK = "CHECK"

private const val COLOR_FOR_WRONG = "#ed6b11"  //"#FF0000" //red
private const val COLOR_FOR_RIGHT = "#C7C8CA"//"#00FF00" //green

private const val PAGE_COMPLETION = "PAGE_COMPLETION"
private const val SHEET_VISUALIZER = "SheetVisualizer"

class SheetVisualizationViewModel(
    private val sheetBD: FoldersAndSheetsFirestore
) : ViewModel(){
    private val _musicXML = MutableLiveData<MusicXMLSheet>()
    val musicXMLSheet: LiveData<MusicXMLSheet>
        get()= _musicXML

    var hasMusicXMLSheetChanged = false

    private val _svg = MutableLiveData("")
    val svg : LiveData<String>
        get() = _svg


    private val sheetChecker: SheetChecker = SheetChecker()
    private var currentNoteIndex = 0  // Absolute index
    private var isCheckingNotes = false
    private var _noteList : MutableList<ScoreElement> = mutableListOf()

    private var totalPages = 1
    private var notesPerPage = mutableMapOf<Int, Pair<Int, Int>>()

    private val _currentPage = MutableLiveData(0)
    val currentPage: LiveData<Int>
        get() = _currentPage

    private val _shouldNavigateToNextPage = MutableLiveData(false)
    val shouldNavigateToNextPage: LiveData<Boolean> get() = _shouldNavigateToNextPage

    private val _noteCheckingState = MutableLiveData(NoteCheckingState.NONE)
    val noteCheckingState: LiveData<NoteCheckingState> get() = _noteCheckingState

    private var startTime: Long = 0L
    private val _elapsedTimeMs = MutableLiveData<Long>()
    val elapsedTimeMs: LiveData<Long> get() = _elapsedTimeMs


    private var isCurrentPageAllSetUp = false

    /**
     * Loads the musicxml given the sheedId and the folderId where the sheet resides
     */
    fun loadMusicSheet(dto: SheetVisualizationDto){
        viewModelScope.launch {
            val givenMusicXMLSheetViaFirebase = sheetBD.getSheetById(dto.sheetId, dto.folderId)
            givenMusicXMLSheetViaFirebase?.let{
                hasMusicXMLSheetChanged = it != _musicXML.value
                if (hasMusicXMLSheetChanged)
                     _musicXML.postValue(it)
            }
        }
    }

    fun parseMusicXML(){
        _musicXML.value?.let {
            val parser = XMLParser(it.musicxml)
            parser.parseAllNotes()
            _noteList = parser.getAllNotes().toMutableList()
            if(_svg.value!!.isEmpty())
                currentNoteIndex=0
        }
    }

    fun setTotalPages(pages: Int) {
        totalPages = pages
        Log.d(PAGES, "Total pages set to: $totalPages")
    }

    fun updateCurrentPage(){
        if(!isCurrentPageAllSetUp){
            isCurrentPageAllSetUp = true
            calculateNotesInCurrentPage()
            notesPerPage[currentPage.value]?.let{
                //has page changed? (going forward or backward)
                if(currentNoteIndex < it.first || currentNoteIndex > it.second )
                    currentNoteIndex = it.first
            }
            _shouldNavigateToNextPage.postValue(false)
            if(noteCheckingState.value == NoteCheckingState.NONE )
                checkNextNote()
        }
    }


    /**
     * Calculates and maps into the notesPerPage map the notes in current page
     */
    private fun calculateNotesInCurrentPage() {
        val page = currentPage.value?:1
        if (_svg.value != null) {
            val regex = Regex("""<g\b[^>]*\bclass="(note|rest)"[^>]*>""")
            val notesInPage = regex.findAll(_svg.value!!).count()

            // Map the num of notes if we are visiting the page for the first time
            if (!notesPerPage.containsKey(page)) {
                val startIndex = (notesPerPage[page-1]?.second ?: -1) +1
                val endIndex = startIndex + notesInPage - 1
                notesPerPage[page] = Pair(startIndex, endIndex)

                Log.d(PAGE_MAPPING, "Página $page mapeada: notas $startIndex-$endIndex (total: $notesInPage)")
            }
        }
    }

    fun moveForward(){
        val currentPage = _currentPage.value!!
        val pageToBeUpdated = currentPage + 1
        if(pageToBeUpdated <= totalPages){
            isCurrentPageAllSetUp=false
            _currentPage.value= pageToBeUpdated
        }
        else
            Log.d(PAGING, "Se intentó ir de ${_currentPage.value} a " +
                    "$pageToBeUpdated when there are $totalPages num of pages")
    }

    fun moveBack(){
        val currentPage = _currentPage.value!!
        val pageToBeUpdated = currentPage - 1
        if(pageToBeUpdated >= 1){
            isCurrentPageAllSetUp=false
            _currentPage.value= pageToBeUpdated
        }

        else
            Log.d(PAGING, "Se intentó ir de ${_currentPage.value} a " +
                    "$pageToBeUpdated when there are $totalPages num of pages")
    }


    private fun checkNextNote() {
        viewModelScope.launch(Dispatchers.Default) {
            _noteCheckingState.postValue(NoteCheckingState.CHECKING)
            startTime = System.currentTimeMillis()
            while (currentNoteIndex < _noteList.size) {
                if(_shouldNavigateToNextPage.value == false || isCurrentPageAllSetUp){
                    val currentNote = _noteList[currentNoteIndex]
                    val currentPageWhenCurrentNote = _currentPage.value

                    Log.d(CHECK, "Checking note index: $currentNoteIndex")

                    val isCorrect = sheetChecker.isNotePlayedCorrectlyWithOnset(
                        noteToCheck = currentNote,
                        dominancePercentage = 0.95,
                        onsetTimeoutMs = 15000L
                    )
                    val relativeIndex = currentNoteIndex - (notesPerPage[_currentPage.value]?.first ?: 0)
                    Log.d(PAGING, "Absolute Index: $currentNoteIndex and relative Index: $relativeIndex")
                    if(_currentPage.value!=currentPageWhenCurrentNote){
                        continue
                    }
                    when (isCorrect) {
                        true -> {
                            highlightNoteByIndex(relativeIndex, COLOR_FOR_RIGHT)

                            Log.d(CHECK, "Note $currentNoteIndex correct, " +
                                    "relative index: $relativeIndex")

                            // Verify if we need to change page
                            if (needsPageChange()) {
                                handlePageCompletion()
                                currentNoteIndex++
                            } else {
                                delay(50)
                                currentNoteIndex++
                            }
                            delay(200)
                        }
                        false -> {
                            highlightNoteByIndex(relativeIndex, COLOR_FOR_WRONG)
                            delay(50)
                        }
                    }
                    }
                }
            if(!_noteCheckingState.equals(NoteCheckingState.NONE)){
                _elapsedTimeMs.postValue(System.currentTimeMillis() - startTime)
                _noteCheckingState.postValue(NoteCheckingState.FINISHED)
                Log.d(CHECK, "All notes finished! Total: ${_elapsedTimeMs.value?.div(1000.0)} segundos")
            }
        }
    }

    /**
     * Verifies if there is a need to change the page
     */
    private fun needsPageChange(): Boolean {
        return currentNoteIndex == notesPerPage[_currentPage.value]?.second
    }

    /**
     * Manages page completion meaning all notes have been played correctly
     */
    private fun handlePageCompletion() {
        val currentPageValue = _currentPage.value ?: 1

        if (currentPageValue < totalPages) {
            Log.d(PAGE_COMPLETION, "Página $currentPageValue completada, navegando a ${currentPageValue + 1}")
            _shouldNavigateToNextPage.postValue(true)
        } else {
            // Finished all pages
            isCheckingNotes = false
            Log.d(PAGE_COMPLETION, "Todas las páginas completadas!")
        }

    }

    private fun highlightNoteByIndex(index: Int, color: String) {

        val regex = Regex("""<g\s+[^>]*class="(note|rest)"[^>]*>""")
        val matches = regex.findAll(_svg.value!!).toList()

        if (index >= matches.size || index < 0) {
            Log.w(SHEET_VISUALIZER, "Índice fuera de rango: $index, notas disponibles: ${matches.size}")
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
        if(svgClean != "null")
            _svg.postValue(svgClean)
    }

    fun updateNoteCheckingState(state : NoteCheckingState){
        _noteCheckingState.postValue(state)
    }

    fun saveNewHistoryEntry(historyEntry: HistoryEntry) {
        viewModelScope.launch {
            sheetBD.saveNewHistoryEntry(historyEntry)
        }
    }
}

enum class NoteCheckingState {
    CHECKING, FINISHED, NONE, NOT_AVAILABLE
}

class SheetVisualizationDto (
    val sheetId : String,
    val folderId : String
): Serializable