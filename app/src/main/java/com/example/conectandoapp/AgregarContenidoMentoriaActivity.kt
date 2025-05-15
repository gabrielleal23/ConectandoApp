package com.example.conectandoapp

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth

class AgregarContenidoMentoriaActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MentoriaAdapters
    private lateinit var contenidoAdapter: VerContenidosMentoriaActivity.ContenidoAdapter
    private lateinit var contenidoList: MutableList<VerContenidosMentoriaActivity.ContenidoMentoria>
    private lateinit var mentoriaList: MutableList<MentoriasData>  // Cambiado el tipo de lista a MentoriasData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_agregar_contenido_mentoria)

        db = FirebaseFirestore.getInstance()

        recyclerView = findViewById(R.id.recyclermentorias)
        recyclerView.layoutManager = LinearLayoutManager(this)

        mentoriaList = mutableListOf()
        contenidoList = mutableListOf()

        adapter = MentoriaAdapters(this.mentoriaList) { mentoriaId ->
            cargarContenidosDeMentoria(mentoriaId)
        }

        recyclerView.adapter = adapter

        // Cargar las mentorías del usuario logueado
        cargarMentoriasDelUsuario()

        val btnVolver: Button = findViewById(R.id.btnVolver)
        btnVolver.setOnClickListener {
            finish()  // Volver a la pantalla anterior
        }


    }

    private fun cargarMentoriasDelUsuario() {
        val user = FirebaseAuth.getInstance().currentUser
        val id = user?.uid

        if (id != null) {
            mentoriaList.clear()

            db.collection("mentorias")
                .whereEqualTo("usuarioId", id)
                .get()
                .addOnSuccessListener { documentos ->
                    for (document in documentos) {
                        val mentoria = document.toObject(MentoriasData::class.java).copy(id = document.id) // Cambiado aquí también
                        mentoriaList.add(mentoria)
                    }
                    adapter.notifyDataSetChanged()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al cargar mentorias", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun cargarContenidosDeMentoria(mentoriaId: String) {
        // Cargar los contenidos de la mentoría seleccionada
        db.collection("mentorias")
            .document(mentoriaId)
            .collection("contenidos")
            .get()
            .addOnSuccessListener { result ->
                contenidoList.clear()
                for (doc in result) {
                    val contenido = doc.toObject(VerContenidosMentoriaActivity.ContenidoMentoria::class.java)
                    contenidoList.add(contenido)
                }
                // Mostrar los contenidos en el RecyclerView
                contenidoAdapter = VerContenidosMentoriaActivity.ContenidoAdapter(contenidoList)
                recyclerView.adapter = contenidoAdapter
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al cargar los contenidos", Toast.LENGTH_SHORT).show()
            }
    }

    private fun guardarContenido(mentoriaId: String, contenidoTexto: String) {
        // Guardar el nuevo contenido en la subcolección 'contenidos' de la mentoría seleccionada
        val contenidoRef = db.collection("mentorias")
            .document(mentoriaId)
            .collection("contenidos")
            .document()

        val contenido = mapOf(
            "id" to contenidoRef.id,
            "texto" to contenidoTexto,
            "fecha" to Timestamp.now()
        )

        contenidoRef.set(contenido)
            .addOnSuccessListener {
                Toast.makeText(this, "Contenido guardado exitosamente", Toast.LENGTH_SHORT).show()
                cargarContenidosDeMentoria(mentoriaId)  // Recargar los contenidos
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al guardar el contenido", Toast.LENGTH_SHORT).show()
            }
    }
}

data class MentoriasData(  // Usa tu data class de mentorías aquí
    val tema: String = "",
    val descripcion: String = "",
    val usuarioId: String = "",
    val id: String = "",
    val estudiantesInscritos:Int = 0
)

class MentoriaAdapters(
    private val listaMentorias: List<MentoriasData>,
    private val onMentoriaClick: (String) -> Unit
) : RecyclerView.Adapter<MentoriaAdapters.MentoriasViewHolder>() {

    class MentoriasViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTema: TextView = view.findViewById(R.id.tvTema)
        val tvInscritos: TextView = view.findViewById(R.id.inscritos)
        val btnVerContenidos: Button = view.findViewById(R.id.btnVerContenidos)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MentoriasViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_mentoria_simple, parent, false)
        return MentoriasViewHolder(view)
    }

    override fun onBindViewHolder(holder: MentoriasViewHolder, position: Int) {
        val mentoria = listaMentorias[position]
        holder.tvTema.text = mentoria.tema
        holder.tvInscritos.text = "👥 Inscritos: ${mentoria.estudiantesInscritos}"
        holder.btnVerContenidos.setOnClickListener {
            val intent = Intent(it.context, VerContenidosMentoriaActivity::class.java)
            intent.putExtra("MENTORIA_ID", mentoria.id)
            it.context.startActivity(intent)

        }
    }

    override fun getItemCount() = listaMentorias.size
}