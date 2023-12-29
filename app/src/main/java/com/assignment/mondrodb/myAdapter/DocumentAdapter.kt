package com.assignment.mondrodb.myAdapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.assignment.mondrodb.R
import com.assignment.mondrodb.myModel.DocumentDetails

class DocumentAdapter (private val mList: ArrayList<DocumentDetails>): RecyclerView.Adapter<DocumentAdapter.ViewHolder>() {
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
        return mList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val document = mList[position]

        val keys = document.vMap.keys.toList()
        val values = document.vMap.values.toList()

        // Assuming you want to display the first key-value pair in the TextViews
        holder.keyField.text = keys.firstOrNull()
        holder.valueField.text = values.firstOrNull().toString()
    }


}