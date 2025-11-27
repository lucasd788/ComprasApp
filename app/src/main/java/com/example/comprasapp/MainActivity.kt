package com.example.comprasapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.comprasapp.adapter.ItemAdapter
import com.example.comprasapp.data.local.Item
import com.example.comprasapp.databinding.ActivityMainBinding
import com.example.comprasapp.databinding.DialogItemBinding
import com.example.comprasapp.viewmodel.MainViewModel
import com.example.comprasapp.viewmodel.MainViewModelFactory
import com.google.android.material.chip.Chip
import java.text.NumberFormat
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: ItemAdapter

    private val viewModel: MainViewModel by viewModels {
        MainViewModelFactory((application as ComprasApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configurarRecyclerView()
        configurarObservadores()
        configurarBotoesInterface()
    }

    private fun configurarRecyclerView() {
        adapter = ItemAdapter(
            aoClicarSomar = { item ->
                viewModel.atualizar(item.copy(quantidade = item.quantidade + 1))
            },
            aoClicarSubtrair = { item ->
                if (item.quantidade > 1) {
                    viewModel.atualizar(item.copy(quantidade = item.quantidade - 1))
                } else {
                    mostrarDialogoConfirmarExclusao(item)
                }
            },
            aoClicarItem = { item ->
                viewModel.atualizar(item.copy(comprado = !item.comprado))
            },
            aoClicarLongo = { item ->
                mostrarDialogoEdicao(item)
            }
        )

        binding.rvListaItens.layoutManager = LinearLayoutManager(this)
        binding.rvListaItens.adapter = adapter
    }

    private fun configurarObservadores() {
        viewModel.itensExibidos.observe(this) { lista ->
            adapter.submitList(lista)
        }

        viewModel.custoTotal.observe(this) { total ->
            val valorFormatado = NumberFormat.getCurrencyInstance(Locale("pt", "BR")).format(total ?: 0.0)
            binding.txtTotalGeral.text = valorFormatado
        }
    }

    private fun configurarBotoesInterface() {
        binding.fabAdicionar.setOnClickListener {
            mostrarDialogoEdicao(null)
        }

        binding.btnOpcoes.setOnClickListener { view ->
            mostrarMenuOpcoes(view)
        }

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }
        })
    }

    private fun mostrarMenuOpcoes(view: View) {
        val popup = PopupMenu(this, view)
        popup.menuInflater.inflate(R.menu.menu_principal, popup.menu)

        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.ordem_alfabetica -> {
                    viewModel.mudarOrdem(MainViewModel.Ordem.ALFABETICA)
                    true
                }
                R.id.ordem_preco_crescente -> {
                    viewModel.mudarOrdem(MainViewModel.Ordem.PRECO_CRESCENTE)
                    true
                }
                R.id.ordem_preco_decrescente -> {
                    viewModel.mudarOrdem(MainViewModel.Ordem.PRECO_DECRESCENTE)
                    true
                }
                R.id.ordem_quantidade -> {
                    viewModel.mudarOrdem(MainViewModel.Ordem.QUANTIDADE)
                    true
                }

                R.id.menu_excluir_comprados -> {
                    viewModel.excluirComprados()
                    Toast.makeText(this, "Itens riscados excluídos", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.menu_desmarcar_todos -> {
                    viewModel.desmarcarTodos()
                    Toast.makeText(this, "Todos os itens desmarcados", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun mostrarDialogoEdicao(item: Item?) {
        val dialogBinding = DialogItemBinding.inflate(layoutInflater)

        if (item != null) {
            dialogBinding.etNome.setText(item.nome)
            dialogBinding.etQuantidade.setText(item.quantidade.toString())
            dialogBinding.etPreco.setText(item.precoEstimado.toString())

            val chipId = when (item.unidade) {
                "KG" -> R.id.chipKG
                "G" -> R.id.chipG
                "L" -> R.id.chipL
                "ML" -> R.id.chipML
                else -> R.id.chipUN
            }
            dialogBinding.chipGroupUnidade.check(chipId)
        }

        AlertDialog.Builder(this)
            .setTitle(if (item == null) "Novo Item" else "Editar Item")
            .setView(dialogBinding.root)
            .setPositiveButton("Salvar") { _, _ ->
                salvarItem(dialogBinding, item)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun salvarItem(dialogBinding: DialogItemBinding, itemExistente: Item?) {
        val nome = dialogBinding.etNome.text.toString()
        val qtd = dialogBinding.etQuantidade.text.toString().toIntOrNull() ?: 1
        val preco = dialogBinding.etPreco.text.toString().toDoubleOrNull() ?: 0.0

        val chipSelecionadoId = dialogBinding.chipGroupUnidade.checkedChipId
        val unidade = if (chipSelecionadoId != View.NO_ID) {
            dialogBinding.root.findViewById<Chip>(chipSelecionadoId).text.toString()
        } else {
            "UN"
        }

        if (nome.isBlank()) {
            Toast.makeText(this, "O nome não pode ser vazio", Toast.LENGTH_SHORT).show()
            return
        }

        if (itemExistente == null) {
            val novoItem = Item(
                nome = nome,
                quantidade = qtd,
                precoEstimado = preco,
                unidade = unidade
            )
            viewModel.inserir(novoItem)
        } else {
            val itemAtualizado = itemExistente.copy(
                nome = nome,
                quantidade = qtd,
                precoEstimado = preco,
                unidade = unidade
            )
            viewModel.atualizar(itemAtualizado)
        }
    }

    private fun mostrarDialogoConfirmarExclusao(item: Item) {
        AlertDialog.Builder(this)
            .setTitle("Remover Item")
            .setMessage("Deseja remover '${item.nome}' da lista?")
            .setPositiveButton("Sim") { _, _ ->
                viewModel.excluir(item)
            }
            .setNegativeButton("Não", null)
            .show()
    }
}