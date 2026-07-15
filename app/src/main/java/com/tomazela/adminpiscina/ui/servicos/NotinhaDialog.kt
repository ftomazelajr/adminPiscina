package com.tomazela.adminpiscina.ui.servicos

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import android.os.Environment
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.firebase.database.*
import com.tomazela.adminpiscina.R
import com.tomazela.adminpiscina.data.models.Empresa
import com.tomazela.adminpiscina.data.models.Servico
import com.tomazela.adminpiscina.utils.FirebaseHelper
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NotinhaDialog(private val context: Context, private val servico: Servico) {

    private val formatador = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "BR"))
    private var empresa: Empresa? = null

    fun show() {
        carregarDadosEmpresa {
            mostrarDialog()
        }
    }

    private fun carregarDadosEmpresa(callback: () -> Unit) {
        val ref = FirebaseHelper.getUserNodeRef("empresa")
        if (ref == null) {
            empresa = null
            callback()
            return
        }

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                empresa = snapshot.getValue(Empresa::class.java)
                callback()
            }

            override fun onCancelled(error: DatabaseError) {
                empresa = null
                callback()
            }
        })
    }

    private fun mostrarDialog() {
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_notinha)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setCancelable(true)

        configurarNotinha(dialog)

        val btnWhatsApp = dialog.findViewById<Button>(R.id.btnWhatsAppNotinha)
        btnWhatsApp?.setOnClickListener {
            compartilharWhatsApp()
        }

        val btnSalvar = dialog.findViewById<Button>(R.id.btnSalvarNotinha)
        btnSalvar?.setOnClickListener {
            salvarComoImagem(dialog)
        }

        val btnFechar = dialog.findViewById<Button>(R.id.btnFecharNotinha)
        btnFechar?.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun configurarNotinha(dialog: Dialog) {
        val data = dateFormat.format(Date(servico.timestamp))
        val total = formatador.format(servico.total)

        // Dados da empresa
        val nomeEmpresa = empresa?.nome ?: "TOM AZELA PISCINAS"
        val cnpj = empresa?.cnpj ?: "CNPJ: 00.000.000/0001-00"
        val endereco = empresa?.endereco ?: ""
        val telefone = empresa?.telefone ?: ""
        val email = empresa?.email ?: ""
        val instagram = empresa?.instagram ?: ""
        val mensagemRodape = empresa?.mensagemRodape ?: "Obrigado pela preferência!"

        // Cabeçalho
        dialog.findViewById<TextView>(R.id.tvEmpresaNome)?.text = nomeEmpresa.uppercase()
        dialog.findViewById<TextView>(R.id.tvEmpresaCnpj)?.text = "CNPJ: $cnpj"
        dialog.findViewById<TextView>(R.id.tvEmpresaEndereco)?.text = endereco
        dialog.findViewById<TextView>(R.id.tvEmpresaTelefone)?.text = "Tel: $telefone"
        dialog.findViewById<TextView>(R.id.tvEmpresaEmail)?.text = "📧 $email"
        dialog.findViewById<TextView>(R.id.tvEmpresaInstagram)?.text = "📷 $instagram"

        // Corpo
        dialog.findViewById<TextView>(R.id.tvDataNotinha)?.text = data
        dialog.findViewById<TextView>(R.id.tvClienteNotinha)?.text = servico.clienteNome

        val tvStatus = dialog.findViewById<TextView>(R.id.tvStatusNotinha)
        tvStatus?.text = servico.status.uppercase()
        tvStatus?.setTextColor(
            when (servico.status) {
                "Pendente" -> ContextCompat.getColor(context, R.color.warning_orange)
                "Aprovado" -> ContextCompat.getColor(context, R.color.success_green)
                else -> ContextCompat.getColor(context, R.color.danger_red)
            }
        )

        dialog.findViewById<TextView>(R.id.tvTotalNotinha)?.text = total

        // Itens
        val container = dialog.findViewById<LinearLayout>(R.id.llItensNotinha)
        container?.removeAllViews()

        servico.itens.forEach { item ->
            val itemView = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                setPadding(0, 4, 0, 4)
            }

            val descricao = TextView(context).apply {
                text = "${item.quantidade}x ${item.nome}"
                textSize = 11f
                setTextColor(Color.parseColor("#2C2C2C"))
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
            }

            val preco = TextView(context).apply {
                text = formatador.format(item.precoUnitario * item.quantidade)
                textSize = 11f
                setTextColor(Color.parseColor("#2C2C2C"))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            itemView.addView(descricao)
            itemView.addView(preco)
            container?.addView(itemView)
        }

        // Rodapé
        dialog.findViewById<TextView>(R.id.tvRodapeMensagem)?.text = mensagemRodape
    }

    private fun compartilharWhatsApp() {
        val notinha = gerarTextoNotinha()
        val intent = Intent(Intent.ACTION_VIEW)
        val url = "https://api.whatsapp.com/send?text=${Uri.encode(notinha)}"
        intent.data = Uri.parse(url)

        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_TEXT, notinha)
            context.startActivity(Intent.createChooser(shareIntent, "Compartilhar Notinha"))
        }
    }

    private fun gerarTextoNotinha(): String {
        val data = dateFormat.format(Date(servico.timestamp))
        val total = formatador.format(servico.total)
        val itensTexto = servico.itens.joinToString("\n") {
            "  ${it.quantidade}x ${it.nome} - ${formatador.format(it.precoUnitario * it.quantidade)}"
        }

        val nomeEmpresa = empresa?.nome ?: "TOM AZELA PISCINAS"
        val cnpj = empresa?.cnpj ?: "00.000.000/0001-00"
        val endereco = empresa?.endereco ?: ""
        val telefone = empresa?.telefone ?: ""
        val email = empresa?.email ?: ""
        val instagram = empresa?.instagram ?: ""
        val mensagemRodape = empresa?.mensagemRodape ?: "Obrigado pela preferência!"

        return """
            ========================================
                   🏊 ${nomeEmpresa.uppercase()}
            ========================================
            CNPJ: $cnpj
            $endereco
            Tel: $telefone
            📷 $instagram
            📧 $email

            📅 DATA: $data
            👤 CLIENTE: ${servico.clienteNome}

            ----------------------------------------
            ITENS DO PEDIDO:
            $itensTexto

            ----------------------------------------
            TOTAL: $total
            ----------------------------------------

            ✅ STATUS: ${servico.status.uppercase()}

            ========================================
            $mensagemRodape
            ========================================
            📷 $instagram
            📧 $email
        """.trimIndent()
    }

    private fun salvarComoImagem(dialog: Dialog) {
        try {
            val view = dialog.findViewById<LinearLayout>(R.id.llNotinha)

            val widthSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            val heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            view.measure(widthSpec, heightSpec)
            view.layout(0, 0, view.measuredWidth, view.measuredHeight)

            val bitmap = Bitmap.createBitmap(view.measuredWidth, view.measuredHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            view.draw(canvas)

            val fileName = "notinha_${System.currentTimeMillis()}.png"
            val picturesDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            if (picturesDir != null && !picturesDir.exists()) {
                picturesDir.mkdirs()
            }
            val file = File(picturesDir, fileName)
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.close()

            Toast.makeText(context, "✅ Notinha salva em: ${file.absolutePath}", Toast.LENGTH_LONG).show()

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(Intent.createChooser(shareIntent, "Compartilhar Imagem"))

        } catch (e: Exception) {
            Toast.makeText(context, "Erro ao salvar imagem: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }
}
