package pl.podkal.citycaller


import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch

suspend fun Fragment.repeatedStarted(block: suspend () -> Unit) {
    viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED){
        block()
    }
}

fun Fragment.viewLifecycleLaunch(block: suspend () -> Unit) {
    viewLifecycleOwner.lifecycleScope.launch {
        block()
    }
}