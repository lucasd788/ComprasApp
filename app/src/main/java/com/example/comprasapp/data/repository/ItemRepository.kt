package com.example.comprasapp.data.repository

import com.example.comprasapp.data.local.Item
import com.example.comprasapp.data.local.ItemDao
import kotlinx.coroutines.flow.Flow

class ItemRepository(private val itemDao: ItemDao) {

    val todosItens: Flow<List<Item>> = itemDao.buscarTodosOsItens()

    suspend fun inserir(item: Item) {
        itemDao.inserir(item)
    }

    suspend fun atualizar(item: Item) {
        itemDao.atualizar(item)
    }

    suspend fun excluir(item: Item) {
        itemDao.excluir(item)
    }

    suspend fun excluirComprados() {
        itemDao.excluirItensComprados()
    }

    suspend fun desmarcarTodos() {
        itemDao.desmarcarTodos()
    }
}