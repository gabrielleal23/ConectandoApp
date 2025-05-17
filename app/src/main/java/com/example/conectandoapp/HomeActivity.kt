package com.example.conectandoapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.TextView
import android.widget.EditText
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import android.graphics.Color
import android.widget.ArrayAdapter
import android.widget.RatingBar
import android.widget.Spinner


class SupportActivity : AppCompatActivity() {

    private lateinit var etMensajeSoporte: EditText
    private lateinit var btnEnviarSoporte: Button
    private lateinit var btnVolver: Button
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_support)

        etMensajeSoporte = findViewById(R.id.etMensajeSoporte)
        btnEnviarSoporte = findViewById(R.id.btnEnviarSoporte)
        btnVolver = findViewById(R.id.btnVolver)

        btnEnviarSoporte.setOnClickListener { enviarMensajeSoporte() }
        btnVolver.setOnClickListener { finish() }
    }

    private fun enviarMensajeSoporte() {
        val mensaje = etMensajeSoporte.text.toString().trim()
        val user = auth.currentUser

        if (mensaje.isEmpty()) {
            showToast("Por favor, escribe un mensaje")
            return
        }

        if (user == null || user.email.isNullOrEmpty()) {
            showToast("Error: Usuario no autenticado")
            return
        }

        val supportMessage = hashMapOf(
            "email" to user.email!!,
            "mensaje" to mensaje,
            "fechahora" to FieldValue.serverTimestamp()
        )

        db.collection("supportMessages")
            .add(supportMessage)
            .addOnSuccessListener {
                showToast("Mensaje enviado a soporte")
                etMensajeSoporte.text.clear()
            }
            .addOnFailureListener { e ->
                showToast("Error al enviar mensaje: ${e.message}")
            }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}


class HistoryActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: HistoryAdapter
    private val listaHistorial = mutableListOf<Oferta>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        recyclerView = findViewById(R.id.recyclerHistory)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = HistoryAdapter(listaHistorial)
        recyclerView.adapter = adapter

        cargarHistorialDesdeFirestore()

        val btnVolver: Button = findViewById(R.id.btnVolver)
        btnVolver.setOnClickListener {
            finish() // Regresa a la pantalla anterior
        }
    }



    private fun cargarHistorialDesdeFirestore() {
        val db = FirebaseFirestore.getInstance()
        val user = FirebaseAuth.getInstance().currentUser
        val correo = user?.email

        if (correo != null) {
            listaHistorial.clear()

            db.collection("ofertas")
                .whereEqualTo("trabajador.correo", correo)
                .get()
                .addOnSuccessListener { documentos ->
                    for (document in documentos) {
                        val estado = document.getString("estado")
                        if (estado == "Aceptada" || estado == "Finalizada" || estado == "Pagada") {
                            val oferta = document.toObject(Oferta::class.java).copy(id = document.id)
                            listaHistorial.add(oferta)
                        }
                    }
                    adapter.notifyDataSetChanged()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al cargar historial", Toast.LENGTH_SHORT).show()
                }
        }
    }


}

data class JobApplication(
    val id: String = "",
    val titulo: String = "",
    val empresa: String = "",
    val estado: String = "", // "aceptada", "pendiente", etc.
    val finalizada: Boolean = false,
    val calificacion: Float? = null  // Nuevo campo para la calificación
)

class HistoryAdapter(private val listaOfertasStudent: List<Oferta>) :
    RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvPuesto: TextView = view.findViewById(R.id.tvPuesto)
        val tvDescripcion: TextView = view.findViewById(R.id.tvDescripcion)
        val tvUbicacion: TextView = view.findViewById(R.id.tvUbicacion)
        val tvHorario: TextView = view.findViewById(R.id.tvHorario)
        val tvPagoHora: TextView = view.findViewById(R.id.tvPagoHora)
        val tvEstadohis : TextView = view.findViewById(R.id.tvEstadohis)
        val calificacionoffer : TextView = view.findViewById(R.id.calificacionoffer)
        val ratingOfertaItem: RatingBar = view.findViewById(R.id.ratingOferta)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_historial_oferta_student, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val oferta = listaOfertasStudent[position]
        holder.tvPuesto.text = oferta.puesto.uppercase()
        holder.tvDescripcion.text = "📌 ${oferta.descripcion}"
        holder.tvUbicacion.text = "📍 ${oferta.ubicacion}"
        holder.tvHorario.text = "⏰ ${oferta.horario}"
        holder.tvPagoHora.text = "💰 Pago Por Hora: $${oferta.pagoHora}"
        holder.calificacionoffer.text = "Calificacion para el Usuario:"
        holder.tvEstadohis.text = when (oferta.estado) {
            "Pendiente" -> "Estado: ⏳ Pendiente"
            "Aceptada" -> "Estado: ✅ Aceptada"
            "Finalizada" -> " Estado:🏁 Finalizada"
            "Rechazada" -> " Estado:❌ Rechazada"
            "En progreso" -> " Estado:🔄 En progreso"
            "Pagada" -> " Estado: \uD83D\uDCB8 Pagada"
            else -> oferta.estado
        }

        // Mostrar rating solo si el estado es Pagada o Finalizada
        if (oferta.estado == "Pagada" || oferta.estado == "Finalizada") {
            holder.ratingOfertaItem.visibility = View.VISIBLE
            holder.calificacionoffer.visibility = View.VISIBLE
            // Si tienes la calificación guardada en oferta, ponla aquí, por ejemplo:
            holder.ratingOfertaItem.rating = oferta.calificacion ?: 0f
            // Permitir que usuario cambie la calificación, si quieres:
            holder.ratingOfertaItem.setOnRatingBarChangeListener { _, rating, fromUser ->
                if (fromUser) {
                    // Guardar la calificación en Firestore o localmente
                    Toast.makeText(holder.itemView.context, "Calificación: $rating", Toast.LENGTH_SHORT).show()
                    // Implementa la lógica para actualizar la calificación en Firestore aquí
                }
            }
        } else {
            holder.ratingOfertaItem.visibility = View.GONE
            holder.calificacionoffer.visibility = View.GONE
        }
    }

    override fun getItemCount() = listaOfertasStudent.size
}

class PaymentActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var totalPagosTextView: TextView
    private val offers = mutableListOf<JobOffer>()
    private lateinit var adapter: PaymentAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)

        recyclerView = findViewById(R.id.recyclerPagos)
        totalPagosTextView = findViewById(R.id.tvTotalGeneral)

        adapter = PaymentAdapter(offers)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        obtenerOfertasPagadas()

        val btnVolver: Button = findViewById(R.id.btnVolver)
        btnVolver.setOnClickListener {
            finish() // Regresa a la pantalla anterior
        }
    }

    private fun obtenerOfertasPagadas() {
        val db = FirebaseFirestore.getInstance()
        val user = FirebaseAuth.getInstance().currentUser
        val correo = user?.email

        db.collection("ofertas")
            .get()
            .addOnSuccessListener { result ->
                offers.clear()
                var totalGeneral = 0.0

                for (document in result) {
                    try {
                        val puesto = document.getString("puesto") ?: ""
                        val horas = document.getLong("horas")?.toInt() ?: 0
                        val pagoHora = document.getDouble("pagoHora") ?: 0.0
                        val estado = document.getString("estado") ?: ""
                        val trabajadorMap = document.get("trabajador") as? Map<*, *>

                        val trabajador = if (trabajadorMap != null) {
                            Trabajador3(
                                nombre = trabajadorMap["nombre"] as? String ?: "",
                                correo = trabajadorMap["correo"] as? String ?: "",
                                telefono = trabajadorMap["telefono"] as? String ?: ""
                            )
                        } else Trabajador3()

                        if (estado == "Pagada" && trabajador.correo == correo) {
                            val offer = JobOffer(
                                id = document.id,
                                puesto = puesto,
                                horas = horas,
                                pagoHora = pagoHora,
                                estado = estado,
                                trabajador = trabajador
                            )
                            offers.add(offer)
                            totalGeneral += horas * pagoHora
                        }

                    } catch (e: Exception) {
                        Log.e("PaymentActivity", "❌ Error al leer oferta", e)
                    }
                }

                totalPagosTextView.text = "Total General: $${String.format("%.2f", totalGeneral)}"
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(this@PaymentActivity, "Error al obtener los pagos", Toast.LENGTH_SHORT).show()
            }
    }

    data class JobOffer(
        val id: String = "",
        val puesto: String = "",
        val horas: Int = 0,
        val pagoHora: Double = 0.0,
        val estado: String = "",
        val trabajador: Trabajador3 = Trabajador3()
    )

    data class Trabajador3(
        val nombre: String = "",
        val correo: String = "",
        val telefono: String = ""
    )

    class PaymentAdapter(private val offers: List<JobOffer>) : RecyclerView.Adapter<PaymentAdapter.PaymentViewHolder>() {

        class PaymentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvTitulo: TextView = view.findViewById(R.id.tvTitulo)
            val tvTotalPago: TextView = view.findViewById(R.id.tvTotalPago)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_payment_student, parent, false)
            return PaymentViewHolder(view)
        }

        override fun onBindViewHolder(holder: PaymentViewHolder, position: Int) {
            val offer = offers[position]
            val total = offer.horas * offer.pagoHora
            holder.tvTitulo.text = offer.puesto
            holder.tvTotalPago.text = "Total: $${String.format("%.2f", total)}"
        }

        override fun getItemCount() = offers.size
    }
}

class ProfileActivity : AppCompatActivity() {

    private lateinit var etNombre: EditText
    private lateinit var etCedula: EditText
    private lateinit var etTelefono: EditText
    private lateinit var etCorreo: EditText
    private lateinit var spinnerRole: Spinner
    private lateinit var btnGuardar: Button

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private var isAdminEditing = false
    private var userIdToEdit: String? = null

    private lateinit var roles: Array<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        etNombre = findViewById(R.id.etNombre)
        etCedula = findViewById(R.id.etCedula)
        etTelefono = findViewById(R.id.etTelefono)
        etCorreo = findViewById(R.id.etCorreo)
        spinnerRole = findViewById(R.id.spinnerRoledit)
        btnGuardar = findViewById(R.id.btnGuardar)

        userIdToEdit = intent.getStringExtra("userId")

        roles = arrayOf("Seleccione un rol", "Docente", "Estudiante", "Mentor", "Empleador")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, roles)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerRole.adapter = adapter

        obtenerDatosUsuario()

        btnGuardar.setOnClickListener {
            guardarCambios()
        }

        findViewById<Button>(R.id.btnVolver).setOnClickListener {
            finish()
        }
    }

    private fun obtenerDatosUsuario() {
        val currentUserId = auth.currentUser?.uid ?: return
        val userId = userIdToEdit ?: currentUserId

        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    etNombre.setText(document.getString("nombre") ?: "")
                    etCedula.setText(document.getString("cedula") ?: "")
                    etTelefono.setText(document.getString("telefono") ?: "")
                    etCorreo.setText(document.getString("email") ?: "")

                    val role = document.getString("role") ?: "Seleccione un rol"
                    val rolePosition = roles.indexOfFirst { it.equals(role, ignoreCase = true) }
                    if (rolePosition >= 0) {
                        spinnerRole.setSelection(rolePosition)
                    } else {
                        spinnerRole.setSelection(0)
                    }

                    // Activar campos si el admin está editando otro usuario
                    if (userIdToEdit != null && currentUserId != userId) {
                        isAdminEditing = true
                        etCorreo.isEnabled = true
                        spinnerRole.isEnabled = true
                        btnGuardar.text = "Actualizar Usuario"
                    } else {
                        etCorreo.isEnabled = false
                        spinnerRole.isEnabled = false
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al cargar perfil", Toast.LENGTH_SHORT).show()
            }
    }

    private fun guardarCambios() {
        val userId = userIdToEdit ?: auth.currentUser?.uid ?: return

        val nuevoNombre = etNombre.text.toString().trim()
        val nuevaCedula = etCedula.text.toString().trim()
        val nuevoTelefono = etTelefono.text.toString().trim()

        if (nuevoNombre.isEmpty() || nuevaCedula.isEmpty() || nuevoTelefono.isEmpty()) {
            Toast.makeText(this, "Todos los campos deben estar llenos", Toast.LENGTH_SHORT).show()
            return
        }

        val datosActualizados = mutableMapOf<String, Any>(
            "nombre" to nuevoNombre,
            "cedula" to nuevaCedula,
            "telefono" to nuevoTelefono
        )

        if (isAdminEditing) {
            val nuevoRol = spinnerRole.selectedItem.toString()

            if (nuevoRol != "Seleccione un rol") {
                datosActualizados["role"] = nuevoRol
            }
        }

        db.collection("users").document(userId)
            .update(datosActualizados)
            .addOnSuccessListener {
                Toast.makeText(this, "Perfil actualizado correctamente", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al actualizar: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}

class JobsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_postulate_student)
    }
}

class MentorsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MentoriaAdapterEstudiante
    private val listaMentoriasdispo = mutableListOf<Mentorias>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mentors_estudiante)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = MentoriaAdapterEstudiante(listaMentoriasdispo)
        recyclerView.adapter = adapter

        cargarMentoriasEstudiantesDesdeFirestore()

        val btnVolver: Button = findViewById(R.id.btnVolver)
        btnVolver.setOnClickListener {
            finish()
        }
    }

    data class Mentorias(
        val id: String = "",
        val tema: String = "",
        val descripcion: String = "",
        val estado: String = "",
        val estudiantesInscritos: Int = 0,
        val inscritos: List<String> = emptyList()
    )

    private fun cargarMentoriasEstudiantesDesdeFirestore() {
        val db = FirebaseFirestore.getInstance()

        db.collection("mentorias")
            .whereIn("estado", listOf("Disponible", "Finalizado"))
            .addSnapshotListener { documentos, e ->
                if (e != null) {
                    Toast.makeText(this, "Error en tiempo real: ${e.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                listaMentoriasdispo.clear()
                if (documentos != null) {
                    for (document in documentos) {
                        val id = document.id
                        val tema = document.getString("tema") ?: ""
                        val descripcion = document.getString("descripcion") ?: ""
                        val estado = document.getString("estado") ?: ""
                        val estudiantesInscritos = document.getLong("estudiantesInscritos")?.toInt() ?: 0
                        val inscritos = document.get("inscritos") as? List<String> ?: emptyList()

                        val mentoria = Mentorias(
                            id = id,
                            tema = tema,
                            descripcion = descripcion,
                            estado = estado,
                            estudiantesInscritos = estudiantesInscritos,
                            inscritos = inscritos
                        )
                        listaMentoriasdispo.add(mentoria)
                    }
                    adapter.notifyDataSetChanged()
                }
            }
    }

    class MentoriaAdapterEstudiante(private val mentorias: List<Mentorias>) :
        RecyclerView.Adapter<MentoriaAdapterEstudiante.MentoriaViewHolder>() {

        class MentoriaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tema: TextView = view.findViewById(R.id.tvTemaMentoria)
            val descripcion: TextView = view.findViewById(R.id.tvDescripcionMentoria)
            val estado: TextView = view.findViewById(R.id.tvEstadoInscripcion)
            val botonInscribirse: Button = view.findViewById(R.id.btnInscribirse)
            val botonVerInfo: Button = view.findViewById(R.id.btnVerInformacion)
            val layoutCalificacion: LinearLayout = view.findViewById(R.id.layoutCalificacion)

        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MentoriaViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_mentoria_estudiante, parent, false)
            return MentoriaViewHolder(view)
        }

        override fun onBindViewHolder(holder: MentoriaViewHolder, position: Int) {
            val mentoria = mentorias[position]

            holder.tema.text = mentoria.tema
            holder.descripcion.text = mentoria.descripcion

            val auth = FirebaseAuth.getInstance()
            val userEmail = auth.currentUser?.email

            if (userEmail == null) {
                holder.estado.text = "No autenticado"
                holder.estado.setTextColor(Color.parseColor("#FF5722"))
                holder.botonInscribirse.isEnabled = false
                holder.botonVerInfo.visibility = View.GONE
                holder.layoutCalificacion.visibility = View.GONE
                return
            }

            val yaInscrito = mentoria.inscritos.contains(userEmail)

            if (yaInscrito) {
                holder.estado.text = "Ya inscrito ✅"
                holder.estado.setTextColor(Color.parseColor("#4CAF50"))
                holder.botonInscribirse.visibility = View.GONE
                holder.botonVerInfo.visibility = View.VISIBLE
                holder.layoutCalificacion.visibility = View.VISIBLE
                holder.botonVerInfo.setOnClickListener {
                    val context = holder.itemView.context
                    val intent = Intent(context, VerContenidosMentoriaActivity::class.java)
                    intent.putExtra("MENTORIA_ID", mentoria.id)
                    context.startActivity(intent)
                }

            } else {
                holder.estado.text = "No inscrito ❌"
                holder.estado.setTextColor(Color.parseColor("#FF5722"))
                holder.botonInscribirse.visibility = View.VISIBLE
                holder.botonVerInfo.visibility = View.GONE
                holder.layoutCalificacion.visibility = View.GONE
            }

            holder.botonInscribirse.setOnClickListener {
                val db = FirebaseFirestore.getInstance()
                val mentoriaId = mentoria.id
                val mentoriaRef = db.collection("mentorias").document(mentoriaId)

                db.runTransaction { transaction ->
                    val snapshot = transaction.get(mentoriaRef)
                    val listaInscritos = snapshot.get("inscritos") as? List<String> ?: emptyList()
                    val estudiantesActuales = snapshot.getLong("estudiantesInscritos") ?: 0

                    if (listaInscritos.contains(userEmail)) {
                        throw Exception("Ya estás inscrito en esta mentoría.")
                    }

                    val nuevaLista = listaInscritos + userEmail
                    transaction.update(mentoriaRef, "inscritos", nuevaLista)
                    transaction.update(mentoriaRef, "estudiantesInscritos", estudiantesActuales + 1)
                }.addOnSuccessListener {
                    Toast.makeText(holder.itemView.context, "Inscripción exitosa", Toast.LENGTH_SHORT).show()
                    holder.estado.text = "Ya inscrito ✅"
                    holder.estado.setTextColor(Color.parseColor("#4CAF50"))
                    holder.botonInscribirse.visibility = View.GONE
                    holder.botonVerInfo.visibility = View.VISIBLE
                    holder.layoutCalificacion.visibility = View.VISIBLE


                }.addOnFailureListener { e ->
                    Toast.makeText(holder.itemView.context, "Error al inscribirse: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        override fun getItemCount(): Int = mentorias.size
    }
}

