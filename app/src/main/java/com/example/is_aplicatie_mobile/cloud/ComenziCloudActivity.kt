package com.example.is_aplicatie_mobila

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ComenziCloudActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ComenziAdapter
    private lateinit var listaComenzi: MutableList<Comanda>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comenzi_cloud)

        recyclerView = findViewById(R.id.recyclerViewComenzi)
        val btnStartTransport: Button = findViewById(R.id.btnStartTransport)
        val btnBack: Button = findViewById(R.id.btnBack)

        recyclerView.layoutManager = LinearLayoutManager(this)
        
        listaComenzi = mutableListOf(
            Comanda("Popescu Ion", "Paracetamol", "în așteptare"),
            Comanda("Ionescu Maria", "Amoxicilină", "livrat"),
            Comanda("Vasile Andrei", "Ibuprofen", "activ")
        )

        adapter = ComenziAdapter(listaComenzi)
        recyclerView.adapter = adapter

        btnStartTransport.setOnClickListener {
            Toast.makeText(this, "Comandă trimisă către robot!", Toast.LENGTH_SHORT).show()
        }

        btnBack.setOnClickListener {
            finish()
        }
    }
}