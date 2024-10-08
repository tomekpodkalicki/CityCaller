package pl.podkal.citycaller.ui.fragments.start_page

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import pl.podkal.citycaller.R
import pl.podkal.citycaller.activities.MainViewModel
import pl.podkal.citycaller.databinding.FragmentStartBinding

class StartFragment : Fragment() {
    private var _binding: FragmentStartBinding? = null
    private val binding get() = _binding!!
    private val vm by viewModels<StartViewModel>()
    private val mainVm by activityViewModels<MainViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStartBinding.inflate(inflater,container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUi()
        /*mainVm.signOut()*/
    }

    private fun setupUi() {
        binding.loginBtn.setOnClickListener{
           findNavController().navigate(R.id.action_startFragment_to_loginFragment)
        }

        binding.registrationBtn.setOnClickListener{
            findNavController().navigate(R.id.action_startFragment_to_registrationFragment)
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}