package com.example.clonbereal

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import android.app.Dialog
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.example.clonbereal.adapter.BeRealAdapter
import com.example.clonbereal.data.BeRealPost
import com.example.clonbereal.data.PostRepository
import com.example.clonbereal.data.SessionManager
import com.example.clonbereal.databinding.ActivityMainBinding
import com.example.clonbereal.databinding.DialogPreviewBinding
import java.text.SimpleDateFormat
import java.util.*
import androidx.camera.core.CameraSelector

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: BeRealAdapter
    private lateinit var session: SessionManager

    private lateinit var repository: PostRepository

    // Foto trasera temporal mientras se toma la selfie
    private var tempMainPhoto: Bitmap? = null

    companion object {
        var pendingMain: Bitmap? = null
        var pendingSelfie: Bitmap? = null
    }

    // Cámara selfie (frontal) -> al terminar, mostramos el preview fiel
    private val takeSelfieLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
            val main = tempMainPhoto
            if (bitmap != null && main != null) {
                showPreviewDialog(main, bitmap)
            } else {
                Toast.makeText(this, "Error al capturar", Toast.LENGTH_SHORT).show()
            }
            tempMainPhoto = null
        }

    // Cámara principal (trasera)
    private val takeMainPhotoLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
            if (bitmap != null) {
                tempMainPhoto = bitmap
                Toast.makeText(this, "¡Ahora la Selfie!", Toast.LENGTH_SHORT).show()
                takeSelfieLauncher.launch(null)
            } else {
                Toast.makeText(this, "Se canceló la captura", Toast.LENGTH_SHORT).show()
            }
        }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) abrirCamara()
            else Toast.makeText(this, "Sin permiso de cámara no hay BeReal", Toast.LENGTH_SHORT).show()
        }

    private val cameraActivityLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val main = pendingMain
                val selfie = pendingSelfie
                if (main != null && selfie != null) {
                    showPreviewDialog(main, selfie)
                }
                pendingMain = null
                pendingSelfie = null
            }
        }

    private fun abrirCamara() {
        val intent = Intent(this, CameraActivity::class.java)
        intent.putExtra(CameraActivity.EXTRA_START_LENS, CameraSelector.LENS_FACING_BACK)
        cameraActivityLauncher.launch(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        session = SessionManager(this)

        // Protección de ruta: si no hay sesión, volvemos al login.
        if (!session.isLoggedIn()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.statusBarColor = getColor(R.color.black)

        repository = PostRepository(this)
        setupRecyclerView()
        adapter.submitList(repository.loadPosts()) {
            updateEmptyState()
        }

        // El botón central de la barra abre la cámara
        binding.btnCamera.setOnClickListener {
            it.animatePress()
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED
            ) {
                abrirCamara()
            } else {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }

        // Feedback en el resto de la navegación (aún no implementada como pantallas)
        val notImpl = View.OnClickListener { v ->
            v.animatePress()
            Toast.makeText(this, "Sección próximamente", Toast.LENGTH_SHORT).show()
        }
        binding.navFriends.setOnClickListener(notImpl)
        binding.navMemories.setOnClickListener(notImpl)
        binding.navProfile.setOnClickListener {
            it.animatePress()
            startActivity(Intent(this, ProfileActivity::class.java))
        }
        binding.navHome.setOnClickListener { it.animatePress() }

        binding.btnActivateNotif.setOnClickListener {
            it.animatePress()
            binding.notifBanner.animate().alpha(0f).setDuration(250).withEndAction {
                binding.notifBanner.visibility = View.GONE
            }.start()
        }

        // Tabs
        binding.tabMine.setOnClickListener { selectTab(true) }
        binding.tabFof.setOnClickListener { selectTab(false) }
    }
    override fun onResume() {
        super.onResume()
        // Actualiza el avatar de la barra con la foto de perfil guardada
        session.getPhotoPath()?.let { path ->
            repository.loadBitmap(path)?.let { binding.imgProfile.setImageBitmap(it) }
        }
    }
    private fun selectTab(mine: Boolean) {
        binding.tabMine.setTextColor(getColor(if (mine) R.color.white else R.color.bereal_text_secondary))
        binding.tabFof.setTextColor(getColor(if (mine) R.color.bereal_text_secondary else R.color.white))
    }

    private fun setupRecyclerView() {
        adapter = BeRealAdapter(repository)
        binding.recyclerViewFeed.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewFeed.adapter = adapter
        // Animación de entrada de la lista completa
        binding.recyclerViewFeed.layoutAnimation =
            AnimationUtils.loadLayoutAnimation(this, R.anim.layout_anim_fall_down)
    }

    /** Muestra u oculta el estado "Ya está bien por hoy" según haya posts. */
    private fun updateEmptyState() {
        val empty = adapter.currentList.isEmpty()
        binding.emptyState.visibility = if (empty) View.VISIBLE else View.GONE
        binding.recyclerViewFeed.visibility = if (empty) View.GONE else View.VISIBLE
        if (empty) {
            binding.tvEmptyTitle.text = getString(R.string.done_title, session.currentUser())
            binding.emptyState.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in))
        }
    }

    private fun guardarPostEnFeed(main: Bitmap, selfie: Bitmap) {
        val timeNow = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
        val id = UUID.randomUUID().toString()

        // Guardamos las fotos como archivos y obtenemos sus rutas
        val mainPath = repository.saveBitmap(main, "main_$id")
        val selfiePath = repository.saveBitmap(selfie, "selfie_$id")

        val newPost = BeRealPost(
            id = id,
            userAvatarResId = R.drawable.ic_user_felipe,
            username = session.currentUser(),
            timePosted = timeNow,
            mainImagePath = mainPath,
            selfieImagePath = selfiePath,
            reactionCount = 0,
            commentCount = 0
        )
        val current = adapter.currentList.toMutableList()
        current.add(0, newPost)
        adapter.submitList(current) {
            binding.recyclerViewFeed.scheduleLayoutAnimation()
            updateEmptyState()
            repository.savePosts(current)   // <-- persistimos la lista
        }
    }

    /** Pantalla de confirmación fiel a BeReal (última imagen). */
    private fun showPreviewDialog(main: Bitmap, selfie: Bitmap) {
        val dialog = Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)

        val vb = DialogPreviewBinding.inflate(layoutInflater)
        dialog.setContentView(vb.root)

        vb.imgMainPreview.setImageBitmap(main)
        vb.imgSelfiePreview.setImageBitmap(selfie)

        vb.btnClose.setOnClickListener {
            it.animatePress()
            dialog.dismiss()
        }

        vb.btnSend.setOnClickListener {
            it.animatePress()
            guardarPostEnFeed(main, selfie)
            it.postDelayed({ dialog.dismiss() }, 150)
        }

        dialog.show()
        dialog.window?.setLayout(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        vb.root.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_up_fade_in))
    }
}
