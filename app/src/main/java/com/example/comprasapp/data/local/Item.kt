package com.example.comprasapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "itens")
data class Item(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nome: String,
    val quantidade: Int,
    val precoEstimado: Double,
    val unidade: String,
    val comprado: Boolean = false
)