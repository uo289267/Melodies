package tfg.uniovi.melodies.fragments.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import tfg.uniovi.melodies.model.HistoryEntry
import tfg.uniovi.melodies.repositories.UsersFirestore

class ProfileViewModelProviderFactory: ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val usersBD = UsersFirestore()
        return ProfileViewModel(usersBD) as T
    }
}

class ProfileViewModel(private val usersBD: UsersFirestore):ViewModel(){
    private val _nickname = MutableLiveData("")
    val nickname : LiveData<String>
        get() = _nickname


    private val _historyEntries = MutableLiveData<List<HistoryEntry>>()
    val historyEntries: LiveData<List<HistoryEntry>> = _historyEntries
    fun loadNickname(userId: String){
        viewModelScope.launch {
            _nickname.value=usersBD.getNicknameFromUserId(userId)
        }
    }


    fun renameUserNewNickname(userId: String, newNickname: String) {

        viewModelScope.launch {
            try {
                usersBD.updateUserNickname(userId, newNickname) // suspend call
                _nickname.postValue(newNickname)
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error renaming nickname", e)
            }
        }
    }

    fun loadHistoryEntries(userId: String) {
        viewModelScope.launch {
            val entries = usersBD.getAllHistoryEntries(userId) // Las 5 más recientes
            _historyEntries.postValue(entries)

        }
    }

    fun isNicknameAvailable(nickname: String): LiveData<Result<Boolean>> = liveData {
        emit(Result.Loading)
        try {
            val taken = usersBD.nicknameExists(nickname) // suspend
            emit(Result.Success(!taken))
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }
}