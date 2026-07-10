package com.tomazela.adminpiscina

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
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
import com.tomazela.adminpiscina.ui.visita.VisitaFragment

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private var currentFragment: Fragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        if (auth.currentUser == null) {
            navigateToLogin()
            return
        }

        // Configurar toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        supportActionBar?.title = "Dashboard"

        // Carregar o DashboardFragment
        loadFragment(DashboardFragment(), "Dashboard")
    }

    fun loadFragment(fragment: Fragment, title: String) {
        currentFragment = fragment
        supportActionBar?.title = title
        
        // Mostrar botão de voltar se não for o Dashboard
        supportActionBar?.setDisplayHomeAsUpEnabled(fragment !is DashboardFragment)
        
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        if (currentFragment is DashboardFragment) {
            // Se estiver no Dashboard, pode sair
            super.onBackPressed()
        } else {
            // Voltar para o Dashboard
            loadFragment(DashboardFragment(), "Dashboard")
        }
    }

    fun onLogoutClick(view: View) {
        auth.signOut()
        navigateToLogin()
    }

    fun onClientesClick(view: View) {
        loadFragment(ClientesFragment(), "Clientes")
    }

    fun onProdutosClick(view: View) {
        loadFragment(ProdutosFragment(), "Produtos")
    }

    fun onPdvClick(view: View) {
        loadFragment(PdvFragment(), "PDV")
    }

    fun onServicosClick(view: View) {
        loadFragment(ServicosFragment(), "Serviços")
    }

    fun onVisitaClick(view: View) {
        loadFragment(VisitaFragment(), "Ficha de Visita")
    }

    fun onFaturamentoClick(view: View) {
        loadFragment(FaturamentoFragment(), "Faturamento")
    }

    fun onRecebimentosClick(view: View) {
        loadFragment(RecebimentosFragment(), "Recebimentos")
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
