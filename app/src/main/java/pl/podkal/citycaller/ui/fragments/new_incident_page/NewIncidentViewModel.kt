package pl.podkal.citycaller.ui.fragments.new_incident_page

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import pl.podkal.citycaller.data.models.IncidentModel
import pl.podkal.citycaller.data.models.LocationModel

class NewIncidentViewModel : ViewModel() {
    private val _lastLocation = MutableStateFlow<LocationModel?>(null)
    val lastLocation = _lastLocation.asStateFlow()

    fun updateLastLocation(last: LocationModel?) {
        _lastLocation.value = last

    }

    private val _isInsertButtonEnabled = MutableStateFlow(false)
    val isInsertButtonEnabled = _isInsertButtonEnabled.asStateFlow()

    fun setButtonEnalbed(boolean: Boolean) {
        _isInsertButtonEnabled.value = boolean

    }

}