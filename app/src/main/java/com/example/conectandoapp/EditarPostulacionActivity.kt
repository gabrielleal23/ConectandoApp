package com.example.conectandoapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class EditarPostulacionActivity : AppCompatActivity() {

    private lateinit var etDescripcion: EditText
    private lateinit var etHorario: EditText
    private lateinit var etHoras: EditText
    private lateinit var etPagoHora: EditText
    private lateinit var etPuesto: EditText
    private lateinit var etRequisitos: EditText
    private lateinit var etUbicacion: EditText
    private lateinit var btnGuardar: Button
    private lateinit var btnDevolver: Button

    private val db = FirebaseFirestore.getInstance()
    private var postulacionId: String? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editar_postulacion)

        inicializarVistas()

        postulacionId = intent.getStringExtra("POSTULACION_ID")

        if (postulacionId != null) {
            cargarPostulacion(postulacionId!!)
        } else {
            mostrarError("No se encontró el ID de la postulación")
            finish()
        }

        btnGuardar.setOnClickListener { guardarCambios() }
        btnDevolver.setOnClickListener { finish() }
    }

    private fun inicializarVistas() {
        etDescripcion = findViewById(R.id.etDescripcion)
        etHorario = findViewById(R.id.etHorario)
        etHoras = findViewById(R.id.etHoras)
        etPagoHora = findViewById(R.id.etPagoHora)
        etPuesto = findViewById(R.id.etPuesto)
        etRequisitos = findViewById(R.id.etRequisitos)
        etUbicacion = findViewById(R.id.etUbicacion)
        btnGuardar = findViewById(R.id.btnGuardar)
        btnDevolver = findViewById(R.id.btnDevolver)

    }

    private fun cargarPostulacion(id: String) {
        db.collection("ofertas").document(id)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    etDescripcion.setText(document.getString("descripcion") ?: "")
                    etHorario.setText(document.getString("horario") ?: "")
                    etHoras.setText(document.getLong("horas")?.toString() ?: "")
                    etPagoHora.setText(document.getDouble("pagoHora")?.toInt()?.toString() ?: "")
                    etPuesto.setText(document.getString("puesto") ?: "")
                    etRequisitos.setText(document.getString("requisitos") ?: "")
                    etUbicacion.setText(document.getString("ubicacion") ?: "")
                } else {
                    mostrarError("No se encontró la postulación")
                    finish()
                }
            }
            .addOnFailureListener { e ->
                mostrarError("Error al cargar datos: ${e.message}")
                finish()
            }
    }

    private fun guardarCambios() {
        if (!validarCampos()) return

        val nuevosDatos = mapOf(
            "descripcion" to etDescripcion.text.toString(),
            "horario" to etHorario.text.toString(),
            "horas" to etHoras.text.toString().toInt(),
            "pagoHora" to (etPagoHora.text.toString().toDoubleOrNull() ?: 0.0),
            "puesto" to etPuesto.text.toString(),
            "requisitos" to etRequisitos.text.toString(),
            "ubicacion" to etUbicacion.text.toString()
        )

        postulacionId?.let { id ->
            db.collection("ofertas").document(id)
                .update(nuevosDatos)
                .addOnSuccessListener {
                    mostrarMensaje("✅ Postulación actualizada")
                    finish()
                }
                .addOnFailureListener { e ->
                    mostrarError("❌ Error al actualizar: ${e.message}")
                }
        }
    }

    private fun validarCampos(): Boolean {
        return when {
            etDescripcion.text.isEmpty() -> {
                mostrarError("La descripción no puede estar vacía"); false
            }
            etHorario.text.isEmpty() -> {
                mostrarError("El horario no puede estar vacío"); false
            }
            etPagoHora.text.isEmpty() || etPagoHora.text.toString().toDoubleOrNull() == null -> {
                mostrarError("Pago por hora inválido"); false
            }
            etPuesto.text.isEmpty() -> {
                mostrarError("El puesto no puede estar vacío"); false
            }
            etRequisitos.text.isEmpty() -> {
                mostrarError("Los requisitos no pueden estar vacíos"); false
            }
            etUbicacion.text.isEmpty() -> {
                mostrarError("La ubicación no puede estar vacía"); false
            }
            etHoras.text.isEmpty() -> {
                mostrarError("Las horas no pueden estar vacias"); false
            }
            else -> true
        }
    }

    private fun mostrarMensaje(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    private fun mostrarError(msg: String) {
        Toast.makeText(this, "⚠️ $msg", Toast.LENGTH_LONG).show()
    }
}
