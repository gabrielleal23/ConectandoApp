package com.example.conectandoapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ListarPostulacionEdit : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PostulacionAdapter
    private val postulaciones = mutableListOf<Postulacion>()
    private lateinit var btnVolver: Button
    private lateinit var tvSinOfertas: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_listar_postulaciones_edit)

        recyclerView = findViewById(R.id.recyclerPostulaciones)
        adapter = PostulacionAdapter(postulaciones)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        btnVolver = findViewById(R.id.btnVolver)
        tvSinOfertas = findViewById(R.id.tvSinOfertas)

        btnVolver.setOnClickListener {
            finish() }

        obtenerPostulacionesPendientes()
    }

    private fun obtenerPostulacionesPendientes() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseFirestore.getInstance().collection("ofertas")
            .whereEqualTo("usuarioId", userId)
            .whereEqualTo("estado", "Pendiente")
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Log.e("Postulaciones", "❌ Error al obtener datos", error)
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    postulaciones.clear()

                    if (snapshots.isEmpty) {
                        tvSinOfertas.visibility = View.VISIBLE
                    } else {
                        tvSinOfertas.visibility = View.GONE


                            for (doc in snapshots.documents) {
                                val id = doc.id
                                val puesto = doc.getString("puesto") ?: ""
                                postulaciones.add(Postulacion(id, puesto))
                            }
                        }
                    }
                    adapter.notifyDataSetChanged()
                }
    }

    data class Postulacion(val id: String, val puesto: String)

    inner class PostulacionAdapter(private val items: List<Postulacion>) :
        RecyclerView.Adapter<PostulacionAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvId: TextView = view.findViewById(R.id.tvPostulacionId)
            val tvPuesto: TextView = view.findViewById(R.id.tvPuesto)
            val btnEditar: Button = view.findViewById(R.id.btnEditar)
            val btnEliminar: Button = view.findViewById(R.id.btnEliminar)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_postuiacion_pendiente, parent, false)
            return ViewHolder(view)
        }

        override fun getItemCount() = items.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val postulacion = items[position]
            holder.tvId.text = "ID: ${postulacion.id}"
            holder.tvPuesto.text = postulacion.puesto

            holder.btnEditar.setOnClickListener {
                val intent = Intent(this@ListarPostulacionEdit, EditarPostulacionActivity::class.java)
                intent.putExtra("POSTULACION_ID", postulacion.id)
                startActivity(intent)
            }

            holder.btnEliminar.setOnClickListener {
                FirebaseFirestore.getInstance().collection("ofertas")
                    .document(postulacion.id)
                    .delete()
                    .addOnSuccessListener {
                        Toast.makeText(this@ListarPostulacionEdit, "✅ Postulación eliminada", Toast.LENGTH_SHORT).show()
                        obtenerPostulacionesPendientes()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this@ListarPostulacionEdit, "❌ Error al eliminar", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }
}
