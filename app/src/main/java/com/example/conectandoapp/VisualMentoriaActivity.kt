package com.example.conectandoapp

import android.content.Context
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

class VisualMentoriaActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MentoriaAdapter
    private val listaMentorias = mutableListOf<Mentorias>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mentoriasvisual)

        recyclerView = findViewById(R.id.recyclermentorias)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = MentoriaAdapter(this, listaMentorias)
        recyclerView.adapter = adapter

        cargarMentoriaslDesdeFirestore()

        val btnVolver: Button = findViewById(R.id.btnVolver)
        btnVolver.setOnClickListener {
            finish()
        }

        val btnActualizar: Button = findViewById(R.id.btnRefresh)
        btnActualizar.setOnClickListener {
            cargarMentoriaslDesdeFirestore()
            Toast.makeText(this, "Lista actualizada", Toast.LENGTH_SHORT).show()
        }
    }

    private fun cargarMentoriaslDesdeFirestore() {
        val db = FirebaseFirestore.getInstance()
        val user = FirebaseAuth.getInstance().currentUser
        val id = user?.uid ?: return

        db.collection("mentorias")
            .whereEqualTo("usuarioId", id)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Toast.makeText(this, "Error en tiempo real: ${e.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    listaMentorias.clear()
                    for (document in snapshots) {
                        val mentoria = document.toObject(Mentorias::class.java).copy(id = document.id)
                        listaMentorias.add(mentoria)
                    }
                    adapter.notifyDataSetChanged()
                }
            }
    }
}

data class Mentorias(
    val tema: String = "",
    val descripcion: String = "",
    val horario: String = "",
    val duracion: String = "",
    val usuarioId: String = "",
    val estado: String = "",
    val estudiantesInscritos: Int = 0,
    val id: String = ""
)

class MentoriaAdapter(
    private val context: Context,
    private val listaMentorias: List<Mentorias>
) : RecyclerView.Adapter<MentoriaAdapter.MentoriaViewHolder>() {

    private val db = FirebaseFirestore.getInstance()

    class MentoriaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTema: TextView = view.findViewById(R.id.tvTema)
        val tvDescripcion: TextView = view.findViewById(R.id.tvDescripcion)
        val tvHorario: TextView = view.findViewById(R.id.tvHorario)
        val tvDuracion: TextView = view.findViewById(R.id.tvDuracion)
        val tvEstado: TextView = view.findViewById(R.id.tvEstado)
        val tvInscritos: TextView = view.findViewById(R.id.tvInscritos)
        val btnEditar: Button = view.findViewById(R.id.btnEditar)
        val btnEliminar: Button = view.findViewById(R.id.btnEliminar)
        val btnCambiarEstado: Button = view.findViewById(R.id.btnCambiarEstado)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MentoriaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_mentoria, parent, false)
        return MentoriaViewHolder(view)
    }

    override fun onBindViewHolder(holder: MentoriaViewHolder, position: Int) {
        val mentoria = listaMentorias[position]
        holder.tvTema.text = "🎓 Tema: ${mentoria.tema}"
        holder.tvDescripcion.text = "📌 ${mentoria.descripcion}"
        holder.tvHorario.text = "⏰ Horario: ${mentoria.horario}"
        holder.tvDuracion.text = "🕒 Duración: ${mentoria.duracion} Horas"
        holder.tvEstado.text = "📄 Estado: ${mentoria.estado}"
        holder.tvInscritos.text = "👥 Inscritos: ${mentoria.estudiantesInscritos}"


        // Botón Editar → visible solo si no hay estudiantes inscritos
        if (mentoria.estudiantesInscritos == 0) {
            holder.btnEditar.visibility = View.VISIBLE
            holder.btnEditar.setOnClickListener {
                val intent = Intent(context, CrearMentoriaActivity::class.java)
                intent.putExtra("mentoriaId", mentoria.id)
                context.startActivity(intent)
            }
        } else {
            holder.btnEditar.visibility = View.GONE
        }

        // Botón Eliminar → visible solo si no hay estudiantes inscritos
        if (mentoria.estudiantesInscritos == 0) {
            holder.btnEliminar.visibility = View.VISIBLE
            holder.btnEliminar.setOnClickListener {
                db.collection("mentorias").document(mentoria.id)
                    .delete()
                    .addOnSuccessListener {
                        Toast.makeText(context, "Mentoría eliminada", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Error al eliminar", Toast.LENGTH_SHORT).show()
                    }
            }
        } else {
            holder.btnEliminar.visibility = View.GONE
        }

        // Botón Cambiar Estado → visible solo si hay al menos un estudiante inscrito
        if (mentoria.estudiantesInscritos > 0) {
            holder.btnCambiarEstado.visibility = View.VISIBLE
            holder.btnCambiarEstado.setOnClickListener {
                val nuevoEstado = if (mentoria.estado == "Disponible") "Finalizado" else "Disponible"
                db.collection("mentorias").document(mentoria.id)
                    .update("estado", nuevoEstado)
                    .addOnSuccessListener {
                        Toast.makeText(context, "Estado actualizado", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Error al cambiar estado", Toast.LENGTH_SHORT).show()
                    }
            }
        } else {
            holder.btnCambiarEstado.visibility = View.GONE
        }
    }

    override fun getItemCount() = listaMentorias.size
}