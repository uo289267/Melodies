package tfg.uniovi.melodies.fragments.viewmodels

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import tfg.uniovi.melodies.repositories.UsersFirestore

class RegisterViewModelProviderFactory: ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val usersBD = UsersFirestore()
        return RegisterViewModel(usersBD) as T
    }
}

class RegisterViewModel(private val usersBD:UsersFirestore):ViewModel() {
    private val _nickname = MutableLiveData("")
    val nickname : LiveData<String>
        get() = _nickname
    private val _newUserId = MutableLiveData("")
    val newUserId : LiveData<String>
        get() = _newUserId

    private val _userExists = MutableLiveData(false)
    val userExists : LiveData<Boolean>
        get() = _userExists
    fun updateNickname(userId: String) {
        _nickname.value = userId
    }

    fun createAndLoadNewUser(nickname: String, context: Context){
        viewModelScope.launch {
            val userId = usersBD.setupUserDataIfNeeded(nickname,context)
            _newUserId.postValue(userId)
        }
    }

    fun checkIfUserExists() {
        viewModelScope.launch {
            _userExists.postValue(_nickname.value?.let { usersBD.nicknameExists(it) })
        }
    }


}