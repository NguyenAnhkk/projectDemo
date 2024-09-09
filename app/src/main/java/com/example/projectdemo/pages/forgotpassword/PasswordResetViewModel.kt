package com.example.projectdemo.pages.forgotpassword

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

class PasswordResetViewModel : ViewModel() {

    fun resetPassword(email: String) {
        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Email đặt lại mật khẩu đã được gửi thành công
                    // Xử lý thành công (ví dụ: chuyển hướng đến màn hình đăng nhập)
                } else {
                    // Đặt lại mật khẩu thất bại
                    // Xử lý thất bại (ví dụ: hiển thị thông báo lỗi)
                }
            }
    }
}