package com.tomazela.adminpiscina.ui.produtos

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tomazela.adminpiscina.R
import com.tomazela.adminpiscina.databinding.ItemProdutoBinding
import com.tomazela.adminpiscina.data.models.Produto
import java.text.NumberFormat
import java.util.Locale

class ProdutoAdapter(
    private val onEdit: (Produto) -> Unit,
    private val onDelete: (Produto) -> Unit
) : RecyclerView.Adapter<ProdutoAdapter.ProdutoViewHolder>() {

    private var produtos = listOf<Produto>()

    fun submitList(list: List<Produto>) {
        produtos = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProdutoViewHolder {
        val binding = ItemProdutoBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ProdutoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProdutoViewHolder, position: Int) {
        holder.bind(produtos[position])
    }

    override fun getItemCount() = produtos.size

    inner class ProdutoViewHolder(
        private val binding: ItemProdutoBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(produto: Produto) {
            binding.tvProdutoNome.text = produto.nome
            val formatador = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
            binding.tvProdutoPreco.text = formatador.format(produto.preco)
            binding.tvProdutoDescricao.text = produto.descricao ?: "Sem descrição"
            
            binding.tvProdutoStatus.text = if (produto.pausado) "Pausado" else "Ativo"
            binding.tvProdutoStatus.setTextColor(
                if (produto.pausado) {
                    binding.root.context.getColor(R.color.danger_red)
                } else {
                    binding.root.context.getColor(R.color.success_green)
                }
            )

            binding.btnEditarProduto.setOnClickListener { onEdit(produto) }
            binding.btnExcluirProduto.setOnClickListener { onDelete(produto) }
        }
    }
}
