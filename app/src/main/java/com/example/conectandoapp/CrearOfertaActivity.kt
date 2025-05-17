package com.example.conectandoapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CrearOfertaActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    @SuppressLint("WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crear_oferta)

        // Inicializar Firestore y Autenticación
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Referencias a los elementos de la interfaz
        val btnVolver = findViewById<Button>(R.id.btnVolver)
        val etPuesto = findViewById<EditText>(R.id.etPuesto)
        val etDescripcion = findViewById<EditText>(R.id.etDescripcion)
        val etUbicacion = findViewById<EditText>(R.id.etUbicacion)
        val etRequisitos = findViewById<EditText>(R.id.etRequisitos)
        val etHorario = findViewById<EditText>(R.id.etHorario)
        val etHoras = findViewById<EditText>(R.id.etHoras)
        val etPagoHora = findViewById<EditText>(R.id.etPagoHora)
        val btnPublicar = findViewById<Button>(R.id.btnPublicar)

        // Evento para volver a la pantalla anterior
        btnVolver.setOnClickListener {
            finish() // Cierra la actividad actual y vuelve atrás
        }

        // Evento del botón para guardar en Firestore
        btnPublicar.setOnClickListener {
            val puesto = etPuesto.text.toString().trim()
            val descripcion = etDescripcion.text.toString().trim()
            val ubicacion = etUbicacion.text.toString().trim()
            val requisitos = etRequisitos.text.toString().trim()
            val horas = etHoras.text.toString().trim()
            val horario = etHorario.text.toString().trim()
            val pagoHora = etPagoHora.text.toString().trim()

            // Validar que todos los campos estén llenos
            if (puesto.isEmpty() || descripcion.isEmpty() || ubicacion.isEmpty() ||
            requisitos.isEmpty() || horario.isEmpty() || pagoHora.isEmpty() ||horas.isEmpty()) {
                Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            // Obtener el UID del usuario autenticado
            val userId = auth.currentUser?.uid
            if (userId == null) {
                Toast.makeText(this, "Error: Usuario no autenticado", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            val nuevaOfertaRef = db.collection("ofertas").document()
            val ofertaId = nuevaOfertaRef.id

            // Crear objeto de la oferta con el ID del usuario y el estado "pendiente"
            val oferta = hashMapOf(
                "id" to ofertaId,
                "puesto" to puesto,
                "descripcion" to descripcion,
                "ubicacion" to ubicacion,
                "requisitos" to requisitos,
                "horas" to horas.toInt(),
                "horario" to horario,
                "pagoHora" to pagoHora.toDouble(), // Almacenar como número decimal
                "usuarioId" to userId, // Guardar el ID del usuario que creó la oferta
                "estado" to "Pendiente", // Estado por defecto
                // 👇 Aquí se guarda como objeto (mapa) correctamente
                "trabajador" to mapOf(
                    "nombre" to "No asignado",
                    "correo" to "",
                    "telefono" to ""
                )
            )

            // Guardar en Firebase Firestore
            db.collection("ofertas")
                .add(oferta)
                .addOnSuccessListener {
                    Toast.makeText(this, "Oferta publicada con éxito", Toast.LENGTH_LONG).show()
                    finish() // Cierra la actividad después de guardar
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error al publicar: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }
    }
}
