package com.example.conectandoapp

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class JobOffersActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_job_offers)

        val btnVolver: Button = findViewById(R.id.btnVolver)
        btnVolver.setOnClickListener {
            finish() // Regresa a la pantalla anterior
        }
    }
}
