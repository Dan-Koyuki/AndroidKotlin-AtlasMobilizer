package com.assignment.mondrodb.documentComponent

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.assignment.mondrodb.R
import com.assignment.mondrodb.mainApp.DashboardSettings
import com.assignment.mondrodb.mainApp.DocumentActivity
import com.assignment.mondrodb.myAdapter.APIAdapter
import com.assignment.mondrodb.myModel.APIModel
import com.assignment.mondrodb.myModel.APIResponse
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.json.JSONObject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class CreateActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "Activity"
    }

    private lateinit var requestQueue: RequestQueue
    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private lateinit var vAuth : FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create)

        requestQueue = Volley.newRequestQueue(this)
        vAuth = FirebaseAuth.getInstance()

        btnHandler()
    }

    // Get User ID from Firebase
    // return@string: User ID
    private fun getUserId(): String {
        val currentUser = vAuth.currentUser
        return currentUser?.uid.toString()
    }

    private fun btnHandler(){
        // Create button calling create()
        val createBtn = findViewById<ImageView>(R.id.tvCreateDocumentBtn)
        createBtn.setOnClickListener {
            try {
                create()
                val intent = Intent(this, DocumentActivity::class.java)
                startActivity(intent)
                finish()
            } catch (e: Exception) {
                Log.d("ClickError:", e.toString())
            }
        }

        // Add Field button calling addField()
        val llDocumentField = findViewById<LinearLayout>(R.id.llDocumentInputField)
        val addFieldBtn = findViewById<Button>(R.id.addFieldButton)
        addFieldBtn.setOnClickListener {
            val newKeyEditText = EditText(this)
            val keyLayoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            keyLayoutParams.setMargins(8, 8, 8, 8) // Set margin
            newKeyEditText.layoutParams = keyLayoutParams
            newKeyEditText.hint = "New Key"
            newKeyEditText.setPadding(8, 8, 8, 8) // Set padding
            llDocumentField.addView(newKeyEditText)

            val newValueEditText = EditText(this)
            val valueLayoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            valueLayoutParams.setMargins(8, 8, 8, 8) // Set margin
            newValueEditText.layoutParams = valueLayoutParams
            newValueEditText.hint = "New Value"
            newValueEditText.setPadding(8, 8, 8, 8) // Set padding
            llDocumentField.addView(newValueEditText)
        }

        // Back button logic, intent to DocumentActivity and delete this activity history
        val backBtn = findViewById<ImageView>(R.id.ivBackBtn)
        backBtn.setOnClickListener {
            val intent = Intent(this, DocumentActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun create(){
        // Handle Create API Calling
        // will need the jsonCreate method
        coroutineScope.launch {
            val apiUrl = "https://mongo-db-api-coral.vercel.app/documents/insert"

            val jsonObject = JSONObject()
            val jsonDoc = jsonCreate()
            jsonObject.put("userId", getUserId())
            jsonObject.put("pDoc", jsonDoc)

            try {
                val response = withContext(Dispatchers.IO) {
                    makeApiCallWithContext(apiUrl, jsonObject)
                }

                Log.d("APIError", response)

                if (response.contains("Document Inserted!")) {
                    Toast.makeText(this@CreateActivity, "Created!!Refresh to see result", Toast.LENGTH_SHORT).show()
                } else {
                    // Handle unexpected response
                    Log.d("APIError", "Unexpected response: $response")
                }

            } catch (e: Exception) {
                Toast.makeText(this@CreateActivity, "Error: $e", Toast.LENGTH_SHORT).show()
                Log.d("APIError", "Error: $e")
            }
        }
    }

    private fun jsonCreate() : JSONObject{
        // Handle Converting Input to JSON
        // Iterate through a view to take each input
        val jsonObject = JSONObject()
        val llDocumentInputField: LinearLayout = findViewById(R.id.llDocumentInputField)
        val childCount: Int = llDocumentInputField.childCount
        var i = 0
        while (i < childCount) {
            val view1: View = llDocumentInputField.getChildAt(i)
            val view2: View = llDocumentInputField.getChildAt(i + 1)
            if (view1 is EditText && view2 is EditText) {
                val editText1 = view1
                val editText2 = view2

                val key = editText1.text.toString()
                val value = editText2.text.toString()

                jsonObject.put(key, value)
            }
            i += 2
        }
        return jsonObject
    }

    private suspend fun makeApiCallWithContext(apiUrl: String, jsonObject: JSONObject): String {
        return suspendCancellableCoroutine { continuation ->
            val request = JsonObjectRequest(
                Request.Method.POST, apiUrl, jsonObject,
                { response ->
                    continuation.resume(response.toString())
                },
                { error ->
                    continuation.resumeWithException(error)
                })

            requestQueue.add(request)
            continuation.invokeOnCancellation {
                requestQueue.cancelAll(CreateActivity.TAG) // Cancel the request if coroutine is cancelled
            }
        }
    }
}