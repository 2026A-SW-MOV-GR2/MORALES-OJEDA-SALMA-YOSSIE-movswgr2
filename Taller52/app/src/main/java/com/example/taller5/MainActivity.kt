package com.example.taller5

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private val TAG = "CICLO_VIDA_EPN"

    // Componentes Módulo Saliente
    private lateinit var etTelefono: EditText
    private lateinit var btnDial: Button
    private lateinit var btnTomarFoto: Button
    private lateinit var ivMiniatura: ImageView

    // Componentes Contador (Taller 6)
    private lateinit var tvContador: TextView
    private lateinit var btnIncrementar: Button

    // Componentes Módulo Entrante
    private lateinit var tvEstado: TextView
    private lateinit var tvTextoRecibido: TextView
    private lateinit var ivImagenRecibida: ImageView

    // VARIABLES DE RETENCIÓN DE ESTADO (Persistencia)
    private var count = 0
    private var bitmapFotoTomada: Bitmap? = null
    private var textoExternoRecibido: String? = null
    private var uriImagenRecibida: Uri? = null

    // Launcher moderno para la Cámara
    private val tomarFotoLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val bitmap = result.data?.extras?.get("data") as? Bitmap
            if (bitmap != null) {
                bitmapFotoTomada = bitmap
                ivMiniatura.scaleType = ImageView.ScaleType.CENTER_CROP
                ivMiniatura.setImageBitmap(bitmap)
                Toast.makeText(this, "Foto retenida en memoria", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ====================================================
    // MÉTODOS DEL CICLO DE VIDA CON LOGS
    // ====================================================

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.d(TAG, "onCreate() - Actividad creada e interfaz inflada.")

        // Inicializar vistas
        etTelefono = findViewById(R.id.etTelefono)
        btnDial = findViewById(R.id.btnDial)
        btnTomarFoto = findViewById(R.id.btnTomarFoto)
        ivMiniatura = findViewById(R.id.ivMiniatura)

        tvContador = findViewById(R.id.tvContador)
        btnIncrementar = findViewById(R.id.btnIncrementar)

        tvEstado = findViewById(R.id.tvEstado)
        tvTextoRecibido = findViewById(R.id.tvTextoRecibido)
        ivImagenRecibida = findViewById(R.id.ivImagenRecibida)

        // Configurar clicks
        configurarIntentsSalientes()

        btnIncrementar.setOnClickListener {
            count++
            tvContador.text = count.toString()
        }

        // Si la app se recrea por rotación, los datos se restauran desde el bundle en onRestoreInstanceState
        // Pero procesamos el intent por si vino un chisme directo al abrir
        procesarIntentEntrante(intent)
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart() - La app es visible.")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume() - La app está activa en primer plano.")
        // Asegurar que la foto se mantenga al volver de la cámara o YouTube
        bitmapFotoTomada?.let {
            ivMiniatura.scaleType = ImageView.ScaleType.CENTER_CROP
            ivMiniatura.setImageBitmap(it)
        }
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause() - La app pierde foco parcial.")
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop() - La app pasa a segundo plano.")
    }

    override fun onRestart() {
        super.onRestart()
        Log.d(TAG, "onRestart() - El usuario regresa desde segundo plano.")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy() - ¡Actividad destruida por completo!")
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        procesarIntentEntrante(intent)
    }

    // ====================================================
    // MÉTODOS DE SALVADO DE INSTANCIA (EVITA BORRADOS EN ROTACIÓN)
    // ====================================================

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("SAVED_COUNT", count)
        outState.putParcelable("SAVED_IMAGE_URI", uriImagenRecibida)
        outState.putString("SAVED_TEXT", textoExternoRecibido)
        outState.putParcelable("SAVED_BITMAP", bitmapFotoTomada)
        Log.d(TAG, "onSaveInstanceState() - Guardando estado de la UI (Contador: $count).")
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        count = savedInstanceState.getInt("SAVED_COUNT", 0)
        textoExternoRecibido = savedInstanceState.getString("SAVED_TEXT")

        // Recuperar URI de imagen recibida de forma segura
        uriImagenRecibida = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            savedInstanceState.getParcelable("SAVED_IMAGE_URI", Uri::class.java)
        } else {
            @Suppress("DEPRECATION")
            savedInstanceState.getParcelable("SAVED_IMAGE_URI")
        }

        // Recuperar Bitmap de la foto tomada
        bitmapFotoTomada = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            savedInstanceState.getParcelable("SAVED_BITMAP", Bitmap::class.java)
        } else {
            @Suppress("DEPRECATION")
            savedInstanceState.getParcelable("SAVED_BITMAP")
        }

        // Re-pintar la UI con los datos salvados
        tvContador.text = count.toString()
        textoExternoRecibido?.let { tvTextoRecibido.text = it }
        uriImagenRecibida?.let { ivImagenRecibida.setImageURI(it) }
        bitmapFotoTomada?.let {
            ivMiniatura.scaleType = ImageView.ScaleType.CENTER_CROP
            ivMiniatura.setImageBitmap(it)
        }
        Log.d(TAG, "onRestoreInstanceState() - ¡UI reconstruida de forma exitosa tras la destrucción!")
    }

    // ====================================================
    // LÓGICA DE DETECCIÓN Y PROCESAMIENTO
    // ====================================================

    private fun configurarIntentsSalientes() {
        btnDial.setOnClickListener {
            val numero = etTelefono.text.toString().trim()
            if (numero.isNotEmpty()) {
                val intentDial = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:$numero")
                }
                startActivity(intentDial)
            } else {
                etTelefono.error = "Ingrese un número"
            }
        }

        btnTomarFoto.setOnClickListener {
            val intentCamara = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            tomarFotoLauncher.launch(intentCamara)
        }
    }

    private fun procesarIntentEntrante(incomingIntent: Intent?) {
        if (incomingIntent == null) return

        val action = incomingIntent.action
        val type = incomingIntent.type

        if (Intent.ACTION_SEND == action && type != null) {
            tvEstado.text = "Estado: ¡Dato Externo Recibido!"

            when {
                type.startsWith("text/") -> {
                    val textoCompartido = incomingIntent.getStringExtra(Intent.EXTRA_TEXT)
                    if (textoCompartido != null) {
                        textoExternoRecibido = textoCompartido
                        uriImagenRecibida = null
                        tvTextoRecibido.text = textoCompartido
                        ivImagenRecibida.setImageResource(android.R.color.transparent)
                        ivImagenRecibida.setBackgroundColor(android.graphics.Color.parseColor("#E0E0E0"))
                    }
                }
                type.startsWith("image/") -> {
                    val imageUri = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                        incomingIntent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        incomingIntent.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as? Uri
                    }

                    if (imageUri != null) {
                        uriImagenRecibida = imageUri
                        textoExternoRecibido = "[Archivo Binario: Imagen Cargada]"
                        grantUriPermission(packageName, imageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        ivImagenRecibida.setImageURI(imageUri)
                        tvTextoRecibido.text = textoExternoRecibido
                    }
                }
            }
        }
    }
}