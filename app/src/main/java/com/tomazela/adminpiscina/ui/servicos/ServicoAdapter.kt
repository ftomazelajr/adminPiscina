package com.tomazela.adminpiscina.ui.servicos

import android.content.Context
import android.content.Intent
import android.net.Uri
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

    private fun gerarNotinha(servico: Servico): String {
        val data = dateFormat.format(Date(servico.timestamp))
        val total = formatador.format(servico.total)
        
        val itensTexto = servico.itens.joinToString("\n") { 
            "  ${it.quantidade}x ${it.nome} - ${formatador.format(it.precoUnitario * it.quantidade)}"
        }
        
        return """
            ========================================
                   🏊 TOMAZELA PISCINAS
                   ========================================
                   
            📅 DATA: $data
            👤 CLIENTE: ${servico.clienteNome}
            
            ----------------------------------------
            ITENS DO PEDIDO:
            $itensTexto
            
            ----------------------------------------
            TOTAL: $total
            ----------------------------------------
            
            ✅ STATUS: ${servico.status.toUpperCase()}
            
            ========================================
            Obrigado pela preferência!
            ========================================
        """.trimIndent()
    }

    private fun compartilharWhatsApp(context: Context, servico: Servico) {
        val notinha = gerarNotinha(servico)
        
        // Tentar obter o telefone do cliente (se disponível)
        val telefone = servico.clienteId // Pode ser melhor ter um campo de telefone
        
        val intent = Intent(Intent.ACTION_VIEW)
        val url = "https://api.whatsapp.com/send?text=${Uri.encode(notinha)}"
        intent.data = Uri.parse(url)
        
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            // Se WhatsApp não estiver instalado, abrir com qualquer app de compartilhamento
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_TEXT, notinha)
            shareIntent.putExtra(Intent.EXTRA_TITLE, "Notinha do Pedido")
            
            try {
                context.startActivity(Intent.createChooser(shareIntent, "Compartilhar Notinha"))
            } catch (ex: Exception) {
                // Fallback: mostrar toast
                android.widget.Toast.makeText(context, "Erro ao compartilhar", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

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

            // Mostrar/ocultar botões de aprovação/rejeição
            if (servico.status == "Pendente") {
                binding.btnAprovar.visibility = android.view.View.VISIBLE
                binding.btnRejeitar.visibility = android.view.View.VISIBLE
            } else {
                binding.btnAprovar.visibility = android.view.View.GONE
                binding.btnRejeitar.visibility = android.view.View.GONE
            }

            // Botão compartilhar sempre visível
            binding.btnCompartilhar.setOnClickListener {
                compartilharWhatsApp(binding.root.context, servico)
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
