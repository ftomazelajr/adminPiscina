package com.tomazela.adminpiscina.ui.produtos

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
import com.tomazela.adminpiscina.databinding.FragmentProdutosBinding
import com.tomazela.adminpiscina.databinding.DialogProdutoBinding
import com.tomazela.adminpiscina.data.models.Produto

class ProdutosFragment : Fragment() {
    private var _binding: FragmentProdutosBinding? = null
    private val binding get() = _binding!!
    private lateinit var database: DatabaseReference
    private lateinit var adapter: ProdutoAdapter
    private val produtos = mutableListOf<Produto>()
    private var produtoEditando: Produto? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProdutosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        database = FirebaseDatabase.getInstance().getReference("produtos")

        setupRecyclerView()
        setupListeners()
        carregarProdutos()
    }

    private fun setupRecyclerView() {
        adapter = ProdutoAdapter(
            onEdit = { produto -> mostrarDialogProduto(produto) },
            onDelete = { produto -> excluirProduto(produto) }
        )
        binding.rvProdutos.layoutManager = LinearLayoutManager(context)
        binding.rvProdutos.adapter = adapter
    }

    private fun setupListeners() {
        binding.btnAddProduto.setOnClickListener {
            produtoEditando = null
            mostrarDialogProduto(null)
        }

        binding.etSearchProduto.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filtrarProdutos(s.toString())
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
    }

    private fun carregarProdutos() {
        binding.progressProdutos.visibility = View.VISIBLE

        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                produtos.clear()
                snapshot.children.forEach { child ->
                    try {
                        val produto = child.getValue(Produto::class.java)
                        if (produto != null) {
                            val produtoComId = produto.copy(id = child.key ?: "")
                            produtos.add(produtoComId)
                        }
                    } catch (e: Exception) {
                        // Ignorar itens com erro
                    }
                }
                adapter.submitList(produtos.sortedBy { it.nome })
                binding.progressProdutos.visibility = View.GONE
            }

            override fun onCancelled(error: DatabaseError) {
                binding.progressProdutos.visibility = View.GONE
                Toast.makeText(context, "Erro ao carregar produtos: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun filtrarProdutos(query: String) {
        if (query.isEmpty()) {
            adapter.submitList(produtos.sortedBy { it.nome })
        } else {
            val filtrados = produtos.filter {
                it.nome.lowercase().contains(query.lowercase())
            }.sortedBy { it.nome }
            adapter.submitList(filtrados)
        }
    }

    private fun mostrarDialogProduto(produto: Produto?) {
        val dialogView = DialogProdutoBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView.root)
            .create()

        dialogView.tvDialogTitle.text = if (produto == null) "Adicionar Produto" else "Editar Produto"

        produto?.let {
            produtoEditando = it
            dialogView.etNome.setText(it.nome)
            dialogView.etPreco.setText(it.preco.toString())
            dialogView.etDescricao.setText(it.descricao ?: "")
            dialogView.switchPausado.isChecked = it.pausado
        }

        dialogView.btnCancelar.setOnClickListener { dialog.dismiss() }

        dialogView.btnSalvar.setOnClickListener {
            val nome = dialogView.etNome.text.toString().trim()
            val preco = dialogView.etPreco.text.toString().toDoubleOrNull() ?: 0.0
            val descricao = dialogView.etDescricao.text.toString().trim()
            val pausado = dialogView.switchPausado.isChecked

            if (nome.isEmpty()) {
                Toast.makeText(context, "Nome é obrigatório", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val novoProduto = Produto(
                id = produto?.id ?: "",
                nome = nome,
                preco = preco,
                descricao = descricao,
                pausado = pausado,
                tipo = "produto"
            )

            if (produto == null) {
                salvarProduto(novoProduto)
            } else {
                atualizarProduto(novoProduto)
            }

            dialog.dismiss()
        }

        dialog.show()
    }

    private fun salvarProduto(produto: Produto) {
        database.push().setValue(produto)
            .addOnSuccessListener {
                Toast.makeText(context, "Produto adicionado!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Erro ao adicionar produto: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun atualizarProduto(produto: Produto) {
        produto.id.let { id ->
            database.child(id).setValue(produto)
                .addOnSuccessListener {
                    Toast.makeText(context, "Produto atualizado!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Erro ao atualizar produto: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun excluirProduto(produto: Produto) {
        AlertDialog.Builder(requireContext())
            .setTitle("Excluir Produto")
            .setMessage("Tem certeza que deseja excluir ${produto.nome}?")
            .setPositiveButton("Sim") { _, _ ->
                produto.id.let { id ->
                    database.child(id).removeValue()
                        .addOnSuccessListener {
                            Toast.makeText(context, "Produto excluído!", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "Erro ao excluir produto: ${it.message}", Toast.LENGTH_SHORT).show()
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
