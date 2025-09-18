package tfg.uniovi.melodies.fragments.viewmodels

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import tfg.uniovi.melodies.repositories.UsersFirestore


class LogInViewModelProviderFactory(): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val usersBD = UsersFirestore()
        return LogInViewModel(usersBD) as T
    }
}
class LogInViewModel (private val usersBD:UsersFirestore):ViewModel(){
    private val _nickname = MutableLiveData("")
    val nickname : LiveData<String>
        get() = _nickname

    private val _userId = MutableLiveData<String?>(null)
    val userId: LiveData<String?> get() = _userId


    fun updateNickname(userId: String) {
        _nickname.value = userId
    }

    fun checkIfUserExists() {
        val id = _nickname.value?.trim()
        if (id.isNullOrEmpty()) {
            return
        }

        viewModelScope.launch {
            if(usersBD.nicknameExists(id))
                _userId.value = usersBD.getUserIdFromNickname(id)
            else
                _userId.value= null
        }
    }
}