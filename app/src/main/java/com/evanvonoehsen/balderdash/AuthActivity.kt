package com.evanvonoehsen.balderdash

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.evanvonoehsen.balderdash.data.FirebaseData
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_auth.*
import java.lang.RuntimeException

class AuthActivity: AppCompatActivity(), CreateUsernameDialog.CreateUsernameHandler {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
//            startActivity(Intent(this, MainMenuActivity::class.java))
        }

        setupActionBars()
    }

    fun signupClick(v: View){
        if (!isFormValid()){
            return
        }

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(
            etEmail.text.toString(), etPassword.text.toString()
        ).addOnSuccessListener {
            Toast.makeText(this@AuthActivity,
                "Successfully Registered",
                Toast.LENGTH_LONG).show()
            CreateUsernameDialog().show(this.supportFragmentManager, "TAG_USERNAME_DIALOG")
        }.addOnFailureListener{
            Toast.makeText(this@AuthActivity,
                "Error: ${it.message}",
                Toast.LENGTH_LONG).show()
        }
    }

    fun loginClick(v: View) {
        if (!isFormValid()) {
            return
        }

        FirebaseAuth.getInstance().signInWithEmailAndPassword(
            etEmail.text.toString(), etPassword.text.toString()
        ).addOnSuccessListener {
            Toast.makeText(this@AuthActivity,
                "Successfully Logged In",
                Toast.LENGTH_LONG).show()

            startActivity(Intent(this, MainMenuActivity::class.java))
        }.addOnFailureListener {
            Toast.makeText(this@AuthActivity,
                "Error: ${it.message}",
                Toast.LENGTH_LONG).show()
        }
    }

    fun isFormValid(): Boolean {
        return when {
            etEmail.text.isEmpty() -> {
                etEmail.error = "This field can not be empty"
                false
            }
            etPassword.text.isEmpty() -> {
                etPassword.error = "The password can not be empty"
                false
            }
            else -> true
        }
    }
    fun setupActionBars() {
        this.supportActionBar?.hide()

        val window: Window = this@AuthActivity.window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.statusBarColor = ContextCompat.getColor(this@AuthActivity, R.color.persimmon_variant)
    }

    override fun usernameCreated(username: String) {
        FirebaseData.username = username
        startActivity(Intent(this, MainMenuActivity::class.java))
    }
}