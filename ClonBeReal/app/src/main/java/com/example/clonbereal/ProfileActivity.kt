package com.example.clonbereal

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import com.example.clonbereal.data.PostRepository
import com.example.clonbereal.data.SessionManager
import com.example.clonbereal.databinding.ActivityProfileBinding
import java.util.UUID

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var session: SessionManager
    private lateinit var repository: PostRepository

    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                // Usamos la primera foto (la "principal") como foto de perfil
                val bmp = MainActivity.pendingMain
                MainActivity.pendingMain = null
                MainActivity.pendingSelfie = null
                if (bmp != null) {
                    val path = repository.saveBitmap(bmp, "profile_${session.currentUser()}_${UUID.randomUUID()}")
                    session.savePhotoPath(path)
                    binding.imgProfilePhoto.setImageBitmap(bmp)
                    Toast.makeText(this, "Foto actualizada", Toast.LENGTH_SHORT).show()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.statusBarColor = getColor(R.color.black)

        session = SessionManager(this)
        repository = PostRepository(this)

        // Cargar datos actuales
        binding.tvProfileUsername.text = session.currentUser()
        binding.etBio.setText(session.getBio())
        session.getPhotoPath()?.let { path ->
            repository.loadBitmap(path)?.let { binding.imgProfilePhoto.setImageBitmap(it) }
        }

        binding.btnBackProfile.setOnClickListener { finish() }

        binding.btnChangePhoto.setOnClickListener {
            it.animatePress()
            val intent = Intent(this, CameraActivity::class.java)
            intent.putExtra(CameraActivity.EXTRA_START_LENS, CameraSelector.LENS_FACING_FRONT)
            cameraLauncher.launch(intent)
        }

        binding.btnSaveBio.setOnClickListener {
            it.animatePress()
            session.saveBio(binding.etBio.text.toString().trim())
            Toast.makeText(this, "Cambios guardados", Toast.LENGTH_SHORT).show()
        }

        binding.btnLogout.setOnClickListener {
            it.animatePress()
            AlertDialog.Builder(this)
                .setTitle("Cerrar sesión")
                .setMessage("¿Seguro que quieres cerrar sesión?")
                .setPositiveButton("Sí") { _, _ -> cerrarSesion() }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }

    private fun cerrarSesion() {
        session.logout()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}