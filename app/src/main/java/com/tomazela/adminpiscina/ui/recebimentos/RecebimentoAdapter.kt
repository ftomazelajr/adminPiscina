package com.tomazela.adminpiscina.ui.recebimentos

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tomazela.adminpiscina.R
import com.tomazela.adminpiscina.databinding.ItemRecebimentoBinding
import com.tomazela.adminpiscina.data.models.Fatura
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class RecebimentoAdapter(
    private val onReceber: (Fatura) -> Unit
) : RecyclerView.Adapter<RecebimentoAdapter.RecebimentoViewHolder>() {

    private var faturas = listOf<Fatura>()
    private val formatador = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))

    fun submitList(list: List<Fatura>) {
        faturas = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecebimentoViewHolder {
        val binding = ItemRecebimentoBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return RecebimentoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecebimentoViewHolder, position: Int) {
        holder.bind(faturas[position])
    }

    override fun getItemCount() = faturas.size

    inner class RecebimentoViewHolder(
        private val binding: ItemRecebimentoBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(fatura: Fatura) {
            binding.tvRecebimentoCliente.text = fatura.cliente.nome
            binding.tvRecebimentoTotal.text = formatador.format(fatura.totalFatura)
            
            try {
                val data = dateFormat.parse(fatura.dataFechamento)
                binding.tvRecebimentoData.text = dateFormat.format(data)
            } catch (e: Exception) {
                binding.tvRecebimentoData.text = fatura.dataFechamento
            }

            val isPendente = fatura.status == "Pendente"
            binding.tvRecebimentoStatus.text = if (isPendente) "Pendente" else "Recebido"
            binding.tvRecebimentoStatus.setTextColor(
                if (isPendente) {
                    binding.root.context.getColor(R.color.warning_orange)
                } else {
                    binding.root.context.getColor(R.color.success_green)
                }
            )

            binding.btnReceber.visibility = if (isPendente) View.VISIBLE else View.GONE
            binding.btnReceber.setOnClickListener {
                onReceber(fatura)
            }
        }
    }
}
