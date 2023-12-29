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

class CollectionActivity : DashboardSettings(), APIAdapter.MyClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_collection)

        val databaseName = findViewById<TextView>(R.id.tvDatabaseName)
        databaseName.text = intent.getStringExtra("DBName")

        vAuth = FirebaseAuth.getInstance()

        requestQueue = Volley.newRequestQueue(this)
        vView = findViewById(R.id.rvCollectionList)
        vList = ArrayList()

        vAdapter = APIAdapter(vList, this@CollectionActivity)

        vView.adapter = vAdapter
        vView.setHasFixedSize(true)
        vView.layoutManager = LinearLayoutManager(this)

        getCollectionList()

        btnHandler()
    }

    private fun getUserId(): String {
        val currentUser = vAuth.currentUser
        return currentUser?.uid.toString()
    }

    override fun onClick(pDBName: String) {
        try {
            select(pDBName)
        } catch (e: Exception) {
            Log.d("ClickError:", e.toString())
        }

        val intent = Intent(this, DocumentActivity::class.java)
        intent.putExtra("CollectionName", pDBName)
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
        val refresh : ImageView = findViewById(R.id.ivCollectionRefreshButton)
        refresh.setOnClickListener {
            vList.clear()
            vAdapter.notifyDataSetChanged() // Notify the adapter to reflect the changes

            // Trigger the API call again to fetch the updated database list
            getCollectionList()
        }

        val collectionname : EditText = findViewById(R.id.CollectionName)

        // Create
        val create : ImageView = findViewById(R.id.tvCreateCollectionBtn)
        create.setOnClickListener {
            try {
                val collectionsname = collectionname.text.toString()
                create(collectionsname)
            } catch (e: Exception){
                Log.d("APIError", e.toString())
            }
        }

        // Remove
        val remove : ImageView = findViewById(R.id.tvRemoveCollectionBtn)
        remove.setOnClickListener {
            val collectionsname = collectionname.text.toString()
            remove(collectionsname)
        }

    }

    private fun getCollectionList(){
        coroutineScope.launch {
            val apiUrl = "https://mongo-db-api-coral.vercel.app/collection"
//            val apiUrl = "http://localhost:3000/collection"

            val jsonObject = JSONObject()
            jsonObject.put("userId", getUserId())

            try {
                val response = withContext(Dispatchers.IO) {
                    makeApiCallWithContext(apiUrl, jsonObject)
                }

                Log.d("APIError", response)

                if (response.isNotEmpty()) {
                    // Assuming the response is an array of strings representing database names
                    val gson = Gson()
                    val collectionNamesArray = gson.fromJson(response, APIResponse::class.java)
                    Log.d("APIError", collectionNamesArray.toString())

                    // Clear the existing list
                    vList.clear()

                    // Add each database name to the list as APIModel objects
                    collectionNamesArray.list.forEach { collectionName ->
                        val collectionsModel = APIModel(collectionName)
                        vList.add(collectionsModel)
                    }

                    // Notify the adapter of the data change
                    vAdapter.notifyDataSetChanged()
                } else {
                    Toast.makeText(this@CollectionActivity, "Failed, Please Check Your Connection or Database Permission!", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                Toast.makeText(this@CollectionActivity, "Error: $e", Toast.LENGTH_SHORT).show()
                Log.d("APIError", "Error: $e")
            }
        }
    }

    private fun create(pCollectionName: String){
        coroutineScope.launch {
            val apiUrl = "https://mongo-db-api-coral.vercel.app/collection/create"

            val jsonObject = JSONObject()
            jsonObject.put("vCollectionName", pCollectionName)
            jsonObject.put("userId", getUserId())
            Log.d("APIError", jsonObject.toString())

            try {
                val response = withContext(Dispatchers.IO) {
                    makeApiCallWithContext(apiUrl, jsonObject)
                }

                // Handle success response
                if (response.contains("Collection Created!")) {
                    Toast.makeText(this@CollectionActivity, "Created!!Refresh to see result", Toast.LENGTH_SHORT).show()
                } else {
                    // Handle unexpected response
                    Log.d("APIError", "Unexpected response: $response")
                }
            } catch (e: Exception) {
                // Handle error
                Toast.makeText(this@CollectionActivity, "Error: Check your Connection or Role Permission", Toast.LENGTH_SHORT).show()
                Log.d("APIError", "Error: $e")
            }
        }
    }

    private fun remove(pCollectionName: String){
        coroutineScope.launch {
            val apiUrl = "https://mongo-db-api-coral.vercel.app/collection/remove"

            val jsonObject = JSONObject()
            jsonObject.put("vCollectionName", pCollectionName)
            jsonObject.put("userId", getUserId())
            Log.d("APIError", jsonObject.toString())

            try {
                val response = withContext(Dispatchers.IO) {
                    makeApiCallWithContext(apiUrl, jsonObject)
                }

                // Handle success response
                if (response.contains("Collection Removed!")) {
                    Toast.makeText(this@CollectionActivity, "Removed!!Refresh to see result", Toast.LENGTH_SHORT).show()
                } else {
                    // Handle unexpected response
                    Log.d("APIError", "Unexpected response: $response")
                }
            } catch (e: Exception) {
                // Handle error
                Toast.makeText(this@CollectionActivity, "Error: Check your Connection or Role Permission", Toast.LENGTH_SHORT).show()
                Log.d("APIError", "Error: $e")
            }
        }
    }

    private fun select(pCollectionName: String){
        coroutineScope.launch {
            val apiUrl = "https://mongo-db-api-coral.vercel.app/collection/select"
//            val apiUrl = "http://localhost:3000/collection/select"

            val jsonObject = JSONObject()
            jsonObject.put("vCollectionName", pCollectionName)
            jsonObject.put("userId", getUserId())
            Log.d("APIError", jsonObject.toString())

            try {
                val response = withContext(Dispatchers.IO) {
                    makeApiCallWithContext(apiUrl, jsonObject)
                }

                // Handle success response
                if (response.contains("Collection Selected!")) {
                    Toast.makeText(this@CollectionActivity, "Collection $pCollectionName has been selected!", Toast.LENGTH_SHORT).show()
                } else {
                    // Handle unexpected response
                    Log.d("APIError", "Unexpected response: $response")
                }
            } catch (e: Exception) {
                // Handle error
                Toast.makeText(this@CollectionActivity, "Error: Check your Connection!", Toast.LENGTH_SHORT).show()
                Log.d("APIError", "Error: $e")
            }
        }
    }

}