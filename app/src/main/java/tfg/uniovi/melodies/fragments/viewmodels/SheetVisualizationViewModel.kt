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
    private var currentNoteIndex = 3
    private var isCheckingNotes = false
    private var _noteList : MutableList<ScoreElement> = mutableListOf()

    //private var currentPage = 1
    private var totalPages = 1
    private var notesInCurrentPage : Int = 0 // Page -> Range of index Notes
    private var notesPerPage = mutableMapOf<Int, IntRange>()
    private var lastProcessedNoteIndex = 0  // Último índice de nota procesado globalmente


    private val _currentPage = MutableLiveData(1)
    val currentPage: LiveData<Int>
        get() = _currentPage


    private val _shouldNavigateToNextPage = MutableLiveData(false)
    val shouldNavigateToNextPage: LiveData<Boolean> get() = _shouldNavigateToNextPage

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
            viewModelScope.launch {
                delay(500)
            }
            _parsingFinished.postValue(true)

        }
    }

    fun setTotalPages(pages: Int) {
        totalPages = pages
        calculateNotesInPage()
    }
    fun updateCurrentPage(page: Int){
        val previousPage = _currentPage.value ?: 1
        _shouldNavigateToNextPage.postValue(false)
        // Si vamos a una página nueva, actualizamos el mapeo
        calculateNotesInPage()
        //if (page != previousPage) {
            updatePageMapping(previousPage, page)
        //}
        _currentPage.value = page

        //voy para atrás
        if(previousPage > page)
            adjustNoteIndexForPage(page)
    }
    /**
     * Actualiza el mapeo de páginas cuando navegamos
     */
    private fun updatePageMapping(fromPage: Int, toPage: Int) {
        // Si estamos creando el mapeo para la página de origen por primera vez
        if (!notesPerPage.containsKey(fromPage)) {
            val startIndex = if (fromPage == 1) 0 else lastProcessedNoteIndex
            val endIndex = startIndex + notesInCurrentPage - 1

            notesPerPage[fromPage] = startIndex..endIndex
            lastProcessedNoteIndex = endIndex + 1

            Log.d("PAGE_MAPPING", "Página $fromPage mapeada: ${startIndex}..${endIndex}")
        }

        // Si vamos hacia adelante y la página destino no existe, prepararla
        if (toPage > fromPage && !notesPerPage.containsKey(toPage)) {
            // La página destino se mapeará cuando se calcule notesInCurrentPage
            Log.d("PAGE_MAPPING", "Preparando mapeo para página $toPage")
        }

        // Si vamos hacia atrás, no necesitamos actualizar nada más
    }

    /**
     * Ajusta el currentNoteIndex basado en la página actual
     */
    private fun adjustNoteIndexForPage(page: Int) {
        val pageRange = notesPerPage[page]
        if (pageRange != null) {
            // Si ya conocemos el rango de esta página, empezamos desde su inicio
            currentNoteIndex = pageRange.first
            Log.d("NOTE_INDEX", "Ajustado índice para página $page: ${currentNoteIndex}")
        } else {
            // Si no conocemos el rango, empezamos desde lastProcessedNoteIndex
            currentNoteIndex = lastProcessedNoteIndex
            Log.d("NOTE_INDEX", "Índice temporal para página $page: ${currentNoteIndex}")
        }
    }


    /**
     * Calculates the number of notes and rests that are in the rendered page currently shown
     */
    private fun calculateNotesInPage(){
        if(_svg.value!=null){
            val regex = Regex("""<g\b[^>]*\bclass="(note|rest)"[^>]*>""")
            this.notesInCurrentPage = regex.findAll(_svg.value!!).count()

            // Actualizar el mapeo para la página actual si no existe
            val currentPageValue = _currentPage.value ?: 1
            if (!notesPerPage.containsKey(currentPageValue)) {
                val startIndex = lastProcessedNoteIndex
                val endIndex = startIndex + notesInCurrentPage - 1

                notesPerPage[currentPageValue] = startIndex..endIndex

                // Solo actualizar lastProcessedNoteIndex si vamos hacia adelante
                if (currentPageValue > (notesPerPage.keys.maxOrNull() ?: 0)) {
                    lastProcessedNoteIndex = endIndex + 1
                }

                Log.d("PAGE_MAPPING", "Página $currentPageValue mapeada: ${startIndex}..${endIndex}, notas: $notesInCurrentPage")
            }
        }
    }
/*
    fun navigateToPage(page: Int) {
        if (page in 1..totalPages && page != currentPage) {
            currentPage = page
            _currentPage.postValue(currentPage)

            // Ajustar currentNoteIndex a la primera nota de la página
            val pageRange = notesPerPage[currentPage]
            if (pageRange != null && pageRange.first < _noteList.size) {
                currentNoteIndex = pageRange.first
                Log.d("PAGE_NAV", "Navegando a página $currentPage, primera nota: $currentNoteIndex")
            }
        }
    }*/
    fun startNoteChecking() {
        if (!isCheckingNotes) {
            Log.d("CHECK", "STARTED NOTE CHECKING")
            isCheckingNotes = true
            // Asegurar que empezamos desde la primera nota de la página actual
            val currentPageValue = _currentPage.value ?: 1
            val pageRange = notesPerPage[currentPageValue]
            currentNoteIndex = pageRange?.first ?: 0
            checkNextNote()
        }
    }
    private fun checkNextNote() {
        if (currentNoteIndex < _noteList.size) {
            val currentNote = _noteList[currentNoteIndex]
            val isRepeatedNote =
                if (currentNoteIndex > 0) {
                    val previousNote = _noteList[currentNoteIndex - 1]
                    // Comparar si son iguales (necesitarás implementar equals en tus clases Note)
                    areNotesEqual(currentNote, previousNote)
                } else {
                    false
                }
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
                        /*highlightNoteByIndex(currentNoteIndex, "#00FF00")
                        //_noteCheckingState.postValue(NoteCheckingState.NoteCorrect(currentNoteIndex))
                        currentNoteIndex++
                        delay(200)
                        checkNextNote()*/
                        // Usar índice relativo dentro de la página para highlighting
                        val relativeIndex = getRelativeNoteIndex(currentNoteIndex)
                        highlightNoteByIndex(relativeIndex, "#00FF00")

                        currentNoteIndex+=1

                        // Verificar si hemos completado la página actual
                        if (hasCompletedCurrentPage()) {
                            handlePageCompletion()
                        } else {
                            delay(200)
                            checkNextNote()
                        }
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
    private fun hasCompletedCurrentPage(): Boolean {
        val currentPageValue = _currentPage.value ?: 1
        val pageRange = notesPerPage[currentPageValue]

        return if (pageRange != null) {
            currentNoteIndex > pageRange.last
        } else {
            // Si no tenemos el rango, usar el conteo de notas de la página
            val pageStartIndex = if (currentPageValue == 1) 0 else lastProcessedNoteIndex - notesInCurrentPage
            currentNoteIndex >= pageStartIndex + notesInCurrentPage
        }
    }

    /**
     * Maneja la completación de una página
     */
    private fun handlePageCompletion() {
        viewModelScope.launch {
            val currentPageValue = _currentPage.value ?: 1

            if (currentPageValue < totalPages) {
                // Hay más páginas, navegar automáticamente
                Log.d("PAGE_COMPLETION", "Página $currentPageValue completada, navegando a ${currentPageValue + 1}")
                _shouldNavigateToNextPage.postValue(true)

                //delay(1000) // Pausa antes de cambiar de página

                // La navegación real se manejará desde el Fragment
                delay(500) // Tiempo para que se renderice la nueva página
                checkNextNote()
            } else {
                // Hemos terminado todas las páginas
                isCheckingNotes = false
                _noteCheckingState.postValue(NoteCheckingState.FINISHED)
            }
        }
    }
    /**
     * Obtiene el índice relativo de la nota dentro de la página actual
     */
    private fun getRelativeNoteIndex(globalIndex: Int): Int {
        val currentPageValue = _currentPage.value ?: 1
        val pageRange = notesPerPage[currentPageValue]

        return if (pageRange != null) {
            globalIndex - pageRange.first
        } else {
            // Fallback: usar el índice basado en el conteo de notas
            val pageStartIndex = if (currentPageValue == 1) 0 else lastProcessedNoteIndex - notesInCurrentPage
            globalIndex - pageStartIndex
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

        if (index >= matches.size) {
            Log.d("SheetVisualizer", "Índice fuera de rango: ${matches.size} notas encontradas")
        } else {
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

