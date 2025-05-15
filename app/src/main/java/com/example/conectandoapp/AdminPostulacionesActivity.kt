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
import com.google.firebase.firestore.FirebaseFirestore

class AdminPostulacionesActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var ofertaAdapter: OfertaAdapter
    private val ofertaList = mutableListOf<Oferta>()
    private lateinit var btnAgregarOferta: Button
    private lateinit var btnVolver: Button

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_postulaciones)

        recyclerView = findViewById(R.id.recyclerViewOfertas)
        recyclerView.layoutManager = LinearLayoutManager(this)
        btnAgregarOferta = findViewById(R.id.btnAgregarOferta)
        btnVolver = findViewById(R.id.btnVolverOfertas)

        ofertaAdapter = OfertaAdapter(ofertaList,
            onEditClick = { oferta ->
                val intent = Intent(this, EditarPostulacionActivity::class.java)
                intent.putExtra("POSTULACION_ID", oferta.id)
                startActivity(intent)
            },
            onDeleteClick = { oferta ->
                deleteOferta(oferta)
            }
        )
        recyclerView.adapter = ofertaAdapter

        fetchOfertasRealtime()

        btnAgregarOferta.setOnClickListener {
            val intent = Intent(this, CrearOfertaActivity::class.java)
            intent.putExtra("modo_edicion", false)
            startActivity(intent)
        }

        btnVolver.setOnClickListener {
            finish()
        }
    }

    private fun fetchOfertasRealtime() {
        db.collection("ofertas")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Toast.makeText(this, "Error al cargar ofertas: ${e.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                ofertaList.clear()
                snapshot?.documents?.forEach { document ->
                    val oferta = document.toObject(Oferta::class.java)
                    oferta?.id = document.id
                    ofertaList.add(oferta!!)
                }
                ofertaAdapter.notifyDataSetChanged()
            }
    }

    private fun deleteOferta(oferta: Oferta) {
        db.collection("ofertas").document(oferta.id)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Postulacion eliminada", Toast.LENGTH_SHORT).show()
                ofertaList.remove(oferta)
                ofertaAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al eliminar postulacion", Toast.LENGTH_SHORT).show()
            }
    }

    data class Oferta(
        var id: String = "",
        var titulo: String = "",
        var puesto: String = "",
        var descripcion: String = ""
        // Agrega otros campos necesarios
    )

    class OfertaAdapter(
        private val ofertaList: List<Oferta>,
        private val onEditClick: (Oferta) -> Unit,
        private val onDeleteClick: (Oferta) -> Unit
    ) : RecyclerView.Adapter<OfertaAdapter.OfertaViewHolder>() {

        inner class OfertaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvTitulo: TextView = view.findViewById(R.id.tvTituloOferta)
            val tvDescripcion: TextView = view.findViewById(R.id.tvDescripcionOferta)

            val btnEditar: Button = view.findViewById(R.id.btnEditarOferta)
            val btnEliminar: Button = view.findViewById(R.id.btnEliminarOferta)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OfertaViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_oferta_admin, parent, false)
            return OfertaViewHolder(view)
        }

        override fun onBindViewHolder(holder: OfertaViewHolder, position: Int) {
            val oferta = ofertaList[position]
            holder.tvTitulo.text = oferta.puesto
            holder.tvDescripcion.text = oferta.descripcion


            holder.btnEditar.setOnClickListener { onEditClick(oferta) }
            holder.btnEliminar.setOnClickListener { onDeleteClick(oferta) }
        }

        override fun getItemCount(): Int = ofertaList.size
    }
}
