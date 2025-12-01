package com.example.comprasapp.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "itens",
    indices = [Index(value = ["listId"])],
    foreignKeys = [
        ForeignKey(
            entity = ShoppingList::class,
            parentColumns = ["id"],
            childColumns = ["listId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Item(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val listId: Int,
    val nome: String,
    val quantidade: Double,
    val precoEstimado: Double,
    val unidade: String,
    val comprado: Boolean = false
)