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

    /**
     * Returns the sheets from a folder given its folderId
     */
    fun loadSheets(){
        viewModelScope.launch {
            _sheets.postValue(folderBD.getAllSheetsFromFolder(folderId))
        }
    }
}