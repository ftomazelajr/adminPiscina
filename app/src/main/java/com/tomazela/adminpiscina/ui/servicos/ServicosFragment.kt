package com.tomazela.adminpiscina.ui.servicos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.*
import com.tomazela.adminpiscina.R
import com.tomazela.adminpiscina.databinding.FragmentServicosBinding
import com.tomazela.adminpiscina.data.models.Servico

class ServicosFragment : Fragment() {
    private var _binding: FragmentServicosBinding? = null
    private val binding get() = _binding!!
    private lateinit var database: DatabaseReference
    private lateinit var adapter: ServicoAdapter
    
    private val servicosPendentes = mutableListOf<Servico>()
    private val servicosAprovados = mutableListOf<Servico>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentServicosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        database = FirebaseDatabase.getInstance().getReference()

        setupRecyclerView()
        setupTabs()
        carregarPendentes()
        carregarAprovados()
    }

    private fun setupRecyclerView() {
        adapter = ServicoAdapter(
            onAprovar = { servico -> aprovarServico(servico) },
            onRejeitar = { servico -> rejeitarServico(servico) }
        )
        binding.rvServicos.layoutManager = LinearLayoutManager(context)
        binding.rvServicos.adapter = adapter
        binding.progressServicos.visibility = View.GONE
    }

    private fun setupTabs() {
        binding.tabLayout.addOnTabSelectedListener(object : com.google.android.material.tabs.TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: com.google.android.material.tabs.TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {
                        if (servicosPendentes.isEmpty()) {
                            binding.progressServicos.visibility = View.VISIBLE
                            carregarPendentes()
                        } else {
                            adapter.submitList(servicosPendentes.sortedByDescending { it.timestamp })
                            binding.progressServicos.visibility = View.GONE
                        }
                    }
                    1 -> {
                        if (servicosAprovados.isEmpty()) {
                            binding.progressServicos.visibility = View.VISIBLE
                            carregarAprovados()
                        } else {
                            adapter.submitList(servicosAprovados.sortedByDescending { it.timestamp })
                            binding.progressServicos.visibility = View.GONE
                        }
                    }
                }
            }

            override fun onTabUnselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
            override fun onTabReselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
        })
        
        // Selecionar a primeira tab por padrão
        binding.tabLayout.getTabAt(0)?.select()
    }

    private fun carregarPendentes() {
        binding.progressServicos.visibility = View.VISIBLE
        
        database.child("pedidos_pendentes").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                servicosPendentes.clear()
                snapshot.children.forEach { child ->
                    val servico = child.getValue(Servico::class.java)?.copy(id = child.key ?: "")
                    servico?.let {
                        if (it.clienteNome.isNotEmpty()) {
                            servicosPendentes.add(it)
                        }
                    }
                }
                adapter.submitList(servicosPendentes.sortedByDescending { it.timestamp })
                binding.progressServicos.visibility = View.GONE
                
                // Se não houver dados, mostrar mensagem
                if (servicosPendentes.isEmpty()) {
                    Toast.makeText(context, "Nenhum pedido pendente", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                binding.progressServicos.visibility = View.GONE
                Toast.makeText(context, "Erro ao carregar pendentes: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun carregarAprovados() {
        database.child("pedidos").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                servicosAprovados.clear()
                snapshot.children.forEach { child ->
                    val servico = child.getValue(Servico::class.java)?.copy(id = child.key ?: "")
                    servico?.let {
                        if (it.clienteNome.isNotEmpty()) {
                            servicosAprovados.add(it)
                        }
                    }
                }
                if (binding.tabLayout.selectedTabPosition == 1) {
                    adapter.submitList(servicosAprovados.sortedByDescending { it.timestamp })
                }
                binding.progressServicos.visibility = View.GONE
            }

            override fun onCancelled(error: DatabaseError) {
                binding.progressServicos.visibility = View.GONE
                Toast.makeText(context, "Erro ao carregar aprovados: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun aprovarServico(servico: Servico) {
        val servicoAprovado = servico.copy(status = "Aprovado")
        
        database.child("pedidos").push().setValue(servicoAprovado)
            .addOnSuccessListener {
                database.child("pedidos_pendentes").child(servico.id).removeValue()
                    .addOnSuccessListener {
                        servicosPendentes.removeAll { it.id == servico.id }
                        Toast.makeText(context, "Serviço aprovado!", Toast.LENGTH_SHORT).show()
                        adapter.submitList(servicosPendentes.sortedByDescending { it.timestamp })
                    }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Erro ao aprovar: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun rejeitarServico(servico: Servico) {
        database.child("pedidos_pendentes").child(servico.id).removeValue()
            .addOnSuccessListener {
                servicosPendentes.removeAll { it.id == servico.id }
                Toast.makeText(context, "Serviço rejeitado", Toast.LENGTH_SHORT).show()
                adapter.submitList(servicosPendentes.sortedByDescending { it.timestamp })
            }
            .addOnFailureListener {
                Toast.makeText(context, "Erro ao rejeitar: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
