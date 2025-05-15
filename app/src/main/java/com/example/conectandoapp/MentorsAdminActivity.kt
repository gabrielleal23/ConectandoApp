package com.example.conectandoapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class MentorsAdminActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var mentoriaAdapter: MentoriaAdapter
    private val mentoriaList = mutableListOf<Mentoria>()
    private lateinit var btnAgregarMentoria: Button
    private lateinit var btnVolver: Button

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mentoria_management)

        recyclerView = findViewById(R.id.recyclerViewMentorias)
        recyclerView.layoutManager = LinearLayoutManager(this)

        btnAgregarMentoria = findViewById(R.id.btnAgregarMentoria)
        btnVolver = findViewById(R.id.btnVolver)


        mentoriaAdapter = MentoriaAdapter(
            mentoriaList,
            onEditClick = { mentoria ->
                val intent = Intent(this, CrearMentoriaActivity::class.java)
                intent.putExtra("mentoriaId", mentoria.mentoriaId)
                intent.putExtra("modo_admin", true)
                startActivity(intent)
            },
            onDeleteClick = { mentoria ->
                deleteMentoria(mentoria)
            },
            onContenidoClick = { mentoria ->
                Log.d("MentorsAdmin", "Mentoría seleccionada: ID=${mentoria.mentoriaId}, Tema=${mentoria.tema}")
                val intent = Intent(this, VerContenidosMentoriaActivity::class.java)
                intent.putExtra("MENTORIA_ID", mentoria.mentoriaId)
                startActivity(intent)

            }
        )
        recyclerView.adapter = mentoriaAdapter

        fetchMentoriasRealtime()

        btnAgregarMentoria.setOnClickListener {
            val intent = Intent(this, CrearMentoriaActivity::class.java)
            startActivity(intent)
        }

        btnVolver.setOnClickListener {
            finish()
        }
    }

    private fun fetchMentoriasRealtime() {
        db.collection("mentorias")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Toast.makeText(this, "Error al cargar mentorías: ${e.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                mentoriaList.clear()
                snapshot?.documents?.forEach { document ->
                    val mentoria = document.toObject(Mentoria::class.java)
                    mentoria?.mentoriaId = document.id
                    mentoriaList.add(mentoria!!)
                }
                mentoriaAdapter.notifyDataSetChanged()
            }
    }

    private fun deleteMentoria(mentoria: Mentoria) {
        db.collection("mentorias").document(mentoria.mentoriaId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Mentoría eliminada", Toast.LENGTH_SHORT).show()
                mentoriaList.remove(mentoria)
                mentoriaAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al eliminar mentoría", Toast.LENGTH_SHORT).show()
            }
    }

    data class Mentoria(
        var mentoriaId: String = "",
        var tema: String = "",
        var descripcion: String = ""
    )

    class MentoriaAdapter(
        private val mentoriaList: List<Mentoria>,
        private val onEditClick: (Mentoria) -> Unit,
        private val onDeleteClick: (Mentoria) -> Unit,
        private val onContenidoClick: (Mentoria) -> Unit
    ) : RecyclerView.Adapter<MentoriaAdapter.MentoriaViewHolder>() {

        inner class MentoriaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvTitulo: TextView = view.findViewById(R.id.tvMentoriaTitulo)
            val tvDescripcion: TextView = view.findViewById(R.id.tvMentoriaDescripcion)
            val btnEdit: Button = view.findViewById(R.id.btnEditMentoria)
            val btnDelete: Button = view.findViewById(R.id.btnDeleteMentoria)
            val btnContenido : Button = view.findViewById(R.id.btnContenidoMentoria)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MentoriaViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_mentoria_admin, parent, false)
            return MentoriaViewHolder(view)
        }

        override fun onBindViewHolder(holder: MentoriaViewHolder, position: Int) {
            val mentoria = mentoriaList[position]
            holder.tvTitulo.text = mentoria.tema
            holder.tvDescripcion.text = mentoria.descripcion

            holder.btnEdit.setOnClickListener { onEditClick(mentoria) }
            holder.btnDelete.setOnClickListener { onDeleteClick(mentoria) }
            holder.btnContenido.setOnClickListener { onContenidoClick(mentoria) }
        }

        override fun getItemCount(): Int = mentoriaList.size
    }
}
