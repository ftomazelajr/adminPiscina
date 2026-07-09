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
    private val servicos = mutableListOf<Produto>()
    private val carrinho = mutableListOf<ItemPedido>()
    private var clienteSelecionado: Cliente? = null
    private val clientes = mutableListOf<Cliente>()

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

        binding.btnVoltar.setOnClickListener {
            activity?.onBackPressed()
        }

        setupRecyclerViews()
        setupListeners()
        setupTabs()
        carregarProdutos()
        carregarServicos()
        carregarClientes()
    }

    private fun setupRecyclerViews() {
        produtosAdapter = ProdutoPdvAdapter { item ->
            adicionarAoCarrinho(item)
        }
        binding.rvProdutos.layoutManager = LinearLayoutManager(context)
        binding.rvProdutos.adapter = produtosAdapter

        carrinhoAdapter = CarrinhoAdapter(
            onQuantidadeChange = { item, novaQuantidade ->
                atualizarQuantidade(item, novaQuantidade)
            },
            onPrecoChange = { item, novoPreco ->
                atualizarPreco(item, novoPreco)
            },
            onRemover = { item ->
                removerDoCarrinho(item)
            }
        )
        binding.rvCarrinho.layoutManager = LinearLayoutManager(context)
        binding.rvCarrinho.adapter = carrinhoAdapter
    }

    private fun setupTabs() {
        binding.tabItens.addOnTabSelectedListener(object : com.google.android.material.tabs.TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: com.google.android.material.tabs.TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {
                        produtosAdapter.submitList(produtos.sortedBy { it.nome })
                        binding.etBuscarProduto.hint = "Buscar produto..."
                    }
                    1 -> {
                        produtosAdapter.submitList(servicos.sortedBy { it.nome })
                        binding.etBuscarProduto.hint = "Buscar serviço..."
                    }
                }
            }

            override fun onTabUnselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
            override fun onTabReselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
        })
    }

    private fun setupListeners() {
        binding.etBuscarProduto.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filtrarItens(s.toString())
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        binding.etBuscarCliente.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                buscarCliente(s.toString())
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
            binding.tvClienteSelecionado.text = "Nenhum cliente selecionado"
            binding.tvClienteSelecionado.setTextColor(resources.getColor(R.color.warning_orange))
            return
        }

        val clientesFiltrados = clientes.filter { 
            it.nome.lowercase().contains(query.lowercase()) 
        }.take(5)

        if (clientesFiltrados.isEmpty()) {
            binding.tvClienteSelecionado.text = "Nenhum cliente encontrado"
            binding.tvClienteSelecionado.setTextColor(resources.getColor(R.color.danger_red))
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
                binding.tvClienteSelecionado.text = "✅ ${clienteSelecionado?.nome}"
                binding.tvClienteSelecionado.setTextColor(resources.getColor(R.color.success_green))
                binding.etBuscarCliente.setText(clienteSelecionado?.nome)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun carregarProdutos() {
        database.child("produtos").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                produtos.clear()
                snapshot.children.forEach { child ->
                    val produto = child.getValue(Produto::class.java)?.copy(id = child.key ?: "")
                    produto?.let {
                        if (!it.pausado && it.tipo != "servico") {
                            produtos.add(it)
                        }
                    }
                }
                if (binding.tabItens.selectedTabPosition == 0) {
                    produtosAdapter.submitList(produtos.sortedBy { it.nome })
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Erro ao carregar produtos", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun carregarServicos() {
        servicos.clear()
        servicos.add(Produto(
            id = "s1",
            nome = "Visita Técnica",
            preco = 80.0,
            tipo = "servico"
        ))
        servicos.add(Produto(
            id = "s2",
            nome = "Limpeza Avulsa",
            preco = 120.0,
            tipo = "servico"
        ))
        servicos.add(Produto(
            id = "s3",
            nome = "Troca de Areia",
            preco = 250.0,
            tipo = "servico"
        ))
        servicos.add(Produto(
            id = "s4",
            nome = "Tratamento de Choque",
            preco = 150.0,
            tipo = "servico"
        ))
        servicos.add(Produto(
            id = "s5",
            nome = "Cobrança de Mensalidade",
            preco = 0.0,
            tipo = "servico"
        ))
        
        if (binding.tabItens.selectedTabPosition == 1) {
            produtosAdapter.submitList(servicos.sortedBy { it.nome })
        }
    }

    private fun filtrarItens(query: String) {
        val lista = if (binding.tabItens.selectedTabPosition == 0) produtos else servicos
        val filtrados = if (query.isEmpty()) {
            lista
        } else {
            lista.filter { it.nome.lowercase().contains(query.lowercase()) }
        }
        produtosAdapter.submitList(filtrados.sortedBy { it.nome })
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

    private fun atualizarPreco(item: ItemPedido, novoPreco: Double) {
        val index = carrinho.indexOfFirst { it.produtoId == item.produtoId }
        if (index != -1) {
            carrinho[index] = item.copy(precoUnitario = novoPreco)
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

        val nomeCliente = cliente?.nome ?: "Consumidor Final"
        val idCliente = cliente?.id ?: ""

        val pedido = Pedido(
            clienteId = idCliente,
            clienteNome = nomeCliente,
            itens = carrinho.toList(),
            total = carrinho.sumOf { it.precoUnitario * it.quantidade },
            status = "Pendente",
            timestamp = System.currentTimeMillis()
        )

        database.child("pedidos_pendentes").push().setValue(pedido)
            .addOnSuccessListener {
                binding.progressPdv.visibility = View.GONE
                Toast.makeText(context, "Pedido enviado para aprovação!", Toast.LENGTH_LONG).show()
                limparCarrinho()
                clienteSelecionado = null
                binding.tvClienteSelecionado.text = "Nenhum cliente selecionado"
                binding.tvClienteSelecionado.setTextColor(resources.getColor(R.color.warning_orange))
                binding.etBuscarCliente.setText("")
            }
            .addOnFailureListener { e ->
                binding.progressPdv.visibility = View.GONE
                Toast.makeText(context, "Erro ao salvar pedido: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
