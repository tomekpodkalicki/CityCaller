package pl.podkal.citycaller.ui.fragments.incidents_page

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.flow.collectLatest
import pl.podkal.citycaller.R
import pl.podkal.citycaller.activities.MainViewModel
import pl.podkal.citycaller.databinding.FragmentIncidentsBinding
import pl.podkal.citycaller.repeatedStarted
import pl.podkal.citycaller.ui.adapters.IncidentdAdapter

class IncidentsFragment : Fragment() {
    private var _binding: FragmentIncidentsBinding? = null
    private val binding get() = _binding!!
    private val vm by viewModels<IncidentsViewModel>()
    private val mainVm by activityViewModels<MainViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentIncidentsBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUi()
        setupCollectors()
    }

    private fun setupCollectors() {
        repeatedStarted {  mainVm.allIncidents.collectLatest {  list ->
            val adapter = IncidentdAdapter(
                incidents = list,
                onClick = {})

            binding.incidentsRecyclerView.adapter = adapter
        }
        }

    }

    private fun setupUi() {
        binding.incidentsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}