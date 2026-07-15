package com.tomazela.adminpiscina.data.models

data class ServicoProduto(
    val id: String = "",
    val nome: String = "",
    val preco: Double = 0.0,
    val tipo: String = "servico", // "produto" ou "servico"
    val pausado: Boolean = false
)
