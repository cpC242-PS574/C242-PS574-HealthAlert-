package com.dicoding.heartalert2.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dicoding.heartalert2.R

data class BPMRecord(val bpm: Int, val timestamp: String)

class BPMHistoryAdapter(private val dataset: List<BPMRecord>) :
    RecyclerView.Adapter<BPMHistoryAdapter.BPMViewHolder>() {

    class BPMViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val bpmTextView: TextView = view.findViewById(R.id.bpm_text)
        val timestampTextView: TextView = view.findViewById(R.id.timestamp_text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BPMViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.bpm_record_item, parent, false) as View
        return BPMViewHolder(view)
    }

    override fun onBindViewHolder(holder: BPMViewHolder, position: Int) {
        val item = dataset[position]
        holder.bpmTextView.text = "BPM: ${item.bpm}"
        holder.timestampTextView.text = item.timestamp
    }

    override fun getItemCount() = dataset.size
}