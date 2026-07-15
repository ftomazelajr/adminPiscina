package com.tomazela.adminpiscina.ui.configuracao

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.database.*
import com.tomazela.adminpiscina.R
import com.tomazela.adminpiscina.databinding.FragmentConfiguracaoBinding
import com.tomazela.adminpiscina.data.models.Empresa
import com.tomazela.adminpiscina.utils.FirebaseHelper

class ConfiguracaoFragment : Fragment() {
    private var _binding: FragmentConfiguracaoBinding? = null
    private val binding get() = _binding!!
    private lateinit var database: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentConfiguracaoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val ref = FirebaseHelper.getUserNodeRef("empresa")
        if (ref == null) {
            Toast.makeText(context, "Usuário não logado", Toast.LENGTH_SHORT).show()
            return
        }
        database = ref

        carregarConfiguracoes()

        binding.btnSalvar.setOnClickListener {
            salvarConfiguracoes()
        }
    }

    private fun carregarConfiguracoes() {
        binding.progressConfig.visibility = View.VISIBLE

        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val empresa = snapshot.getValue(Empresa::class.java)
                if (empresa != null) {
                    binding.etNome.setText(empresa.nome)
                    binding.etCnpj.setText(empresa.cnpj)
                    binding.etEndereco.setText(empresa.endereco)
                    binding.etTelefone.setText(empresa.telefone)
                    binding.etEmail.setText(empresa.email)
                    binding.etInstagram.setText(empresa.instagram)
                    binding.etSite.setText(empresa.site)
                    binding.etMensagemRodape.setText(empresa.mensagemRodape)
                }
                binding.progressConfig.visibility = View.GONE
            }

            override fun onCancelled(error: DatabaseError) {
                binding.progressConfig.visibility = View.GONE
                Toast.makeText(context, "Erro ao carregar configurações", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun salvarConfiguracoes() {
        binding.progressConfig.visibility = View.VISIBLE
        binding.btnSalvar.isEnabled = false

        val empresa = Empresa(
            nome = binding.etNome.text.toString().trim(),
            cnpj = binding.etCnpj.text.toString().trim(),
            endereco = binding.etEndereco.text.toString().trim(),
            telefone = binding.etTelefone.text.toString().trim(),
            email = binding.etEmail.text.toString().trim(),
            instagram = binding.etInstagram.text.toString().trim(),
            site = binding.etSite.text.toString().trim(),
            mensagemRodape = binding.etMensagemRodape.text.toString().trim()
        )

        database.setValue(empresa)
            .addOnSuccessListener {
                binding.progressConfig.visibility = View.GONE
                binding.btnSalvar.isEnabled = true
                Toast.makeText(context, "✅ Configurações salvas!", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener { e ->
                binding.progressConfig.visibility = View.GONE
                binding.btnSalvar.isEnabled = true
                Toast.makeText(context, "Erro ao salvar: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
