package com.assignment.mondrodb.mainApp

import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.Gravity
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.assignment.mondrodb.R
import com.assignment.mondrodb.databinding.LandingViewBinding
import com.google.firebase.auth.FirebaseAuth


class LandingViewActivity : AppCompatActivity() {

    private lateinit var auth : FirebaseAuth
    private lateinit var binding : LandingViewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = LandingViewBinding.inflate(layoutInflater)
        auth = FirebaseAuth.getInstance()

        setContentView(binding.root)

        supportActionBar?.hide()

        binding.ivSignUpBtn.setOnClickListener{
            val email = binding.etLoginEmail.text.toString()
            val password = binding.etLoginPassword.text.toString()
            if (checkField()){
                auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
                    if (it.isSuccessful){
                        // this will immediatelly sign in user
                        Toast.makeText(this, "Account Successfully Created!", Toast.LENGTH_SHORT).show()
                        // new intent, going to connectivity setting
                        val cIntent = Intent(this, ConnectionActivity::class.java)
                        startActivity(cIntent)

                        // destory activity, so user cant go back to landing view when they already logged in
                        finish()
                    } else {
                        Log.d("Error:", it.exception.toString())
                    }
                }
            }
        }

        binding.ivLoginBtn.setOnClickListener {
            val email = binding.etLoginEmail.text.toString()
            val password = binding.etLoginPassword.text.toString()
            if (checkField()){
                auth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
                    if (it.isSuccessful){
                        Toast.makeText(this, "Login Success!", Toast.LENGTH_SHORT).show()
                        // new intent, going to connectivity setting
                        val cIntent = Intent(this, ConnectionActivity::class.java)
                        startActivity(cIntent)

                        // destory activity, so user cant go back to landing view when they already logged in
                        finish()
                    } else {
                        Log.d("Error:", it.exception.toString())
                    }
                }
            }
        }

        binding.ivExitBtn.setOnClickListener {
            finishAffinity()
            System.exit(0)
        }

        btnHandler()
    }

    fun checkField(): Boolean{
        val email = binding.etLoginEmail.text.toString()
        if (binding.etLoginEmail.text.toString() == ""){
            binding.etLoginEmail.error = "Field Required!!"
            return false
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            binding.etLoginEmail.error = "Wrong Email Format!!"
            return false
        }
        if (binding.etLoginPassword.length() < 6){
            binding.etLoginPassword.error = "Password should at least 6 character long!!"
            return false
        }
        return true
    }

    // Popup window/dialog settings
    private fun btnHandler() {
        val helpBtn : ImageView = findViewById(R.id.ivLoginHelpBtn)
        helpBtn.setOnClickListener{
            showHelpDialog()
        }

        val privacy = findViewById<Button>(R.id.PrivacyPolicy)
        privacy.setOnClickListener {
            openWebPage("https://dan-koyuki.github.io/Mobile-Native_Atlas-Mobilizer/")
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
        val view = layoutInflater.inflate(R.layout.help_fragment, null)
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
        val whatsappBtn = view.findViewById<TextView>(R.id.WhatsApp_contact)
        val discordBtn = view.findViewById<TextView>(R.id.discord_contact)

        // Set up button click listeners
        whatsappBtn.setOnClickListener {
            openWebPage("https://wa.me/+6282116424576")
            dialog.dismiss()
        }

        discordBtn.setOnClickListener {
            openWebPage("https://discord.gg/uApdCfZVmp")
            dialog.dismiss()
        }
    }

    private fun openWebPage(url: String) {
        val uri = Uri.parse(url)
        val intent = Intent(Intent.ACTION_VIEW, uri)
        startActivity(intent)
    }
}