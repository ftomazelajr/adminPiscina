package com.tomazela.adminpiscina.ui.pdv

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tomazela.adminpiscina.R
import com.tomazela.adminpiscina.databinding.ItemCarrinhoBinding
import com.tomazela.adminpiscina.data.models.ItemPedido
import java.text.NumberFormat
import java.util.Locale

class CarrinhoAdapter(
    private val onQuantidadeChange: (ItemPedido, Int) -> Unit,
    private val onRemover: (ItemPedido) -> Unit
) : RecyclerView.Adapter<CarrinhoAdapter.CarrinhoViewHolder>() {

    private var itens = listOf<ItemPedido>()

    fun submitList(list: List<ItemPedido>) {
        itens = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarrinhoViewHolder {
        val binding = ItemCarrinhoBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return CarrinhoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CarrinhoViewHolder, position: Int) {
        holder.bind(itens[position])
    }

    override fun getItemCount() = itens.size

    inner class CarrinhoViewHolder(
        private val binding: ItemCarrinhoBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ItemPedido) {
            binding.tvItemNome.text = "${item.quantidade}x ${item.nome}"
            val formatador = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
            binding.tvItemPreco.text = formatador.format(item.precoUnitario * item.quantidade)
            binding.tvQuantidade.text = item.quantidade.toString()

            binding.btnRemover.setOnClickListener {
                if (item.quantidade > 1) {
                    onQuantidadeChange(item, item.quantidade - 1)
                } else {
                    onRemover(item)
                }
            }

            binding.btnAdicionarItem.setOnClickListener {
                onQuantidadeChange(item, item.quantidade + 1)
            }
        }
    }
}
