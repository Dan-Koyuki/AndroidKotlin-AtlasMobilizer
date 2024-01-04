package com.assignment.mondrodb.mainApp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.toolbox.Volley
import com.assignment.mondrodb.R
import com.assignment.mondrodb.documentComponent.CreateActivity
import com.assignment.mondrodb.documentComponent.DetailsActivity
import com.assignment.mondrodb.myAdapter.APIAdapter
import com.assignment.mondrodb.myModel.APIModel
import com.assignment.mondrodb.myModel.APIResponse
import com.assignment.mondrodb.myModel.RetrofitDocument
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class DocumentActivity : DashboardSettings(), APIAdapter.MyClickListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_document)

        val collectionName = findViewById<TextView>(R.id.tvCollectionName)
        collectionName.text = intent.getStringExtra("CollectionName")

        vAuth = FirebaseAuth.getInstance()

        requestQueue = Volley.newRequestQueue(this)
        vView = findViewById(R.id.documentList)
        vList = ArrayList()

        vAdapter = APIAdapter(vList, this@DocumentActivity)

        vView.adapter = vAdapter
        vView.setHasFixedSize(true)
        vView.layoutManager = LinearLayoutManager(this)

        getDocumentList()
//        getDocument()
        btnHandler()
    }

    override fun onClick(pDBName: String) {
        try{
            val intent = Intent(this, DetailsActivity::class.java)
            intent.putExtra("DocumentId", pDBName)
            startActivity(intent)
        } catch (e: Exception){
            Log.d("ClickError: ", e.toString())
        }
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

            // Trigger the API call again to fetch the updated documents list
            getDocumentList()
//            getDocument()
        }

        // Create
        val create : ImageView = findViewById(R.id.tvCreateDocumentBtn)
        create.setOnClickListener {
            val intent = Intent(this, CreateActivity::class.java)
            startActivity(intent)
        }

        //help button
//        val help = findViewById<ImageView>(R.id.ivHelpDocument)
//        help.setOnClickListener {
//            Toast.makeText(this, "Coming Soon", Toast.LENGTH_SHORT).show()
//        }

    }

    // get list of document id
    private fun getDocumentList(){
        coroutineScope.launch {
            val apiUrl = "https://mongo-db-api-coral.vercel.app/documents"
//            val apiUrl = "http://localhost:3000/documents"

            val jsonObject = JSONObject()
            jsonObject.put("userId", getUserId())

            try {
                val response = withContext(Dispatchers.IO) {
                    makeApiCallWithContext(apiUrl, jsonObject)
                }

                Log.d("APIError", response)

                if (response.isNotEmpty()) {
                    // Assuming the response is an array of strings representing document _id
                    val gson = Gson()
                    val documentsIDArray = gson.fromJson(response, APIResponse::class.java)
                    Log.d("APIError", documentsIDArray.toString())

                    // Clear the existing list
                    vList.clear()

                    // Add each document _id to the list as APIModel objects
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
                Toast.makeText(this@DocumentActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.d("APIError", "Error: $e")
            }
        }
    }

    private fun getDocument(){
        val retrofit = Retrofit.Builder()
            .baseUrl("https://mongo-db-api-coral.vercel.app/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(RetrofitDocument::class.java)

        val call = service.getDocument(getUserId())
        call.enqueue(object : Callback<List<String>> {
            override fun onResponse(call: Call<List<String>>, response: Response<List<String>>) {
                Log.d("Response", response.body().toString())
                if (response.isSuccessful){
                    val document = response.body()

                    Toast.makeText(this@DocumentActivity, "Document is ${document?.joinToString(", ")}", Toast.LENGTH_LONG).show()

                    document?.forEach { ID ->
                        val documentID = APIModel(ID)
                        vList.add(documentID)
                    }

                    vAdapter.notifyDataSetChanged()
                } else {
                    Toast.makeText(this@DocumentActivity, response.body().toString(), Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<List<String>>, t: Throwable) {
                Toast.makeText(this@DocumentActivity, t.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

}