package com.tomazela.adminpiscina.ui.login

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.tomazela.adminpiscina.MainActivity
import com.tomazela.adminpiscina.R

class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        if (auth.currentUser != null) {
            navigateToMain()
            return
        }

        findViewById<android.widget.Button>(R.id.btnLogin).setOnClickListener {
            realizarLogin()
        }
    }

    private fun realizarLogin() {
        val email = findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etEmail).text.toString().trim()
        val password = findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etPassword).text.toString().trim()
        val tvError = findViewById<android.widget.TextView>(R.id.tvError)

        if (email.isEmpty() || password.isEmpty()) {
            tvError.text = "Preencha todos os campos"
            tvError.visibility = android.view.View.VISIBLE
            return
        }

        val btnLogin = findViewById<com.google.android.material.button.MaterialButton>(R.id.btnLogin)
        val progressLogin = findViewById<android.widget.ProgressBar>(R.id.progressLogin)

        btnLogin.isEnabled = false
        progressLogin.visibility = android.view.View.VISIBLE
        tvError.visibility = android.view.View.GONE

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                btnLogin.isEnabled = true
                progressLogin.visibility = android.view.View.GONE

                if (task.isSuccessful) {
                    navigateToMain()
                } else {
                    tvError.text = "Email ou senha inválidos"
                    tvError.visibility = android.view.View.VISIBLE
                    Toast.makeText(this, "Falha no login", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
