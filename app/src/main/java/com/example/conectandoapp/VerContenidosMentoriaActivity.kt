package com.example.conectandoapp

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale
import java.text.SimpleDateFormat
import java.util.TimeZone

class VerContenidosMentoriaActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var recyclerView: RecyclerView
    private lateinit var contenidoAdapter: ContenidoAdapter
    private var contenidoList = mutableListOf<ContenidoMentoria>()
    private var mentoriaId: String = ""

    private lateinit var etContenido: EditText
    private lateinit var btnGuardarContenido: Button
    private lateinit var textotit: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ver_contenidos_mentoria)

        db = FirebaseFirestore.getInstance()
        recyclerView = findViewById(R.id.recyclerContenidos)
        recyclerView.layoutManager = LinearLayoutManager(this)



        mentoriaId = intent.getStringExtra("MENTORIA_ID") ?: ""



        val btnVolver: Button = findViewById(R.id.btnVolver)
        btnVolver.setOnClickListener { finish() }

        etContenido = findViewById(R.id.etContenidoExtra)
        btnGuardarContenido = findViewById(R.id.btnGuardarContenido)
        textotit = findViewById(R.id.titulocont)

        cargarContenidosDeMentoria()

        val userEmail = FirebaseAuth.getInstance().currentUser?.email
        if (userEmail != null) {
            verificarSiEsMentor(userEmail)
        }

        btnGuardarContenido.setOnClickListener {
            val contenidoTexto = etContenido.text.toString().trim()
            if (contenidoTexto.isEmpty()) {
                Toast.makeText(this, "El contenido no puede estar vacío", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            guardarContenido(contenidoTexto)
            etContenido.text.clear()
        }
    }

    private fun verificarSiEsMentor(email: String) {
        //Toast.makeText(this, "Email recibido: $email", Toast.LENGTH_SHORT).show()

        db.collection("users")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { documentos ->
                if (documentos.isEmpty) {
                    Toast.makeText(this, "Usuario no encontrado con ese email", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                val documento = documentos.firstOrNull()
                val rol = documento?.getString("role")?.trim()
               // Toast.makeText(this, "Rol encontrado: $rol", Toast.LENGTH_SHORT).show()

                if (rol != null) {
                    val puedeAgregarContenido = (rol == "Mentor" || rol == "Admin")


                    if (puedeAgregarContenido) {
                        etContenido.visibility = View.VISIBLE
                        btnGuardarContenido.visibility = View.VISIBLE
                        textotit.visibility = View.VISIBLE
                    } else {
                        etContenido.visibility = View.GONE
                        btnGuardarContenido.visibility = View.GONE
                        textotit.visibility = View.GONE
                    }
                } else {
                    Toast.makeText(this, "Rol no encontrado en el documento", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al verificar el rol del usuario", Toast.LENGTH_SHORT).show()
            }
    }



    private fun cargarContenidosDeMentoria() {
        db.collection("mentorias")
            .document(mentoriaId)
            .get()
            .addOnSuccessListener { document ->
                contenidoList.clear()
                if (document.exists()) {
                    val contenidos = document.get("contenidos") as? List<Map<String, Any>> ?: emptyList()
                    for (item in contenidos) {
                        val texto = item["texto"] as? String ?: ""
                        val fecha = item["fecha"] as? Timestamp
                        contenidoList.add(ContenidoMentoria(texto, fecha))
                    }
                    contenidoAdapter = ContenidoAdapter(contenidoList)
                    recyclerView.adapter = contenidoAdapter
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al cargar el contenido", Toast.LENGTH_SHORT).show()
            }
    }

    private fun guardarContenido(contenidoTexto: String) {
        val nuevoContenido = mapOf(
            "texto" to contenidoTexto,
            "fecha" to Timestamp.now()
        )

        db.collection("mentorias")
            .document(mentoriaId)
            .update("contenidos", FieldValue.arrayUnion(nuevoContenido))
            .addOnSuccessListener {
                Toast.makeText(this, "Contenido guardado exitosamente", Toast.LENGTH_SHORT).show()
                cargarContenidosDeMentoria()
            }
            .addOnFailureListener {
                db.collection("mentorias")
                    .document(mentoriaId)
                    .set(mapOf("contenidos" to listOf(nuevoContenido)))
                    .addOnSuccessListener {
                        Toast.makeText(this, "Contenido inicial agregado", Toast.LENGTH_SHORT).show()
                        cargarContenidosDeMentoria()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Error al guardar el contenido", Toast.LENGTH_SHORT).show()
                    }
            }
    }

    data class ContenidoMentoria(
        val texto: String = "",
        val fecha: Timestamp? = null
    )

    class ContenidoAdapter(private val contenidoList: List<ContenidoMentoria>) :
        RecyclerView.Adapter<ContenidoAdapter.ContenidoViewHolder>() {

        class ContenidoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvContenido: TextView = view.findViewById(R.id.tvContenido)
            val tvFecha: TextView = view.findViewById(R.id.tvFecha)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContenidoViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_contenido_mentoria, parent, false)
            return ContenidoViewHolder(view)
        }

        @RequiresApi(Build.VERSION_CODES.N)
        override fun onBindViewHolder(holder: ContenidoViewHolder, position: Int) {
            val contenido = contenidoList[position]
            holder.tvContenido.text = contenido.texto

            val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            formatter.timeZone = TimeZone.getTimeZone("America/Bogota")
            val fechaFormateada = contenido.fecha?.toDate()?.let {
                formatter.format(it)
            } ?: "Fecha no disponible"

            holder.tvFecha.text = fechaFormateada
        }

        override fun getItemCount() = contenidoList.size
    }
}
