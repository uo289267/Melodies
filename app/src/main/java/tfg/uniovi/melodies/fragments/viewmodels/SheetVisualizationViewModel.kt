package tfg.uniovi.melodies.fragments.viewmodels

import android.os.Parcelable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import tfg.uniovi.melodies.entities.MusicXMLSheet
import tfg.uniovi.melodies.repositories.UsersFirestore
import java.io.Serializable
import java.util.UUID

class SheetVisualizationViewModelFactory (
    private val currentUserUUID : UUID):
    ViewModelProvider.Factory{
        override fun <T: ViewModel> create(modelClass: Class<T>): T {
            val sheetBD = UsersFirestore(currentUserUUID)
            return SheetVisualizationViewModel(sheetBD) as T
        }
    }
class SheetVisualizationViewModel(
    private val sheetBD: UsersFirestore
) : ViewModel(){
    private val _musicXML = MutableLiveData<MusicXMLSheet>()
    val musicXMLSheet: LiveData<MusicXMLSheet>
        get()= _musicXML

    /**
     * Loads the musicxml given the sheedId and the folderId where the sheet resides
     */
    fun loadMusicSheet(dto: SheetVisualizationDto){
        viewModelScope.launch {
            _musicXML.postValue(sheetBD.getSheetById(dto.sheetId, dto.folderId))
        }
    }
}

class SheetVisualizationDto (
    val sheetId : String,
    val folderId : String
): Serializable

