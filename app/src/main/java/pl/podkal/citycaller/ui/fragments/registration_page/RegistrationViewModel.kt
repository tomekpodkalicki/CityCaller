package pl.podkal.citycaller.ui.fragments.registration_page

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pl.podkal.citycaller.FirebaseRepository

class RegistrationViewModel : ViewModel() {
    private val repos = FirebaseRepository()

    fun registerNewUserByEmail(email: String, password: String, name: String) {
        CoroutineScope(Dispatchers.IO).launch {
            repos.registerNewUserByEmail(email, password, name)
        }
    }
}