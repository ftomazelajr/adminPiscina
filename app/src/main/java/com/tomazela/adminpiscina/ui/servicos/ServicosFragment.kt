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
                    0 -> adapter.submitList(servicosPendentes.sortedByDescending { it.timestamp })
                    1 -> adapter.submitList(servicosAprovados.sortedByDescending { it.timestamp })
                }
            }

            override fun onTabUnselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
            override fun onTabReselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
        })
    }

    private fun carregarServicos() {
        binding.progressServicos.visibility = View.VISIBLE

        // Carregar pedidos pendentes
        listenerPendentes = database.child("pedidos_pendentes")
            .addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val servico = snapshot.getValue(Servico::class.java)?.copy(id = snapshot.key ?: "")
                    servico?.let {
                        servicosPendentes.add(it)
                        if (binding.tabLayout.selectedTabPosition == 0) {
                            adapter.submitList(servicosPendentes.sortedByDescending { it.timestamp })
                        }
                    }
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                    val servico = snapshot.getValue(Servico::class.java)?.copy(id = snapshot.key ?: "")
                    servico?.let {
                        val index = servicosPendentes.indexOfFirst { it.id == servico.id }
                        if (index != -1) {
                            servicosPendentes[index] = servico
                            if (binding.tabLayout.selectedTabPosition == 0) {
                                adapter.submitList(servicosPendentes.sortedByDescending { it.timestamp })
                            }
                        }
                    }
                }

                override fun onChildRemoved(snapshot: DataSnapshot) {
                    servicosPendentes.removeAll { it.id == snapshot.key }
                    if (binding.tabLayout.selectedTabPosition == 0) {
                        adapter.submitList(servicosPendentes.sortedByDescending { it.timestamp })
                    }
                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onCancelled(error: DatabaseError) {
                    binding.progressServicos.visibility = View.GONE
                }
            })

        // Carregar pedidos aprovados
        listenerAprovados = database.child("pedidos")
            .addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val servico = snapshot.getValue(Servico::class.java)?.copy(id = snapshot.key ?: "")
                    servico?.let {
                        servicosAprovados.add(it)
                        if (binding.tabLayout.selectedTabPosition == 1) {
                            adapter.submitList(servicosAprovados.sortedByDescending { it.timestamp })
                        }
                    }
                    binding.progressServicos.visibility = View.GONE
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                    val servico = snapshot.getValue(Servico::class.java)?.copy(id = snapshot.key ?: "")
                    servico?.let {
                        val index = servicosAprovados.indexOfFirst { it.id == servico.id }
                        if (index != -1) {
                            servicosAprovados[index] = servico
                            if (binding.tabLayout.selectedTabPosition == 1) {
                                adapter.submitList(servicosAprovados.sortedByDescending { it.timestamp })
                            }
                        }
                    }
                }

                override fun onChildRemoved(snapshot: DataSnapshot) {
                    servicosAprovados.removeAll { it.id == snapshot.key }
                    if (binding.tabLayout.selectedTabPosition == 1) {
                        adapter.submitList(servicosAprovados.sortedByDescending { it.timestamp })
                    }
                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun aprovarServico(servico: Servico) {
        // Mover para pedidos aprovados
        val servicoAprovado = servico.copy(status = "Aprovado")
        
        database.child("pedidos").push().setValue(servicoAprovado)
            .addOnSuccessListener {
                database.child("pedidos_pendentes").child(servico.id).removeValue()
                    .addOnSuccessListener {
                        Toast.makeText(context, "Serviço aprovado!", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Erro ao aprovar", Toast.LENGTH_SHORT).show()
            }
    }

    private fun rejeitarServico(servico: Servico) {
        database.child("pedidos_pendentes").child(servico.id).removeValue()
            .addOnSuccessListener {
                Toast.makeText(context, "Serviço rejeitado", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Erro ao rejeitar", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        listenerPendentes?.let { database.child("pedidos_pendentes").removeEventListener(it) }
        listenerAprovados?.let { database.child("pedidos").removeEventListener(it) }
        _binding = null
    }
}
