package com.dicoding.heartalert2.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dicoding.heartalert2.Hospital
import com.dicoding.heartalert2.R

class HospitalAdapter(private val hospitals: List<Hospital>) : RecyclerView.Adapter<HospitalAdapter.HospitalViewHolder>() {

    inner class HospitalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val hospitalName: TextView = itemView.findViewById(R.id.hospitalName)
        private val hospitalLocation: TextView = itemView.findViewById(R.id.hospitalLocation)
        private val hospitalDuration: TextView = itemView.findViewById(R.id.hospitalDuration)

        fun bind(hospital: Hospital) {
            hospitalName.text = hospital.name
            hospitalLocation.text = "Lat: ${hospital.location.lat}, Lng: ${hospital.location.lng}"
            hospitalDuration.text = hospital.duration
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HospitalViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_hospital, parent, false)
        return HospitalViewHolder(view)
    }

    override fun onBindViewHolder(holder: HospitalViewHolder, position: Int) {
        holder.bind(hospitals[position])
    }

    override fun getItemCount(): Int = hospitals.size
}