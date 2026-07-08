package com.tomazela.adminpiscina.ui.pdv

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tomazela.adminpiscina.R
import com.tomazela.adminpiscina.databinding.ItemProdutoPdvBinding
import com.tomazela.adminpiscina.data.models.Produto
import java.text.NumberFormat
import java.util.Locale

class ProdutoPdvAdapter(
    private val onAddClick: (Produto) -> Unit
) : RecyclerView.Adapter<ProdutoPdvAdapter.ProdutoViewHolder>() {

    private var produtos = listOf<Produto>()

    fun submitList(list: List<Produto>) {
        produtos = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProdutoViewHolder {
        val binding = ItemProdutoPdvBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ProdutoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProdutoViewHolder, position: Int) {
        holder.bind(produtos[position])
    }

    override fun getItemCount() = produtos.size

    inner class ProdutoViewHolder(
        private val binding: ItemProdutoPdvBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(produto: Produto) {
            binding.tvProdutoNome.text = produto.nome
            val formatador = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
            binding.tvProdutoPreco.text = formatador.format(produto.preco)

            binding.btnAdicionar.setOnClickListener {
                onAddClick(produto)
            }
        }
    }
}
