package com.example.conectandoapp


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class StudentActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student)

        val btnCerrarSesionstu: Button = findViewById(R.id.btnCerrarSesionstuden)

        auth = FirebaseAuth.getInstance()
        val user = auth.currentUser

        if (user != null) {
            cargarDatosUsuario(user.uid)
        } else {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        // Botones
        findViewById<LinearLayout>(R.id.btnViewJobs).setOnClickListener {
            startActivity(Intent(this, PostulateStudentActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.btnProfile).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.btnPaymentMethod).setOnClickListener {
            startActivity(Intent(this, PaymentActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.btnHistory).setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.btnSupport).setOnClickListener {
            startActivity(Intent(this, SupportActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.btnMentors).setOnClickListener {
            startActivity(Intent(this, MentorsActivity::class.java))
        }

        btnCerrarSesionstu.setOnClickListener {
            Toast.makeText(this, "Sesion Cerrada", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun cargarDatosUsuario(userId: String) {
        val tvTitle = findViewById<TextView>(R.id.tvTitle)

        db.collection("users").document(userId)
            .addSnapshotListener { documentSnapshot, error ->
                if (error != null) {
                    Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                val document = documentSnapshot
                if (document != null && document.exists()) {
                    val nombre = document.getString("nombre") ?: "Usuario"
                    tvTitle.text = "Bienvenido, $nombre"
                } else {
                    Log.e("Firestore", "No se encontraron datos del usuario")
                }
            }
    }
}
