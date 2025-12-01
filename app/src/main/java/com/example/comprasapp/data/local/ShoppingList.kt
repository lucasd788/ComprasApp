package com.example.comprasapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "listas")
data class ShoppingList(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nome: String
)