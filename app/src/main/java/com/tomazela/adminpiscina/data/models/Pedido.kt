package com.tomazela.adminpiscina.data.models

data class Pedido(
    val id: String = "",
    val clienteId: String = "",
    val clienteNome: String = "",
    val itens: List<ItemPedido> = emptyList(),
    val total: Double = 0.0,
    val status: String = "Pendente",
    val timestamp: Long = System.currentTimeMillis()
)

data class ItemPedido(
    val produtoId: String = "",
    val nome: String = "",
    val quantidade: Int = 1,
    val precoUnitario: Double = 0.0
)

// Classe para compatibilidade com o Firebase
data class Servico(
    val id: String = "",
    val tipo: String = "PDV",
    val clienteId: String = "",
    val clienteNome: String = "",
    val data: String = "",
    val itens: List<ItemPedido> = emptyList(),
    val total: Double = 0.0,
    val status: String = "Pendente",
    val observacoes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
