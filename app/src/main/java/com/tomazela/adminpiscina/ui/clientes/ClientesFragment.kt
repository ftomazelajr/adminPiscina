package com.tomazela.adminpiscina.ui.clientes

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.*
import com.tomazela.adminpiscina.R
import com.tomazela.adminpiscina.databinding.FragmentClientesBinding
import com.tomazela.adminpiscina.databinding.DialogClienteBinding
import com.tomazela.adminpiscina.data.models.Cliente
import kotlinx.coroutines.*

class ClientesFragment : Fragment() {
    private var _binding: FragmentClientesBinding? = null
    private val binding get() = _binding!!
    private lateinit var database: DatabaseReference
    private lateinit var adapter: ClienteAdapter
    private val clientes = mutableListOf<Cliente>()
    private var clienteEditando: Cliente? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentClientesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        database = FirebaseDatabase.getInstance().getReference("clientes")

        setupRecyclerView()
        setupListeners()
        carregarClientes()
    }

    private fun setupRecyclerView() {
        adapter = ClienteAdapter(
            onEdit = { cliente -> mostrarDialogCliente(cliente) },
            onDelete = { cliente -> excluirCliente(cliente) }
        )
        binding.rvClientes.layoutManager = LinearLayoutManager(context)
        binding.rvClientes.adapter = adapter
    }

    private fun setupListeners() {
        binding.btnAddCliente.setOnClickListener {
            clienteEditando = null
            mostrarDialogCliente(null)
        }

        binding.etSearchCliente.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filtrarClientes(s.toString())
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
    }

    private fun carregarClientes() {
        binding.progressClientes.visibility = View.VISIBLE

        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                clientes.clear()
                snapshot.children.forEach { child ->
                    val cliente = child.getValue(Cliente::class.java)?.copy(id = child.key ?: "")
                    cliente?.let { clientes.add(it) }
                }
                adapter.submitList(clientes.sortedBy { it.nome })
                binding.progressClientes.visibility = View.GONE
            }

            override fun onCancelled(error: DatabaseError) {
                binding.progressClientes.visibility = View.GONE
                Toast.makeText(context, "Erro ao carregar clientes", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun filtrarClientes(query: String) {
        if (query.isEmpty()) {
            adapter.submitList(clientes.sortedBy { it.nome })
        } else {
            val filtrados = clientes.filter {
                it.nome.lowercase().contains(query.lowercase())
            }.sortedBy { it.nome }
            adapter.submitList(filtrados)
        }
    }

    private fun mostrarDialogCliente(cliente: Cliente?) {
        val dialogView = DialogClienteBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView.root)
            .create()

        dialogView.tvDialogTitle.text = if (cliente == null) "Adicionar Cliente" else "Editar Cliente"

        cliente?.let {
            clienteEditando = it
            dialogView.etNome.setText(it.nome)
            dialogView.etTelefone.setText(it.telefone)
            dialogView.etEndereco.setText(it.enderecoRua)
            dialogView.etMensalidade.setText(it.mensalidade.toString())
        }

        dialogView.btnCancelar.setOnClickListener { dialog.dismiss() }

        dialogView.btnSalvar.setOnClickListener {
            val nome = dialogView.etNome.text.toString().trim()
            val telefone = dialogView.etTelefone.text.toString().trim()
            val endereco = dialogView.etEndereco.text.toString().trim()
            val mensalidade = dialogView.etMensalidade.text.toString().toDoubleOrNull() ?: 0.0

            if (nome.isEmpty() || telefone.isEmpty()) {
                Toast.makeText(context, "Nome e telefone são obrigatórios", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val novoCliente = Cliente(
                id = cliente?.id ?: "",
                nome = nome,
                telefone = telefone,
                enderecoRua = endereco,
                mensalidade = mensalidade
            )

            if (cliente == null) {
                salvarCliente(novoCliente)
            } else {
                atualizarCliente(novoCliente)
            }

            dialog.dismiss()
        }

        dialog.show()
    }

    private fun salvarCliente(cliente: Cliente) {
        database.push().setValue(cliente)
            .addOnSuccessListener {
                Toast.makeText(context, "Cliente adicionado!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Erro ao adicionar cliente", Toast.LENGTH_SHORT).show()
            }
    }

    private fun atualizarCliente(cliente: Cliente) {
        cliente.id.let { id ->
            database.child(id).setValue(cliente)
                .addOnSuccessListener {
                    Toast.makeText(context, "Cliente atualizado!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Erro ao atualizar cliente", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun excluirCliente(cliente: Cliente) {
        AlertDialog.Builder(requireContext())
            .setTitle("Excluir Cliente")
            .setMessage("Tem certeza que deseja excluir ${cliente.nome}?")
            .setPositiveButton("Sim") { _, _ ->
                cliente.id.let { id ->
                    database.child(id).removeValue()
                        .addOnSuccessListener {
                            Toast.makeText(context, "Cliente excluído!", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "Erro ao excluir cliente", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .setNegativeButton("Não", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
