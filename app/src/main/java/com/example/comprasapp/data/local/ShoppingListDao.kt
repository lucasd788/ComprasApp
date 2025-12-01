package com.example.comprasapp.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ShoppingListDao {
    @Query("SELECT * FROM listas ORDER BY id ASC")
    fun buscarTodasAsListas(): Flow<List<ShoppingList>>

    @Insert
    suspend fun inserir(lista: ShoppingList): Long

    @Update
    suspend fun atualizar(lista: ShoppingList)

    @Delete
    suspend fun excluir(lista: ShoppingList)
}