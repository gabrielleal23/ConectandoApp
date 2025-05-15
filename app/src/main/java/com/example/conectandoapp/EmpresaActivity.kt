package com.example.conectandoapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EmpresaActivity : AppCompatActivity() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private lateinit var empresaName: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_empresa)

        empresaName = findViewById(R.id.empresaName)
        val btnCrearPostulacion = findViewById<LinearLayout>(R.id.btnCrearPostulacion)
        val btnVerPostulaciones = findViewById<LinearLayout>(R.id.btnVerPostulaciones)
        val btnSupport = findViewById<LinearLayout>(R.id.btnSupport)
        val btnPago = findViewById<LinearLayout>(R.id.btnPago)
        val btnEditar = findViewById<LinearLayout>(R.id.btnEditar)
        val btnPerfil = findViewById<LinearLayout>(R.id.btnPerfil)
        val btnCerrarSesion: Button = findViewById(R.id.btnCerrarSesionEmpresa)

        auth.currentUser?.uid?.let {
            loadEmpresa(it)
        } ?: run {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
            finish()
        }

        btnCrearPostulacion.setOnClickListener {
            startActivity(Intent(this, CrearOfertaActivity::class.java))
        }

        btnVerPostulaciones.setOnClickListener {
            startActivity(Intent(this, VerPostulacionesActivity::class.java))
        }

        btnSupport.setOnClickListener {
            startActivity(Intent(this, SupportActivity::class.java))
        }

        btnPago.setOnClickListener {
            startActivity(Intent(this, PagoPostulacionActivity::class.java))
        }

        btnEditar.setOnClickListener {
            startActivity(Intent(this, ListarPostulacionEdit::class.java))
        }

        btnPerfil.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        btnCerrarSesion.setOnClickListener {
            Toast.makeText(this, "Sesion Cerrada", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun loadEmpresa(userId: String) {
        db.collection("users").document(userId)
            .addSnapshotListener { documentSnapshot, error ->
                if (error != null) {
                    Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                val document = documentSnapshot
                if (document != null && document.exists()) {
                    if (document.getString("role") == "Empleador") {
                        empresaName.text = document.getString("nombre") ?: "Sin nombre"
                    } else {
                        Toast.makeText(this, "No tienes permisos de empresa", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            }
    }
}
