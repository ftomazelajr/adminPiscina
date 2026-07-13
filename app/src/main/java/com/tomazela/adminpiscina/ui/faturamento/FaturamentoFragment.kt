package com.tomazela.adminpiscina.ui.faturamento

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.*
import com.tomazela.adminpiscina.R
import com.tomazela.adminpiscina.databinding.FragmentFaturamentoBinding
import com.tomazela.adminpiscina.data.models.Fatura
import java.text.NumberFormat
import java.util.Locale

class FaturamentoFragment : Fragment() {
    private var _binding: FragmentFaturamentoBinding? = null
    private val binding get() = _binding!!
    private lateinit var database: DatabaseReference
    private lateinit var adapter: FaturaAdapter
    private val faturas = mutableListOf<Fatura>()
    private var totalPendente = 0.0
    private var totalPago = 0.0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFaturamentoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        database = FirebaseDatabase.getInstance().getReference()

        setupRecyclerView()
        carregarFaturas()
    }

    private fun setupRecyclerView() {
        adapter = FaturaAdapter(
            onPagar = { fatura -> pagarFatura(fatura) }
        )
        binding.rvFaturas.layoutManager = LinearLayoutManager(context)
        binding.rvFaturas.adapter = adapter
    }

    private fun carregarFaturas() {
        binding.progressFaturamento.visibility = View.VISIBLE
        binding.tvTotalFaturas.text = "R$ 0,00"
        binding.tvPendente.text = "R$ 0,00"
        binding.tvPago.text = "R$ 0,00"

        database.child("faturas").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                faturas.clear()
                totalPendente = 0.0
                totalPago = 0.0

                if (!snapshot.exists()) {
                    binding.progressFaturamento.visibility = View.GONE
                    adapter.submitList(emptyList())
                    atualizarResumo()
                    Toast.makeText(context, "Nenhuma fatura encontrada. Crie uma no PDV!", Toast.LENGTH_LONG).show()
                    return
                }

                snapshot.children.forEach { child ->
                    try {
                        val fatura = child.getValue(Fatura::class.java)
                        if (fatura != null) {
                            val faturaComId = fatura.copy(id = child.key ?: "")
                            faturas.add(faturaComId)
                            if (faturaComId.status == "Pendente") {
                                totalPendente += faturaComId.totalFatura
                            } else {
                                totalPago += faturaComId.totalFatura
                            }
                        }
                    } catch (e: Exception) {
                        // Ignorar itens com erro
                    }
                }

                adapter.submitList(faturas.sortedByDescending { it.dataFechamento })
                atualizarResumo()
                binding.progressFaturamento.visibility = View.GONE
            }

            override fun onCancelled(error: DatabaseError) {
                binding.progressFaturamento.visibility = View.GONE
                Toast.makeText(context, "Erro ao carregar faturas: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun atualizarResumo() {
        val formatador = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
        val total = totalPendente + totalPago
        
        binding.tvTotalFaturas.text = formatador.format(total)
        binding.tvPendente.text = formatador.format(totalPendente)
        binding.tvPago.text = formatador.format(totalPago)
    }

    private fun pagarFatura(fatura: Fatura) {
        database.child("faturas").child(fatura.id).child("status").setValue("Pago")
            .addOnSuccessListener {
                Toast.makeText(context, "Fatura paga com sucesso!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Erro ao pagar fatura", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
