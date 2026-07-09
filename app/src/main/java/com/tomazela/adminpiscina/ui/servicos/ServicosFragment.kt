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
    private var listenerPendentes: ChildEventListener? = null
    private var listenerAprovados: ChildEventListener? = null

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
        carregarServicos()
    }

    private fun setupRecyclerView() {
        adapter = ServicoAdapter(
            onAprovar = { servico -> aprovarServico(servico) },
            onRejeitar = { servico -> rejeitarServico(servico) }
        )
        binding.rvServicos.layoutManager = LinearLayoutManager(context)
        binding.rvServicos.adapter = adapter
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

    private fun carregarServicos() {
        binding.progressServicos.visibility = View.VISIBLE
        carregarPendentes()
        carregarAprovados()
    }

    private fun carregarPendentes() {
        // Remove listener antigo se existir
        listenerPendentes?.let { database.child("pedidos_pendentes").removeEventListener(it) }
        
        listenerPendentes = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                try {
                    val servico = snapshot.getValue(Servico::class.java)
                    if (servico != null) {
                        val servicoComId = servico.copy(id = snapshot.key ?: "")
                        if (servicoComId.clienteNome.isNotEmpty()) {
                            servicosPendentes.add(servicoComId)
                            if (binding.tabLayout.selectedTabPosition == 0) {
                                adapter.submitList(servicosPendentes.sortedByDescending { it.timestamp })
                            }
                            binding.progressServicos.visibility = View.GONE
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                try {
                    val servico = snapshot.getValue(Servico::class.java)
                    if (servico != null) {
                        val servicoComId = servico.copy(id = snapshot.key ?: "")
                        val index = servicosPendentes.indexOfFirst { it.id == servicoComId.id }
                        if (index != -1) {
                            servicosPendentes[index] = servicoComId
                            if (binding.tabLayout.selectedTabPosition == 0) {
                                adapter.submitList(servicosPendentes.sortedByDescending { it.timestamp })
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                servicosPendentes.removeAll { it.id == snapshot.key }
                if (binding.tabLayout.selectedTabPosition == 0) {
                    adapter.submitList(servicosPendentes.sortedByDescending { it.timestamp })
                }
                if (servicosPendentes.isEmpty()) {
                    binding.progressServicos.visibility = View.GONE
                }
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {
                binding.progressServicos.visibility = View.GONE
                Toast.makeText(context, "Erro ao carregar pendentes: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        }

        database.child("pedidos_pendentes").addChildEventListener(listenerPendentes!!)
    }

    private fun carregarAprovados() {
        // Remove listener antigo se existir
        listenerAprovados?.let { database.child("pedidos").removeEventListener(it) }
        
        listenerAprovados = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                try {
                    val servico = snapshot.getValue(Servico::class.java)
                    if (servico != null) {
                        val servicoComId = servico.copy(id = snapshot.key ?: "")
                        if (servicoComId.clienteNome.isNotEmpty()) {
                            servicosAprovados.add(servicoComId)
                            if (binding.tabLayout.selectedTabPosition == 1) {
                                adapter.submitList(servicosAprovados.sortedByDescending { it.timestamp })
                            }
                            binding.progressServicos.visibility = View.GONE
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                try {
                    val servico = snapshot.getValue(Servico::class.java)
                    if (servico != null) {
                        val servicoComId = servico.copy(id = snapshot.key ?: "")
                        val index = servicosAprovados.indexOfFirst { it.id == servicoComId.id }
                        if (index != -1) {
                            servicosAprovados[index] = servicoComId
                            if (binding.tabLayout.selectedTabPosition == 1) {
                                adapter.submitList(servicosAprovados.sortedByDescending { it.timestamp })
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                servicosAprovados.removeAll { it.id == snapshot.key }
                if (binding.tabLayout.selectedTabPosition == 1) {
                    adapter.submitList(servicosAprovados.sortedByDescending { it.timestamp })
                }
                if (servicosAprovados.isEmpty()) {
                    binding.progressServicos.visibility = View.GONE
                }
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {
                binding.progressServicos.visibility = View.GONE
                Toast.makeText(context, "Erro ao carregar aprovados: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        }

        database.child("pedidos").addChildEventListener(listenerAprovados!!)
    }

    private fun aprovarServico(servico: Servico) {
        val servicoAprovado = servico.copy(status = "Aprovado")
        
        database.child("pedidos").push().setValue(servicoAprovado)
            .addOnSuccessListener {
                database.child("pedidos_pendentes").child(servico.id).removeValue()
                    .addOnSuccessListener {
                        servicosPendentes.removeAll { it.id == servico.id }
                        Toast.makeText(context, "Serviço aprovado!", Toast.LENGTH_SHORT).show()
                        if (binding.tabLayout.selectedTabPosition == 0) {
                            adapter.submitList(servicosPendentes.sortedByDescending { it.timestamp })
                        }
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
                if (binding.tabLayout.selectedTabPosition == 0) {
                    adapter.submitList(servicosPendentes.sortedByDescending { it.timestamp })
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Erro ao rejeitar: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        listenerPendentes?.let { database.child("pedidos_pendentes").removeEventListener(it) }
        listenerAprovados?.let { database.child("pedidos").removeEventListener(it) }
        _binding = null
    }
}
