package com.example.conectandoapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class EliminarPostulacionActivity : AppCompatActivity() {

    private lateinit var tvConfirmacion: TextView
    private lateinit var btnEliminar: Button
    private lateinit var btnCancelar: Button
    private val db = FirebaseFirestore.getInstance()
    private var postulacionId: String? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_eliminar_postulacion)

        tvConfirmacion = findViewById(R.id.tvConfirmacion)
        btnEliminar = findViewById(R.id.btnEliminar)
        btnCancelar = findViewById(R.id.btnCancelar)

        postulacionId = intent.getStringExtra("POSTULACION_ID")
        val puesto = intent.getStringExtra("PUESTO")

        tvConfirmacion.text = "¿Deseas eliminar la postulación para el puesto de '$puesto'?"

        btnEliminar.setOnClickListener {
            eliminarPostulacion()
        }

        btnCancelar.setOnClickListener {
            finish()
        }
    }

    private fun eliminarPostulacion() {
        postulacionId?.let { id ->
            db.collection("ofertas").document(id)
                .delete()
                .addOnSuccessListener {
                    Toast.makeText(this, "Postulación eliminada", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al eliminar", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
