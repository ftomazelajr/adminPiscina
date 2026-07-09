package com.tomazela.adminpiscina.data.models

data class Fatura(
    val id: String = "",
    val cliente: ClienteResumo,
    val mensalidadeBase: Double = 0.0,
    val totalAvulsos: Double = 0.0,
    val totalFatura: Double = 0.0,
    val itensFaturados: List<ItemFaturado> = emptyList(),
    val dataFechamento: String = "",
    val status: String = "Pendente"
)

data class ClienteResumo(
    val id: String = "",
    val nome: String = "",
    val endereco: String? = null
)

data class ItemFaturado(
    val id: String = "",
    val itens: List<ItemPedido>? = null,
    val total: Double = 0.0
)
