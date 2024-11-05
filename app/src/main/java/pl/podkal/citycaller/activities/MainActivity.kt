package pl.podkal.citycaller.activities

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.isVisible
import androidx.fragment.app.commit
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.navOptions
import androidx.navigation.ui.NavigationUI
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import pl.podkal.citycaller.R
import pl.podkal.citycaller.databinding.ActivityMainBinding
import pl.podkal.citycaller.ui.fragments.detail_page.DetailFragment

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
        setupCollectors()


    }


    private fun setupCollectors() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                mainVm.bottomBar.collectLatest {
                    hideBottomBar(it)
                }
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                mainVm.selectedIncident.collectLatest { incident ->
                    if(incident != null) {
                        val detailFragment = DetailFragment()
                        supportFragmentManager.commit {
                            setCustomAnimations(R.anim.slide_left, 0)
                            replace(
                                binding.detailFragmentContainer.id,
                                detailFragment,
                                "Detail_fragment"
                            )
                        }
                        binding.detailFragmentContainer.visibility = View.VISIBLE

                    } else hideDetailFragment()

                    }
                }
            }
}

    private fun hideDetailFragment() {
        supportFragmentManager.commit {
            supportFragmentManager.findFragmentByTag("Detail_fragment")?.let {
                setCustomAnimations(0, R.anim.slide_right)
                remove(it)
            }
        }

    }

    override fun onBackPressed() {
        if(mainVm.selectedIncident.value != null) {
            hideDetailFragment()
            mainVm.setIncident(null)
        } else super.onBackPressed()
    }

    private fun hideBottomBar( bool: Boolean) {
        binding.bottomNavigationView.isVisible = bool
        binding.addNewIncidentFab.isVisible = bool
    }


    private fun setupUi() {
        setupBottomMenu()
        setupAddNewAccident()

    }

    private fun setupAddNewAccident() {
        binding.addNewIncidentFab.setOnClickListener{
            mainVm.setBottomBarVisible(false)
            navController.navigate(
                R.id.newIncidentFragment,
                null,
                navOptions {
                    anim{
                        enter = androidx.fragment.R.animator.fragment_open_enter
                        exit = androidx.fragment.R.animator.fragment_close_exit
                    }

                })
        }
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

