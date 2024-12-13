package com.dicoding.heartalert2

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dicoding.heartalert2.adapter.HistoryAdapter
import kotlinx.coroutines.launch

class AllHistoryFragment : Fragment(R.layout.fragment_all_history) {

    private lateinit var sharedPreferencesHelper: SharedPreferencesHelper
    private lateinit var historyAdapter: HistoryAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var appDataStore: AppDataStore

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        appDataStore = AppDataStore(requireContext())
        sharedPreferencesHelper = SharedPreferencesHelper(requireContext())

        // Setup RecyclerView
        recyclerView = view.findViewById(R.id.allHistoryRv)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        historyAdapter = HistoryAdapter(emptyList()) { date, result ->
            Toast.makeText(context, "Clicked: $date - $result", Toast.LENGTH_SHORT).show()
        }
        recyclerView.adapter = historyAdapter

        // Load history from AppDataStore
        loadHistory()
    }

    private fun loadHistory() {
        // Collect history data using Flow from AppDataStore
        lifecycleScope.launch {
            appDataStore.historyFlow.collect { historyList ->
                // Update RecyclerView with the retrieved history data
                historyAdapter.updateData(historyList)
            }
        }
    }
}