package com.example.comprasapp

import android.app.Application
import com.example.comprasapp.data.local.AppDatabase
import com.example.comprasapp.data.repository.ItemRepository

class ComprasApplication : Application() {

    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { ItemRepository(database.itemDao()) }
}