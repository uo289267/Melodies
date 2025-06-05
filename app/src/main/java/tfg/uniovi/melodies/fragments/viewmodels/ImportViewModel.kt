package tfg.uniovi.melodies.fragments.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import tfg.uniovi.melodies.entities.Folder
import tfg.uniovi.melodies.repositories.FoldersAndSheetsFirestore
import java.util.UUID

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
                dto.folderId=folderChosen.value!!.folderId
                ids.add(folderBD.addMusicXMLSheet(dto))
                Log.d("MUSICXML", "View model sending ${dto.name} sheet to bd")
            }
        }
        for(id in ids)
            if(id == null)
                return false
        return true
    }
}


data class MusicXMLDTO(
    val name: String,
    val stringSheet: String,
    val author: String
){
    lateinit var folderId: String

    override fun equals(other: Any?): Boolean {
        val other = other as MusicXMLDTO
        return other.name.equals(this.name) && other.stringSheet.equals(this.stringSheet)
    }
}