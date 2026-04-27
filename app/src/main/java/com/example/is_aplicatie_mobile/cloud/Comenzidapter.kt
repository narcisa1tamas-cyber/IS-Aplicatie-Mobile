package com.example.is_aplicatie_mobila

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ComenziAdapter(private val listaComenzi: List<Comanda>) :
    RecyclerView.Adapter<ComenziAdapter.ComandaViewHolder>() {

    class ComandaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvPacient: TextView = itemView.findViewById(R.id.tvPacient)
        val tvMedicament: TextView = itemView.findViewById(R.id.tvMedicament)
        val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ComandaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_comanda, parent, false)
        return ComandaViewHolder(view)
    }

    override fun onBindViewHolder(holder: ComandaViewHolder, position: Int) {
        val comanda = listaComenzi[position]
        holder.tvPacient.text = "Pacient: ${comanda.numePacient}"
        holder.tvMedicament.text = "Medicament: ${comanda.numeMedicament}"
        holder.tvStatus.text = "Status: ${comanda.status}"
    }

    override fun getItemCount(): Int = listaComenzi.size
}