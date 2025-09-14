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
    private val _userId = MutableLiveData("")
    val userId : LiveData<String>
        get() = _userId

    private val _userExists = MutableLiveData<Boolean>()
    val userExists: LiveData<Boolean> get() = _userExists

    private val _newUserId = MutableLiveData("")
    val newUserId : LiveData<String>
        get() = _newUserId

    fun updateUserId(userId: String) {
        _userId.value = userId
    }
    fun createAndLoadNewUser(context: Context){
        viewModelScope.launch {
            val userId = usersBD.setupUserDataIfNeeded(context)
            if(userId!=null)
                _newUserId.value=userId
        }
    }
    fun checkIfUserExists() {
        val id = _userId.value?.trim()
        if (id.isNullOrEmpty()) {
            _userExists.value = false
            return
        }

        viewModelScope.launch {
            _userExists.value = usersBD.userExists(id)
        }
    }
}