package com.tomazela.adminpiscina.ui.faturamento

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tomazela.adminpiscina.R
import com.tomazela.adminpiscina.databinding.ItemFaturaBinding
import com.tomazela.adminpiscina.data.models.Fatura
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class FaturaAdapter(
    private val onPagar: (Fatura) -> Unit
) : RecyclerView.Adapter<FaturaAdapter.FaturaViewHolder>() {

    private var faturas = listOf<Fatura>()
    private val formatador = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))

    fun submitList(list: List<Fatura>) {
        faturas = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FaturaViewHolder {
        val binding = ItemFaturaBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return FaturaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FaturaViewHolder, position: Int) {
        holder.bind(faturas[position])
    }

    override fun getItemCount() = faturas.size

    inner class FaturaViewHolder(
        private val binding: ItemFaturaBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(fatura: Fatura) {
            binding.tvFaturaCliente.text = fatura.cliente.nome
            binding.tvFaturaTotal.text = formatador.format(fatura.totalFatura)
            
            try {
                val data = dateFormat.parse(fatura.dataFechamento)
                binding.tvFaturaData.text = dateFormat.format(data)
            } catch (e: Exception) {
                binding.tvFaturaData.text = fatura.dataFechamento
            }

            val isPendente = fatura.status == "Pendente"
            binding.tvFaturaStatus.text = if (isPendente) "Pendente" else "Pago"
            binding.tvFaturaStatus.setTextColor(
                if (isPendente) {
                    binding.root.context.getColor(R.color.warning_orange)
                } else {
                    binding.root.context.getColor(R.color.success_green)
                }
            )

            binding.btnPagar.visibility = if (isPendente) View.VISIBLE else View.GONE
            binding.btnPagar.setOnClickListener {
                onPagar(fatura)
            }
        }
    }
}
