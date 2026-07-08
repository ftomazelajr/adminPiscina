package com.tomazela.adminpiscina.data.models

data class Pedido(
    val id: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val clienteId: String = "",
    val clienteNome: String = "",
    val itens: List<ItemPedido> = emptyList(),
    val total: Double = 0.0,
    val status: String = "Pendente"
)

data class ItemPedido(
    val produtoId: String = "",
    val nome: String = "",
    val quantidade: Int = 1,
    val precoUnitario: Double = 0.0
)
