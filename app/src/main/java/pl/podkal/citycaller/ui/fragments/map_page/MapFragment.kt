package pl.podkal.citycaller.ui.fragments.map_page

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.flow.collectLatest
import pl.podkal.citycaller.activities.MainViewModel
import pl.podkal.citycaller.databinding.CustomInfoWindowBinding
import pl.podkal.citycaller.databinding.FragmentMapBinding
import pl.podkal.citycaller.repeatedStarted
import pl.podkal.citycaller.ui.adapters.WindowAdapter
import pl.podkal.citycaller.viewLifecycleLaunch

class MapFragment : Fragment(), OnMapReadyCallback {
    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!
    private val vm by viewModels<MapViewModel>()
    private val mainVm by activityViewModels<MainViewModel>()
    private lateinit var gmap: GoogleMap

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainVm.setBottomBarVisible(true)
        setupMap(savedInstanceState)
        mainVm.loadAllIncidents()

    }

    private fun setupMap(savedInstanceState: Bundle?) {
        binding.mapView.onCreate(savedInstanceState)
        binding.mapView.onResume()
        binding.mapView.getMapAsync(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onMapReady(googleMap: GoogleMap) {
        gmap = googleMap

        val customInfoAdap = WindowAdapter(requireContext())

        gmap.setInfoWindowAdapter(customInfoAdap)

        viewLifecycleLaunch {
            repeatedStarted {
                mainVm.allIncidents.collectLatest { list ->
                    list
                        .filter { it.location?.lat != null && it.location?.lng != null}
                        .onEach { incident ->
                            val latlng = LatLng(
                                incident.location?.lat!!, incident.location?.lng!!)
                            val markerOpt = MarkerOptions()
                                .position(latlng)
                                .snippet(incident.reactions?.toString() ?: "0")

                            val marker = googleMap.addMarker(markerOpt)
                        }

                }

            }
        }
    }
}