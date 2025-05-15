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

class UserManagementActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var userAdapter: UserAdapter
    private val userList = mutableListOf<User>()
    private lateinit var btnAgregarUsuario: Button
    private lateinit var btnVolver2: Button

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_management)

        recyclerView = findViewById(R.id.recyclerViewUsers)
        recyclerView.layoutManager = LinearLayoutManager(this)
        btnVolver2 = findViewById(R.id.btnVolver2)
        btnAgregarUsuario = findViewById(R.id.btnAgregarUsuario) // <--- Nuevo

        userAdapter = UserAdapter(userList,
            onEditClick = { user ->
                val intent = Intent(this, ProfileActivity::class.java)
                intent.putExtra("userId", user.userId)
                startActivity(intent)
            },
            onDeleteClick = { user ->
                deleteUser(user)
            }
        )
        recyclerView.adapter = userAdapter

        fetchUsersInRealtime()

        btnAgregarUsuario.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            intent.putExtra("modo_admin", true)
            startActivity(intent)
        }

        btnVolver2.setOnClickListener {
            finish()
        }
    }

    private fun fetchUsersInRealtime() {
        db.collection("users")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Toast.makeText(this, "Error al cargar usuarios: ${e.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                userList.clear()
                snapshot?.documents?.forEach { document ->
                    val user = document.toObject(User::class.java)
                    user?.userId = document.id
                    userList.add(user!!)
                }
                userAdapter.notifyDataSetChanged() // Notificar al adaptador que los datos han cambiado
            }
    }

    private fun deleteUser(user: User) {
        db.collection("users").document(user.userId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Usuario eliminado", Toast.LENGTH_SHORT).show()
                userList.remove(user)
                userAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al eliminar usuario", Toast.LENGTH_SHORT).show()
            }
    }

    data class User(
        var userId: String = "",
        var nombre: String = "",
        var email: String = ""
    )

    class UserAdapter(
        private val userList: List<User>,
        private val onEditClick: (User) -> Unit,
        private val onDeleteClick: (User) -> Unit
    ) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

        inner class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvName: TextView = view.findViewById(R.id.tvUserName)
            val tvEmail: TextView = view.findViewById(R.id.tvUserEmail)
            val btnEdit: Button = view.findViewById(R.id.btnEditUser)
            val btnDelete: Button = view.findViewById(R.id.btnDeleteUser)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_user_admin, parent, false)
            return UserViewHolder(view)
        }

        override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
            val user = userList[position]
            holder.tvName.text = user.nombre
            holder.tvEmail.text = user.email

            holder.btnEdit.setOnClickListener { onEditClick(user) }
            holder.btnDelete.setOnClickListener { onDeleteClick(user) }
        }

        override fun getItemCount(): Int = userList.size
    }
}
