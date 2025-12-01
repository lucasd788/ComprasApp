package com.example.comprasapp

import android.os.Bundle
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.comprasapp.adapter.ItemAdapter
import com.example.comprasapp.adapter.ListaAdapter
import com.example.comprasapp.data.local.Item
import com.example.comprasapp.data.local.ShoppingList
import com.example.comprasapp.databinding.ActivityMainBinding
import com.example.comprasapp.databinding.BottomSheetListasBinding
import com.example.comprasapp.databinding.DialogItemBinding
import com.example.comprasapp.viewmodel.MainViewModel
import com.example.comprasapp.viewmodel.MainViewModelFactory
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import java.text.NumberFormat
import java.util.Locale
import androidx.core.content.edit

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: ItemAdapter

    private var adapterMenuSuspenso: ListaAdapter? = null

    private val viewModel: MainViewModel by viewModels {
        MainViewModelFactory((application as ComprasApplication).repository)
    }

    private var listasAtivasIds: List<Int> = emptyList()
    private var todasAsListasCache: List<ShoppingList> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configurarRecyclerView()
        configurarObservadores()
        configurarBotoesInterface()
    }

    private fun configurarObservadores() {
        viewModel.todasAsListas.observe(this) { listas ->
            todasAsListasCache = listas
            atualizarTituloLista()
            adapterMenuSuspenso?.submitList(listas)
            adapter.atualizarNomesListas(listas)

            if (listas.isEmpty()) {
                viewModel.criarLista("Minha Lista")
            } else if (viewModel.listasAtivas.value.isNullOrEmpty()) {
                viewModel.mudarListaAtiva(listas.first().id)
            }
        }

        viewModel.listasAtivas.observe(this) { ids ->
            listasAtivasIds = ids
            atualizarTituloLista()
            adapter.isVisualizacaoCombinada = ids.size > 1
            adapter.notifyDataSetChanged()
        }

        viewModel.itensExibidos.observe(this) { lista ->
            adapter.submitList(lista)
        }

        viewModel.custoTotal.observe(this) { total ->
            val valorFormatado = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR")).format(total ?: 0.0)
            binding.txtTotalGeral.text = valorFormatado
        }
    }

    private fun atualizarTituloLista() {
        if (listasAtivasIds.isEmpty()) {
            binding.txtNomeListaAtual.text = "Selecione uma lista"
            return
        }

        if (listasAtivasIds.size == 1) {
            val lista = todasAsListasCache.find { it.id == listasAtivasIds[0] }
            binding.txtNomeListaAtual.text = lista?.nome ?: "Lista"
        } else {
            binding.txtNomeListaAtual.text = "${listasAtivasIds.size} Listas"
        }
    }

    private fun configurarRecyclerView() {
        adapter = ItemAdapter(
            aoClicarSomar = { item ->
                val novaQtd = arredondar(item.quantidade + 1.0)
                viewModel.atualizar(item.copy(quantidade = novaQtd))
            },
            aoClicarSubtrair = { item ->
                if (item.quantidade > 0.0) {
                    val novaQtd = arredondar((item.quantidade - 1.0).coerceAtLeast(0.0))
                    viewModel.atualizar(item.copy(quantidade = novaQtd))
                } else {
                    mostrarDialogoConfirmarExclusao(item)
                }
            },
            aoSegurarSomar = { item ->
                val novaQtd = arredondar(item.quantidade + 0.1)
                viewModel.atualizar(item.copy(quantidade = novaQtd))
            },
            aoSegurarSubtrair = { item ->
                if (item.quantidade > 0.0) {
                    val novaQtd = arredondar((item.quantidade - 0.1).coerceAtLeast(0.0))
                    viewModel.atualizar(item.copy(quantidade = novaQtd))
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

    private fun configurarBotoesInterface() {
        binding.fabAdicionar.setOnClickListener {
            prepararAdicaoItem()
        }

        binding.btnSelecionarLista.setOnClickListener {
            mostrarBottomSheetListas()
        }

        binding.btnOpcoes.setOnClickListener { view ->
            mostrarMenuOpcoes(view)
        }

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                viewModel.buscar(query ?: "")
                return true
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.buscar(newText ?: "")
                return true
            }
        })
    }

    private fun mostrarBottomSheetListas() {
        val sheetDialog = BottomSheetDialog(this)
        val sheetBinding = BottomSheetListasBinding.inflate(layoutInflater)
        sheetDialog.setContentView(sheetBinding.root)

        var idsParaVisualizar: Set<Int> = emptySet()

        val listaAdapter = ListaAdapter(
            onClick = { lista ->
                viewModel.mudarListaAtiva(lista.id)
                sheetDialog.dismiss()
            },
            onLongClick = {
                adapterMenuSuspenso?.modoSelecao = true
            },
            onEditClick = { lista ->
                sheetDialog.dismiss()
                mostrarDialogoGerenciarLista(lista)
            },
            onSelectionChanged = { ids ->
                idsParaVisualizar = ids
                if (ids.isNotEmpty()) {
                    sheetBinding.btnConfirmarSelecao.text = "Visualizar ${ids.size} listas"
                    sheetBinding.btnConfirmarSelecao.visibility = View.VISIBLE
                } else {
                    sheetBinding.btnConfirmarSelecao.visibility = View.GONE
                }
            }
        )

        sheetBinding.rvListas.layoutManager = LinearLayoutManager(this)
        sheetBinding.rvListas.adapter = listaAdapter

        adapterMenuSuspenso = listaAdapter

        listaAdapter.submitList(todasAsListasCache)

        sheetDialog.setOnDismissListener {
            adapterMenuSuspenso = null
        }

        sheetBinding.btnNovaLista.setOnClickListener {
            sheetDialog.dismiss()
            mostrarDialogoNovaLista()
        }

        sheetBinding.btnConfirmarSelecao.setOnClickListener {
            viewModel.mudarListasAtivas(idsParaVisualizar.toList())
            sheetDialog.dismiss()
        }

        sheetDialog.show()
    }

    private fun mostrarDialogoNovaLista() {
        val dialogBinding = com.example.comprasapp.databinding.DialogListaBinding.inflate(layoutInflater)

        dialogBinding.layoutInputLista.hint = "Ex: Mercado, Farmácia..."

        AlertDialog.Builder(this)
            .setTitle("Nova Lista")
            .setView(dialogBinding.root)
            .setPositiveButton("Criar") { _, _ ->
                val nome = dialogBinding.etNomeLista.text.toString()
                if (nome.isNotBlank()) {
                    viewModel.criarLista(nome)
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun mostrarDialogoGerenciarLista(lista: ShoppingList) {
        val dialogBinding = com.example.comprasapp.databinding.DialogListaBinding.inflate(layoutInflater)

        dialogBinding.etNomeLista.setText(lista.nome)
        dialogBinding.etNomeLista.setSelection(lista.nome.length)
        dialogBinding.layoutInputLista.hint = "Nome da Lista"

        AlertDialog.Builder(this)
            .setTitle("Gerenciar Lista")
            .setView(dialogBinding.root)
            .setPositiveButton("Renomear") { _, _ ->
                val novoNome = dialogBinding.etNomeLista.text.toString()
                if (novoNome.isNotBlank()) {
                    viewModel.renomearLista(lista, novoNome)
                }
            }
            .setNegativeButton("Cancelar", null)
            .setNeutralButton("Excluir") { _, _ ->
                mostrarConfirmacaoExclusaoLista(lista)
            }
            .show()
    }

    private fun prepararAdicaoItem() {
        if (listasAtivasIds.size == 1) {
            mostrarDialogoEdicao(null, listasAtivasIds[0])
        } else {
            mostrarSeletorDeListaParaAdicao()
        }
    }

    private fun mostrarSeletorDeListaParaAdicao() {
        val listasParaEscolha = todasAsListasCache.filter { listasAtivasIds.contains(it.id) }
        val nomes = listasParaEscolha.map { it.nome }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("Adicionar em qual lista?")
            .setItems(nomes) { _, which ->
                val listaEscolhida = listasParaEscolha[which]
                mostrarDialogoEdicao(null, listaEscolhida.id)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun mostrarConfirmacaoExclusaoLista(lista: ShoppingList) {
        AlertDialog.Builder(this)
            .setTitle("Excluir Lista?")
            .setMessage("A lista '${lista.nome}' e TODOS os seus itens serão apagados para sempre.")
            .setPositiveButton("Excluir") { _, _ ->
                viewModel.excluirLista(lista)
                Toast.makeText(this, "Lista excluída", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun mostrarDialogoEdicao(item: Item?, idListaDestino: Int? = null) {
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

        val builder = AlertDialog.Builder(this)
            .setTitle(if (item == null) "Novo Item" else "Editar Item")
            .setView(dialogBinding.root)
            .setPositiveButton("Salvar") { _, _ ->
                val listaIdFinal = item?.listId ?: idListaDestino ?: listasAtivasIds.firstOrNull() ?: 0

                if (listaIdFinal != 0) {
                    salvarItem(dialogBinding, item, listaIdFinal)
                } else {
                    Toast.makeText(this, "Erro: Nenhuma lista selecionada", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)

        if (item != null) {
            builder.setNeutralButton("Excluir") { _, _ ->
                mostrarDialogoConfirmarExclusao(item)
            }
        }

        builder.show()
    }

    private fun salvarItem(binding: DialogItemBinding, itemExistente: Item?, listId: Int) {
        val nome = binding.etNome.text.toString()
        val qtd = binding.etQuantidade.text.toString().toDoubleOrNull() ?: 1.0
        val preco = binding.etPreco.text.toString().toDoubleOrNull() ?: 0.0

        val chipSelecionadoId = binding.chipGroupUnidade.checkedChipId
        val unidade = if (chipSelecionadoId != View.NO_ID) {
            binding.root.findViewById<Chip>(chipSelecionadoId).text.toString()
        } else {
            "UN"
        }

        if (nome.isBlank()) return

        if (itemExistente == null) {
            val novoItem = Item(
                listId = listId,
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
            .setPositiveButton("Sim") { _, _ -> viewModel.excluir(item) }
            .setNegativeButton("Não", null)
            .show()
    }

    private fun mostrarMenuOpcoes(view: View) {
        val popup = PopupMenu(this, view)
        popup.menuInflater.inflate(R.menu.menu_principal, popup.menu)
        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.ordem_alfabetica -> { viewModel.mudarOrdem(MainViewModel.Ordem.ALFABETICA); true }
                R.id.ordem_preco_crescente -> { viewModel.mudarOrdem(MainViewModel.Ordem.PRECO_CRESCENTE); true }
                R.id.ordem_preco_decrescente -> { viewModel.mudarOrdem(MainViewModel.Ordem.PRECO_DECRESCENTE); true }
                R.id.ordem_quantidade -> { viewModel.mudarOrdem(MainViewModel.Ordem.QUANTIDADE); true }
                R.id.menu_excluir_comprados -> { viewModel.excluirComprados(); true }
                R.id.menu_desmarcar_todos -> { viewModel.desmarcarTodos(); true }
                R.id.menu_tema -> { alternarTema(); true }
                else -> false
            }
        }
        popup.show()
    }

    private fun alternarTema() {
        val preferencias = getSharedPreferences("app_preferencias", MODE_PRIVATE)
        val modoEscuroAtual = preferencias.getBoolean("modo_escuro", true)
        val novoModoEscuro = !modoEscuroAtual
        preferencias.edit { putBoolean("modo_escuro", novoModoEscuro) }
        val modoParaAplicar = if (novoModoEscuro) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        AppCompatDelegate.setDefaultNightMode(modoParaAplicar)
    }

    private fun arredondar(valor: Double): Double {
        return kotlin.math.round(valor * 1000) / 1000.0
    }
}