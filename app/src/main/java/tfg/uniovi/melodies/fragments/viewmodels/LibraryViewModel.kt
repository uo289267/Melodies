package tfg.uniovi.melodies.fragments.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import tfg.uniovi.melodies.entities.MusicXMLSheet
import tfg.uniovi.melodies.repositories.FoldersAndSheetsFirestore

class LibraryViewModelProviderFactory(
    private val currentUserUUID: String,
    private val folderId: String
) : ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val folderBD = FoldersAndSheetsFirestore(currentUserUUID)
        return LibraryViewModel(folderBD, folderId) as T
    }
}

class LibraryViewModel(
    private val folderBD: FoldersAndSheetsFirestore,
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
    fun deleteSheet(sheetId: String, folderId: String) {
        viewModelScope.launch {
            val currentList = _sheets.value?.toMutableList() ?: return@launch
            val sheet = currentList.firstOrNull { it.id == sheetId && it.folderId == folderId }
                ?: return@launch

            folderBD.deleteSheet(sheet.id, sheet.folderId)
            currentList.remove(sheet)
            _sheets.postValue(currentList)
        }
    }

    fun deleteSheetAt(position: Int) {
        val currentList = _sheets.value ?: return
        val sheet = currentList.getOrNull(position) ?: return
        deleteSheet(sheet.id, sheet.folderId)
    }

    fun renameSheet(sheetId: String, folderId: String, newName: String){
        viewModelScope.launch {
            folderBD.setNewSheetName(sheetId,folderId, newName)
        }
    }
    fun isSheetNameAvailable(name: String, folderId: String): LiveData<Result<Boolean>> = liveData {
            emit(Result.Loading)
            try {
                val taken = folderBD.isSheetNameInUse(name, folderId)
                emit(Result.Success(!taken!!))
            } catch (e: Exception) {
                emit(Result.Error(e))
            }

    }

}