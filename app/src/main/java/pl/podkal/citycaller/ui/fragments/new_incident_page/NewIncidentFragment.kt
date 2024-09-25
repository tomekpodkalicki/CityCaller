package pl.podkal.citycaller.ui.fragments.new_incident_page

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import pl.podkal.citycaller.R
import pl.podkal.citycaller.databinding.FragmentNewIncidentBinding

class NewIncidentFragment : Fragment() {
    private val vm by viewModels<NewIncidentViewModel>()
    private var _binding: FragmentNewIncidentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewIncidentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}