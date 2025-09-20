package tfg.uniovi.melodies.fragments.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
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

    private val _isNicknameTaken = MutableLiveData(false)
        val isNicknameTaken : LiveData<Boolean>
        get() = _isNicknameTaken

    fun loadNickname(userId: String){
        viewModelScope.launch {
            _nickname.value=usersBD.getNicknameFromUserId(userId)
        }
    }
    fun checkNicknameAvailability(){
        viewModelScope.launch {
            _isNicknameTaken.value= _nickname.value?.let { usersBD.nicknameExists(it) }
        }
    }

    fun renameUserNewNickname(userId: String, newNickname: String){
        viewModelScope.launch {
            usersBD.updateUserNickname(userId,newNickname )
        }
    }
}