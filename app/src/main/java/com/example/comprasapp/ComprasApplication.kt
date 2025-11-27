package com.example.comprasapp

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import com.example.comprasapp.data.local.AppDatabase
import com.example.comprasapp.data.repository.ItemRepository

class ComprasApplication : Application() {

    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { ItemRepository(database.itemDao()) }

    override fun onCreate() {
        super.onCreate()
        configurarTemaPadrao()
    }

    private fun configurarTemaPadrao() {
        val preferencias = getSharedPreferences("app_preferencias", Context.MODE_PRIVATE)

        val modoEscuroAtivo = preferencias.getBoolean("modo_escuro", true)

        val modoParaAplicar = if (modoEscuroAtivo) {
            AppCompatDelegate.MODE_NIGHT_YES
        } else {
            AppCompatDelegate.MODE_NIGHT_NO
        }

        AppCompatDelegate.setDefaultNightMode(modoParaAplicar)
    }
}