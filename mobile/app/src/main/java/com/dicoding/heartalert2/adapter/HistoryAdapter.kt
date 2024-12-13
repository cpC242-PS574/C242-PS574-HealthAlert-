package com.dicoding.heartalert2.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.dicoding.heartalert2.R
import androidx.recyclerview.widget.RecyclerView

class HistoryAdapter(
    private var historyList: List<String>,
    private val onItemClick: (String, String) -> Unit
) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    inner class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dateTextView: TextView = itemView.findViewById(R.id.tvDate)
        private val resultTextView: TextView = itemView.findViewById(R.id.tvResult)

        fun bind(historyItem: String) {
            val (date, result) = historyItem.split(", ")
            dateTextView.text = date
            resultTextView.text = result

            // Handle click events inside the adapter
            itemView.setOnClickListener {
                onItemClick(date, result)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history, parent, false)
        return HistoryViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val historyItem = historyList[position]
        holder.bind(historyItem)
    }

    override fun getItemCount(): Int = historyList.size

    fun updateData(newHistoryList: List<String>) {
        historyList = newHistoryList
        notifyDataSetChanged()
    }
}