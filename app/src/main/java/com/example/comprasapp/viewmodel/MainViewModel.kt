package com.example.comprasapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.example.comprasapp.data.local.Item
import com.example.comprasapp.data.local.ShoppingList
import com.example.comprasapp.data.repository.ItemRepository
import kotlinx.coroutines.launch
import java.util.Locale

class MainViewModel(private val repository: ItemRepository) : ViewModel() {

    enum class Ordem {
        ALFABETICA, PRECO_CRESCENTE, PRECO_DECRESCENTE, QUANTIDADE
    }

    private var ordemAtual = Ordem.ALFABETICA
    private val termoBusca = MutableLiveData("")

    val todasAsListas = repository.todasAsListas.asLiveData()

    val listasAtivas = MutableLiveData<List<Int>>()

    private val listaDoBanco: LiveData<List<Item>> = listasAtivas.switchMap { ids ->
        repository.buscarItensPorListas(ids).asLiveData()
    }

    val itensExibidos = MediatorLiveData<List<Item>>().apply {
        addSource(listaDoBanco) { processarLista() }
        addSource(termoBusca) { processarLista() }
    }

    val custoTotal: LiveData<Double> = itensExibidos.map { lista ->
        lista.filter { it.quantidade > 0.0 }
            .sumOf { it.quantidade * it.precoEstimado }
    }

    fun mudarListaAtiva(id: Int) {
        listasAtivas.value = listOf(id)
    }

    fun mudarListasAtivas(ids: List<Int>) {
        listasAtivas.value = ids
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
        val colator = java.text.Collator.getInstance(Locale.forLanguageTag("pt-BR"))
        return when (ordemAtual) {
            Ordem.ALFABETICA -> lista.sortedWith { a, b -> colator.compare(a.nome, b.nome) }
            Ordem.PRECO_CRESCENTE -> lista.sortedBy { it.precoEstimado }
            Ordem.PRECO_DECRESCENTE -> lista.sortedByDescending { it.precoEstimado }
            Ordem.QUANTIDADE -> lista.sortedByDescending { it.quantidade }
        }
    }

    fun criarLista(nome: String) = viewModelScope.launch {
        val novaLista = ShoppingList(nome = nome)
        val novoId = repository.criarLista(novaLista)
        mudarListaAtiva(novoId.toInt())
    }

    fun renomearLista(lista: ShoppingList, novoNome: String) = viewModelScope.launch {
        repository.renomearLista(lista.copy(nome = novoNome))
    }

    fun excluirLista(lista: ShoppingList) = viewModelScope.launch {
        repository.excluirLista(lista)

        val ativasAtuais = listasAtivas.value ?: emptyList()
        if (ativasAtuais.contains(lista.id)) {
            val novaListaAtiva = ativasAtuais.toMutableList()
            novaListaAtiva.remove(lista.id)
            listasAtivas.value = novaListaAtiva
        }
    }

    fun inserir(item: Item) = viewModelScope.launch { repository.inserir(item) }
    fun atualizar(item: Item) = viewModelScope.launch { repository.atualizar(item) }

    fun excluir(item: Item) = viewModelScope.launch { repository.excluir(item) }

    fun excluirComprados() {
        val ids = listasAtivas.value ?: return
        viewModelScope.launch { repository.excluirComprados(ids) }
    }

    fun desmarcarTodos() {
        val ids = listasAtivas.value ?: return
        viewModelScope.launch { repository.desmarcarTodos(ids) }
    }
}