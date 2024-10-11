package pl.podkal.citycaller.ui.fragments.map_page

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.flow.collectLatest
import pl.podkal.citycaller.R
import pl.podkal.citycaller.activities.MainViewModel
import pl.podkal.citycaller.databinding.FragmentMapBinding
import pl.podkal.citycaller.repeatedStarted
import pl.podkal.citycaller.services.LocalizationBackgroundService
import pl.podkal.citycaller.ui.adapters.WindowAdapter

class MapFragment : Fragment(), OnMapReadyCallback {
    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!
    private val vm by viewModels<MapViewModel>()
    private val mainVm by activityViewModels<MainViewModel>()
    private lateinit var gmap: GoogleMap
    private lateinit var localizationServiceIntent: Intent

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
        setupUi()

        requireActivity().registerReceiver(localStopReceiver, IntentFilter("pl.podkal.citycaller.LOCALIZATION_STOPPED"),
            Context.RECEIVER_NOT_EXPORTED)

    }

    private fun setupUi() {
        setupLocBtn()
    }

    private fun setupLocBtn() {
        when ( mainVm.isLocalizationStarted) {
            true -> binding.startLocalizationBtn.setImageResource(R.drawable.ic_navigation_start)
            false -> binding.startLocalizationBtn.setImageResource(R.drawable.navigate_stop_img)
        }

        binding.startLocalizationBtn.setOnClickListener {
            if (mainVm.isLocalizationStarted) {
                stopLocalization()
            } else {
                if (vm.checkLocationPermissions(requireContext())) {
                    startLocalization()
                } else {
                    vm.requestLocationPermissions(requireContext())
                }
            }
            mainVm.isLocalizationStarted = !mainVm.isLocalizationStarted
        }
    }

    private fun startLocalization () {
        localizationServiceIntent = Intent(
        requireContext(),
        LocalizationBackgroundService::class.java)
        requireActivity().startService(localizationServiceIntent)
        binding.startLocalizationBtn.setImageResource(R.drawable.ic_navigation_start)

    }

    private fun stopLocalization () {
        requireActivity().stopService(localizationServiceIntent)
        binding.startLocalizationBtn.setImageResource(R.drawable.navigate_stop_img)

    }

    private fun setupMap(savedInstanceState: Bundle?) {
        binding.mapView.onCreate(savedInstanceState)
        binding.mapView.onResume()
        binding.mapView.getMapAsync(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null

        requireActivity().unregisterReceiver(localStopReceiver)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        gmap = googleMap

        val customInfoAdap = WindowAdapter(requireContext())

        gmap.setInfoWindowAdapter(customInfoAdap)

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
                            val markerId = marker?.id
                            incident.markerId = markerId

                            loadMarkerIcon(
                                marker,
                                requireContext(),
                                incident.imageUrl
                            )
                        }

                }

            }
        }

    private fun loadMarkerIcon(marker: Marker?,
                               context: Context,
                               imageUrl: String?) {
        marker?: return

        val bitmapRes = BitmapFactory.decodeResource(context.resources, R.drawable.bubble_img)
        val copyBitmap = Bitmap.createBitmap(bitmapRes.copy(Bitmap.Config.ARGB_8888, true))
        val canvas = android.graphics.Canvas(copyBitmap)

        Glide.with(context)
            .asBitmap()
            .fitCenter() //wyposrodkowac
            .load(imageUrl) //zaladuj zdjecie
            .apply(
                RequestOptions
                    .overrideOf(80,80) //nadpisz wymiary
                    .circleCrop() //zastosuj okraglanie
            )
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    canvas.drawBitmap(
                        resource,
                        copyBitmap.width / 8f,
                        copyBitmap.height / 8f,
                        Paint() //rysuje do isttniejacej bitmapy nowy pobrany obraz z imageUrL
                    )
                    val icon = BitmapDescriptorFactory.fromBitmap(copyBitmap)
                    marker.setIcon(icon)
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    TODO("Not yet implemented")
                }

            })
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == MapViewModel.PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocalization()  // Start localization if permission granted
            } else {
                // Handle the case where permission is denied
                Toast.makeText(requireContext(), "Location permission is required", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val localStopReceiver = object: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            binding.startLocalizationBtn.setImageResource(R.drawable.navigate_stop_img)
            mainVm.isLocalizationStarted = false
        }
    }
}