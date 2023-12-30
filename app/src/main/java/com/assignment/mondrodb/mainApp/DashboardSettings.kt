package com.assignment.mondrodb.mainApp

import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.assignment.mondrodb.myAdapter.APIAdapter
import com.assignment.mondrodb.myModel.APIModel
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

open class DashboardSettings : AppCompatActivity() {

    companion object {
        private const val TAG = "Activity"
    }

    protected lateinit var requestQueue: RequestQueue
    protected val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    protected lateinit var vList : ArrayList<APIModel>
    protected lateinit var vView : RecyclerView
    protected lateinit var vAdapter : APIAdapter
    protected lateinit var vAuth : FirebaseAuth

    // disconnect: Disconnection from MongoDB
    // APIEndpoints: mongoDB/disconnect
    protected fun disconnect(){
        coroutineScope.launch {
            val apiUrl = "https://mongo-db-api-coral.vercel.app/mongoDB/disconnect"

            val jsonObject = JSONObject()
            jsonObject.put("userId", getUserId())

            try {
                val response = withContext(Dispatchers.IO) {
                    makeApiCallWithContext(apiUrl, jsonObject)
                }
                // Handle response
                if (!response.contains("Disconnected From Atlas")) {
                    Log.d("Disconnect Error", "Unexpected response: $response")
                }
            } catch (e: Exception) {
                // Handle error
                Log.d("Disconnect Error", "Error: ${e.message}")
            }
        }
    }

    // getUserID
    // @return: userID from Firebase Authentication
    protected fun getUserId(): String {
        val currentUser = vAuth.currentUser
        return currentUser?.uid.toString()
    }

    // makeApiCallWithContext: handle interaction with backend using Volley
    // params@apiUrl: url endpoint
    // params@jsonObject: requested jsonObject by the endpoint
    protected suspend fun makeApiCallWithContext(apiUrl: String, jsonObject: JSONObject): String {
        return suspendCancellableCoroutine { continuation ->
            val request = JsonObjectRequest(
                Request.Method.POST, apiUrl, jsonObject,
                { response ->
                    // Instead of trying to parse the response as JSONObject, handle it as String
                    Log.d("API Response", response.toString())
                    continuation.resume(response.toString())
                },
                { error ->
                    continuation.resumeWithException(error)
                })

            requestQueue.add(request)
            continuation.invokeOnCancellation {
                requestQueue.cancelAll(TAG) // Cancel the request if coroutine is cancelled
            }
        }
    }

}