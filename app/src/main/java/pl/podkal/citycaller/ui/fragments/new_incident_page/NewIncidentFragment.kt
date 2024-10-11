package pl.podkal.citycaller.ui.fragments.new_incident_page

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.collectLatest
import pl.podkal.citycaller.R
import pl.podkal.citycaller.activities.MainViewModel
import pl.podkal.citycaller.data.models.LocationModel
import pl.podkal.citycaller.databinding.FragmentNewIncidentBinding
import pl.podkal.citycaller.repeatedStarted

class NewIncidentFragment : Fragment() {
    private val vm by viewModels<NewIncidentViewModel>()
    private var _binding: FragmentNewIncidentBinding? = null
    private val binding get() = _binding!!
    private val MainVm by activityViewModels<MainViewModel>()

    private lateinit var locationRequest: LocationRequest
    private lateinit var susedLocation: FusedLocationProviderClient
    private val db = FirebaseFirestore.getInstance()
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)

            val lastLocationModel = LocationModel(
                locationResult.lastLocation?.longitude,
                locationResult.lastLocation?.latitude
            )
            vm.updateLastLocation(lastLocationModel)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewIncidentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        startLocalization()
        setupCollectors()
        setupOnCLicks()
    }

    private fun setupCollectors() {
            repeatedStarted {
                vm.lastLocation.collectLatest {
                    if(it != null) vm.setButtonEnalbed(true)
                }
            }

    }

    private fun setupOnCLicks() {
        binding.addPhotoBtn.setOnClickListener {
            findNavController().navigate(R.id.action_newIncidentFragment_to_photoFragment)
        }

        binding.addNewIncidentBtn.setOnClickListener {
           if(MainVm.photoPath.isNotBlank())
            addNewIncident(MainVm.photoPath)
        }
    }

    private fun addNewIncident(path: String) {
        startLocalization()

        val userId = MainVm.user.value?.uid ?: throw Exception("User is null!")
        val desc = binding.incidentDescEt.text.toString()

    }

    private fun startLocalization() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            throw Exception("No permission")
        }
        susedLocation.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.myLooper()
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}