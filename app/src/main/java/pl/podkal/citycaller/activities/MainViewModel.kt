package pl.podkal.citycaller.activities

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pl.podkal.citycaller.data.models.IncidentModel
import pl.podkal.citycaller.data.repository.FirebaseRepository
import java.io.File

sealed class LoginState {
    object Loading: LoginState()
    data class Success(val user: FirebaseUser) : LoginState()
    object Failure: LoginState()
}

class MainViewModel: ViewModel() {

    var photoPath = ""

    var isLocalizationStarted = false

    private val repos = FirebaseRepository()

    private val _loginState = MutableStateFlow<LoginState?>(null)
    val loginState = _loginState.asStateFlow()

    private val _bottomBar = MutableStateFlow(false)
    val bottomBar = _bottomBar.asStateFlow()
    fun setBottomBarVisible(bool: Boolean) {
        _bottomBar.value = bool
    }

    private val _user = MutableStateFlow(getCurrentUser())
    val user = _user.asStateFlow()


    private val _allIncidents = MutableStateFlow<List<IncidentModel>>(emptyList())
    val allIncidents = _allIncidents.asStateFlow()

    private val _selectedIncident: MutableStateFlow<IncidentModel?> = MutableStateFlow(null)
    val selectedIncident = _selectedIncident.asStateFlow()

    fun setIncident(incident: IncidentModel?) {
        _selectedIncident.value = incident
    }

    private fun getCurrentUser():
            FirebaseUser? = repos.getCurrentUser()

    fun registerNewUserByEmail(name: String,
                               email: String,
                               password: String){
       CoroutineScope(Dispatchers.IO).launch {
           val newUser =  repos.registerNewUserByEmail(email = email,
               password = password,
               name = name)

           _user.emit(newUser)
       }

    }

    fun loginUser(
        email: String,
        password: String) {
        viewModelScope.launch {
            _loginState.emit(LoginState.Loading)
            try {
                val user = repos.loginUser(email, password)
                if (user != null ) {
                    _loginState.emit(LoginState.Success(user))
                } else { _loginState.emit(LoginState.Failure)
                }
            } catch (e: Exception) {
                _loginState.emit(LoginState.Failure)
            }
        }
    }

    fun loadAllIncidents() {
        viewModelScope.launch {
            _allIncidents.update {
                repos.getAllIncidents()
            }
        }
    }

    fun getUserIncidents(userId: String?): Flow<List<IncidentModel>>{
        return flow {
            emit(repos.getUserIncidents(userId))
        }
    }

    fun insertIncident(incidentModel: IncidentModel, path: String){
        CoroutineScope(Dispatchers.IO).launch {
            val file = File(path)
            repos.uploadIncidentData(incidentModel, file)
        }
    }

    fun signOut() {
        repos.singOut()
    }

    fun updateIncident(incidentModel: IncidentModel) {
        incidentModel ?: return
        viewModelScope.launch {
                repos.updateIncident(incidentModel)
        }
    }


}