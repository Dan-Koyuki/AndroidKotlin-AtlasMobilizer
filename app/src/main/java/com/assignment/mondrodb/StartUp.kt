package com.assignment.mondrodb

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.assignment.mondrodb.mainApp.LandingViewActivity

class StartUp : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.start_up_view)

        supportActionBar?.hide()

        Handler(Looper.getMainLooper()).postDelayed({
            // can do background process here if needed

            // create an intent to new activity
            val intent = Intent(this, LandingViewActivity::class.java)
            startActivity(intent)

            // destroy activity
            finish()
        }, 3000)
    }
}