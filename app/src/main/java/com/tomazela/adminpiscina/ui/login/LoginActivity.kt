package com.tomazela.adminpiscina.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
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

        findViewById<MaterialButton>(R.id.btnLogin).setOnClickListener {
            realizarLogin()
        }
    }

    private fun realizarLogin() {
        val etEmail = findViewById<TextInputEditText>(R.id.etEmail)
        val etPassword = findViewById<TextInputEditText>(R.id.etPassword)
        val tvError = findViewById<TextView>(R.id.tvError)
        val btnLogin = findViewById<MaterialButton>(R.id.btnLogin)
        val progressLogin = findViewById<ProgressBar>(R.id.progressLogin)

        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            tvError.text = "Preencha todos os campos"
            tvError.visibility = View.VISIBLE
            return
        }

        btnLogin.isEnabled = false
        progressLogin.visibility = View.VISIBLE
        tvError.visibility = View.GONE

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                btnLogin.isEnabled = true
                progressLogin.visibility = View.GONE

                if (task.isSuccessful) {
                    navigateToMain()
                } else {
                    tvError.text = "Email ou senha inválidos"
                    tvError.visibility = View.VISIBLE
                    Toast.makeText(this, "Falha no login: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
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
