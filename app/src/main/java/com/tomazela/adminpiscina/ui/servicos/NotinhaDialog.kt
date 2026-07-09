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
import com.tomazela.adminpiscina.R
import com.tomazela.adminpiscina.data.models.Servico
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NotinhaDialog(private val context: Context, private val servico: Servico) {

    private lateinit var dialog: Dialog
    private val formatador = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "BR"))

    fun show() {
        dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_notinha)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setCancelable(true)
        
        configurarNotinha()
        
        // Adicionar botões no rodapé do dialog
        val container = dialog.findViewById<LinearLayout>(R.id.llNotinha)?.parent as? LinearLayout
        
        // Botão WhatsApp
        val btnWhatsApp = Button(context).apply {
            text = "📤 Compartilhar no WhatsApp"
            setTextColor(Color.WHITE)
            setBackgroundColor(ContextCompat.getColor(context, R.color.success_green))
            setOnClickListener {
                compartilharWhatsApp()
            }
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(24, 8, 24, 4)
            }
        }
        
        // Botão Salvar PNG
        val btnSalvar = Button(context).apply {
            text = "💾 Salvar como Imagem (PNG)"
            setTextColor(Color.WHITE)
            setBackgroundColor(ContextCompat.getColor(context, R.color.accent_blue))
            setOnClickListener {
                salvarComoImagem()
            }
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(24, 4, 24, 4)
            }
        }
        
        // Botão Fechar
        val btnFechar = Button(context).apply {
            text = "✕ Fechar"
            setTextColor(Color.WHITE)
            setBackgroundColor(ContextCompat.getColor(context, R.color.danger_red))
            setOnClickListener {
                dialog.dismiss()
            }
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(24, 4, 24, 16)
            }
        }
        
        container?.addView(btnWhatsApp)
        container?.addView(btnSalvar)
        container?.addView(btnFechar)
        
        dialog.show()
    }

    private fun configurarNotinha() {
        val data = dateFormat.format(Date(servico.timestamp))
        val total = formatador.format(servico.total)
        
        dialog.findViewById<TextView>(R.id.tvDataNotinha)?.text = data
        dialog.findViewById<TextView>(R.id.tvClienteNotinha)?.text = servico.clienteNome
        
        val tvStatus = dialog.findViewById<TextView>(R.id.tvStatusNotinha)
        tvStatus?.text = servico.status.toUpperCase()
        tvStatus?.setTextColor(
            when (servico.status) {
                "Pendente" -> ContextCompat.getColor(context, R.color.warning_orange)
                "Aprovado" -> ContextCompat.getColor(context, R.color.success_green)
                else -> ContextCompat.getColor(context, R.color.danger_red)
            }
        )
        
        dialog.findViewById<TextView>(R.id.tvTotalNotinha)?.text = total
        
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
        
        return """
            ========================================
                   🏊 TOMAZELA PISCINAS
            ========================================
            CNPJ: 12.345.678/0001-90
            Rua Romualdo Albino Balestrin, 35
            Tel: (14) 98172-2063
            
            📅 DATA: $data
            👤 CLIENTE: ${servico.clienteNome}
            
            ----------------------------------------
            ITENS DO PEDIDO:
            $itensTexto
            
            ----------------------------------------
            TOTAL: $total
            ----------------------------------------
            
            ✅ STATUS: ${servico.status.toUpperCase()}
            
            ========================================
            Obrigado pela preferência!
            ========================================
        """.trimIndent()
    }

    private fun salvarComoImagem() {
        try {
            val view = dialog.findViewById<LinearLayout>(R.id.llNotinha)
            
            // Medir a view antes de criar o bitmap
            view.measure(
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            )
            view.layout(0, 0, view.measuredWidth, view.measuredHeight)
            
            // Criar bitmap
            val bitmap = Bitmap.createBitmap(view.measuredWidth, view.measuredHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            view.draw(canvas)
            
            // Salvar no armazenamento
            val fileName = "notinha_${System.currentTimeMillis()}.png"
            val picturesDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            val file = File(picturesDir, fileName)
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.close()
            
            Toast.makeText(context, "✅ Notinha salva em: ${file.absolutePath}", Toast.LENGTH_LONG).show()
            
            // Abrir para compartilhar
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
            Toast.makeText(context, "Erro ao salvar imagem: ${e.message}", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }
}
