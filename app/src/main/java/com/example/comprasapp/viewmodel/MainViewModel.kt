package com.example.comprasapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.example.comprasapp.data.local.Item
import com.example.comprasapp.data.repository.ItemRepository
import kotlinx.coroutines.launch

class MainViewModel(private val repository: ItemRepository) : ViewModel() {

    enum class Ordem {
        ALFABETICA, PRECO_CRESCENTE, PRECO_DECRESCENTE, QUANTIDADE
    }

    private var ordemAtual = Ordem.ALFABETICA

    private val listaDoBanco = repository.todosItens.asLiveData()

    val itensExibidos = MediatorLiveData<List<Item>>().apply {
        addSource(listaDoBanco) { lista ->
            value = ordenarLista(lista)
        }
    }

    val custoTotal: LiveData<Double> = itensExibidos.map { lista ->
        lista.sumOf { it.quantidade * it.precoEstimado }
    }

    fun mudarOrdem(novaOrdem: Ordem) {
        ordemAtual = novaOrdem
        val listaAtual = listaDoBanco.value
        if (listaAtual != null) {
            itensExibidos.value = ordenarLista(listaAtual)
        }
    }

    private fun ordenarLista(lista: List<Item>): List<Item> {
        return when (ordemAtual) {
            Ordem.ALFABETICA -> lista.sortedBy { it.nome.lowercase() }
            Ordem.PRECO_CRESCENTE -> lista.sortedBy { it.precoEstimado }
            Ordem.PRECO_DECRESCENTE -> lista.sortedByDescending { it.precoEstimado }
            Ordem.QUANTIDADE -> lista.sortedByDescending { it.quantidade }
        }
    }

    fun inserir(item: Item) = viewModelScope.launch {
        repository.inserir(item)
    }

    fun atualizar(item: Item) = viewModelScope.launch {
        repository.atualizar(item)
    }

    fun excluir(item: Item) = viewModelScope.launch {
        repository.excluir(item)
    }

    fun excluirComprados() = viewModelScope.launch {
        repository.excluirComprados()
    }

    fun desmarcarTodos() = viewModelScope.launch {
        repository.desmarcarTodos()
    }
}