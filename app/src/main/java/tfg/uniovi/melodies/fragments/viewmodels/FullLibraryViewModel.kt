package tfg.uniovi.melodies.fragments.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import tfg.uniovi.melodies.model.Folder
import tfg.uniovi.melodies.repositories.FoldersAndSheetsFirestore

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
    val folders: LiveData<List<Folder>>
        get() = _folders

    /**
     * Returns all folders
     */
    fun loadFolders(){
        viewModelScope.launch {
            _folders.postValue(folderBD.getAllFolders())
        }
    }

    fun renameSheet(sheetId: String, folderId: String, newName: String) {
        viewModelScope.launch {
            folderBD.setNewSheetName(sheetId,folderId, newName)
        }
    }

}