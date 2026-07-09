package com.tomazela.adminpiscina

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.tomazela.adminpiscina.databinding.ActivityMainBinding
import com.tomazela.adminpiscina.ui.clientes.ClientesFragment
import com.tomazela.adminpiscina.ui.dashboard.DashboardFragment
import com.tomazela.adminpiscina.ui.faturamento.FaturamentoFragment
import com.tomazela.adminpiscina.ui.login.LoginActivity
import com.tomazela.adminpiscina.ui.pdv.PdvFragment
import com.tomazela.adminpiscina.ui.produtos.ProdutosFragment
import com.tomazela.adminpiscina.ui.recebimentos.RecebimentosFragment
import com.tomazela.adminpiscina.ui.servicos.ServicosFragment

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        if (auth.currentUser == null) {
            navigateToLogin()
            return
        }

        loadFragment(DashboardFragment())
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    fun onLogoutClick(view: View) {
        auth.signOut()
        navigateToLogin()
    }

    fun onClientesClick(view: View) {
        loadFragment(ClientesFragment())
    }

    fun onProdutosClick(view: View) {
        loadFragment(ProdutosFragment())
    }

    fun onPdvClick(view: View) {
        loadFragment(PdvFragment())
    }

    fun onServicosClick(view: View) {
        loadFragment(ServicosFragment())
    }

    fun onFaturamentoClick(view: View) {
        loadFragment(FaturamentoFragment())
    }

    fun onRecebimentosClick(view: View) {
        loadFragment(RecebimentosFragment())
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
