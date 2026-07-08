package com.tomazela.adminpiscina.ui.pdv

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
import com.tomazela.adminpiscina.databinding.FragmentPdvBinding
import com.tomazela.adminpiscina.data.models.Cliente
import com.tomazela.adminpiscina.data.models.ItemPedido
import com.tomazela.adminpiscina.data.models.Pedido
import com.tomazela.adminpiscina.data.models.Produto
import java.text.NumberFormat
import java.util.Locale

class PdvFragment : Fragment() {
    private var _binding: FragmentPdvBinding? = null
    private val binding get() = _binding!!
    private lateinit var database: DatabaseReference
    private lateinit var produtosAdapter: ProdutoPdvAdapter
    private lateinit var carrinhoAdapter: CarrinhoAdapter
    
    private val produtos = mutableListOf<Produto>()
    private val carrinho = mutableListOf<ItemPedido>()
    private var clienteSelecionado: Cliente? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPdvBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        database = FirebaseDatabase.getInstance().getReference()

        setupRecyclerViews()
        setupListeners()
        carregarProdutos()
        carregarClientes()
    }

    private fun setupRecyclerViews() {
        produtosAdapter = ProdutoPdvAdapter { produto ->
            adicionarAoCarrinho(produto)
        }
        binding.rvProdutos.layoutManager = LinearLayoutManager(context)
        binding.rvProdutos.adapter = produtosAdapter

        carrinhoAdapter = CarrinhoAdapter(
            onQuantidadeChange = { item, novaQuantidade ->
                atualizarQuantidade(item, novaQuantidade)
            },
            onRemover = { item ->
                removerDoCarrinho(item)
            }
        )
        binding.rvCarrinho.layoutManager = LinearLayoutManager(context)
        binding.rvCarrinho.adapter = carrinhoAdapter
    }

    private fun setupListeners() {
        binding.etBuscarProduto.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filtrarProdutos(s.toString())
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        binding.etBuscarCliente.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filtrarClientes(s.toString())
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        binding.btnLimpar.setOnClickListener {
            limparCarrinho()
        }

        binding.btnFinalizar.setOnClickListener {
            finalizarVenda()
        }
    }

    private fun carregarProdutos() {
        database.child("produtos").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                produtos.clear()
                snapshot.children.forEach { child ->
                    val produto = child.getValue(Produto::class.java)?.copy(id = child.key ?: "")
                    produto?.let {
                        if (!it.pausado) {
                            produtos.add(it)
                        }
                    }
                }
                produtosAdapter.submitList(produtos.sortedBy { it.nome })
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Erro ao carregar produtos", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun carregarClientes() {
        database.child("clientes").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Só para autocomplete, não precisa armazenar
            }

            override fun onCancelled(error: DatabaseError) {
                // Ignorar
            }
        })
    }

    private fun filtrarProdutos(query: String) {
        val filtrados = if (query.isEmpty()) {
            produtos
        } else {
            produtos.filter { it.nome.lowercase().contains(query.lowercase()) }
        }
        produtosAdapter.submitList(filtrados.sortedBy { it.nome })
    }

    private fun filtrarClientes(query: String) {
        if (query.isEmpty()) {
            clienteSelecionado = null
            binding.tvClienteSelecionado.text = "Nenhum cliente selecionado"
            return
        }

        database.child("clientes")
            .orderByChild("nome")
            .startAt(query)
            .endAt(query + "\uf8ff")
            .limitToFirst(5)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val clientes = mutableListOf<Cliente>()
                    snapshot.children.forEach { child ->
                        child.getValue(Cliente::class.java)?.copy(id = child.key ?: "")?.let {
                            clientes.add(it)
                        }
                    }
                    mostrarDialogClientes(clientes, query)
                }
            }
    }

    private fun mostrarDialogClientes(clientes: List<Cliente>, query: String) {
        if (clientes.isEmpty()) {
            binding.tvClienteSelecionado.text = "Nenhum cliente encontrado"
            return
        }

        val nomes = clientes.map { it.nome }.toTypedArray()
        AlertDialog.Builder(requireContext())
            .setTitle("Selecione um cliente")
            .setItems(nomes) { _, which ->
                clienteSelecionado = clientes[which]
                binding.tvClienteSelecionado.text = "Cliente: ${clienteSelecionado?.nome}"
                binding.etBuscarCliente.setText(clienteSelecionado?.nome)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun adicionarAoCarrinho(produto: Produto) {
        val index = carrinho.indexOfFirst { it.produtoId == produto.id }
        if (index != -1) {
            val item = carrinho[index]
            carrinho[index] = item.copy(quantidade = item.quantidade + 1)
        } else {
            carrinho.add(
                ItemPedido(
                    produtoId = produto.id,
                    nome = produto.nome,
                    quantidade = 1,
                    precoUnitario = produto.preco
                )
            )
        }
        atualizarCarrinho()
    }

    private fun atualizarQuantidade(item: ItemPedido, novaQuantidade: Int) {
        val index = carrinho.indexOfFirst { it.produtoId == item.produtoId }
        if (index != -1) {
            carrinho[index] = item.copy(quantidade = novaQuantidade)
            atualizarCarrinho()
        }
    }

    private fun removerDoCarrinho(item: ItemPedido) {
        carrinho.removeAll { it.produtoId == item.produtoId }
        atualizarCarrinho()
    }

    private fun limparCarrinho() {
        carrinho.clear()
        atualizarCarrinho()
    }

    private fun atualizarCarrinho() {
        carrinhoAdapter.submitList(carrinho)
        val total = carrinho.sumOf { it.precoUnitario * it.quantidade }
        val formatador = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
        binding.tvTotal.text = formatador.format(total)
    }

    private fun finalizarVenda() {
        if (carrinho.isEmpty()) {
            Toast.makeText(context, "Carrinho vazio!", Toast.LENGTH_SHORT).show()
            return
        }

        if (clienteSelecionado == null) {
            AlertDialog.Builder(requireContext())
                .setTitle("Cliente não selecionado")
                .setMessage("Deseja continuar sem cliente?")
                .setPositiveButton("Sim") { _, _ ->
                    salvarPedido(null)
                }
                .setNegativeButton("Cancelar", null)
                .show()
            return
        }

        salvarPedido(clienteSelecionado)
    }

    private fun salvarPedido(cliente: Cliente?) {
        binding.progressPdv.visibility = View.VISIBLE

        val pedido = Pedido(
            clienteId = cliente?.id ?: "",
            clienteNome = cliente?.nome ?: "Consumidor Final",
            itens = carrinho.toList(),
            total = carrinho.sumOf { it.precoUnitario * it.quantidade },
            status = "Pendente"
        )

        database.child("pedidos_pendentes").push().setValue(pedido)
            .addOnSuccessListener {
                binding.progressPdv.visibility = View.GONE
                Toast.makeText(context, "Pedido enviado para aprovação!", Toast.LENGTH_LONG).show()
                limparCarrinho()
                clienteSelecionado = null
                binding.tvClienteSelecionado.text = "Nenhum cliente selecionado"
                binding.etBuscarCliente.setText("")
            }
            .addOnFailureListener {
                binding.progressPdv.visibility = View.GONE
                Toast.makeText(context, "Erro ao salvar pedido", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
