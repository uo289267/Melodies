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

class ImportViewModelProviderFactory (
    private val currentUserUUID : UUID)
    : ViewModelProvider.Factory
{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val folderBD = UsersFirestore(currentUserUUID)
        return ImportViewModel(folderBD, currentUserUUID) as T
    }
}

class ImportViewModel(
    private val folderBD: UsersFirestore,
    private val currentUserUUID: UUID) : ViewModel(){
    private val _folders = MutableLiveData<List<Folder>>()
    val folders: LiveData<List<Folder>>
        get() = _folders

    private val _musicXMLSheetDto = MutableLiveData<MusicXMLDTO>()
    val musicXMLSheet: LiveData<MusicXMLDTO>
        get() = _musicXMLSheetDto

    /**
     * To be called before from the adapter
     */
    fun loadFolders(){
        viewModelScope.launch {
            _folders.postValue(folderBD.getAllFolders())
        }
    }
    fun storeMusicXMLstring(){

    }

    fun updateFolderChosen(folderChosen: Folder) {
       // _musicXMLSheetDto.value = _
    }

}

data class MusicXMLDTO(
    val name: String,
    val stringSheet: String,
    val author: String
    //TODO after merge folderId
)