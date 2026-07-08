package com.tomazela.adminpiscina.ui.clientes

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tomazela.adminpiscina.R
import com.tomazela.adminpiscina.databinding.ItemClienteBinding
import com.tomazela.adminpiscina.data.models.Cliente
import java.text.NumberFormat
import java.util.Locale

class ClienteAdapter(
    private val onEdit: (Cliente) -> Unit,
    private val onDelete: (Cliente) -> Unit
) : RecyclerView.Adapter<ClienteAdapter.ClienteViewHolder>() {

    private var clientes = listOf<Cliente>()

    fun submitList(list: List<Cliente>) {
        clientes = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClienteViewHolder {
        val binding = ItemClienteBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ClienteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ClienteViewHolder, position: Int) {
        holder.bind(clientes[position])
    }

    override fun getItemCount() = clientes.size

    inner class ClienteViewHolder(
        private val binding: ItemClienteBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(cliente: Cliente) {
            binding.tvClienteNome.text = cliente.nome
            binding.tvClienteTelefone.text = cliente.telefone
            binding.tvClienteEndereco.text = cliente.enderecoRua ?: "Sem endereço"
            
            val formatador = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
            binding.tvClienteMensalidade.text = "Mensalidade: ${formatador.format(cliente.mensalidade)}"

            binding.btnEditarCliente.setOnClickListener { onEdit(cliente) }
            binding.btnExcluirCliente.setOnClickListener { onDelete(cliente) }
        }
    }
}
