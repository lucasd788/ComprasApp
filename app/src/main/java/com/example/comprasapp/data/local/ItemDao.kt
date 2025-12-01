package com.example.comprasapp.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {

    @Query("SELECT * FROM itens WHERE listId IN (:listaIds) ORDER BY nome ASC")
    fun buscarItensDasListas(listaIds: List<Int>): Flow<List<Item>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir(item: Item)

    @Update
    suspend fun atualizar(item: Item)

    @Delete
    suspend fun excluir(item: Item)

    @Query("DELETE FROM itens WHERE listId IN (:listaIds) AND comprado = 1")
    suspend fun excluirItensComprados(listaIds: List<Int>)

    @Query("UPDATE itens SET comprado = 0 WHERE listId IN (:listaIds)")
    suspend fun desmarcarTodos(listaIds: List<Int>)
}