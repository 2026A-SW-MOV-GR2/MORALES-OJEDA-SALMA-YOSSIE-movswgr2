package com.example.examenb1

import android.graphics.Color
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.examenb1.model.Publicacion
import com.example.examenb1.repository.DataOrchestrator

class MainActivity : AppCompatActivity() {

    private lateinit var orchestrator: DataOrchestrator
    private lateinit var adapter: ArrayAdapter<String>

    private val listaUI = mutableListOf<String>()
    private val listaModelos = mutableListOf<Publicacion>()

    // Variable de control para saber si estamos editando un ítem existente o creando uno nuevo
    private var idSeleccionadoParaEditar: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicializamos nuestro orquestador dual de persistencia
        orchestrator = DataOrchestrator(this)

        // Vinculación de los componentes de la interfaz de usuario (UI)
        val switchEngine = findViewById<com.google.android.material.switchmaterial.SwitchMaterial>(R.id.switchEngine)
        val chipIndicador = findViewById<TextView>(R.id.chipIndicador)
        val etTitulo = findViewById<EditText>(R.id.etTitulo)
        val etContenido = findViewById<EditText>(R.id.etContenido)
        val btnGuardar = findViewById<Button>(R.id.btnGuardar)
        val btnLimpiar = findViewById<Button>(R.id.btnLimpiar)
        val listView = findViewById<ListView>(R.id.listView)

        // Configuración básica del adaptador de la lista
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, listaUI)
        listView.adapter = adapter

        // [RÚBRICA - 40%] Conmutación reactiva instantánea al alternar el Switch superior
        switchEngine.setOnCheckedChangeListener { _, isChecked ->
            orchestrator.switchSource(isChecked)

            if (isChecked) {
                chipIndicador.text = "Origen Activo: NoSQL (JSON Local)"
                chipIndicador.setBackgroundColor(Color.parseColor("#FF85B0")) // Color distintivo NoSQL
            } else {
                chipIndicador.text = "Origen Activo: SQLite (SQL Nativo)"
                chipIndicador.setBackgroundColor(Color.parseColor("#940046")) // Color distintivo SQL EPN
            }
            limpiarFormulario(etTitulo, etContenido, btnGuardar)
            refrescarListaUI() // Carga los datos del nuevo motor al instante sin reiniciar la app
        }

        // [CRUD - CREATE / UPDATE] Lógica del botón GUARDAR
        btnGuardar.setOnClickListener {
            val titulo = etTitulo.text.toString().trim()
            val contenido = etContenido.text.toString().trim()

            if (titulo.isEmpty() || contenido.isEmpty()) {
                Toast.makeText(this, "Por favor complete todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (idSeleccionadoParaEditar == null) {
                // MODO CREATE: Generamos una nueva publicación con un ID único basado en tiempo
                val nuevaPublicacion = Publicacion(System.currentTimeMillis().toString(), titulo, contenido)
                orchestrator.activeRepository.create(nuevaPublicacion)
                Toast.makeText(this, "Registro CREADO con éxito", Toast.LENGTH_SHORT).show()
            } else {
                // MODO UPDATE: Actualizamos la publicación existente usando su ID guardado
                val publicacionEditada = Publicacion(idSeleccionadoParaEditar!!, titulo, contenido)
                orchestrator.activeRepository.update(publicacionEditada)
                Toast.makeText(this, "Registro ACTUALIZADO con éxito", Toast.LENGTH_SHORT).show()
            }

            limpiarFormulario(etTitulo, etContenido, btnGuardar)
            refrescarListaUI()
        }

        // [CRUD - READ / PRE-UPDATE] Cargar datos en el formulario al hacer click simple en la lista
        listView.setOnItemClickListener { _, _, position, _ ->
            val publicacionSeleccionada = listaModelos[position]
            idSeleccionadoParaEditar = publicacionSeleccionada.id

            etTitulo.setText(publicacionSeleccionada.titulo)
            etContenido.setText(publicacionSeleccionada.contenido)
            btnGuardar.text = "ACTUALIZAR REGISTRO"
        }

        // [CRUD - DELETE] Borrar registro mediante presión larga (Long Click)
        listView.setOnItemLongClickListener { _, _, position, _ ->
            val publicacionParaEliminar = listaModelos[position]

            android.app.AlertDialog.Builder(this)
                .setTitle("Eliminar Registro")
                .setMessage("¿Estás seguro de que deseas borrar permanentemente a \"${publicacionParaEliminar.titulo}\" de la base de datos activa?")
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

        // Acción del botón Cancelar / Limpiar formulario
        btnLimpiar.setOnClickListener {
            limpiarFormulario(etTitulo, etContenido, btnGuardar)
        }

        // Carga de datos inicial al abrir la aplicación
        refrescarListaUI()
    }

    // Función auxiliar para refrescar el árbol de vistas (UI) consultando al repositorio activo
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