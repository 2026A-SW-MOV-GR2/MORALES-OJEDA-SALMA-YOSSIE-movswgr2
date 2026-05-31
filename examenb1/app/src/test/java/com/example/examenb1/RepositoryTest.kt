package com.example.examenb1

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.examenb1.model.Publicacion
import com.example.examenb1.repository.NoSqlRepository
import com.example.examenb1.repository.SqlRepository
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE) // 🚀 Soluciona el error del AndroidManifest.xml ausente
class RepositoryTest {

    private lateinit var context: Context
    private lateinit var sqlRepository: SqlRepository
    private lateinit var noSqlRepository: NoSqlRepository

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        sqlRepository = SqlRepository(context)
        noSqlRepository = NoSqlRepository(context)

        // 🧼 Limpieza segura del motor relacional SQLite
        context.deleteDatabase("EpnDatabase.db")

        // 🧼 En lugar de borrar el archivo de golpe, lo reiniciamos con una estructura JSON válida
        val file = File(context.filesDir, "nosql_store.json")
        file.writeText("[]")
    }

    @Test
    fun testEscrituraSqlNoAfectaNoSql() {
        val pubSql = Publicacion("101", "Tarea de Móviles", "Estudiar Kotlin Nativo")

        // 1. Guardar en SQLite
        sqlRepository.create(pubSql)

        // 2. Verificar que exista en SQL
        val listaSql = sqlRepository.readAll()
        assertEquals(1, listaSql.size)
        assertEquals("Tarea de Móviles", listaSql[0].titulo)

        // 3. Validar el aislamiento crítico: La base NoSQL debe mantenerse en 0 registros
        val listaNoSql = noSqlRepository.readAll()
        assertEquals(0, listaNoSql.size)
    }

    @Test
    fun testEscrituraNoSqlCorrecta() {
        val pubNoSql = Publicacion("202", "Doc Flexible", "JSON semiestructurado")

        // 1. Guardar en NoSQL
        noSqlRepository.create(pubNoSql)

        // 2. Verificar persistencia ágil
        val listaNoSql = noSqlRepository.readAll()
        assertEquals(1, listaNoSql.size)
        assertEquals("Doc Flexible", listaNoSql[0].titulo)
    }
}