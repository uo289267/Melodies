package tfg.uniovi.melodies.fragments.viewmodels

import android.graphics.Color
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import tfg.uniovi.melodies.entities.Colors
import tfg.uniovi.melodies.entities.Folder
import tfg.uniovi.melodies.repositories.FoldersAndSheetsFirestore

class ImportViewModelProviderFactory(
    private val currentUserUUID: String
)
    : ViewModelProvider.Factory
{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val folderBD = FoldersAndSheetsFirestore(currentUserUUID)
        return ImportViewModel(folderBD) as T
    }
}

private const val MUSICXML = "MUSICXML"

class ImportViewModel(
    private val folderBD: FoldersAndSheetsFirestore) : ViewModel(){
    private val _folders = MutableLiveData<List<Folder>>()
    val folders: LiveData<List<Folder>>
        get() = _folders

    private val _musicXMLSheetDtos = MutableLiveData<MutableList<MusicXMLDTO>>()
    val musicXMLSheets: LiveData<MutableList<MusicXMLDTO>>
        get() = _musicXMLSheetDtos

    private val _folderChosen = MutableLiveData<Folder>()
    val folderChosen: LiveData<Folder>
        get() = _folderChosen

    private val _folderChosenColor = MutableLiveData<Colors>()
    val folderChosenColor: LiveData<Colors>
        get() = _folderChosenColor
    /**
     * To be called before from the adapter
     */
    fun loadFolders(){
        viewModelScope.launch {
            _folders.postValue(folderBD.getAllFolders())
        }
    }
    fun addMusicXMLSheet(musicxml: String, title: String, author: String) {
        val currentList = _musicXMLSheetDtos.value ?: mutableListOf()
        val isMusicXMLAlreadyIn = currentList.contains(MusicXMLDTO(title, musicxml, author))

        if (!isMusicXMLAlreadyIn) {
            currentList.add(MusicXMLDTO(title, musicxml, author))
            _musicXMLSheetDtos.value = currentList
        }
    }
    fun cleanMusicXMLSheets(){
        _musicXMLSheetDtos.value = listOf<MusicXMLDTO>().toMutableList()
    }


    fun updateFolderChosen(folderChosen: Folder) {
        _folderChosen.postValue(folderChosen)
    }

    /**
     * Returns true if all musicxml have being stored, false if some or all
     */
    fun storeNewMusicXML():Boolean {
        if(musicXMLSheets.value== null)
            return false
       val ids = mutableListOf<String?>()
        viewModelScope.launch {
            for(dto in musicXMLSheets.value!!){
                dto.folderId=_folderChosen.value!!.folderId
                ids.add(folderBD.addMusicXMLSheet(dto))
                Log.d(MUSICXML, "View model sending ${dto.name} sheet to bd")
            }
        }
        for(id in ids)
            if(id == null)
                return false
        return true
    }

    fun getColorOfSelectedFolder(folderId: String){
        viewModelScope.launch {
            _folderChosenColor.value = folderBD.getFolderColor(folderId)
        }
    }
}


data class MusicXMLDTO(
    val name: String,
    val stringSheet: String,
    val author: String
){
    lateinit var folderId: String

    override fun equals(other: Any?): Boolean {
        val otherDto = other as MusicXMLDTO
        return otherDto.name == this.name && otherDto.stringSheet == this.stringSheet
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + stringSheet.hashCode()
        return result
    }
}