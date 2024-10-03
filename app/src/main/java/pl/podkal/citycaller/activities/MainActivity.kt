package pl.podkal.citycaller.activities

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import pl.podkal.citycaller.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val mainVm by viewModels<MainViewModel>()
    private lateinit var navController: NavController


    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupUi()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                mainVm.bottomBar.collectLatest {
                    hideBottomBar(it)
                }
            }
        }


    }
    private fun hideBottomBar( bool: Boolean) {
        binding.bottomNavigationView.isVisible = bool
        binding.addNewIncidentFab.isVisible = bool
    }


    private fun setupUi() {
        setupBottomMenu()
    }

    private fun setupBottomMenu() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(
                binding.fragmentContainerView.id) as NavHostFragment
        navController = navHostFragment.navController
        NavigationUI.setupWithNavController(binding.bottomNavigationView,
            navController)

    }
}

