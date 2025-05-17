package com.example.conectandoapp

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class PagoPostulacionActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var totalPagosTextView: TextView
    private val offers = mutableListOf<JobOffer>()
    private lateinit var adapter: PagoPostulacionAdapter
    private var listener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pago_postulacion)

        recyclerView = findViewById(R.id.recyclerPagos)
        totalPagosTextView = findViewById(R.id.tvTotalGeneral)

        adapter = PagoPostulacionAdapter(offers)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        escucharOfertasFinalizadas()

        findViewById<Button>(R.id.btnVolver).setOnClickListener {
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        listener?.remove() // detener el listener en tiempo real al cerrar
    }

    private fun escucharOfertasFinalizadas() {
        val db = FirebaseFirestore.getInstance()
        val user = FirebaseAuth.getInstance().currentUser
        val userId = user?.uid ?: return

        listener = db.collection("ofertas")
            .whereEqualTo("usuarioId", userId)
            .whereIn("estado", listOf("Finalizada", "Pagada")) // Mostrar ambas
            .addSnapshotListener { result, error ->
                if (error != null) {
                    Toast.makeText(this, "Error al cargar ofertas", Toast.LENGTH_SHORT).show()
                    Log.e("PagoPostulacion", "Error realtime", error)
                    return@addSnapshotListener
                }

                offers.clear()
                var totalGeneral = 0.0

                for (document in result!!) {
                    try {
                        val estadoReal = document.getString("estado") ?: "Finalizada"
                        val puesto = document.getString("puesto") ?: ""
                        val horas = document.getLong("horas")?.toInt() ?: 0
                        val pagoHora = document.getDouble("pagoHora") ?: 0.0
                        val trabajadorMap = document.get("trabajador") as? Map<*, *>

                        val trabajador = if (trabajadorMap != null) {
                            Trabajador3(
                                nombre = trabajadorMap["nombre"] as? String ?: "",
                                correo = trabajadorMap["correo"] as? String ?: "",
                                telefono = trabajadorMap["telefono"] as? String ?: ""
                            )
                        } else Trabajador3()

                        val offer = JobOffer(
                            id = document.id,
                            puesto = puesto,
                            horas = horas,
                            pagoHora = pagoHora,
                            estado = estadoReal,  // Aquí pasamos el estado real
                            trabajador = trabajador
                        )

                        offers.add(offer)
                        totalGeneral += horas * pagoHora

                    } catch (e: Exception) {
                        Log.e("PagoPostulacion", "❌ Error procesando oferta", e)
                    }
                }

                totalPagosTextView.text = "Total Pagado: $${String.format("%.2f", totalGeneral)}"
                adapter.notifyDataSetChanged()
            }
    }

    data class JobOffer(
        val id: String = "",
        val puesto: String = "",
        val horas: Int = 0,
        val pagoHora: Double = 0.0,
        var estado: String = "Finalizada",
        val trabajador: Trabajador3 = Trabajador3()
    )

    data class Trabajador3(
        val nombre: String = "",
        val correo: String = "",
        val telefono: String = ""
    )

    inner class PagoPostulacionAdapter(private val offers: List<JobOffer>) : RecyclerView.Adapter<PagoPostulacionAdapter.PagoViewHolder>() {

        inner class PagoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvTitulo: TextView = view.findViewById(R.id.tvTitulo)
            val tvTotalPago: TextView = view.findViewById(R.id.tvTotalPago)
            val btnPagar: Button = view.findViewById(R.id.btnPagar)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PagoViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_pago_postulacion, parent, false)
            return PagoViewHolder(view)
        }

        override fun onBindViewHolder(holder: PagoViewHolder, position: Int) {
            val offer = offers[position]
            val total = offer.horas * offer.pagoHora

            holder.tvTitulo.text = offer.puesto
            holder.tvTotalPago.text = "Pago: $${String.format("%.2f", total)}"

            if (offer.estado == "Pagada") {
                holder.btnPagar.visibility = View.GONE  // Oculta el botón si ya pagada
            } else {
                holder.btnPagar.visibility = View.VISIBLE
                holder.btnPagar.text = "Pagar"
                holder.btnPagar.isEnabled = true

                holder.btnPagar.setOnClickListener {
                    val db = FirebaseFirestore.getInstance()
                    db.collection("ofertas").document(offer.id)
                        .update("estado", "Pagada")
                        .addOnSuccessListener {
                            Toast.makeText(this@PagoPostulacionActivity, "✅ Pago registrado", Toast.LENGTH_SHORT).show()
                            offer.estado = "Pagada"              // Actualizar el estado local
                            notifyItemChanged(position)         // Refrescar solo ese ítem
                        }
                        .addOnFailureListener {
                            Toast.makeText(this@PagoPostulacionActivity, "❌ Error al pagar", Toast.LENGTH_SHORT).show()
                        }
                }
            }
        }

        override fun getItemCount() = offers.size
    }
}
