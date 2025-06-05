package tfg.uniovi.melodies.fragments.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import tfg.uniovi.melodies.entities.Folder
import tfg.uniovi.melodies.repositories.FoldersAndSheetsFirestore
import java.util.UUID

class FullLibraryViewModelProviderFactory(
    private val currentUserUUID: String
) : ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val folderBD = FoldersAndSheetsFirestore(currentUserUUID)
        return FullLibraryViewModel(folderBD) as T
    }
}
class FullLibraryViewModel(
    private val folderBD: FoldersAndSheetsFirestore
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