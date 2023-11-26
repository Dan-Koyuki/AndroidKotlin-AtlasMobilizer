package com.assignment.mondrodb.mainApp

import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.assignment.mondrodb.BaseActivity
import com.assignment.mondrodb.R
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


class ConnectionActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "ConnectionActivity"
    }
    private lateinit var auth : FirebaseAuth
    private lateinit var requestQueue: RequestQueue
    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.connection_view)

        auth = FirebaseAuth.getInstance()
        requestQueue = Volley.newRequestQueue(this)

        val logout : ImageView = findViewById(R.id.ivLogoutBtnI)
        logout.setOnClickListener {
            auth.signOut()
            val landIntent = Intent(this, LandingViewActivity::class.java)
            startActivity(landIntent)

            finish()
        }

        btnHandler()

    }

    private fun btnHandler(){
        val connectionString : EditText = findViewById(R.id.etMongoConnection)
        val clusterName : EditText = findViewById(R.id.etMongoCluster)
        val connectBtn : ImageView = findViewById(R.id.ivConnectBtn)
        connectBtn.setOnClickListener {
            val uri = connectionString.text.toString()
            val cluster = clusterName.text.toString()
            Log.d("APIError", uri)
            try {
                connectToMongo(uri)
            } catch (e: Exception) {
                Toast.makeText(this@ConnectionActivity, "Error: $e", Toast.LENGTH_SHORT).show()
            }
            val intent = Intent(this, DatabaseActivity::class.java)
            intent.putExtra("Cluster", cluster)
            startActivity(intent)
            finish()
        }

        

        val helpBtn : ImageView = findViewById(R.id.ivConnectionHelpBtn)
        helpBtn.setOnClickListener{
            showHelpDialog()
        }
    }

    private fun connectToMongo(uri: String) {
        coroutineScope.launch {
            val apiUrl = "https://mongo-db-api-coral.vercel.app/connectToMongoDB"

            Log.d("APIError", uri)
            val jsonObject = JSONObject()
            jsonObject.put("uri", uri)
            Log.d("APIError", jsonObject.toString())

            try {
                val response = withContext(Dispatchers.IO) {
                    makeApiCall(apiUrl, jsonObject)
                }

                // Handle success response
                if (response.contains("Connected to MongoDB Atlas")) {
                    Toast.makeText(this@ConnectionActivity, "Connected!!", Toast.LENGTH_SHORT).show()
                } else {
                    // Handle unexpected response
                    Log.d("APIError", "Unexpected response: $response")
                }
            } catch (e: Exception) {
                // Handle error
                Toast.makeText(this@ConnectionActivity, "Error: $e", Toast.LENGTH_SHORT).show()
                Log.d("APIError", "Error: $e")
            }
        }
    }

    private suspend fun makeApiCall(apiUrl: String, jsonObject: JSONObject): String {
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
                requestQueue.cancelAll(TAG) // Cancel the request if coroutine is cancelled
            }
        }
    }

    private fun showHelpDialog() {
        val builder = AlertDialog.Builder(this, R.style.TransparentDialog)
        // Create a custom title TextView with a larger text size and bold style
        val customTitle = TextView(this)
        customTitle.text = "Welcome to Atlas Mobilizer"
        customTitle.textSize = 24f
        customTitle.setTextColor(ContextCompat.getColor(this, R.color.white))
        customTitle.setTypeface(null, Typeface.BOLD)

        // Set the custom title view for the AlertDialog
        builder.setCustomTitle(customTitle)
        val view = layoutInflater.inflate(R.layout.connect_help_fragment, null)
        builder.setView(view)
        builder.setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }

        val dialog = builder.create()

        // Set the gravity to bottom
        val layoutParams = WindowManager.LayoutParams()
        val window = dialog.window
        layoutParams.copyFrom(window?.attributes)
        layoutParams.gravity = Gravity.BOTTOM
        window?.attributes = layoutParams

        dialog.show()

        // Get references to buttons in the dialog's layout
        val mongoDBLink = view.findViewById<TextView>(R.id.mongoLink)

        // Set up button click listeners
        mongoDBLink.setOnClickListener {
            openWebPage("https://bit.ly/3QI2KBz")
            dialog.dismiss()
        }
    }

    private fun openWebPage(url: String) {
        val uri = Uri.parse(url)
        val intent = Intent(Intent.ACTION_VIEW, uri)
        startActivity(intent)
    }

}