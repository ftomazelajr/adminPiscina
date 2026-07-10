package com.tomazela.adminpiscina.ui.recebimentos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.*
import com.tomazela.adminpiscina.R
import com.tomazela.adminpiscina.databinding.FragmentRecebimentosBinding
import com.tomazela.adminpiscina.data.models.Fatura

class RecebimentosFragment : Fragment() {
    private var _binding: FragmentRecebimentosBinding? = null
    private val binding get() = _binding!!
    private lateinit var database: DatabaseReference
    private lateinit var adapter: RecebimentoAdapter
    
    private val recebimentosPendentes = mutableListOf<Fatura>()
    private val recebimentosPagos = mutableListOf<Fatura>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecebimentosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        database = FirebaseDatabase.getInstance().getReference()

        setupRecyclerView()
        setupTabs()
        carregarRecebimentos()
    }

    private fun setupRecyclerView() {
        adapter = RecebimentoAdapter(
            onReceber = { fatura -> receberFatura(fatura) }
        )
        binding.rvRecebimentos.layoutManager = LinearLayoutManager(context)
        binding.rvRecebimentos.adapter = adapter
    }

    private fun setupTabs() {
        binding.tabLayout.addOnTabSelectedListener(object : com.google.android.material.tabs.TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: com.google.android.material.tabs.TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {
                        adapter.submitList(recebimentosPendentes.sortedByDescending { it.dataFechamento })
                        binding.progressRecebimentos.visibility = View.GONE
                    }
                    1 -> {
                        adapter.submitList(recebimentosPagos.sortedByDescending { it.dataFechamento })
                        binding.progressRecebimentos.visibility = View.GONE
                    }
                }
            }

            override fun onTabUnselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
            override fun onTabReselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
        })
        
        // Selecionar a primeira tab por padrão
        binding.tabLayout.getTabAt(0)?.select()
    }

    private fun carregarRecebimentos() {
        binding.progressRecebimentos.visibility = View.VISIBLE

        database.child("faturas").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                recebimentosPendentes.clear()
                recebimentosPagos.clear()

                snapshot.children.forEach { child ->
                    try {
                        val fatura = child.getValue(Fatura::class.java)
                        if (fatura != null) {
                            val faturaComId = fatura.copy(id = child.key ?: "")
                            if (faturaComId.status == "Pendente") {
                                recebimentosPendentes.add(faturaComId)
                            } else {
                                recebimentosPagos.add(faturaComId)
                            }
                        }
                    } catch (e: Exception) {
                        // Ignorar itens com erro
                    }
                }

                // Mostrar pendentes por padrão
                adapter.submitList(recebimentosPendentes.sortedByDescending { it.dataFechamento })
                binding.progressRecebimentos.visibility = View.GONE
            }

            override fun onCancelled(error: DatabaseError) {
                binding.progressRecebimentos.visibility = View.GONE
                Toast.makeText(context, "Erro ao carregar recebimentos: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun receberFatura(fatura: Fatura) {
        database.child("faturas").child(fatura.id).child("status").setValue("Pago")
            .addOnSuccessListener {
                Toast.makeText(context, "Recebimento registrado!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Erro ao registrar recebimento", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
