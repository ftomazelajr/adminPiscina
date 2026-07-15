package com.tomazela.adminpiscina.ui.visita

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tomazela.adminpiscina.R
import com.tomazela.adminpiscina.databinding.ItemVisitaBinding
import com.tomazela.adminpiscina.data.models.Visita
import java.text.SimpleDateFormat
import java.util.*

class VisitaAdapter : RecyclerView.Adapter<VisitaAdapter.VisitaViewHolder>() {

    private var visitas = listOf<Visita>()
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "BR"))

    fun submitList(list: List<Visita>) {
        visitas = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VisitaViewHolder {
        val binding = ItemVisitaBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VisitaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VisitaViewHolder, position: Int) {
        holder.bind(visitas[position])
    }

    override fun getItemCount() = visitas.size

    inner class VisitaViewHolder(
        private val binding: ItemVisitaBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(visita: Visita) {
            binding.tvCliente.text = "Cliente: ${visita.clienteNome}"
            binding.tvData.text = "Data: ${visita.dataVisita}"
            binding.tvParamentros.text = "pH: ${visita.ph} | Alc: ${visita.alcalinidade} | Cloro: ${visita.cloro}"
            binding.tvServicos.text = "Serviços: ${visita.servicos.joinToString(", ")}"
            binding.tvStatus.text = visita.status
            binding.tvStatus.setTextColor(
                when (visita.status) {
                    "Pendente" -> binding.root.context.getColor(R.color.warning_orange)
                    "Aprovado" -> binding.root.context.getColor(R.color.success_green)
                    else -> binding.root.context.getColor(R.color.danger_red)
                }
            )
        }
    }
}
