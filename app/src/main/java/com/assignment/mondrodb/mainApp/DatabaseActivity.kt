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
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class DatabaseActivity : DashboardSettings(), APIAdapter.MyClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_database)

        // set Cluster Name
        val clusterName = findViewById<TextView>(R.id.tvClusterName)
        clusterName.text = intent.getStringExtra("Cluster")

        // Initialization
        vAuth = FirebaseAuth.getInstance()

        requestQueue = Volley.newRequestQueue(this)
        vView = findViewById(R.id.rvDatabaseList)
        vList = ArrayList()

        vAdapter = APIAdapter(vList, this@DatabaseActivity)

        vView.adapter = vAdapter
        vView.setHasFixedSize(true)
        vView.layoutManager = LinearLayoutManager(this)

        // First Time retrieving Database List within Cluster
        getDatabaseList()

        // Initialize Button Behaviour
        btnHandler()
    }

    // onClick: Click Behaviour for Item in Recycler View (List)
    // params@... : Clicked Item Value
    // invoke@select()
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

    // btnHandler: Initialize button behaviour
    // initialize:
    //  Disconnect -> invoke@disconnect() (from Parent class)
    //  Refresh -> invoke@getDatabaseList()
    //  Create -> invoke@create()
    //  Remove -> invoke@remove()
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
        val refresh : ImageView = findViewById(R.id.databaseRefreshButton)
        refresh.setOnClickListener {
            vList.clear()
            vAdapter.notifyDataSetChanged() // Notify the adapter to reflect the changes

            // Trigger the API call again to fetch the updated database list
            getDatabaseList()
        }

        val databasename : EditText = findViewById(R.id.databaseNameInput)

        // Create
        val create : ImageView = findViewById(R.id.databaseCreateButton)
        create.setOnClickListener {
            try {
                val dbname = databasename.text.toString()
                create(dbname)
            } catch (e: Exception){
                Log.d("APIError", e.toString())
            }
        }

        // Remove
        val remove : ImageView = findViewById(R.id.databaseRemoveButton)
        remove.setOnClickListener {
            val dbname = databasename.text.toString()
            remove(dbname)
        }

        //help button
//        val help = findViewById<ImageView>(R.id.databaseHelpButton)
//        help.setOnClickListener {
//            Toast.makeText(this, "Coming Soon", Toast.LENGTH_SHORT).show()
//        }

    }

    // getDatabaseList: Retrieve Database list within a Cluster
    // APIEndpoints: db
    private fun getDatabaseList(){
        coroutineScope.launch {
            val apiUrl = "https://mongo-db-api-coral.vercel.app/db"
//            val apiUrl = "http://localhost:3000/db"

            val jsonObject = JSONObject()
            jsonObject.put("userId", getUserId())

            try {
                val response = withContext(Dispatchers.IO) {
                    makeApiCallWithContext(apiUrl, jsonObject)
                }

                Log.d("Database Retrieve Response", response)

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
                Toast.makeText(this@DatabaseActivity, "Unexpected Error: ${e.message}", Toast.LENGTH_SHORT).show()
                Toast.makeText(this@DatabaseActivity, "Refresh or Try Again after a moment!", Toast.LENGTH_SHORT).show()
                Log.d("Database Retrieve Error", "${e.message}")
            }
        }
    }

    // create: Create new Database
    // params@pDBName: New Database Name
    // APIEndpoint: db/create
    private fun create(pDBName: String){
        coroutineScope.launch {
            val apiUrl = "https://mongo-db-api-coral.vercel.app/db/create"

            val jsonObject = JSONObject()
            jsonObject.put("vDBName", pDBName)
            jsonObject.put("userId", getUserId())
            Log.d("Requested JSONObject DB Create", jsonObject.toString())

            try {
                val response = withContext(Dispatchers.IO) {
                    makeApiCallWithContext(apiUrl, jsonObject)
                }

                // Handle success response
                if (response.contains("Database Created!")) {
                    Toast.makeText(this@DatabaseActivity, "Created!!Refresh to see result", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@DatabaseActivity, "Unexpected Response,Try Again Later!", Toast.LENGTH_SHORT).show()
                    // Handle unexpected response
                    Log.d("Database Create Error", response)
                }
            } catch (e: Exception) {
                // Handle error
                Toast.makeText(this@DatabaseActivity, "Error: Check your Connection or Role Permission", Toast.LENGTH_SHORT).show()
                Log.d("Database Create Error", "{$e.message}")
            }
        }
    }

    // remove: Remove a Database
    // params@pDBName: Removed Database Name
    // APIEndpoint: db/remove
    private fun remove(pDBName: String){
        coroutineScope.launch {
            val apiUrl = "https://mongo-db-api-coral.vercel.app/db/remove"

            val jsonObject = JSONObject()
            jsonObject.put("vDBName", pDBName)
            jsonObject.put("userId", getUserId())
            Log.d("Requested JSONObject DB Remove", jsonObject.toString())

            try {
                val response = withContext(Dispatchers.IO) {
                    makeApiCallWithContext(apiUrl, jsonObject)
                }

                // Handle success response
                if (response.contains("Database Removed!")) {
                    Toast.makeText(this@DatabaseActivity, "Removed!!Refresh to see result", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@DatabaseActivity, "Unexpected Response,Try Again Later!", Toast.LENGTH_SHORT).show()
                    // Handle unexpected response
                    Log.d("Database Remove Error", "Unexpected response: $response")
                }
            } catch (e: Exception) {
                // Handle error
                Toast.makeText(this@DatabaseActivity, "Error: Check your Connection or Role Permission", Toast.LENGTH_SHORT).show()
                Log.d("Database Remove Error", "Error: ${e.message}")
            }
        }
    }


    // remove: Select a Database
    // params@pDBName: Selected Database Name
    // APIEndpoint: db/select
    private fun select(pDBName: String){
        coroutineScope.launch {
            val apiUrl = "https://mongo-db-api-coral.vercel.app/db/select"
//            val apiUrl = "http://localhost:3000/db/select"

            val jsonObject = JSONObject()
            jsonObject.put("vDBName", pDBName)
            jsonObject.put("userId", getUserId())
            Log.d("APIError", jsonObject.toString())

            try {
                val response = withContext(Dispatchers.IO) {
                    makeApiCallWithContext(apiUrl, jsonObject)
                }

                // Handle success response
                if (response.contains("Database Selected!")) {
                    Toast.makeText(this@DatabaseActivity, "Database $pDBName has been selected!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@DatabaseActivity, "Unexpected Response,Try Again Later!", Toast.LENGTH_SHORT).show()
                    // Handle unexpected response
                    Log.d("Selected Database Error", "Unexpected response: $response")
                }
            } catch (e: Exception) {
                // Handle error
                Toast.makeText(this@DatabaseActivity, "Error: Check your Connection!", Toast.LENGTH_SHORT).show()
                Log.d("Selected Database Error", "Error: ${e.message}")
            }
        }
    }

}