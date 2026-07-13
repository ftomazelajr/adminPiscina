package com.tomazela.adminpiscina.ui.visita

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.tomazela.adminpiscina.R
import com.tomazela.adminpiscina.databinding.FragmentVisitaBinding
import com.tomazela.adminpiscina.data.models.Cliente
import com.tomazela.adminpiscina.data.models.Visita
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID

class VisitaFragment : Fragment() {
    private var _binding: FragmentVisitaBinding? = null
    private val binding get() = _binding!!
    private lateinit var database: DatabaseReference
    private lateinit var storage: StorageReference
    private val clientes = mutableListOf<Cliente>()
    private var clienteSelecionado: Cliente? = null
    private val calendar = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale("pt", "BR"))
    
    private var fotoAntesUri: Uri? = null
    private var fotoDepoisUri: Uri? = null
    private var fotoAntesUrl: String? = null
    private var fotoDepoisUrl: String? = null

    // Launcher para tirar foto
    private val tirarFotoLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            // Foto tirada com sucesso
            Toast.makeText(context, "Foto salva!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Erro ao tirar foto", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVisitaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        database = FirebaseDatabase.getInstance().getReference()
        storage = FirebaseStorage.getInstance().reference

        setupListeners()
        carregarClientes()
        configurarData()
        setupFotoListeners()
    }

    private fun setupListeners() {
        binding.etBuscarClienteVisita.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                buscarCliente(s.toString())
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        binding.etDataVisita.setOnClickListener {
            mostrarDatePicker()
        }

        binding.btnSalvarVisita.setOnClickListener {
            salvarVisita()
        }
    }

    private fun setupFotoListeners() {
        binding.btnFotoAntes.setOnClickListener {
            tirarFoto("antes")
        }

        binding.btnFotoDepois.setOnClickListener {
            tirarFoto("depois")
        }
    }

    private fun tirarFoto(tipo: String) {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(requireActivity().packageManager) != null) {
            // Criar um arquivo temporário para a foto
            val photoFile = java.io.File(
                requireContext().cacheDir,
                "temp_${System.currentTimeMillis()}.jpg"
            )
            val photoUri = androidx.core.content.FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.fileprovider",
                photoFile
            )

            if (tipo == "antes") {
                fotoAntesUri = photoUri
                tirarFotoLauncher.launch(photoUri)
                binding.tvFotoAntes.text = "📸 Foto ANTES capturada!"
                binding.tvFotoAntes.setTextColor(requireContext().getColor(R.color.success_green))
            } else {
                fotoDepoisUri = photoUri
                tirarFotoLauncher.launch(photoUri)
                binding.tvFotoDepois.text = "📸 Foto DEPOIS capturada!"
                binding.tvFotoDepois.setTextColor(requireContext().getColor(R.color.success_green))
            }
        } else {
            Toast.makeText(context, "Câmera não disponível", Toast.LENGTH_SHORT).show()
        }
    }

    private fun carregarClientes() {
        database.child("clientes").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                clientes.clear()
                snapshot.children.forEach { child ->
                    val cliente = child.getValue(Cliente::class.java)?.copy(id = child.key ?: "")
                    cliente?.let { clientes.add(it) }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Erro ao carregar clientes", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun buscarCliente(query: String) {
        if (query.isEmpty()) {
            clienteSelecionado = null
            binding.tvClienteSelecionadoVisita.text = "Nenhum cliente selecionado"
            binding.tvClienteSelecionadoVisita.setTextColor(requireContext().getColor(R.color.warning_orange))
            return
        }

        val clientesFiltrados = clientes.filter {
            it.nome.lowercase().contains(query.lowercase())
        }.take(5)

        if (clientesFiltrados.isEmpty()) {
            binding.tvClienteSelecionadoVisita.text = "Nenhum cliente encontrado"
            binding.tvClienteSelecionadoVisita.setTextColor(requireContext().getColor(R.color.danger_red))
            return
        }

        mostrarDialogClientes(clientesFiltrados)
    }

    private fun mostrarDialogClientes(clientes: List<Cliente>) {
        val nomes = clientes.map { it.nome }.toTypedArray()
        AlertDialog.Builder(requireContext())
            .setTitle("Selecione um cliente")
            .setItems(nomes) { _, which ->
                clienteSelecionado = clientes[which]
                binding.tvClienteSelecionadoVisita.text = "✅ ${clienteSelecionado?.nome}"
                binding.tvClienteSelecionadoVisita.setTextColor(requireContext().getColor(R.color.success_green))
                binding.etBuscarClienteVisita.setText(clienteSelecionado?.nome)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun configurarData() {
        val dataAtual = dateFormat.format(calendar.time)
        binding.etDataVisita.setText(dataAtual)
    }

    private fun mostrarDatePicker() {
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                binding.etDataVisita.setText(dateFormat.format(calendar.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    private fun uploadFoto(uri: Uri?, tipo: String): String? {
        if (uri == null) return null

        try {
            val nomeArquivo = "visitas/${System.currentTimeMillis()}_${tipo}_${UUID.randomUUID()}.jpg"
            val ref = storage.child(nomeArquivo)
            
            // Upload síncrono (bloqueante)
            val uploadTask = ref.putFile(uri)
            val taskSnapshot = uploadTask.await()
            
            // Pegar URL
            val downloadUrl = ref.downloadUrl.await()
            return downloadUrl.toString()
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    private suspend fun uploadFotos() {
        fotoAntesUrl = uploadFoto(fotoAntesUri, "antes")
        fotoDepoisUrl = uploadFoto(fotoDepoisUri, "depois")
    }

    private fun salvarVisita() {
        if (clienteSelecionado == null) {
            Toast.makeText(context, "Selecione um cliente!", Toast.LENGTH_SHORT).show()
            return
        }

        val dataVisita = binding.etDataVisita.text.toString().trim()
        if (dataVisita.isEmpty()) {
            Toast.makeText(context, "Selecione a data da visita!", Toast.LENGTH_SHORT).show()
            return
        }

        binding.progressVisita.visibility = View.VISIBLE
        binding.btnSalvarVisita.isEnabled = false

        // Coletar serviços selecionados
        val servicos = mutableListOf<String>()
        val checkboxes = listOf(
            binding.chkAspiracao,
            binding.chkEscovacao,
            binding.chkLimpezaBordas,
            binding.chkPeneiracao,
            binding.chkLimpezaFiltro,
            binding.chkRetrolavagem
        )
        checkboxes.forEach { checkbox ->
            if (checkbox.isChecked) {
                servicos.add(checkbox.text.toString())
            }
        }

        // Upload das fotos em background
        val scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO)
        scope.launch {
            uploadFotos()

            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                val visita = Visita(
                    clienteId = clienteSelecionado?.id ?: "",
                    clienteNome = clienteSelecionado?.nome ?: "",
                    dataVisita = dataVisita,
                    ph = binding.etPh.text.toString().trim(),
                    alcalinidade = binding.etAlcalinidade.text.toString().trim(),
                    cloro = binding.etCloro.text.toString().trim(),
                    servicos = servicos,
                    produtos = binding.etProdutos.text.toString().trim(),
                    observacoes = binding.etObservacoes.text.toString().trim(),
                    fotoAntesUrl = fotoAntesUrl ?: "",
                    fotoDepoisUrl = fotoDepoisUrl ?: "",
                    status = "Pendente",
                    timestamp = System.currentTimeMillis()
                )

                database.child("visitas_pendentes").push().setValue(visita)
                    .addOnSuccessListener {
                        binding.progressVisita.visibility = View.GONE
                        binding.btnSalvarVisita.isEnabled = true
                        Toast.makeText(context, "✅ Visita enviada para aprovação!", Toast.LENGTH_LONG).show()
                        limparFormulario()
                    }
                    .addOnFailureListener { e ->
                        binding.progressVisita.visibility = View.GONE
                        binding.btnSalvarVisita.isEnabled = true
                        Toast.makeText(context, "Erro ao salvar: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    private fun limparFormulario() {
        binding.etBuscarClienteVisita.setText("")
        binding.tvClienteSelecionadoVisita.text = "Nenhum cliente selecionado"
        binding.tvClienteSelecionadoVisita.setTextColor(requireContext().getColor(R.color.warning_orange))
        clienteSelecionado = null
        
        val checkboxes = listOf(
            binding.chkAspiracao,
            binding.chkEscovacao,
            binding.chkLimpezaBordas,
            binding.chkPeneiracao,
            binding.chkLimpezaFiltro,
            binding.chkRetrolavagem
        )
        checkboxes.forEach { it.isChecked = false }
        
        binding.etPh.setText("")
        binding.etAlcalinidade.setText("")
        binding.etCloro.setText("")
        binding.etProdutos.setText("")
        binding.etObservacoes.setText("")
        
        fotoAntesUri = null
        fotoDepoisUri = null
        fotoAntesUrl = null
        fotoDepoisUrl = null
        binding.tvFotoAntes.text = "Nenhuma foto selecionada"
        binding.tvFotoAntes.setTextColor(requireContext().getColor(R.color.text_secondary))
        binding.tvFotoDepois.text = "Nenhuma foto selecionada"
        binding.tvFotoDepois.setTextColor(requireContext().getColor(R.color.text_secondary))
        
        configurarData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
