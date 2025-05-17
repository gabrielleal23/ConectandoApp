package com.example.conectandoapp

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PostulateStudentActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: OfertasAdapter
    private val ofertasList = mutableListOf<Oferta>()
    private lateinit var tvSinOfertas: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_postulate_student)
        tvSinOfertas = findViewById(R.id.tvSinOfertas)

        recyclerView = findViewById(R.id.recyclerViewJobs)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = OfertasAdapter(ofertasList)
        recyclerView.adapter = adapter

        cargarOfertasDesdeFirestore()

        val btnVolver: Button = findViewById(R.id.btnVolver)
        btnVolver.setOnClickListener {
            finish() // Regresa a la pantalla anterior
        }
    }

    private fun cargarOfertasDesdeFirestore() {
        val db = FirebaseFirestore.getInstance()
        db.collection("ofertas")
            .whereEqualTo("estado", "Pendiente")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Toast.makeText(this, "Error al cargar ofertas", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    ofertasList.clear()

                    if (snapshots.isEmpty) {
                        tvSinOfertas.visibility = View.VISIBLE
                    } else {
                        tvSinOfertas.visibility = View.GONE
                        for (document in snapshots) {

                            val trabajadorObj = document.get("trabajador")
                            val trabajador = when (trabajadorObj) {
                                is Map<*, *> -> Trabajador(
                                    nombre = trabajadorObj["nombre"] as? String ?: "",
                                    correo = trabajadorObj["correo"] as? String ?: ""
                                )
                                else -> Trabajador(nombre = "No asignado", correo = "")
                            }

                            val oferta = Oferta(
                                id = document.id,
                                puesto = document.getString("puesto") ?: "",
                                descripcion = document.getString("descripcion") ?: "",
                                ubicacion = document.getString("ubicacion") ?: "",
                                horario = document.getString("horario") ?: "",
                                pagoHora = document.getDouble("pagoHora") ?: 0.0,
                                requisitos = document.getString("requisitos") ?: "",
                                estado = document.getString("estado") ?: "",
                                trabajador = trabajador
                            )

                            ofertasList.add(oferta)
                        }
                    }
                    adapter.notifyDataSetChanged()
                }
            }
    }

}

data class Trabajador(
    val nombre: String = "",
    val correo: String = ""
)

// Modelo de datos Oferta
data class Oferta(
    val id: String = "",
    val puesto: String = "",
    val descripcion: String = "",
    val ubicacion: String = "",
    val horario: String = "",
    val pagoHora: Double = 0.0,
    val requisitos: String = "",
    val estado: String = "",
    val trabajador:Trabajador = Trabajador(),
    val calificacion: Float? = null  // Este campo debe existir

)

// Adapter para RecyclerView
class OfertasAdapter(private val listaOfertas: List<Oferta>) :
    RecyclerView.Adapter<OfertasAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvPuesto: TextView = view.findViewById(R.id.tvPuesto)
        val tvDescripcion: TextView = view.findViewById(R.id.tvDescripcion)
        val tvUbicacion: TextView = view.findViewById(R.id.tvUbicacion)
        val tvHorario: TextView = view.findViewById(R.id.tvHorario)
        val tvPagoHora: TextView = view.findViewById(R.id.tvPagoHora)
        val tvRequisitos: TextView = view.findViewById(R.id.tvRequisitos)
        val tvEstado: TextView = view.findViewById(R.id.tvEstado)
        val btnAceptar: Button = view.findViewById(R.id.btnAceptar)


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_postulate_offer_student, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val oferta = listaOfertas[position]
        holder.tvPuesto.text = oferta.puesto.uppercase()
        holder.tvDescripcion.text = "📌 ${oferta.descripcion}"
        holder.tvUbicacion.text = "📍 ${oferta.ubicacion}"
        holder.tvHorario.text = "⏰ ${oferta.horario}"
        holder.tvPagoHora.text = "💰 Pago: $${oferta.pagoHora}"
        holder.tvRequisitos.text = "📑 Requisitos: ${oferta.requisitos}"
        holder.tvEstado.text = when (oferta.estado) {
            "pendiente" -> "⏳ Pendiente"
            "Aceptada" -> "✅ Aceptada"
            "Finalizada" -> "🏁 Finalizada"
            "Rechazada" -> "❌ Rechazada"
            "En progreso" -> "🔄 En progreso"
            else -> oferta.estado
        }
        holder.btnAceptar.setOnClickListener {

            val db = FirebaseFirestore.getInstance()
            val auth = FirebaseAuth.getInstance()
            val user = auth.currentUser

            if (user != null) {
                val userId = user.uid

                db.collection("users").document(userId).get()
                    .addOnSuccessListener { document ->
                        if (document.exists()) {
                            val nombre = document.getString("nombre") ?: "Nombre no disponible"
                            val correo = document.getString("email") ?: "Correo no disponible"
                            val telefono = document.getString("telefono") ?: "Telefono no disponible"

                            val trabajadorData = mapOf(
                                "nombre" to nombre,
                                "correo" to correo,
                                "telefono" to telefono

                            )

                            val ofertaId = listaOfertas[position].id

                            db.collection("ofertas").document(ofertaId)
                                .update(
                                    mapOf(
                                        "estado" to "Aceptada",
                                        "trabajador" to trabajadorData
                                    )
                                )
                                .addOnSuccessListener {
                                    Toast.makeText(holder.itemView.context, "Oferta aceptada", Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener {
                                    Toast.makeText(holder.itemView.context, "Error al aceptar oferta", Toast.LENGTH_SHORT).show()
                                }

                        } else {
                            Toast.makeText(holder.itemView.context, "Usuario no encontrado", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(holder.itemView.context, "Error al obtener datos del usuario", Toast.LENGTH_SHORT).show()
                    }

            } else {
                Toast.makeText(holder.itemView.context, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun getItemCount() = listaOfertas.size
}
