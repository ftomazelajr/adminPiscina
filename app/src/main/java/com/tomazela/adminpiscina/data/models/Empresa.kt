package com.tomazela.adminpiscina.data.models

data class Empresa(
    val nome: String = "",
    val cnpj: String = "",
    val endereco: String = "",
    val telefone: String = "",
    val email: String = "",
    val instagram: String = "",
    val site: String = "",
    val logo: String = "", // URL da logo (opcional)
    val mensagemRodape: String = "Obrigado pela preferência!"
)
