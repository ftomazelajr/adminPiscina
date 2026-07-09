package com.tomazela.adminpiscina.data.models

data class Visita(
    val id: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val clienteId: String = "",
    val clienteNome: String = "",
    val dataVisita: String = "",
    val ph: String = "",
    val alcalinidade: String = "",
    val cloro: String = "",
    val servicos: List<String> = emptyList(),
    val produtos: String = "",
    val observacoes: String = "",
    val fotoAntesUrl: String = "",
    val fotoDepoisUrl: String = "",
    val status: String = "Pendente"
)
