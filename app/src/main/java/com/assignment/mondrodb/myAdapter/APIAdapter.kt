package com.assignment.mondrodb.myAdapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.assignment.mondrodb.R
import com.assignment.mondrodb.myModel.APIModel

class APIAdapter (private val mList:ArrayList<APIModel>, val listener: MyClickListener): RecyclerView.Adapter<APIAdapter.ViewHolder>() {

    class ViewHolder (itemView: View): RecyclerView.ViewHolder(itemView) {
        val dbName: TextView = itemView.findViewById(R.id.tvDatabaseName)
    }

    interface MyClickListener{
        fun onClick(pDBName: String)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.api_item_model, parent, false)

        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val databaseList = mList[position]

        holder.dbName.text = databaseList.vDBName

        holder.itemView.setOnClickListener{
            listener.onClick(databaseList.vDBName)
        }
    }

}