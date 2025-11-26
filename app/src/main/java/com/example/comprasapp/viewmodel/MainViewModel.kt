package com.example.comprasapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.example.comprasapp.data.local.Item
import com.example.comprasapp.data.repository.ItemRepository
import kotlinx.coroutines.launch

class MainViewModel(private val repository: ItemRepository) : ViewModel() {

    val todosItens: LiveData<List<Item>> = repository.todosItens.asLiveData()

    val custoTotal: LiveData<Double> = todosItens.map { lista ->
        lista.sumOf { it.quantidade * it.precoEstimado }
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