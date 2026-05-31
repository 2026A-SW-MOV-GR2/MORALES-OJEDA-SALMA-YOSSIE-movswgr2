package com.example.examenb1.repository

import android.content.Context
import android.util.Log

class DataOrchestrator(context: Context) {
    private val sqlRepo = SqlRepository(context)
    private val noSqlRepo = NoSqlRepository(context)

    // El estado del switch determinará cuál se usa
    var activeRepository: PublicacionRepository = sqlRepo
        private set

    fun switchSource(useNoSql: Boolean): String {
        activeRepository = if (useNoSql) {
            Log.w("CONMUTACION", "Capa de datos conmutada a: NoSQL (JSON Document)")
            noSqlRepo
        } else {
            Log.w("CONMUTACION", "Capa de datos conmutada a: SQL (SQLite Engine)")
            sqlRepo
        }
        return if (useNoSql) "NoSQL" else "SQLite"
    }
}