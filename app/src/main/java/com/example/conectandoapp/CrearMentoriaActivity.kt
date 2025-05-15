package com.example.conectandoapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CrearMentoriaActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private lateinit var etTema: EditText
    private lateinit var etDescripcion: EditText
    private lateinit var etHorario: EditText
    private lateinit var etDuracion: EditText
    private lateinit var btnPublicar: Button

    private var mentoriaId: String? = null
    private var estudiantesInscritos = 0

    @SuppressLint("WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crear_mentoria)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val btnVolver = findViewById<Button>(R.id.btnVolver)
        etTema = findViewById(R.id.etTema)
        etDescripcion = findViewById(R.id.etDescripcion)
        etHorario = findViewById(R.id.etHorario)
        etDuracion = findViewById(R.id.etDuracion)
        btnPublicar = findViewById(R.id.btnPublicarMentoria)

        btnVolver.setOnClickListener { finish() }

        // Si viene con ID, es edición
        mentoriaId = intent.getStringExtra("mentoriaId")

        if (mentoriaId != null) {
            btnPublicar.text = "Actualizar"
            cargarMentoria()
        }

        btnPublicar.setOnClickListener {
            if (mentoriaId != null) {
                actualizarMentoria()
            } else {
                crearMentoria()
            }
        }
    }

    private fun cargarMentoria() {
        db.collection("mentorias").document(mentoriaId!!)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    etTema.setText(doc.getString("tema") ?: "")
                    etDescripcion.setText(doc.getString("descripcion") ?: "")
                    etHorario.setText(doc.getString("horario") ?: "")
                    etDuracion.setText(doc.getString("duracion") ?: "")
                    estudiantesInscritos = (doc.getLong("estudiantesInscritos") ?: 0).toInt()

                    if (estudiantesInscritos > 0) {
                        Toast.makeText(this, "Esta mentoría tiene estudiantes inscritos. No se puede editar.", Toast.LENGTH_LONG).show()
                        btnPublicar.isEnabled = false
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al cargar mentoría", Toast.LENGTH_SHORT).show()
            }
    }

    private fun crearMentoria() {
        val tema = etTema.text.toString().trim()
        val descripcion = etDescripcion.text.toString().trim()
        val horario = etHorario.text.toString().trim()
        val duracion = etDuracion.text.toString().trim()

        if (tema.isEmpty() || descripcion.isEmpty() || horario.isEmpty() || duracion.isEmpty()) {
            Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_LONG).show()
            return
        }

        val userId = auth.currentUser?.uid ?: return

        val nuevoDocRef = db.collection("mentorias").document()
        val mentoria = hashMapOf(
            "id" to nuevoDocRef.id,
            "tema" to tema,
            "descripcion" to descripcion,
            "horario" to horario,
            "duracion" to duracion,
            "usuarioId" to userId,
            "estado" to "Disponible",
            "estudiantesInscritos" to 0,
            "inscritos" to emptyList<String>(),
            "contenidos" to emptyList<String>()
        )

        nuevoDocRef.set(mentoria)
            .addOnSuccessListener {
                Toast.makeText(this, "Mentoría creada con éxito", Toast.LENGTH_LONG).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun actualizarMentoria() {
        val tema = etTema.text.toString().trim()
        val descripcion = etDescripcion.text.toString().trim()
        val horario = etHorario.text.toString().trim()
        val duracion = etDuracion.text.toString().trim()

        if (tema.isEmpty() || descripcion.isEmpty() || horario.isEmpty() || duracion.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        val data = mapOf(
            "tema" to tema,
            "descripcion" to descripcion,
            "horario" to horario,
            "duracion" to duracion
        )

        db.collection("mentorias").document(mentoriaId!!)
            .update(data)
            .addOnSuccessListener {
                Toast.makeText(this, "Mentoría actualizada correctamente", Toast.LENGTH_LONG).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al actualizar: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
