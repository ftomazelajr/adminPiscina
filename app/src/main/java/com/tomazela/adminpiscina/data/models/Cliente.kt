package com.tomazela.adminpiscina.data.models

data class Cliente(
    val id: String = "",
    val nome: String = "",
    val telefone: String = "",
    val emailAcesso: String? = null,
    val enderecoRua: String? = null,
    val enderecoNum: String? = null,
    val enderecoBairro: String? = null,
    val mensalidade: Double = 0.0,
    val diaVencimento: Int = 5,
    val diaVisita: String? = null,
    val observacoes: String? = null
)
