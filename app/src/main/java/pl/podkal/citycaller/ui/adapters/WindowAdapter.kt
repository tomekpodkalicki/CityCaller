package pl.podkal.citycaller.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import pl.podkal.citycaller.databinding.CustomInfoWindowBinding

class WindowAdapter(
    private val context: Context)
    : GoogleMap.InfoWindowAdapter {

        private lateinit var binding : CustomInfoWindowBinding
    override fun getInfoContents(marker: Marker): View? {
        renderInfoWindow(marker)
        return binding.root
    }

    override fun getInfoWindow(marker: Marker): View? {
        renderInfoWindow(marker = marker)
        return binding.root
    }

    private fun renderInfoWindow(marker: Marker) {
        binding = CustomInfoWindowBinding.inflate(LayoutInflater.from(context))
        val reaction = marker.snippet
        binding.reactionTv.text = "Reakcje:${reaction ?: "0"}"
    }

}