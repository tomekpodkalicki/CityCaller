package pl.podkal.citycaller.ui.fragments.registration_page

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.flow.collectLatest
import pl.podkal.citycaller.R
import pl.podkal.citycaller.activities.MainViewModel
import pl.podkal.citycaller.databinding.FragmentRegistrationBinding
import pl.podkal.citycaller.repeatedStarted
import pl.podkal.citycaller.viewLifecycleLaunch


class RegistrationFragment : Fragment() {
    private var _binding: FragmentRegistrationBinding? = null
    private val binding get() = _binding!!
    private val vm by viewModels<RegistrationViewModel>()
    private val mainVm by activityViewModels<MainViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegistrationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
       setupUi()
        setupCollectors()

    }

    private fun setupCollectors() {
        viewLifecycleLaunch {
            repeatedStarted {
                mainVm.user.collectLatest { user ->
                    if (user!= null) {
                        Log.d("REG_D", "${user.email}")
                        Log.d("REG_D", "${user.uid}")
                       findNavController().navigate(R.id.action_registrationFragment_to_loginFragment)
                    }
                }
            }
        }
    }

    private fun setupUi() {
        binding.loginBtn.setOnClickListener{
            findNavController().navigate(R.id.action_registrationFragment_to_loginFragment)
        }
        binding.registerBtn.setOnClickListener{
            val name = binding.registerNameTv.text.toString()
            val email = binding.registerEmailTv.text.toString()
            val passwo = binding.registerPassTv.text.toString()

            mainVm.registerNewUserByEmail(name = name,
                email = email,
                password = passwo)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}