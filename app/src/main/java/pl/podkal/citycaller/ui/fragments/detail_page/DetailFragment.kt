package pl.podkal.citycaller.ui.fragments.detail_page

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.coroutines.flow.collectLatest
import pl.podkal.citycaller.R
import pl.podkal.citycaller.activities.MainViewModel
import pl.podkal.citycaller.data.models.IncidentModel
import pl.podkal.citycaller.databinding.FragmentDetailBinding
import pl.podkal.citycaller.databinding.FragmentIncidentsBinding
import pl.podkal.citycaller.repeatedStarted

class DetailFragment : Fragment() {

    private var _binding: FragmentDetailBinding? = null
    private val binding get() = _binding!!
    private val vm by viewModels<DetailViewModel>()
    private val mainVm by activityViewModels<MainViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailBinding.inflate(inflater,container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUi()
        setupCollectors()
    }

    private fun setupCollectors() {
        repeatedStarted {
            mainVm.selectedIncident.collectLatest { incident ->
                if (incident != null) bindData(incident)
            }
        }
    }

    private fun bindData(incident: IncidentModel) {
        Glide.with(this)
            .load(incident.imageUrl)
            .apply(RequestOptions.overrideOf(400,400))
            .into(binding.incidentPhoto)

        binding.incidentDescTv.text = incident.desc

        val reaction = "Reakcje: ${incident.reactions}"
        binding.reactionNumberTv.text = reaction

    }

    private fun setupUi() {
        binding.confirmBtn.setOnClickListener {
            val incident = mainVm.selectedIncident.value
            val updatedIncident = incident?.copy(reactions = incident.reactions!! + 1)
            if (updatedIncident != null) {
                mainVm.updateIncident(updatedIncident)
            }
            mainVm.setIncident(null)

        }

        binding.discardBtn.setOnClickListener {
            val incident = mainVm.selectedIncident.value
            val updatedIncident = incident?.copy(reactions = incident.reactions!! - 1)
            if (updatedIncident != null) {
                mainVm.updateIncident(updatedIncident)
            }
            mainVm.setIncident(null)

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}