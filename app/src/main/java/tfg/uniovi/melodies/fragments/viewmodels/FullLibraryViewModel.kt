package tfg.uniovi.melodies.fragments.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import tfg.uniovi.melodies.entities.Folder
import tfg.uniovi.melodies.entities.MusicXMLSheet
import tfg.uniovi.melodies.repositories.UsersFirestore
import java.util.UUID

class FullLibraryViewModelProviderFactory (
    private val currentUserUUID : UUID
) : ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val folderBD = UsersFirestore(currentUserUUID)
        return FullLibraryViewModel(folderBD, currentUserUUID) as T
    }
}
class FullLibraryViewModel(
    private val folderBD: UsersFirestore,
    private val currentUserUUID: UUID
) : ViewModel(){
    private val _folders = MutableLiveData<List<Folder>>()
    val folder: LiveData<List<Folder>>
        get() = _folders

    /**
     * Returns all folders
     */
    fun loadFolders(){
        viewModelScope.launch {
            _folders.postValue(folderBD.getAllFolders())
        }
    }
}