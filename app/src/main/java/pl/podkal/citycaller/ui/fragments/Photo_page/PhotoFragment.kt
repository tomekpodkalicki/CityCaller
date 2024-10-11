package pl.podkal.citycaller.ui.fragments.Photo_page

import android.net.Uri
import androidx.fragment.app.viewModels
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Surface
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.core.UseCaseGroup
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.gms.common.images.Size
import pl.podkal.citycaller.R
import pl.podkal.citycaller.activities.MainViewModel
import pl.podkal.citycaller.databinding.FragmentPhotoBinding
import java.io.File
import java.util.Calendar
import java.util.concurrent.Executors

class PhotoFragment : Fragment() {
    private var _binding: FragmentPhotoBinding? = null
    private val binding get() = _binding!!
    private val vm by viewModels<PhotoViewModel>()
    private  val mainVm by activityViewModels<MainViewModel>()
    private val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    private val c = Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPhotoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val preview = Preview.Builder()
            .build()
            .also {
                it.setSurfaceProvider(binding.previewView.surfaceProvider)
            }

        val imageCapture = ImageCapture.Builder()
            .setTargetRotation(Surface.ROTATION_0)
            .setTargetResolution(android.util.Size
                (3840, 2160))//4k resolution
            .build()

        cameraInita(preview, imageCapture, binding.previewView)

        binding.shutterBtn.setOnClickListener {
            takePhoto(imageCapture)
        }
    }

    private fun cameraInita(
        preview: Preview,
        imageCapture: ImageCapture,
        previewView: PreviewView
    ) {

        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            try {
                cameraProvider.unbindAll()

                val viewPort = previewView.viewPort ?: throw Exception("Viewport is null")
                val useCaseGroup = UseCaseGroup.Builder()
                    .addUseCase(preview)
                    .addUseCase(imageCapture)
                    .setViewPort(viewPort)
                    .build()

                cameraProvider.bindToLifecycle(viewLifecycleOwner, cameraSelector, useCaseGroup)
            } catch (exc: Exception) {

            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun takePhoto(imageCapture: ImageCapture) {

        val photoFile =
            File(requireContext().getExternalFilesDir(null)?.path + "/${c.timeInMillis}.jpeg")

        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(photoFile)
            .build()

        imageCapture.takePicture(
            outputOptions,
            Executors.newSingleThreadExecutor(),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)
                    savedUri.path?.let {
                        mainVm.photoPath = it
                    }
                    requireActivity().runOnUiThread {
                        findNavController().popBackStack()
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e("CAM_E", "Photo capture failed! ${exception.message}")
                }
            }
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}