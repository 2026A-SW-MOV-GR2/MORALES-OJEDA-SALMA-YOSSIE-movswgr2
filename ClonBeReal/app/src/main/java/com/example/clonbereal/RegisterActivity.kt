package com.example.clonbereal

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.clonbereal.data.SessionManager
import com.example.clonbereal.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.statusBarColor = getColor(R.color.black)
        session = SessionManager(this)

        binding.btnBack.setOnClickListener { finish() }

        binding.btnRegister.setOnClickListener {
            it.animatePress()
            val user = binding.etRegUsername.text.toString().trim()
            val pass = binding.etRegPassword.text.toString()
            val pass2 = binding.etRegPassword2.text.toString()

            when {
                user.isEmpty() || pass.isEmpty() ->
                    Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                pass.length < 4 ->
                    Toast.makeText(this, "La contraseña debe tener al menos 4 caracteres", Toast.LENGTH_SHORT).show()
                pass != pass2 ->
                    Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                !session.register(user, pass) ->
                    Toast.makeText(this, "Ese usuario ya existe", Toast.LENGTH_SHORT).show()
                else -> {
                    session.login(user)
                    Toast.makeText(this, "¡Cuenta creada!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finishAffinity()
                }
            }
        }
    }
}
