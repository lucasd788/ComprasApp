package com.example.comprasapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
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

    private val termoBusca = MutableLiveData("")

    private val listaDoBanco = repository.todosItens.asLiveData()

    val itensExibidos = MediatorLiveData<List<Item>>().apply {
        addSource(listaDoBanco) { processarLista() }
        addSource(termoBusca) { processarLista() }
    }

    val custoTotal: LiveData<Double> = itensExibidos.map { lista ->
        lista.filter { it.quantidade > 0.0 }
            .sumOf { it.quantidade * it.precoEstimado }
    }

    fun buscar(texto: String) {
        termoBusca.value = texto
    }

    fun mudarOrdem(novaOrdem: Ordem) {
        ordemAtual = novaOrdem
        processarLista()
    }

    private fun processarLista() {
        val listaAtual = listaDoBanco.value ?: emptyList()
        val busca = termoBusca.value ?: ""

        val listaFiltrada = if (busca.isBlank()) {
            listaAtual
        } else {
            listaAtual.filter { it.nome.contains(busca, ignoreCase = true) }
        }

        val (ativos, inativos) = listaFiltrada.partition { it.quantidade > 0.0 }
        val (pendentes, comprados) = ativos.partition { !it.comprado }

        val pendentesOrdenados = aplicarOrdem(pendentes)
        val compradosOrdenados = aplicarOrdem(comprados)

        val inativosOrdenados = inativos.sortedBy { it.nome.lowercase() }

        itensExibidos.value = pendentesOrdenados + compradosOrdenados + inativosOrdenados
    }

    private fun aplicarOrdem(lista: List<Item>): List<Item> {
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