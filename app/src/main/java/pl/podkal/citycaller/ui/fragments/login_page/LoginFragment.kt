package pl.podkal.citycaller.ui.fragments.login_page

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import pl.podkal.citycaller.R
import pl.podkal.citycaller.activities.MainViewModel
import pl.podkal.citycaller.databinding.FragmentLoginBinding
import pl.podkal.citycaller.repeatedStarted

class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get()  = _binding!!
    private val vm by viewModels<LoginViewModel>()
    private val mainVm by activityViewModels<MainViewModel>()



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUi()

        lifecycleScope.launch {
            repeatedStarted {
                mainVm.user.collectLatest { userr ->
                    if (userr != null) {
                        Log.d("LOG_D", "${userr.email}")
                        findNavController().navigate(R.id.action_loginFragment_to_mapFragment)
                    }
                }
            }
        }
    }

    private fun setupUi() {
        binding.loginUserBtn.setOnClickListener{
            val pass = binding.loginPassEt.text.toString()
            val login = binding.loginEmailEt.text.toString()

            loginUser(login, pass)

        }
        binding.registerTv.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registrationFragment)
        }
    }

    private fun loginUser(login: String, pass: String) {
        mainVm.loginUser(login, pass)

    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}