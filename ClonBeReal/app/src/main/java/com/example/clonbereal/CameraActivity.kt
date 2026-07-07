package com.example.clonbereal

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.example.clonbereal.databinding.ActivityCameraBinding
import java.io.ByteArrayOutputStream

class CameraActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCameraBinding
    private var cameraProvider: ProcessCameraProvider? = null
    private var imageCapture: ImageCapture? = null

    // Cámara con la que estamos apuntando ahora mismo
    private var currentLens = CameraSelector.LENS_FACING_BACK

    // Guardamos las dos fotos aquí mientras se toman
    private var firstBitmap: Bitmap? = null
    private var firstWasBack = true
    private var capturing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.statusBarColor = getColor(R.color.black)

        // La cámara con la que arrancas la decide MainActivity (frontal o trasera)
        currentLens = intent.getIntExtra(EXTRA_START_LENS, CameraSelector.LENS_FACING_BACK)

        startCamera()

        binding.btnCloseCamera.setOnClickListener { finish() }

        binding.btnFlip.setOnClickListener {
            it.animatePress()
            currentLens = if (currentLens == CameraSelector.LENS_FACING_BACK)
                CameraSelector.LENS_FACING_FRONT else CameraSelector.LENS_FACING_BACK
            startCamera()
        }

        binding.btnShutter.setOnClickListener {
            if (!capturing) {
                capturing = true
                it.animatePress()
                tomarSecuencia()
            }
        }
    }

    private fun startCamera() {
        val future = ProcessCameraProvider.getInstance(this)
        future.addListener({
            cameraProvider = future.get()
            bindUseCases()
        }, ContextCompat.getMainExecutor(this))
    }

    private fun bindUseCases() {
        val provider = cameraProvider ?: return
        provider.unbindAll()

        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(binding.previewView.surfaceProvider)
        }
        imageCapture = ImageCapture.Builder().build()

        val selector = CameraSelector.Builder()
            .requireLensFacing(currentLens)
            .build()

        try {
            provider.bindToLifecycle(this, selector, preview, imageCapture)
        } catch (e: Exception) {
            Toast.makeText(this, "No se pudo abrir la cámara", Toast.LENGTH_SHORT).show()
        }
    }

    /** Toma la primera foto y, al terminar, voltea y toma la segunda automáticamente. */
    private fun tomarSecuencia() {
        firstWasBack = (currentLens == CameraSelector.LENS_FACING_BACK)
        capturar { bmp ->
            firstBitmap = bmp
            Toast.makeText(this, "¡Ahora la otra cámara!", Toast.LENGTH_SHORT).show()

            // Voltear cámara automáticamente
            currentLens = if (currentLens == CameraSelector.LENS_FACING_BACK)
                CameraSelector.LENS_FACING_FRONT else CameraSelector.LENS_FACING_BACK
            startCamera()

            // Pequeña espera para que la segunda cámara enfoque, luego dispara sola
            binding.previewView.postDelayed({
                capturar { bmp2 ->
                    devolverResultado(firstBitmap!!, bmp2)
                }
            }, 1200)
        }
    }

    private fun capturar(onCaptured: (Bitmap) -> Unit) {
        val ic = imageCapture ?: return
        ic.takePicture(
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    val bmp = image.toBitmap()
                    image.close()
                    onCaptured(bmp)
                }

                override fun onError(exc: ImageCaptureException) {
                    capturing = false
                    Toast.makeText(this@CameraActivity, "Error al capturar", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    /**
     * Devuelve las dos fotos a MainActivity.
     * La FOTO PRINCIPAL (recuadro grande) es la que tomaste PRIMERO.
     * Si empezaste con la trasera -> principal = trasera, selfie = frontal.
     * Si empezaste con la frontal -> principal = frontal, selfie = trasera.
     */
    private fun devolverResultado(first: Bitmap, second: Bitmap) {
        // "first" siempre es la grande (con la que disparaste)
        MainActivity.pendingMain = first
        MainActivity.pendingSelfie = second
        setResult(RESULT_OK)
        finish()
    }

    companion object {
        const val EXTRA_START_LENS = "start_lens"
    }
}