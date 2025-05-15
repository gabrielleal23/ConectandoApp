package com.example.conectandoapp

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var tvRegister: TextView

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        etEmail = findViewById(R.id.etEmailLogin)
        etPassword = findViewById(R.id.etPasswordLogin)
        btnLogin = findViewById(R.id.btnLogin)
        tvRegister = findViewById(R.id.tvRegister)

        btnLogin.setOnClickListener { loginUser() }
        tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun loginUser() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid

                    userId?.let { id ->
                        db.collection("users").document(id).get()
                            .addOnSuccessListener { document ->
                                if (document.exists()) {
                                    val userRole = document.getString("role") ?: "default"

                                    // Switch-case usando `when`
                                    when (userRole) {
                                        "Admin" -> startActivity(Intent(this, AdminActivity::class.java))
                                        "Estudiante" -> startActivity(Intent(this, StudentActivity::class.java))
                                        "Mentor" -> startActivity(Intent(this, MentorActivity::class.java))
                                        "Empleador" -> startActivity(Intent(this, EmpresaActivity::class.java))
                                        else -> Toast.makeText(this, "Rol no reconocido", Toast.LENGTH_SHORT).show()
                                    }

                                    finish() // Cierra la actividad de Login
                                } else {
                                    Toast.makeText(this, "Usuario no registrado en la base de datos", Toast.LENGTH_LONG).show()
                                }
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Error al obtener datos: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                    }
                } else {
                    Toast.makeText(this, "Correo o contraseña incorrectos", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
