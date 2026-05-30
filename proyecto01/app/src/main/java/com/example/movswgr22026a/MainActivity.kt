package com.example.movswgr22026a

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

//class MainActivity : AppCompatActivity() {
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
//        setContentView(R.layout.activity_main)
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }
//    }
//}

class MainActivity : AppCompatActivity() {
    private lateinit var myWebView: WebView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        myWebView = findViewById(R.id.myWebView)
        myWebView.settings.javaScriptEnabled = true

        // Vinculamos el Bridge ANTES de cargar la URL
        myWebView.addJavascriptInterface(WebAppInterface(this), "Android")

        myWebView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                aplicarConfiguracion()
            }
        }
        myWebView.loadUrl("file:///android_asset/index.html")
    }

    private fun aplicarConfiguracion() {
        // 1. Obtenemos los recursos desde el contexto actual
        val texto = getString(R.string.saludo)
        val colorTextInt = getColor(R.color.color_text)
        val colorFondoInt = getColor(R.color.color_fondo)

        // 2. Convertimos los colores de Int a formato Hexadecimal
        val colorTextHex = String.format("#%06X", (0xFFFFFF and colorTextInt))
        val colorFondoHex = String.format("#%06X", (0xFFFFFF and colorFondoInt))

        // 3. Inyectamos los valores como variables CSS
        // Usamos el id "texto" que definimos en el HTML
        val js = """
        document.documentElement.style.setProperty('--text-color', '$colorTextHex');
        document.documentElement.style.setProperty('--bg-color', '$colorFondoHex');
        document.getElementById('texto').innerText = '$texto';
    """.trimIndent()

        myWebView.evaluateJavascript(js, null)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        aplicarConfiguracion()
    }

    // Clase para conectar JS con funciones nativas de Android
    class WebAppInterface(private val mContext: Context) {
        @JavascriptInterface
        fun showToast(message: String) {
            Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show()
        }

        @JavascriptInterface
        fun showDeleteDialog(id: String) { // Cambiamos a String para evitar problemas de precisión con IDs largos
            val builder = AlertDialog.Builder(mContext)
            builder.setTitle("Confirmación")
            builder.setMessage("¿Estás seguro de que deseas eliminar este elemento?")

            builder.setPositiveButton("Eliminar") { _, _ ->
                // ¡CRÍTICO! Usar runOnUiThread para evitar que la app se cierre
                (mContext as MainActivity).runOnUiThread {
                    mContext.findViewById<WebView>(R.id.myWebView)
                        .evaluateJavascript("confirmDelete('$id')", null)

                }
                showToast("Eliminado con éxito")
            }
            builder.setNegativeButton("Cancelar", null)

            // Los diálogos deben mostrarse en el hilo principal
            (mContext as MainActivity).runOnUiThread { builder.show() }
        }
    }
}
