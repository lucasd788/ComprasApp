package com.example.comprasapp.data.repository

import com.example.comprasapp.data.local.Item
import com.example.comprasapp.data.local.ItemDao
import com.example.comprasapp.data.local.ShoppingList
import com.example.comprasapp.data.local.ShoppingListDao
import kotlinx.coroutines.flow.Flow

class ItemRepository(
    private val itemDao: ItemDao,
    private val shoppingListDao: ShoppingListDao
) {

    val todasAsListas: Flow<List<ShoppingList>> = shoppingListDao.buscarTodasAsListas()

    suspend fun criarLista(lista: ShoppingList): Long {
        return shoppingListDao.inserir(lista)
    }

    suspend fun renomearLista(lista: ShoppingList) {
        shoppingListDao.atualizar(lista)
    }

    suspend fun excluirLista(lista: ShoppingList) {
        shoppingListDao.excluir(lista)
    }

    fun buscarItensPorListas(ids: List<Int>): Flow<List<Item>> {
        return itemDao.buscarItensDasListas(ids)
    }

    suspend fun inserir(item: Item) {
        itemDao.inserir(item)
    }

    suspend fun atualizar(item: Item) {
        itemDao.atualizar(item)
    }

    suspend fun excluir(item: Item) {
        itemDao.excluir(item)
    }

    suspend fun excluirComprados(listaIds: List<Int>) {
        itemDao.excluirItensComprados(listaIds)
    }

    suspend fun desmarcarTodos(listaIds: List<Int>) {
        itemDao.desmarcarTodos(listaIds)
    }
}