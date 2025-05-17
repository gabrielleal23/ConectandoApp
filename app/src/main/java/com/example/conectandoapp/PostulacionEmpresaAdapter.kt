package com.example.conectandoapp

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class PostulacionEmpresaAdapter(private val listaPostulaciones: List<Postulacion>) :
    RecyclerView.Adapter<PostulacionEmpresaAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvUserId: TextView = view.findViewById(R.id.tvUserId)
        val tvPuesto: TextView = view.findViewById(R.id.tvPuesto)
        val tvDescripcion: TextView = view.findViewById(R.id.tvDescripcion)
        val tvUbicacion: TextView = view.findViewById(R.id.tvUbicacion)
        val tvHorario: TextView = view.findViewById(R.id.tvHorario)
        val tvPagoHora: TextView = view.findViewById(R.id.tvPagoHora)
        val tvRequisitos: TextView = view.findViewById(R.id.tvRequisitos)
        val tvEstado: TextView = view.findViewById(R.id.tvEstado)
        val tvPostulante : TextView = view.findViewById(R.id.tvPostulante)
        val btnFinalizar: Button = view.findViewById(R.id.btnFinalizar)
        val ratingBar: RatingBar = view.findViewById(R.id.ratingBar)
        val tvcalifica: TextView = view.findViewById(R.id.tvcalifica)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_postulacion, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val postulacion = listaPostulaciones[position]
        val trabajador = postulacion.trabajador

        holder.tvUserId.text = "ID Postulacion: ${postulacion.id}"
        holder.tvPuesto.text = "Puesto: ${postulacion.puesto}"
        holder.tvDescripcion.text = "Descripción: ${postulacion.descripcion}"
        holder.tvUbicacion.text = "Ubicación: ${postulacion.ubicacion}"
        holder.tvHorario.text = "Horario: ${postulacion.horario}"
        holder.tvPagoHora.text = "Pago por Hora: $${postulacion.pagoHora}"
        holder.tvRequisitos.text = "Requisitos: ${postulacion.requisitos}"
        holder.tvEstado.text = "Estado: ${postulacion.estado}"
        holder.tvPostulante.text = "Postulante: ${trabajador.nombre}"

        if (postulacion.estado == "Aceptada") {
            // Mostrar botón Finalizar
            holder.btnFinalizar.visibility = View.VISIBLE
            holder.btnFinalizar.isEnabled = true
            holder.ratingBar.visibility = View.GONE
            holder.tvcalifica.visibility = View.GONE

            holder.btnFinalizar.setOnClickListener {
                val db = FirebaseFirestore.getInstance()
                db.collection("ofertas").document(postulacion.id)
                    .update("estado", "Finalizada")
                    .addOnSuccessListener {
                        Toast.makeText(holder.itemView.context, "✅ Oferta finalizada", Toast.LENGTH_SHORT).show()
                        postulacion.estado = "Finalizada"
                        holder.tvEstado.text = "Estado: Finalizada"

                        holder.btnFinalizar.visibility = View.GONE
                        holder.ratingBar.visibility = View.VISIBLE
                        holder.tvcalifica.visibility = View.VISIBLE

                    }
                    .addOnFailureListener {
                        Toast.makeText(holder.itemView.context, "❌ Error al finalizar", Toast.LENGTH_SHORT).show()
                    }
            }
        }
        else if (postulacion.estado == "Finalizada" || postulacion.estado == "Pagada") {
            // Ocultar botón y mostrar rating visual y texto
            holder.btnFinalizar.visibility = View.GONE
            holder.ratingBar.visibility = View.VISIBLE
            holder.tvcalifica.visibility = View.VISIBLE
            
        }
        else {
            // Otros estados: ocultar botón y rating
            holder.btnFinalizar.visibility = View.GONE
            holder.ratingBar.visibility = View.GONE
            holder.tvcalifica.visibility = View.GONE
        }
    }




    override fun getItemCount() = listaPostulaciones.size
}
