package com.tomazela.adminpiscina.utils

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

object FirebaseHelper {
    
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()
    
    // Pega a referência do usuário atual
    fun getUserRef(): DatabaseReference? {
        val uid = auth.currentUser?.uid ?: return null
        return database.getReference("dados").child(uid)
    }
    
    // Pega a referência de um nó específico do usuário
    fun getUserNodeRef(node: String): DatabaseReference? {
        val uid = auth.currentUser?.uid ?: return null
        return database.getReference("dados").child(uid).child(node)
    }
    
    // Pega a referência raiz para dados compartilhados (ex: lista de usuários)
    fun getSharedRef(node: String): DatabaseReference {
        return database.getReference(node)
    }
}
