package com.assignment.mondrodb.mainApp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.toolbox.Volley
import com.assignment.mondrodb.R
import com.assignment.mondrodb.myAdapter.APIAdapter
import com.assignment.mondrodb.myModel.APIModel
import com.assignment.mondrodb.myModel.APIResponse
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class DatabaseActivity : DashboardSettings(), APIAdapter.MyClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_database)

        val clusterName = findViewById<TextView>(R.id.tvClusterName)
        clusterName.text = intent.getStringExtra("Cluster")

        requestQueue = Volley.newRequestQueue(this)
        vView = findViewById(R.id.rvDatabaseList)
        vList = ArrayList()

        vAdapter = APIAdapter(vList, this@DatabaseActivity)

        vView.adapter = vAdapter
        vView.setHasFixedSize(true)
        vView.layoutManager = LinearLayoutManager(this)

        getDatabaseList()

        btnHandler()
    }

    override fun onClick(pDBName: String) {
        try {
            select(pDBName)
        } catch (e: Exception) {
            Log.d("ClickError:", e.toString())
        }

        val intent = Intent(this, CollectionActivity::class.java)
        intent.putExtra("DBName", pDBName)
        startActivity(intent)
    }

    private fun btnHandler(){
        // Disconnect
        val disconnect : ImageView = findViewById(R.id.ivDisconnectBtn)
        disconnect.setOnClickListener {
            disconnect()
            val intent = Intent(this, ConnectionActivity::class.java)
            startActivity(intent)

            finish()
        }

        // Refresh
        val refresh : ImageView = findViewById(R.id.ivDBRefreshButton)
        refresh.setOnClickListener {
            vList.clear()
            vAdapter.notifyDataSetChanged() // Notify the adapter to reflect the changes

            // Trigger the API call again to fetch the updated database list
            getDatabaseList()
        }

        val databasename : EditText = findViewById(R.id.DatabaseName)

        // Create
        val create : ImageView = findViewById(R.id.tvCreateDatabaseBtn)
        create.setOnClickListener {
            try {
                val dbname = databasename.text.toString()
                create(dbname)
            } catch (e: Exception){
                Log.d("APIError", e.toString())
            }
        }

        // Remove
        val remove : ImageView = findViewById(R.id.tvRemoveDatabaseBtn)
        remove.setOnClickListener {
            val dbname = databasename.text.toString()
            remove(dbname)
        }

    }

    private fun getDatabaseList(){
        coroutineScope.launch {
            val apiUrl = getString(R.string.getDatabaseList)

            try {
                val response = withContext(Dispatchers.IO) {
                    makeApiCall(apiUrl)
                }

                Log.d("APIError", response)

                // Assuming the response is an array of strings representing database names
                val gson = Gson()
                val databaseNamesArray = gson.fromJson(response, APIResponse::class.java)

                // Clear the existing list
                vList.clear()

                // Add each database name to the list as DatabaseModel objects
                databaseNamesArray.list.forEach { dbName ->
                    val databaseModel = APIModel(dbName)
                    vList.add(databaseModel)
                }

                // Notify the adapter of the data change
                vAdapter.notifyDataSetChanged()

            } catch (e: Exception) {
                Toast.makeText(this@DatabaseActivity, "Error: $e", Toast.LENGTH_SHORT).show()
                Log.d("APIError", "Error: $e")
            }
        }
    }

    private fun create(pDBName: String){
        coroutineScope.launch {
            val apiUrl = "https://mongo-db-api-coral.vercel.app/createDatabase"

            val jsonObject = JSONObject()
            jsonObject.put("vDBName", pDBName)
            Log.d("APIError", jsonObject.toString())

            try {
                val response = withContext(Dispatchers.IO) {
                    makeApiCallWithContext(apiUrl, jsonObject)
                }

                // Handle success response
                if (response.contains("Database Created!")) {
                    Toast.makeText(this@DatabaseActivity, "Created!!Refresh to see result", Toast.LENGTH_SHORT).show()
                } else {
                    // Handle unexpected response
                    Log.d("APIError", "Unexpected response: $response")
                }
            } catch (e: Exception) {
                // Handle error
                Toast.makeText(this@DatabaseActivity, "Error: Check your Connection or Role Permission", Toast.LENGTH_SHORT).show()
                Log.d("APIError", "Error: $e")
            }
        }
    }

    private fun remove(pDBName: String){
        coroutineScope.launch {
            val apiUrl = "https://mongo-db-api-coral.vercel.app/removeDatabase"

            val jsonObject = JSONObject()
            jsonObject.put("vDBName", pDBName)
            Log.d("APIError", jsonObject.toString())

            try {
                val response = withContext(Dispatchers.IO) {
                    makeApiCallWithContext(apiUrl, jsonObject)
                }

                // Handle success response
                if (response.contains("Database Removed!")) {
                    Toast.makeText(this@DatabaseActivity, "Removed!!Refresh to see result", Toast.LENGTH_SHORT).show()
                } else {
                    // Handle unexpected response
                    Log.d("APIError", "Unexpected response: $response")
                }
            } catch (e: Exception) {
                // Handle error
                Toast.makeText(this@DatabaseActivity, "Error: Check your Connection or Role Permission", Toast.LENGTH_SHORT).show()
                Log.d("APIError", "Error: $e")
            }
        }
    }

    private fun select(pDBName: String){
        coroutineScope.launch {
            val apiUrl = "https://mongo-db-api-coral.vercel.app/selectDatabase"

            val jsonObject = JSONObject()
            jsonObject.put("vDBName", pDBName)
            Log.d("APIError", jsonObject.toString())

            try {
                val response = withContext(Dispatchers.IO) {
                    makeApiCallWithContext(apiUrl, jsonObject)
                }

                // Handle success response
                if (response.contains("Database Selected!")) {
                    Toast.makeText(this@DatabaseActivity, "Database $pDBName has been selected!", Toast.LENGTH_SHORT).show()
                } else {
                    // Handle unexpected response
                    Log.d("APIError", "Unexpected response: $response")
                }
            } catch (e: Exception) {
                // Handle error
                Toast.makeText(this@DatabaseActivity, "Error: Check your Connection!", Toast.LENGTH_SHORT).show()
                Log.d("APIError", "Error: $e")
            }
        }
    }

}