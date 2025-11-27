package com.example.comprasapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "itens")
data class Item(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nome: String,
    val quantidade: Double,
    val precoEstimado: Double,
    val unidade: String,
    val comprado: Boolean = false
)