package pl.podkal.citycaller.ui.fragments.incidents_page

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import pl.podkal.citycaller.R

class IncidentsFragment : Fragment() {

    companion object {
        fun newInstance() = IncidentsFragment()
    }

    private val viewModel: IncidentsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_incidents, container, false)
    }
}