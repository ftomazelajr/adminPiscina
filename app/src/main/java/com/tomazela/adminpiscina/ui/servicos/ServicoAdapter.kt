package com.tomazela.adminpiscina.ui.servicos

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tomazela.adminpiscina.R
import com.tomazela.adminpiscina.databinding.ItemServicoBinding
import com.tomazela.adminpiscina.data.models.Servico
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ServicoAdapter(
    private val onAprovar: (Servico) -> Unit,
    private val onRejeitar: (Servico) -> Unit
) : RecyclerView.Adapter<ServicoAdapter.ServicoViewHolder>() {

    private var servicos = listOf<Servico>()
    private val formatador = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "BR"))

    fun submitList(list: List<Servico>) {
        servicos = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServicoViewHolder {
        val binding = ItemServicoBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ServicoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ServicoViewHolder, position: Int) {
        holder.bind(servicos[position])
    }

    override fun getItemCount() = servicos.size

    inner class ServicoViewHolder(
        private val binding: ItemServicoBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(servico: Servico) {
            binding.tvTipo.text = when (servico.tipo) {
                "PDV" -> "🛒 Venda"
                "Visita" -> "📝 Visita"
                else -> "🔧 Serviço"
            }

            binding.tvCliente.text = "Cliente: ${servico.clienteNome}"
            binding.tvData.text = "Data: ${dateFormat.format(Date(servico.timestamp))}"

            val itensTexto = servico.itens.joinToString { "${it.nome} x${it.quantidade}" }
            binding.tvItens.text = "Itens: $itensTexto"

            binding.tvTotal.text = "Total: ${formatador.format(servico.total)}"

            binding.tvStatus.text = servico.status
            binding.tvStatus.setTextColor(
                when (servico.status) {
                    "Pendente" -> binding.root.context.getColor(R.color.warning_orange)
                    "Aprovado" -> binding.root.context.getColor(R.color.success_green)
                    else -> binding.root.context.getColor(R.color.danger_red)
                }
            )

            if (servico.status == "Pendente") {
                binding.btnAprovar.visibility = android.view.View.VISIBLE
                binding.btnRejeitar.visibility = android.view.View.VISIBLE
            } else {
                binding.btnAprovar.visibility = android.view.View.GONE
                binding.btnRejeitar.visibility = android.view.View.GONE
            }

            // Botão compartilhar - abre o Dialog com a notinha
            binding.btnCompartilhar.setOnClickListener {
                val dialog = NotinhaDialog(binding.root.context, servico)
                dialog.show()
            }

            binding.btnAprovar.setOnClickListener {
                onAprovar(servico)
            }

            binding.btnRejeitar.setOnClickListener {
                onRejeitar(servico)
            }
        }
    }
}
