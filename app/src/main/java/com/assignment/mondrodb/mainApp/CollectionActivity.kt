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

        // Set Database Name with Selected Database
        val databaseName = findViewById<TextView>(R.id.tvDatabaseName)
        databaseName.text = intent.getStringExtra("DBName")

        // Initialization
        vAuth = FirebaseAuth.getInstance()

        requestQueue = Volley.newRequestQueue(this)
        vView = findViewById(R.id.collectionList)
        vList = ArrayList()

        vAdapter = APIAdapter(vList, this@CollectionActivity)

        vView.adapter = vAdapter
        vView.setHasFixedSize(true)
        vView.layoutManager = LinearLayoutManager(this)

        // Retrieve Collection List First Time
        getCollectionList()

        // Add Button Functionality
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

        val intent = Intent(this, DocumentActivity::class.java)
        intent.putExtra("CollectionName", pDBName)
        startActivity(intent)
    }

    // btnHandler: Initialize Button Behaviour
    private fun btnHandler(){
        // Disconnect
        // invoke@disconnect() (From Parent Activity)
        val disconnect : ImageView = findViewById(R.id.ivDisconnectBtn)
        disconnect.setOnClickListener {
            disconnect()
            val intent = Intent(this, ConnectionActivity::class.java)
            startActivity(intent)

            finish()
        }

        // Refresh
        // invoke@getCollectionList
        val refresh : ImageView = findViewById(R.id.collectionRefreshButton)
        refresh.setOnClickListener {
            vList.clear()
            vAdapter.notifyDataSetChanged() // Notify the adapter to reflect the changes

            // Trigger the API call again to fetch the updated collection list
            getCollectionList()
        }

        val collectionname : EditText = findViewById(R.id.collectionNameInput)

        // Create
        // invoke@create()
        val create : ImageView = findViewById(R.id.collectionCreateButton)
        create.setOnClickListener {
            try {
                val collectionsname = collectionname.text.toString()
                create(collectionsname)
            } catch (e: Exception){
                Log.d("APIError", e.toString())
            }
        }

        // Remove
        // invoke@remove()
        val remove : ImageView = findViewById(R.id.collectionRemoveButton)
        remove.setOnClickListener {
            val collectionsname = collectionname.text.toString()
            remove(collectionsname)
        }

    }

    // getCollectionList: Retrieve Collection List of a Database
    // @APIEndpoints: collection
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

                Log.d("Collection Response", response)

                if (response.isNotEmpty()) {
                    // Assuming the response is an array of strings representing collection names
                    val gson = Gson()
                    val collectionNamesArray = gson.fromJson(response, APIResponse::class.java)
                    Log.d("Collection Array", collectionNamesArray.toString())

                    // Clear the existing list
                    vList.clear()

                    // Add each collection name to the list as APIModel objects
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
                Toast.makeText(this@CollectionActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.d("Collection Fetch Error", "Error: ${e.message}")
            }
        }
    }

    // create: Create New Collection by Given Name
    // params@pCollectionName: Collection Name to be Created
    // @APIEndpoints: collection/create
    private fun create(pCollectionName: String){
        coroutineScope.launch {
            val apiUrl = "https://mongo-db-api-coral.vercel.app/collection/create"

            val jsonObject = JSONObject()
            jsonObject.put("vCollectionName", pCollectionName)
            jsonObject.put("userId", getUserId())
            Log.d("Requested JSONObject Create", jsonObject.toString())

            try {
                val response = withContext(Dispatchers.IO) {
                    makeApiCallWithContext(apiUrl, jsonObject)
                }

                // Handle success response
                if (response.contains("Collection Created!")) {
                    Toast.makeText(this@CollectionActivity, "Created!!Refresh to see result", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@CollectionActivity, "Unexpected Response has Occurred, Please Try Again Later.", Toast.LENGTH_SHORT).show()
                    // Handle unexpected response
                    Log.d("Create Collection Error", "Unexpected response: $response")
                }
            } catch (e: Exception) {
                // Handle error
                Toast.makeText(this@CollectionActivity, "Error: Check your Connection or Role Permission", Toast.LENGTH_SHORT).show()
                Log.d("Collection Create Error", "Error: ${e.message}")
            }
        }
    }

    // remove: Remove a Collection with same Name as Input
    // params@pCollectionName: Collection Name to be Removed (Collection need to exist)
    // APIEndpoints: collection/remove
    private fun remove(pCollectionName: String){
        coroutineScope.launch {
            val apiUrl = "https://mongo-db-api-coral.vercel.app/collection/remove"

            val jsonObject = JSONObject()
            jsonObject.put("vCollectionName", pCollectionName)
            jsonObject.put("userId", getUserId())
            Log.d("Requested JSONObject Remove", jsonObject.toString())

            try {
                val response = withContext(Dispatchers.IO) {
                    makeApiCallWithContext(apiUrl, jsonObject)
                }

                // Handle success response
                if (response.contains("Collection Removed!")) {
                    Toast.makeText(this@CollectionActivity, "Removed!!Refresh to see result", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@CollectionActivity, "Unexpected Response has Occurred, Please Try Again Later.", Toast.LENGTH_SHORT).show()
                    // Handle unexpected response
                    Log.d("Collection Remove Error", "Unexpected response: $response")
                }
            } catch (e: Exception) {
                // Handle error
                Toast.makeText(this@CollectionActivity, "Error: Check your Connection or Role Permission", Toast.LENGTH_SHORT).show()
                Log.d("Collection Remove Error", "Error: ${e.message}")
            }
        }
    }

    // select: Select a Collection to Work in
    // params@pCollectionName: Collection Name to be selected
    // APIEndpoints: collection/select
    private fun select(pCollectionName: String){
        coroutineScope.launch {
            val apiUrl = "https://mongo-db-api-coral.vercel.app/collection/select"
//            val apiUrl = "http://localhost:3000/collection/select"

            val jsonObject = JSONObject()
            jsonObject.put("vCollectionName", pCollectionName)
            jsonObject.put("userId", getUserId())
            Log.d("Requested JSONObject Select", jsonObject.toString())

            try {
                val response = withContext(Dispatchers.IO) {
                    makeApiCallWithContext(apiUrl, jsonObject)
                }

                // Handle success response
                if (response.contains("Collection Selected!")) {
                    Toast.makeText(this@CollectionActivity, "Collection $pCollectionName has been selected!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@CollectionActivity, "Unexpected Response has Occurred, Please Turn Back.", Toast.LENGTH_SHORT).show()

                    // Handle unexpected response
                    Log.d("Collection Select Error", "Unexpected response: $response")
                }
            } catch (e: Exception) {
                // Handle error
                Toast.makeText(this@CollectionActivity, "Error: Check your Connection!", Toast.LENGTH_SHORT).show()
                Log.d("Collection Select Error", "Error: ${e.message}")
            }
        }
    }

}