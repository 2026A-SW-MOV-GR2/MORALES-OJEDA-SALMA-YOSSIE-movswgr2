package com.example.examenb1

import android.graphics.Color
import android.os.Bundle
import android.view.View // <- CORRIGE EL ROJO DE View.VISIBLE Y View.GONE
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.ProgressBar // <- CORRIGE EL ROJO DE ProgressBar EN EL FINDVIEWBYID
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.examenb1.model.Publicacion
import com.example.examenb1.repository.DataOrchestrator
import com.example.examenb1.ui.viewmodel.MainViewModel
class MainActivity : AppCompatActivity() {

    private lateinit var orchestrator: DataOrchestrator
    private lateinit var adapter: ArrayAdapter<String>

    private val listaUI = mutableListOf<String>()
    private val listaModelos = mutableListOf<Publicacion>()

    private var idSeleccionadoParaEditar: String? = null

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        orchestrator = DataOrchestrator(this)

        val switchEngine = findViewById<com.google.android.material.switchmaterial.SwitchMaterial>(R.id.switchEngine)
        val chipIndicador = findViewById<TextView>(R.id.chipIndicador)
        val etTitulo = findViewById<EditText>(R.id.etTitulo)
        val etContenido = findViewById<EditText>(R.id.etContenido)
        val btnGuardar = findViewById<Button>(R.id.btnGuardar)
        val btnLimpiar = findViewById<Button>(R.id.btnLimpiar)
        val listView = findViewById<ListView>(R.id.listView)

        // Componentes de Red y Secretos Criptográficos
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        val btnCargarDatos = findViewById<Button>(R.id.btnCargarDatos)
        val btnGuardarSecreto = findViewById<Button>(R.id.btnGuardarSecreto)
        val edtSecretoInput = findViewById<EditText>(R.id.edtSecretoInput)
        val txtSecretoActual = findViewById<TextView>(R.id.txtSecretoActual)

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, listaUI)
        listView.adapter = adapter

        setupProjectObservers(progressBar, btnCargarDatos, btnGuardarSecreto, txtSecretoActual)
        setupProjectListeners(btnCargarDatos, btnGuardarSecreto, edtSecretoInput)

        viewModel.loadSecretKey("API_PRIVATE_KEY")

        switchEngine.setOnCheckedChangeListener { _, isChecked ->
            orchestrator.switchSource(isChecked)
            if (isChecked) {
                chipIndicador.text = "Origen Activo: NoSQL (JSON Local)"
                chipIndicador.setBackgroundColor(Color.parseColor("#FF85B0"))
            } else {
                chipIndicador.text = "Origen Activo: SQLite (SQL Nativo)"
                chipIndicador.setBackgroundColor(Color.parseColor("#940046"))
            }
            limpiarFormulario(etTitulo, etContenido, btnGuardar)
            refrescarListaUI()
        }

        btnGuardar.setOnClickListener {
            val titulo = etTitulo.text.toString().trim()
            val contenido = etContenido.text.toString().trim()

            if (titulo.isEmpty() || contenido.isEmpty()) {
                Toast.makeText(this, "Por favor complete todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (idSeleccionadoParaEditar == null) {
                val nuevaPublicacion = Publicacion(System.currentTimeMillis().toString(), titulo, contenido)
                orchestrator.activeRepository.create(nuevaPublicacion)
                Toast.makeText(this, "Registro CREADO con éxito", Toast.LENGTH_SHORT).show()
            } else {
                val publicacionEditada = Publicacion(idSeleccionadoParaEditar!!, titulo, contenido)
                orchestrator.activeRepository.update(publicacionEditada)
                Toast.makeText(this, "Registro ACTUALIZADO con éxito", Toast.LENGTH_SHORT).show()
            }

            limpiarFormulario(etTitulo, etContenido, btnGuardar)
            refrescarListaUI()
        }

        listView.setOnItemClickListener { _, _, position, _ ->
            val publicacionSeleccionada = listaModelos[position]
            idSeleccionadoParaEditar = publicacionSeleccionada.id
            etTitulo.setText(publicacionSeleccionada.titulo)
            etContenido.setText(publicacionSeleccionada.contenido)
            btnGuardar.text = "ACTUALIZAR REGISTRO"
        }

        listView.setOnItemLongClickListener { _, _, position, _ ->
            val publicacionParaEliminar = listaModelos[position]
            android.app.AlertDialog.Builder(this)
                .setTitle("Eliminar Registro")
                .setMessage("¿Estás seguro de que deseas borrar permanentemente a \"${publicacionParaEliminar.titulo}\"?")
                .setPositiveButton("Sí, eliminar") { _, _ ->
                    orchestrator.activeRepository.delete(publicacionParaEliminar.id)
                    if (idSeleccionadoParaEditar == publicacionParaEliminar.id) {
                        limpiarFormulario(etTitulo, etContenido, btnGuardar)
                    }
                    refrescarListaUI()
                    Toast.makeText(this, "Registro eliminado exitosamente", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Cancelar", null)
                .show()
            true
        }

        btnLimpiar.setOnClickListener {
            limpiarFormulario(etTitulo, etContenido, btnGuardar)
        }

        refrescarListaUI()
    }

    private fun setupProjectObservers(
        progressBar: ProgressBar,
        btnCargarDatos: Button,
        btnGuardarSecreto: Button,
        txtSecretoActual: TextView
    ) {
        viewModel.isLoading.observe(this@MainActivity) { isLoading ->
            if (isLoading) {
                progressBar.visibility = View.VISIBLE
                btnCargarDatos.isEnabled = false
                btnGuardarSecreto.isEnabled = false
            } else {
                progressBar.visibility = View.GONE
                btnCargarDatos.isEnabled = true
                btnGuardarSecreto.isEnabled = true
            }
        }

        viewModel.remoteElements.observe(this@MainActivity) { publicacionesRemotas ->
            if (publicacionesRemotas != null) {
                listaUI.clear()
                listaModelos.clear()
                for (item in publicacionesRemotas) {
                    listaModelos.add(item)
                    // ¡AQUÍ YA NO DA ERROR! Reconoce perfectamente item.titulo e item.contenido
                    listaUI.add("☁️ [REMOTO] Título: ${item.titulo}\n📝 Contenido: ${item.contenido}")
                }
                adapter.notifyDataSetChanged()
                Toast.makeText(this@MainActivity, "Sincronizado con JSONPlaceholder con éxito", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@MainActivity, "Error de conectividad HTTP REST remota", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.savedSecret.observe(this@MainActivity) { secreto ->
            txtSecretoActual.text = secreto ?: "No hay claves privadas aseguradas"
        }
    }

    private fun setupProjectListeners(
        btnCargarDatos: Button,
        btnGuardarSecreto: Button,
        edtSecretoInput: EditText
    ) {
        // Disparador asíncrono para consumir la API externa
        btnCargarDatos.setOnClickListener {
            viewModel.loadDataFromNetwork()
        }

        // Almacenamiento seguro del secreto usando Jetpack Security
        btnGuardarSecreto.setOnClickListener {
            val nuevoSecreto = edtSecretoInput.text.toString().trim()
            if (nuevoSecreto.isNotBlank()) {
                viewModel.persistSecretKey("API_PRIVATE_KEY", nuevoSecreto)
                edtSecretoInput.text.clear()
                Toast.makeText(this, "Secreto encriptado en Keystore", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "El secreto no puede estar vacío", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun refrescarListaUI() {
        listaUI.clear()
        listaModelos.clear()
        val datosBBDD = orchestrator.activeRepository.readAll()
        for (item in datosBBDD) {
            listaModelos.add(item)
            listaUI.add("📌 Título: ${item.titulo}\n📝 Contenido: ${item.contenido}")
        }
        adapter.notifyDataSetChanged()
    }

    private fun limpiarFormulario(etTitulo: EditText, etContenido: EditText, btnGuardar: Button) {
        etTitulo.text.clear()
        etContenido.text.clear()
        idSeleccionadoParaEditar = null
        btnGuardar.text = "GUARDAR"
    }
}