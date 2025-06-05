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

class FolderViewModelProviderFactory(
    private val currentUserId : String
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val folderBD = FoldersAndSheetsFirestore(currentUserId)
        return FolderViewModel(folderBD) as T
    }
}

class FolderViewModel(
    private val folderBD: FoldersAndSheetsFirestore) : ViewModel(){

    private val _folders = MutableLiveData<List<Folder>>()
    val folders: LiveData<List<Folder>>
        get() = _folders

    /**
     * To be called before from the adapter
     */
    fun loadFolders(){
        viewModelScope.launch {
            _folders.postValue(folderBD.getAllFolders())
        }
    }

    /**
     * Deletes the folder from the bd and the folders list
     *
     * @param folderId the id of the folder to be deleted
     */
    fun deleteFolder(folderId: String) {
        viewModelScope.launch {
            folderBD.deleteFolder(folderId)

            val updatedList = _folders.value?.filter { it.folderId != folderId }
            _folders.postValue(updatedList!!)
        }
    }


}