package com.example.projectdemo.feature.auth.forgotpassword

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

class PasswordResetViewModel : ViewModel() {

    fun resetPassword(email: String, context: Context) {

        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(context, "Password reset instructions sent to your email", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Error resetting password", Toast.LENGTH_SHORT).show()
                }
            }
    }
}