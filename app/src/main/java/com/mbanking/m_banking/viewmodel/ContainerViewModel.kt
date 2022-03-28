package com.mbanking.m_banking.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.mbanking.m_banking.R
import java.util.*

class ContainerViewModel(app: Application) : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(app.getString(R.string.custom_web_client_id))
        .requestEmail()
        .build()

    val googleSignInClient = GoogleSignIn.getClient(app, gso)

    private val calendar = Calendar.getInstance()

    val greetings = when (calendar[Calendar.HOUR_OF_DAY]) {
        in 5..12 -> {
            app.resources.getString(R.string.good_morning)
        }
        in 12..17 -> {
            app.resources.getString(R.string.good_afternoon)
        }
        else -> {
            app.resources.getString(R.string.good_evening)
        }
    }

    fun updateData(email: String, id: String, image: String, name: String) {
        _isLoading.value = true
        val userRef = Firebase.database.reference.child("Users").child(id)
        val map = HashMap<String, Any>()
        map["id"] = id
        map["email"] = email
        map["name"] = name
        map["image"] = image
        map["login"] = false

        userRef.updateChildren(map).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                _isLoading.value = false
            } else {
                _isLoading.value = false
            }
        }
    }
}