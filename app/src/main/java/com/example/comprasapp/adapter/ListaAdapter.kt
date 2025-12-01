package com.example.comprasapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.comprasapp.data.local.ShoppingList
import com.example.comprasapp.databinding.ItemListaSelecaoBinding

class ListaAdapter(
    private val onClick: (ShoppingList) -> Unit,
    private val onLongClick: (ShoppingList) -> Unit,
    private val onEditClick: (ShoppingList) -> Unit,
    private val onSelectionChanged: (Set<Int>) -> Unit
) : ListAdapter<ShoppingList, ListaAdapter.ListaViewHolder>(ComparadorListas()) {

    var modoSelecao = false
        set(value) {
            field = value
            idsSelecionados.clear()
            notifyDataSetChanged()
        }

    private val idsSelecionados = mutableSetOf<Int>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListaViewHolder {
        val binding = ItemListaSelecaoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ListaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ListaViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ListaViewHolder(private val binding: ItemListaSelecaoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(lista: ShoppingList) {
            binding.txtNomeLista.text = lista.nome

            if (modoSelecao) {
                binding.cbSelecionado.visibility = View.VISIBLE
                binding.btnEditarLista.visibility = View.GONE
                binding.cbSelecionado.isChecked = idsSelecionados.contains(lista.id)
            } else {
                binding.cbSelecionado.visibility = View.GONE
                binding.btnEditarLista.visibility = View.VISIBLE
            }

            binding.root.setOnClickListener {
                if (modoSelecao) {
                    alternarSelecao(lista.id)
                } else {
                    onClick(lista)
                }
            }

            binding.cbSelecionado.setOnClickListener {
                alternarSelecao(lista.id)
            }

            binding.btnEditarLista.setOnClickListener {
                onEditClick(lista)
            }

            binding.root.setOnLongClickListener {
                if (!modoSelecao) {
                    onLongClick(lista)
                    alternarSelecao(lista.id)
                }
                true
            }
        }

        private fun alternarSelecao(id: Int) {
            if (idsSelecionados.contains(id)) {
                idsSelecionados.remove(id)
            } else {
                idsSelecionados.add(id)
            }
            notifyItemChanged(adapterPosition)
            onSelectionChanged(idsSelecionados)
        }
    }

    class ComparadorListas : DiffUtil.ItemCallback<ShoppingList>() {
        override fun areItemsTheSame(oldItem: ShoppingList, newItem: ShoppingList) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: ShoppingList, newItem: ShoppingList) = oldItem == newItem
    }
}