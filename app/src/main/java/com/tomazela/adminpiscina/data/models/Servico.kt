package com.tomazela.adminpiscina.data.models

data class Servico(
    val id: String = "",
    val tipo: String = "PDV", // PDV, Visita, Servico
    val clienteId: String = "",
    val clienteNome: String = "",
    val data: String = "",
    val itens: List<ItemPedido> = emptyList(),
    val total: Double = 0.0,
    val status: String = "Pendente", // Pendente, Aprovado, Rejeitado
    val observacoes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class VisitaServico(
    val id: String = "",
    val clienteId: String = "",
    val clienteNome: String = "",
    val dataVisita: String = "",
    val ph: String = "",
    val alcalinidade: String = "",
    val cloro: String = "",
    val servicos: List<String> = emptyList(),
    val produtos: String = "",
    val observacoes: String = "",
    val status: String = "Pendente"
)
