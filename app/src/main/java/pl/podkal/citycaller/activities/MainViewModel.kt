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

class MainViewModel: ViewModel() {

    private val repos = FirebaseRepository()

    private val _bottomBar = MutableStateFlow(false)
    val bottomBar = _bottomBar.asStateFlow()
    fun setBottomBarVisible(bool: Boolean) {
        _bottomBar.value = bool
    }

    private val _user = MutableStateFlow(getCurrentUser())
    val user = _user.asStateFlow()

    private val _allIncidents = MutableStateFlow<List<IncidentModel>>(emptyList())
    val allIncidents = _allIncidents.asStateFlow()

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
        CoroutineScope(Dispatchers.IO ).launch {
            _user.emit(repos.loginUser(email, password))
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
}