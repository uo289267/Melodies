package tfg.uniovi.melodies.fragments.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import tfg.uniovi.melodies.model.Folder
import tfg.uniovi.melodies.repositories.FoldersAndSheetsFirestore

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

    /**
     * Renames the folder given its id
     *
     * @param folderId the id of the folder to rename
     * @param newName the new name of the folder
     */
    fun renameFolder(folderId: String, newName: String){
        viewModelScope.launch {
            folderBD.setNewFolderName(folderId, newName)
        }
        loadFolders()
    }

    /**
     * Checks availability of the new folder name
     *
     */
    fun isFolderNameAvailable(name: String): LiveData<Result<Boolean>> = liveData {
        emit(Result.Loading)
        try {
            val taken = folderBD.isFolderNameInUse(name)
            emit(Result.Success(!taken!!))
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }

}
sealed class Result<out T> {
    data object Loading : Result<Nothing>()
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val exception: Throwable) : Result<Nothing>()
}
