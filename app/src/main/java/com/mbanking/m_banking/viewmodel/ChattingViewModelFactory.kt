package com.mbanking.m_banking.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

@Suppress("UNCHECKED_CAST")
class ChattingViewModelFactory(private val app: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChattingViewModel::class.java)) {
            return ChattingViewModel(app) as T
        }
        throw IllegalArgumentException("Unknown ViewModel Class")
    }
}