package com.tomazela.adminpiscina.ui.visita

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.*
import com.tomazela.adminpiscina.R
import com.tomazela.adminpiscina.databinding.FragmentVisitaBinding
import com.tomazela.adminpiscina.data.models.Visita
import com.tomazela.adminpiscina.utils.FirebaseHelper
import java.text.SimpleDateFormat
import java.util.*

class VisitaFragment : Fragment() {
    private var _binding: FragmentVisitaBinding? = null
    private val binding get() = _binding!!
    private lateinit var databaseVisitas: DatabaseReference
    private lateinit var adapter: VisitaAdapter
    private val visitas = mutableListOf<Visita>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVisitaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val ref = FirebaseHelper.getUserNodeRef("visitas_pendentes")
        if (ref == null) {
            Toast.makeText(context, "Usuário não logado", Toast.LENGTH_SHORT).show()
            return
        }
        databaseVisitas = ref

        setupRecyclerView()
        carregarVisitas()
    }

    private fun setupRecyclerView() {
        adapter = VisitaAdapter()
        binding.rvVisitas.layoutManager = LinearLayoutManager(context)
        binding.rvVisitas.adapter = adapter
    }

    private fun carregarVisitas() {
        binding.progressVisita.visibility = View.VISIBLE

        databaseVisitas.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                visitas.clear()
                snapshot.children.forEach { child ->
                    val visita = child.getValue(Visita::class.java)?.copy(id = child.key ?: "")
                    visita?.let { visitas.add(it) }
                }
                adapter.submitList(visitas.sortedByDescending { it.timestamp })
                binding.progressVisita.visibility = View.GONE
            }

            override fun onCancelled(error: DatabaseError) {
                binding.progressVisita.visibility = View.GONE
                Toast.makeText(context, "Erro ao carregar visitas", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
