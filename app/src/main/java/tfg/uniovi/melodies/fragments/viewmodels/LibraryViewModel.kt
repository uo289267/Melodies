package tfg.uniovi.melodies.fragments.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import tfg.uniovi.melodies.entities.MusicXMLSheet
import tfg.uniovi.melodies.repositories.UsersFirestore
import java.util.UUID

class LibraryViewModelProviderFactory(
    private val currentUserUUID : UUID,
    private val folderId: String
) : ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val folderBD = UsersFirestore(currentUserUUID)
        return LibraryViewModel(folderBD, currentUserUUID, folderId) as T
    }
}

class LibraryViewModel(
    private val folderBD: UsersFirestore,
    private val currentUserUUID: UUID,
    private val folderId :String
) : ViewModel(){
    private val _sheets = MutableLiveData<List<MusicXMLSheet>>()
    val sheets: LiveData<List<MusicXMLSheet>>
        get() = _sheets

    private val _folderName = MutableLiveData<String>()
    val folderName: LiveData<String>
        get() = _folderName

    /**
     * Returns the sheets from a folder based on its folderId
     */
    fun loadSheets(){
        viewModelScope.launch {
            _sheets.postValue(folderBD.getAllSheetsFromFolder(folderId))
        }
    }

    /**
     * Loads the folder name based on its folderId
     */
    fun loadFolderName() {
        viewModelScope.launch {
            val name = folderBD.getFolderById(folderId)?.name ?: ""
            _folderName.postValue(name)
        }
    }

    fun deleteSheetAt(position: Int) {
        viewModelScope.launch {
            folderBD.deleteSheet(sheets.value!![position].id,folderId )
        }
    }
}