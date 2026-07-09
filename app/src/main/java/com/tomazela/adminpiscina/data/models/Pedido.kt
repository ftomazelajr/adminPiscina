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
