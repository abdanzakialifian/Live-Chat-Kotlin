package com.mbanking.m_banking.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

@Suppress("UNCHECKED_CAST")
class ContainerViewModelFactory(private val app: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ContainerViewModel::class.java)) {
            return ContainerViewModel(app) as T
        }
        throw IllegalArgumentException("Unknown ViewModel Class")
    }
}