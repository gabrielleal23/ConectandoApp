package com.example.conectandoapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class AdminActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_home)

        val btnUsuarios: LinearLayout = findViewById(R.id.btnUsuariosadmin)
        val btnPostulaciones: LinearLayout = findViewById(R.id.btnPostulaciones)
        val btnOfertas: LinearLayout = findViewById(R.id.btnOfertas)
        val btnCerrarSesion: Button = findViewById(R.id.btnCerrarSesion)
        val btnAdminProfile: LinearLayout = findViewById(R.id.btnAdminProfile)

        btnUsuarios.setOnClickListener {
            startActivity(Intent(this, UserManagementActivity::class.java))
        }

        btnPostulaciones.setOnClickListener {
            startActivity(Intent(this, AdminPostulacionesActivity::class.java))
        }

        btnOfertas.setOnClickListener {
            startActivity(Intent(this, MentorsAdminActivity::class.java))
        }

        btnAdminProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        btnCerrarSesion.setOnClickListener {
            Toast.makeText(this, "Sesion Cerrada", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}