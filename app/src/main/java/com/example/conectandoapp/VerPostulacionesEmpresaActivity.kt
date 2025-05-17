package com.example.conectandoapp

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

// Modelo de datos actualizado para cada postulación


data class Postulacion(
    val id: String = "",
    val usuarioNombre: String = "",
    val usuarioEmail: String = "",
    val puesto: String = "",
    val descripcion: String = "",
    val ubicacion: String = "",
    val horario: String = "",
    val pagoHora: Double = 0.0,
    val requisitos: String = "",
    var estado: String = "",
    var trabajador: Trabajador2 = Trabajador2()
)

data class Trabajador2(
    val nombre: String = "",
    val correo: String = "",
    val telefono: String = ""
)

class VerPostulacionesActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PostulacionEmpresaAdapter
    private val db = FirebaseFirestore.getInstance()
    private val listaPostulaciones = mutableListOf<Postulacion>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ver_postulaciones)

        recyclerView = findViewById(R.id.recyclerViewPostulaciones)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = PostulacionEmpresaAdapter(listaPostulaciones)
        recyclerView.adapter = adapter

        val btnVolver: Button = findViewById(R.id.btnVolver)
        btnVolver.setOnClickListener { finish() } // Cierra la actividad y vuelve atrás

        obtenerPostulaciones()
    }

    private fun obtenerPostulaciones() {
        val usuario = FirebaseAuth.getInstance().currentUser
        val id = usuario?.uid

        if (id != null) {
            db.collection("ofertas")
                .whereEqualTo("usuarioId", id)
                .whereIn("estado", listOf("Pendiente", "Aceptada", "Finalizada", "Pagada"))
                .addSnapshotListener { snapshots, e ->
                    if (e != null) {
                        Log.e("Firestore", "❌ Error al escuchar cambios", e)
                        return@addSnapshotListener
                    }
                    if (snapshots != null) {
                        listaPostulaciones.clear()
                        for (document in snapshots) {
                            try {


                                val postulacion = document.toObject(Postulacion::class.java).copy(id = document.id)
                                listaPostulaciones.add(postulacion)

                            } catch (ex: Exception) {
                                Log.e("Postulaciones", "⚠️ Error deserializando documento ${document.id}: ${ex.message}")
                            }
                        }
                        adapter.notifyDataSetChanged()
                    }
                }
        } else {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
        }
    }
}
