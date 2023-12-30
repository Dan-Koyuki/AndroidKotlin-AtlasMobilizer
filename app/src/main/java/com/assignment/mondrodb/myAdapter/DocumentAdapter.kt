package com.assignment.mondrodb.myAdapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.assignment.mondrodb.R
import com.assignment.mondrodb.myModel.DocumentDetails

class DocumentAdapter (private val document: Map<String, String>): RecyclerView.Adapter<DocumentAdapter.ViewHolder>() {
    class ViewHolder (itemView: View): RecyclerView.ViewHolder(itemView) {
        val keyField: TextView = itemView.findViewById(R.id.tvDocumentKey)
        val valueField: TextView = itemView.findViewById(R.id.tvDocumentValue)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.api_details_model, parent, false)

        return ViewHolder(view);
    }

    override fun getItemCount(): Int {
        return document.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val keys = document.keys.toList()
        val values = document.values.toList()

        // Use keys and values to populate your ViewHolder views
        holder.keyField.text = keys.getOrNull(position) ?: ""
        holder.valueField.text = values.getOrNull(position) ?: ""
    }


}