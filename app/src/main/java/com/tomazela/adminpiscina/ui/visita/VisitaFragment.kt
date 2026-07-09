package com.tomazela.adminpiscina.ui.visita

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.database.*
import com.tomazela.adminpiscina.R
import com.tomazela.adminpiscina.databinding.FragmentVisitaBinding
import com.tomazela.adminpiscina.data.models.Cliente
import com.tomazela.adminpiscina.data.models.Visita
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class VisitaFragment : Fragment() {
    private var _binding: FragmentVisitaBinding? = null
    private val binding get() = _binding!!
    private lateinit var database: DatabaseReference
    private val clientes = mutableListOf<Cliente>()
    private var clienteSelecionado: Cliente? = null
    private val calendar = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale("pt", "BR"))

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

        database = FirebaseDatabase.getInstance().getReference()

        setupListeners()
        carregarClientes()
        configurarData()
    }

    private fun setupListeners() {
        binding.etBuscarClienteVisita.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                buscarCliente(s.toString())
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        binding.etDataVisita.setOnClickListener {
            mostrarDatePicker()
        }

        binding.btnSalvarVisita.setOnClickListener {
            salvarVisita()
        }
    }

    private fun carregarClientes() {
        database.child("clientes").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                clientes.clear()
                snapshot.children.forEach { child ->
                    val cliente = child.getValue(Cliente::class.java)?.copy(id = child.key ?: "")
                    cliente?.let { clientes.add(it) }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Erro ao carregar clientes", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun buscarCliente(query: String) {
        if (query.isEmpty()) {
            clienteSelecionado = null
            binding.tvClienteSelecionadoVisita.text = "Nenhum cliente selecionado"
            binding.tvClienteSelecionadoVisita.setTextColor(resources.getColor(R.color.warning_orange))
            return
        }

        val clientesFiltrados = clientes.filter {
            it.nome.lowercase().contains(query.lowercase())
        }.take(5)

        if (clientesFiltrados.isEmpty()) {
            binding.tvClienteSelecionadoVisita.text = "Nenhum cliente encontrado"
            binding.tvClienteSelecionadoVisita.setTextColor(resources.getColor(R.color.danger_red))
            return
        }

        mostrarDialogClientes(clientesFiltrados)
    }

    private fun mostrarDialogClientes(clientes: List<Cliente>) {
        val nomes = clientes.map { it.nome }.toTypedArray()
        AlertDialog.Builder(requireContext())
            .setTitle("Selecione um cliente")
            .setItems(nomes) { _, which ->
                clienteSelecionado = clientes[which]
                binding.tvClienteSelecionadoVisita.text = "✅ ${clienteSelecionado?.nome}"
                binding.tvClienteSelecionadoVisita.setTextColor(resources.getColor(R.color.success_green))
                binding.etBuscarClienteVisita.setText(clienteSelecionado?.nome)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun configurarData() {
        val dataAtual = dateFormat.format(calendar.time)
        binding.etDataVisita.setText(dataAtual)
    }

    private fun mostrarDatePicker() {
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                binding.etDataVisita.setText(dateFormat.format(calendar.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    private fun salvarVisita() {
        if (clienteSelecionado == null) {
            Toast.makeText(context, "Selecione um cliente!", Toast.LENGTH_SHORT).show()
            return
        }

        val dataVisita = binding.etDataVisita.text.toString().trim()
        if (dataVisita.isEmpty()) {
            Toast.makeText(context, "Selecione a data da visita!", Toast.LENGTH_SHORT).show()
            return
        }

        binding.progressVisita.visibility = View.VISIBLE
        binding.btnSalvarVisita.isEnabled = false

        // Coletar serviços selecionados
        val servicos = mutableListOf<String>()
        val checkboxes = listOf(
            binding.chkAspiracao,
            binding.chkEscovacao,
            binding.chkLimpezaBordas,
            binding.chkPeneiracao,
            binding.chkLimpezaFiltro,
            binding.chkRetrolavagem
        )
        checkboxes.forEach { checkbox ->
            if (checkbox.isChecked) {
                servicos.add(checkbox.text.toString())
            }
        }

        val visita = Visita(
            clienteId = clienteSelecionado?.id ?: "",
            clienteNome = clienteSelecionado?.nome ?: "",
            dataVisita = dataVisita,
            ph = binding.etPh.text.toString().trim(),
            alcalinidade = binding.etAlcalinidade.text.toString().trim(),
            cloro = binding.etCloro.text.toString().trim(),
            servicos = servicos,
            produtos = binding.etProdutos.text.toString().trim(),
            observacoes = binding.etObservacoes.text.toString().trim(),
            status = "Pendente"
        )

        database.child("visitas_pendentes").push().setValue(visita)
            .addOnSuccessListener {
                binding.progressVisita.visibility = View.GONE
                binding.btnSalvarVisita.isEnabled = true
                Toast.makeText(context, "✅ Visita enviada para aprovação!", Toast.LENGTH_LONG).show()
                limparFormulario()
            }
            .addOnFailureListener { e ->
                binding.progressVisita.visibility = View.GONE
                binding.btnSalvarVisita.isEnabled = true
                Toast.makeText(context, "Erro ao salvar: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun limparFormulario() {
        binding.etBuscarClienteVisita.setText("")
        binding.tvClienteSelecionadoVisita.text = "Nenhum cliente selecionado"
        binding.tvClienteSelecionadoVisita.setTextColor(resources.getColor(R.color.warning_orange))
        clienteSelecionado = null
        
        val checkboxes = listOf(
            binding.chkAspiracao,
            binding.chkEscovacao,
            binding.chkLimpezaBordas,
            binding.chkPeneiracao,
            binding.chkLimpezaFiltro,
            binding.chkRetrolavagem
        )
        checkboxes.forEach { it.isChecked = false }
        
        binding.etPh.setText("")
        binding.etAlcalinidade.setText("")
        binding.etCloro.setText("")
        binding.etProdutos.setText("")
        binding.etObservacoes.setText("")
        
        configurarData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
