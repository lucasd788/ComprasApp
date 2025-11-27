package com.example.comprasapp.adapter

import com.example.comprasapp.R
import android.graphics.Color
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.comprasapp.data.local.Item
import com.example.comprasapp.databinding.ItemListaBinding
import java.text.NumberFormat
import java.util.Locale

class ItemAdapter(
    private val aoClicarSomar: (Item) -> Unit,
    private val aoClicarSubtrair: (Item) -> Unit,
    private val aoSegurarSomar: (Item) -> Unit,
    private val aoSegurarSubtrair: (Item) -> Unit,
    private val aoClicarItem: (Item) -> Unit,
    private val aoClicarLongo: (Item) -> Unit
) : ListAdapter<Item, ItemAdapter.ItemViewHolder>(ComparadorDeItens()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding = ItemListaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    inner class ItemViewHolder(private val binding: ItemListaBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Item) {
            binding.txtNome.text = item.nome

            val qtdFormatada = when {
                item.unidade.equals("KG", ignoreCase = true) && item.quantidade > 0.0 && item.quantidade < 1.0 -> {
                    val gramas = (item.quantidade * 1000).toInt()
                    "${gramas}g"
                }
                item.quantidade % 1.0 == 0.0 -> {
                    val qtdInteira = item.quantidade.toInt()
                    "$qtdInteira${item.unidade.lowercase()}"
                }
                else -> {
                    val qtd = item.quantidade.toString().trimEnd('0').trimEnd('.')
                    "$qtd${item.unidade.lowercase()}"
                }
            }
            binding.txtQuantidade.text = qtdFormatada

            val formatoMoeda = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR"))
            val precoUnitario = formatoMoeda.format(item.precoEstimado)
            binding.txtDetalhes.text = "$precoUnitario / ${item.unidade}"

            val totalItem = item.precoEstimado * item.quantidade
            binding.txtPrecoTotal.text = formatoMoeda.format(totalItem)

            binding.btnMais.setOnClickListener { aoClicarSomar(item) }
            binding.btnMenos.setOnClickListener { aoClicarSubtrair(item) }

            binding.btnMais.setOnLongClickListener {
                aoSegurarSomar(item)
                true
            }
            binding.btnMenos.setOnLongClickListener {
                aoSegurarSubtrair(item)
                true
            }

            binding.root.setOnClickListener { aoClicarItem(item) }
            binding.root.setOnLongClickListener {
                aoClicarLongo(item)
                true
            }

            if (item.quantidade == 0.0) {
                binding.fundoCard.alpha = 0.5f
                binding.txtPrecoTotal.text = ""
            } else {
                binding.fundoCard.alpha = 1.0f
            }

            atualizarVisualComprado(item.comprado)
        }

        private fun atualizarVisualComprado(estaComprado: Boolean) {
            if (estaComprado) {
                binding.txtNome.paintFlags = binding.txtNome.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                binding.txtNome.setTextColor(Color.GRAY)
                binding.txtPrecoTotal.setTextColor(Color.GRAY)
            } else {
                binding.txtNome.paintFlags = binding.txtNome.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                val corPadrao = androidx.core.content.ContextCompat.getColor(binding.root.context, R.color.text_primary)
                binding.txtNome.setTextColor(corPadrao)
                binding.txtPrecoTotal.setTextColor("#388E3C".toColorInt())
            }
        }
    }

    class ComparadorDeItens : DiffUtil.ItemCallback<Item>() {
        override fun areItemsTheSame(oldItem: Item, newItem: Item): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Item, newItem: Item): Boolean {
            return oldItem == newItem
        }
    }
}