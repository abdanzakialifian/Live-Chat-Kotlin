package com.mbanking.m_banking.view.activity

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import com.mbanking.m_banking.R
import com.mbanking.m_banking.databinding.ActivityLoginBinding
import com.mbanking.m_banking.utils.Global.KEY_EMAIL_USER
import com.mbanking.m_banking.utils.Global.KEY_ID_USER
import com.mbanking.m_banking.utils.Global.KEY_IMAGE_USER
import com.mbanking.m_banking.utils.Global.KEY_NAME_USER
import com.mbanking.m_banking.utils.Global.KEY_SIGN_IN
import com.mbanking.m_banking.utils.Global.PREFS_NAME
import com.mbanking.m_banking.utils.IOnSignInStartedListener
import com.mbanking.m_banking.viewmodel.LoginViewModel
import com.mbanking.m_banking.viewmodel.LoginViewModelFactory

@Suppress("DEPRECATION")
class LoginActivity : AppCompatActivity(), IOnSignInStartedListener {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var loginViewModel: LoginViewModel
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var firebaseAnalytics: FirebaseAnalytics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAnalytics = Firebase.analytics
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        loginViewModel = obtainViewModel(application, this)

        binding.layoutLogin.btnSignGoogle.setOnClickListener {
            loginViewModel.signIn()
        }

        checkSignIn()

        saveDataUser()
    }

    private fun obtainViewModel(
        app: Application,
        listenerI: IOnSignInStartedListener
    ): LoginViewModel {
        val factory = LoginViewModelFactory(app, listenerI)
        return ViewModelProvider(this, factory)[LoginViewModel::class.java]
    }

    private fun saveDataUser() {
        loginViewModel.currentUser.observe(this) { user ->
            firebaseAnalytics.logEvent("login_successfully") {
                param(FirebaseAnalytics.Param.ITEM_ID, user.uid)
                param(FirebaseAnalytics.Param.ITEM_NAME, user.displayName.toString())
                param(FirebaseAnalytics.Param.CONTENT_TYPE, "Button Login")
            }
            val editor = sharedPreferences.edit()
            editor.putString(KEY_ID_USER, user?.uid)
            editor.putString(KEY_EMAIL_USER, user?.email)
            editor.putString(KEY_IMAGE_USER, user.photoUrl?.toString())
            editor.putString(KEY_NAME_USER, user.displayName)
            editor.apply()
        }
    }

    private fun checkSignIn() {
        loginViewModel.isSuccessfully.observe(this) { isSuccessfully ->
            if (isSuccessfully) {
                val editor = sharedPreferences.edit()
                editor.putBoolean(KEY_SIGN_IN, true)
                editor.apply()
                Intent(this, ContainerActivity::class.java).apply {
                    startActivity(this)
                    finish()
                }
            } else {
                Toast.makeText(
                    this,
                    resources.getString(R.string.sign_in_failed),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        loginViewModel.isLoading.observe(this) { isLoading ->
            if (isLoading) {
                binding.loadingAnimation.visibility = View.VISIBLE
            } else {
                binding.loadingAnimation.visibility = View.GONE
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                loginViewModel.firebaseAuthWithGoogle(account.idToken)
            } catch (e: ApiException) {
                Toast.makeText(this, "Error : $e", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onSignInStarted(client: GoogleSignInClient) {
        startActivityForResult(client.signInIntent, RC_SIGN_IN)
    }

    override fun onResume() {
        super.onResume()
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, "Login")
            param(FirebaseAnalytics.Param.SCREEN_CLASS, "LoginActivity")
        }
    }

    companion object {
        const val RC_SIGN_IN = 1
    }
}