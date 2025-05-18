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

class MentorActivity : AppCompatActivity() {
    private lateinit var mentorName: TextView
    private lateinit var mentorExperience: TextView
    private lateinit var btnStudents: LinearLayout
    private lateinit var btnMentorProfile: LinearLayout
    private lateinit var btnSupport: LinearLayout
    private lateinit var btnmentorias : LinearLayout
    private lateinit var btnvisualmentorias : LinearLayout
    private lateinit var btnCerrarSesion : Button
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mentors)

        mentorName = findViewById(R.id.mentorName)
        btnStudents = findViewById(R.id.btnStudents)
        btnMentorProfile = findViewById(R.id.btnMentorProfile)
        btnSupport = findViewById(R.id.btnSupport)
        btnmentorias = findViewById(R.id.btnmentorias)
        btnvisualmentorias = findViewById(R.id.btnvisualmentorias)
        btnCerrarSesion = findViewById(R.id.btnCerrarSesion)

        auth.currentUser?.uid?.let {
            loadMentorData(it)
        } ?: run {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
            finish()
        }


        btnmentorias.setOnClickListener {
            startActivity(Intent(this, CrearMentoriaActivity::class.java))
        }

        btnvisualmentorias.setOnClickListener {
            startActivity(Intent(this, VisualMentoriaActivity::class.java))
        }

        btnStudents.setOnClickListener {
            startActivity(Intent(this, AgregarContenidoMentoriaActivity::class.java))
        }

        btnMentorProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        btnSupport.setOnClickListener {
            startActivity(Intent(this, SupportActivity::class.java))
        }

        btnCerrarSesion.setOnClickListener {
            Toast.makeText(this, "Sesion Cerrada", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }



    private fun loadMentorData(userId: String) {
        db.collection("users").document(userId)
            .addSnapshotListener { documentSnapshot,error ->
                if (error != null) {
                    Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                val document = documentSnapshot
                if (document != null && document.exists()) {
                    if (document.getString("role") == "Mentor") {
                        mentorName.text = document.getString("nombre") ?: "Sin nombre"
                    } else {
                        Toast.makeText(this, "No tienes permisos de Mentor", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            }
    }
}