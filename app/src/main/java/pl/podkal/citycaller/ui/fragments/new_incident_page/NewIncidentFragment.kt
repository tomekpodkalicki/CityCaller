package pl.podkal.citycaller.ui.fragments.new_incident_page

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.collectLatest
import pl.podkal.citycaller.R
import pl.podkal.citycaller.activities.MainViewModel
import pl.podkal.citycaller.data.models.IncidentModel
import pl.podkal.citycaller.data.models.LocationModel
import pl.podkal.citycaller.databinding.FragmentNewIncidentBinding
import pl.podkal.citycaller.repeatedStarted

class NewIncidentFragment : Fragment() {
    private val vm by viewModels<NewIncidentViewModel>()
    private var _binding: FragmentNewIncidentBinding? = null
    private val binding get() = _binding!!
    private val MainVm by activityViewModels<MainViewModel>()

    private var locationRequest: LocationRequest = LocationRequest.create().apply {
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }
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

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        susedLocation = LocationServices.getFusedLocationProviderClient(requireContext())
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
                if (it != null) {
                    Log.d("NewIncidentFragment", "Location received: $it")
                    vm.setButtonEnalbed(true)
                }
            }
        }

        repeatedStarted {
            vm.isInsertButtonEnabled.collectLatest {
                Log.d("NewIncidentFragment", "Insert button enabled: $it")
                binding.addNewIncidentBtn.isEnabled = it
            }
        }
    }

    private fun setupOnCLicks() {
        binding.addPhotoBtn.setOnClickListener {
            findNavController().navigate(R.id.action_newIncidentFragment_to_photoFragment)
        }

        binding.addNewIncidentBtn.setOnClickListener {
            val lastLocation = vm.lastLocation.value
            if (lastLocation == null) {
                Log.d("NewIncidentFragment", "Location not available, can't add incident")
            } else {
                Log.d("NewIncidentFragment", "Location available: $lastLocation")
                if (MainVm.photoPath.isNotBlank())
                    addNewIncident(MainVm.photoPath)
                else
                    Log.d("NewIncidentFragment", "Photo path is blank")
            }
        }
    }



    private fun addNewIncident(path: String) {
        val userId = MainVm.user.value?.uid ?: throw Exception("User is null!")
        val desc = binding.incidentDescEt.text.toString()

        val lastLocation = vm.lastLocation.value
        if (lastLocation == null) {
            Log.d("NewIncidentFragment", "Location not found, cannot add incident")
            return
        }

        if (desc.isBlank()) {
            Log.d("NewIncidentFragment", "Description cannot be empty")
            return
        }

        val incident = IncidentModel(
            userId = userId,
            desc = desc,
            location = lastLocation,
            imageUrl = null,
            reactions = 0
        )

        MainVm.insertIncident(incident, path)
        Log.d("NewIncidentFragment", "Incident added with path: $path")
        exitFragment()
    }

    private fun exitFragment() {
        Log.d("NewIncidentFragment", "Navigating to mapFragment")
        requireActivity().runOnUiThread {
            findNavController().navigate(
                R.id.mapFragment,
                null,
                navOptions {
                    popUpTo(R.id.mapFragment) {
                        inclusive = true
                    }
                }
            )
        }
    }

    private fun checkLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        requestPermissions(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    private fun startLocalization() {
        if (checkLocationPermission()) {
            susedLocation.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.myLooper()
            )
        } else {
            requestLocationPermission()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super. onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted, start localization
                startLocalization()
            } else {
                // Permission denied, show some message to the user
                Log.e("NewIncidentFragment", "Permission denied for location")
                throw Exception("No permission to access location")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
