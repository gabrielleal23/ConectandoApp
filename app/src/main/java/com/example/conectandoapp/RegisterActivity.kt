package com.example.conectandoapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var etNombre: EditText
    private lateinit var etCedula: EditText
    private lateinit var etTelefono: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var spinnerRole: Spinner
    private lateinit var btnRegister: Button
    private lateinit var btnLogin: Button
    private lateinit var btnVolver: Button

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        etNombre = findViewById(R.id.etNombre)
        etCedula = findViewById(R.id.etCedula)
        etTelefono = findViewById(R.id.etTelefono)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        spinnerRole = findViewById(R.id.spinnerRole)
        btnRegister = findViewById(R.id.btnRegister)
        btnLogin = findViewById(R.id.btnLogin)
        btnVolver = findViewById(R.id.btnVolver)

        val roles = arrayOf("Seleccione un rol","Estudiante", "Mentor", "Empleador")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, roles)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerRole.adapter = adapter

        btnRegister.setOnClickListener {
            registerUser()
        }

        val esModoAdmin = intent.getBooleanExtra("modo_admin", false) // recibe el dato

        if (esModoAdmin) {
            btnLogin.visibility = View.GONE
            btnVolver.visibility = View.VISIBLE // mostrar el botón volver en modo admin

            btnVolver.setOnClickListener {
                finish()
            }
        } else {
            btnLogin.visibility = View.VISIBLE
            btnVolver.visibility = View.GONE // ocultar el botón volver en modo usuario
        }

        btnLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun registerUser() {
        val nombre = etNombre.text.toString().trim()
        val cedula = etCedula.text.toString().trim()
        val telefono = etTelefono.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()
        val role = spinnerRole.selectedItem?.toString() ?: "Seleccione un rol"

        if (nombre.isEmpty() || cedula.isEmpty() || telefono.isEmpty() || email.isEmpty() || password.isEmpty() || role == "Seleccione un rol") {
            Toast.makeText(this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        val esModoAdmin = intent.getBooleanExtra("modo_admin", false)

        if (esModoAdmin) {
            // Guardar solo en Firestore
            val userId = db.collection("users").document().id // Crear ID único

            val user = hashMapOf(
                "userId" to userId,
                "nombre" to nombre,
                "cedula" to cedula,
                "telefono" to telefono,
                "email" to email,
                "role" to role
            )

            db.collection("users").document(userId)
                .set(user)
                .addOnSuccessListener {
                    Toast.makeText(this, "Usuario creado exitosamente", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error al crear usuario: ${e.message}", Toast.LENGTH_LONG).show()
                }

        } else {
            // Registro normal
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val userId = auth.currentUser?.uid

                        val user = hashMapOf(
                            "userId" to userId,
                            "nombre" to nombre,
                            "cedula" to cedula,
                            "telefono" to telefono,
                            "email" to email,
                            "role" to role
                        )

                        db.collection("users").document(userId!!)
                            .set(user)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show()
                                limpiarCampos()
                                startActivity(Intent(this, LoginActivity::class.java))
                                finish()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Error al guardar en Firestore: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                    } else {
                        Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }


    private fun limpiarCampos() {
        etNombre.text.clear()
        etCedula.text.clear()
        etTelefono.text.clear()
        etEmail.text.clear()
        etPassword.text.clear()
        spinnerRole.setSelection(0)
    }
}
