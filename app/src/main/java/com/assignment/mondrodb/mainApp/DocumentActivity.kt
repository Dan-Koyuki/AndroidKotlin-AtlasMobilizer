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
import com.assignment.mondrodb.documentComponent.CreateActivity
import com.assignment.mondrodb.myAdapter.APIAdapter
import com.assignment.mondrodb.myModel.APIModel
import com.assignment.mondrodb.myModel.APIResponse
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class DocumentActivity : DashboardSettings(), APIAdapter.MyClickListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_document)

        val collectionName = findViewById<TextView>(R.id.tvCollectionName)
        collectionName.text = intent.getStringExtra("CollectionName")

        vAuth = FirebaseAuth.getInstance()

        requestQueue = Volley.newRequestQueue(this)
        vView = findViewById(R.id.rvDatabaseList)
        vList = ArrayList()

        vAdapter = APIAdapter(vList, this@DocumentActivity)

        vView.adapter = vAdapter
        vView.setHasFixedSize(true)
        vView.layoutManager = LinearLayoutManager(this)

        getDocumentList()

    }

    private fun getUserId(): String {
        val currentUser = vAuth.currentUser
        return currentUser?.uid.toString()
    }

    override fun onClick(pDBName: String) {
        TODO("Go to Detailed Documents where update and delete button of that document exist there")
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

            // Trigger the API call again to fetch the updated documents list
            getDocumentList()
        }

        // Create
        val create : ImageView = findViewById(R.id.tvCreateCollectionBtn)
        create.setOnClickListener {
            val intent = Intent(this, CreateActivity::class.java)
            startActivity(intent)
        }

    }

    // get list of document id
    private fun getDocumentList(){
        coroutineScope.launch {
            val apiUrl = "https://mongo-db-api-coral.vercel.app/documents"

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
                    val documentsIDArray = gson.fromJson(response, APIResponse::class.java)
                    Log.d("APIError", documentsIDArray.toString())

                    // Clear the existing list
                    vList.clear()

                    // Add each database name to the list as APIModel objects
                    documentsIDArray.list.forEach { documentsID ->
                        val documentsModel = APIModel(documentsID)
                        vList.add(documentsModel)
                    }

                    // Notify the adapter of the data change
                    vAdapter.notifyDataSetChanged()
                } else {
                    Toast.makeText(this@DocumentActivity, "Failed, Please Check Your Connection or Database Permission!", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                Toast.makeText(this@DocumentActivity, "Error: $e", Toast.LENGTH_SHORT).show()
                Log.d("APIError", "Error: $e")
            }
        }
    }

}