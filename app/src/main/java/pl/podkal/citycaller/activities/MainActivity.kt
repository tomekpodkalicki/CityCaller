package pl.podkal.citycaller.activities

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import pl.podkal.citycaller.databinding.ActivityMainBinding
import pl.podkal.citycaller.ui.fragments.registration_page.RegistrationFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val mainVm by viewModels<MainViewModel>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val frag = RegistrationFragment()
        supportFragmentManager.beginTransaction().add(frag,"").commit()
    }
}

