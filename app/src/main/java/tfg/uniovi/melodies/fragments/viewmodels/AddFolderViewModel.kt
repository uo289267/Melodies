package tfg.uniovi.melodies.fragments.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import tfg.uniovi.melodies.entities.Colors
import tfg.uniovi.melodies.repositories.UsersFirestore
import java.util.UUID

class AddFolderViewModelProviderFactory(
    private val currentUserUUID: UUID
): ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val folderBD = UsersFirestore(currentUserUUID)
        return AddFolderViewModel(folderBD, currentUserUUID) as T
    }
}

class AddFolderViewModel (private val folderBD: UsersFirestore,
                          private val currentUserUUID: UUID)
    : ViewModel() {


    private val _folderDTO = MutableLiveData(FolderDTO( Colors.YELLOW, ""))
    val folderDTO : LiveData<FolderDTO>
        get() = _folderDTO

    fun updateFolderName(newName: String) {
        _folderDTO.value = _folderDTO.value?.copy(name = newName)
    }
    fun updateFolderColor(newColor:Colors){
        _folderDTO.value = _folderDTO.value?.copy(color = newColor)
    }
    fun createFolder(){
        viewModelScope.launch {
            folderBD.addFolder(folderDTO.value!!)
            Log.d("FOLDER", "View model sending folderdto to bd")
        }

    }
}

data class FolderDTO(
    var color: Colors,
    var name: String
)

