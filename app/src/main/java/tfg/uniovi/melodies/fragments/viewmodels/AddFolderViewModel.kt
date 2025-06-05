package tfg.uniovi.melodies.fragments.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import tfg.uniovi.melodies.entities.Colors
import tfg.uniovi.melodies.repositories.FoldersAndSheetsFirestore
import java.util.UUID

class AddFolderViewModelProviderFactory(
    private val currentUserUUID: String
): ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val folderBD = FoldersAndSheetsFirestore(currentUserUUID)
        return AddFolderViewModel(folderBD) as T
    }
}

class AddFolderViewModel (private val folderBD: FoldersAndSheetsFirestore)
    : ViewModel() {


    private val _folderDTO = MutableLiveData(FolderDTO( Colors.YELLOW, ""))
    val folderDTO : LiveData<FolderDTO>
        get() = _folderDTO

    private val _folderNameExists = MutableLiveData<Boolean>()
    val folderNameExists : LiveData<Boolean>
        get() = _folderNameExists
    fun updateFolderName(newName: String) {
        _folderDTO.value = _folderDTO.value?.copy(name = newName)
    }
    fun updateFolderColor(newColor:Colors){
        _folderDTO.value = _folderDTO.value?.copy(color = newColor)
    }
    fun createFolder(){
        viewModelScope.launch {
            folderBD.addFolder(_folderDTO.value!!)
            Log.d("FOLDER", "View model sending folderdto to bd")
        }
    }

     fun checkIfFolderNameExists(){
         val folderName = _folderDTO.value?.name?.trim()
         if(folderName.isNullOrEmpty()){
             _folderNameExists.value = false
             return
         }
         viewModelScope.launch{
             _folderNameExists.value = folderBD.isFolderNameInUse(folderName)
         }
     }
}

data class FolderDTO(
    var color: Colors,
    var name: String
)

