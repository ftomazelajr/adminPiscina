package com.tomazela.adminpiscina.utils

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

object FirebaseHelper {
    
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()
    
    // Pega a referência do nó do usuário atual
    fun getUserNodeRef(node: String): DatabaseReference? {
        val uid = auth.currentUser?.uid
        return if (uid != null) {
            database.getReference("dados").child(uid).child(node)
        } else {
            null
        }
    }
    
    // Pega a referência do usuário
    fun getUserRef(): DatabaseReference? {
        val uid = auth.currentUser?.uid
        return if (uid != null) {
            database.getReference("dados").child(uid)
        } else {
            null
        }
    }
}
