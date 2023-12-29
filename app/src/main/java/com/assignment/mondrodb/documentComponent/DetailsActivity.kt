package com.assignment.mondrodb.documentComponent

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.assignment.mondrodb.R
import com.assignment.mondrodb.mainApp.DashboardSettings
import com.assignment.mondrodb.myAdapter.APIAdapter
import com.assignment.mondrodb.myAdapter.DocumentAdapter
import com.assignment.mondrodb.myModel.DocumentDetails
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.json.JSONObject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class DetailsActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "Activity"
    }

    private lateinit var requestQueue: RequestQueue
    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private lateinit var vAuth : FirebaseAuth
    private lateinit var vList : ArrayList<DocumentDetails>
    private lateinit var vView : RecyclerView
    private lateinit var vAdapter : DocumentAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)

        vAuth = FirebaseAuth.getInstance()

        requestQueue = Volley.newRequestQueue(this)
        vView = findViewById(R.id.rvDocumentDetails)
        vList = ArrayList()

        vAdapter = DocumentAdapter(vList)

        vView.adapter = vAdapter
        vView.setHasFixedSize(true)
        vView.layoutManager = LinearLayoutManager(this)

    }

    private fun getUserId(): String {
        val currentUser = vAuth.currentUser
        return currentUser?.uid.toString()
    }

    protected suspend fun makeApiCallWithContext(apiUrl: String, jsonObject: JSONObject): String {
        return suspendCancellableCoroutine { continuation ->
            val request = JsonObjectRequest(
                Request.Method.POST, apiUrl, jsonObject,
                { response ->
                    // Instead of trying to parse the response as JSONObject, handle it as String
                    continuation.resume(response.toString())
                },
                { error ->
                    continuation.resumeWithException(error)
                })

            requestQueue.add(request)
            continuation.invokeOnCancellation {
                requestQueue.cancelAll(DetailsActivity.TAG) // Cancel the request if coroutine is cancelled
            }
        }
    }

    private fun getDocument(){
        coroutineScope.launch {
            val apiUrl = "https://mongo-db-api-coral.vercel.app/document/select"

            val jsonObject = JSONObject()
            jsonObject.put("userId", getUserId())
            jsonObject.put("pID", intent.getStringExtra("DocumentId"))

            try {
                val response = withContext(Dispatchers.IO){
                    makeApiCallWithContext(apiUrl, jsonObject)
                }

                Log.d("One Document API", response)

                if (response.isNotEmpty()){
                    // due to continuation.resume(response.toString()) in the makeApiCallWithContext,
                    // the response is String version from JSON object of a Document from MongoDB
                    // vList is expecting DocumentDetails as element of its array
                    // DocumentDetails expecting vMap: Map<String, Any>
                    // Parse the JSON response string into a JSONObject
                    val jsonResponse = JSONObject(response)

                    // Extract the "document" object from the JSON response
                    val documentObject = jsonResponse.getJSONObject("document")

                    // Create a Map<String, Any> to hold the document details
                    val documentMap = mutableMapOf<String, Any>()

                    // Iterate through the keys in the documentObject and add them to the documentMap
                    val keys = documentObject.keys()
                    while (keys.hasNext()) {
                        val key = keys.next()
                        val value = documentObject.opt(key) // Get the value corresponding to the key

                        // Add key-value pair to the documentMap
                        documentMap[key] = value ?: "" // Replace null values with an appropriate default value
                    }

                    // Add the documentMap to your vList of DocumentDetails
                    vList.add(DocumentDetails(documentMap)) // Assuming DocumentDetails accepts a Map<String, Any> in its constructor
                    vAdapter.notifyDataSetChanged() // Notify the adapter of changes in the list

                } else {
                    Toast.makeText(this@DetailsActivity, "Failed, Please Check Your Connection or Database Permission!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@DetailsActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.d("APIError", "Error: $e")
            }
        }
    }

}