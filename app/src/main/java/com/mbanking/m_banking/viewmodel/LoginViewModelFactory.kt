package com.mbanking.m_banking.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mbanking.m_banking.utils.IOnSignInStartedListener

@Suppress("UNCHECKED_CAST")
class LoginViewModelFactory(
    private val application: Application,
    private val listenerI: IOnSignInStartedListener
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            return LoginViewModel(application, listenerI) as T
        }
        throw IllegalArgumentException("Unknown ViewModel Class")
    }
}