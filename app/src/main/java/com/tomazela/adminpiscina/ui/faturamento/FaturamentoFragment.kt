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

        database.child("faturas").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                faturas.clear()
                totalPendente = 0.0
                totalPago = 0.0

                snapshot.children.forEach { child ->
                    val fatura = child.getValue(Fatura::class.java)?.copy(id = child.key ?: "")
                    fatura?.let {
                        faturas.add(it)
                        if (it.status == "Pendente") {
                            totalPendente += it.totalFatura
                        } else {
                            totalPago += it.totalFatura
                        }
                    }
                }

                adapter.submitList(faturas.sortedByDescending { it.dataFechamento })
                atualizarResumo()
                binding.progressFaturamento.visibility = View.GONE
            }

            override fun onCancelled(error: DatabaseError) {
                binding.progressFaturamento.visibility = View.GONE
                Toast.makeText(context, "Erro ao carregar faturas", Toast.LENGTH_SHORT).show()
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
