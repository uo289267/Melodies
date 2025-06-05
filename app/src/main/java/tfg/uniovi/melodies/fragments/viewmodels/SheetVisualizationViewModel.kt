package tfg.uniovi.melodies.fragments.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import tfg.uniovi.melodies.entities.MusicXMLSheet
import tfg.uniovi.melodies.entities.notes.ScoreElement
import tfg.uniovi.melodies.repositories.FoldersAndSheetsFirestore
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

    private val _correctlyPlayedNotesCounter = MutableLiveData<Int>()
    val correctlyPlayedNotesCounter : LiveData<Int>
        get()= _correctlyPlayedNotesCounter

    private var _noteList : MutableList<ScoreElement> = mutableListOf()
    /**
     * Loads the musicxml given the sheedId and the folderId where the sheet resides
     */
    fun loadMusicSheet(dto: SheetVisualizationDto){
        viewModelScope.launch {
            _musicXML.postValue(sheetBD.getSheetById(dto.sheetId, dto.folderId))
        }

    }
    /**
     * Updates counter when a note has been played correctly
     */
    fun updateCounter(){
        _correctlyPlayedNotesCounter.postValue(_correctlyPlayedNotesCounter.value!!+1)
    }

    fun parseMusicXML(){
        _musicXML.value?.let {
            val parser = XMLParser(it.musicxml)
            parser.parseAllNotes()
            _noteList = parser.getAllNotes().toMutableList()
        }

    }
}

class SheetVisualizationDto (
    val sheetId : String,
    val folderId : String
): Serializable

