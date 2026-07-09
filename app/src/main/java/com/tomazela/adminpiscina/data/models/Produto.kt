package com.tomazela.adminpiscina.data.models

data class Produto(
    val id: String = "",
    val nome: String = "",
    val preco: Double = 0.0,
    val pausado: Boolean = false,
    val descricao: String? = null,
    val tipo: String = "produto" // "produto" ou "servico"
)
