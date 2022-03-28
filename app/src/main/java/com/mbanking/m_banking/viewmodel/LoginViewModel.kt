package com.mbanking.m_banking.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.mbanking.m_banking.R
import com.mbanking.m_banking.utils.IOnSignInStartedListener

class LoginViewModel(val app: Application, private val listenerI: IOnSignInStartedListener) :
    ViewModel() {
    private var firebaseAuth = Firebase.auth

    private val _currentUser = MutableLiveData<FirebaseUser>()
    val currentUser: LiveData<FirebaseUser> = _currentUser

    private val _isSuccessfully = MutableLiveData<Boolean>()
    val isSuccessfully: LiveData<Boolean> = _isSuccessfully

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(app.getString(R.string.custom_web_client_id))
        .requestEmail()
        .build()

    private val googleSignInClient = GoogleSignIn.getClient(app, gso)

    fun firebaseAuthWithGoogle(idToken: String?) {
        _isLoading.value = true
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = firebaseAuth.currentUser
                val firebaseDatabase = Firebase.database
                val userRef = firebaseDatabase.reference.child("Users").child(user?.uid.toString())

                val map = HashMap<String, Any>()
                map["id"] = user?.uid.toString()
                map["email"] = user?.email.toString()
                map["name"] = user?.displayName.toString()
                map["image"] = user?.photoUrl.toString()
                map["login"] = true

                userRef.setValue(map).addOnCompleteListener { taskRef ->
                    if (taskRef.isSuccessful) {
                        _isLoading.value = false
                        _isSuccessfully.value = true
                        _currentUser.value = firebaseAuth.currentUser
                    } else {
                        _isLoading.value = false
                        _isSuccessfully.value = false
                    }
                }
            } else {
                _isLoading.value = false
                _isSuccessfully.value = false
            }
        }
    }

    fun signIn() {
        listenerI.onSignInStarted(googleSignInClient)
    }
}