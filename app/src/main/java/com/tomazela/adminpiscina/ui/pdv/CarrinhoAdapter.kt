package com.tomazela.adminpiscina.ui.pdv

import android.text.Editable
import android.text.TextWatcher
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
    private val onPrecoChange: (ItemPedido, Double) -> Unit,
    private val onRemover: (ItemPedido) -> Unit
) : RecyclerView.Adapter<CarrinhoAdapter.CarrinhoViewHolder>() {

    private var itens = listOf<ItemPedido>()
    private val formatador = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))

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

        private var isUpdating = false

        fun bind(item: ItemPedido) {
            binding.tvItemNome.text = "${item.quantidade}x ${item.nome}"
            
            // Preço unitário formatado (sem R$)
            val precoFormatado = formatador.format(item.precoUnitario).replace("R$", "").trim()
            binding.etPrecoUnitario.setText(precoFormatado)
            
            binding.tvQuantidade.text = item.quantidade.toString()

            // Remover listener antigo para evitar duplicação
            binding.etPrecoUnitario.removeTextChangedListener(precoWatcher)
            
            // Adicionar novo listener
            binding.etPrecoUnitario.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                
                override fun afterTextChanged(s: Editable?) {
                    if (isUpdating) return
                    if (s.isNullOrEmpty()) return
                    
                    val position = adapterPosition
                    if (position == RecyclerView.NO_POSITION) return
                    
                    val item = itens[position]
                    try {
                        val precoStr = s.toString().replace(",", ".").trim()
                        val novoPreco = precoStr.toDoubleOrNull()
                        if (novoPreco != null && novoPreco >= 0) {
                            isUpdating = true
                            onPrecoChange(item, novoPreco)
                            // Atualizar a exibição formatada
                            val formatado = formatador.format(novoPreco).replace("R$", "").trim()
                            binding.etPrecoUnitario.setText(formatado)
                            binding.etPrecoUnitario.setSelection(formatado.length)
                            isUpdating = false
                        }
                    } catch (e: Exception) {
                        // Ignorar erros de parsing
                        isUpdating = false
                    }
                }
            })

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
