package com.mbanking.m_banking.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.mbanking.m_banking.model.ApiConfig
import com.mbanking.m_banking.model.PushNotification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationViewModel : ViewModel() {
    fun sendNotification(notification: PushNotification) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                ApiConfig.getNotificationApi().postNotification(notification)
            } catch (e: Exception) {
                Log.d("Exception", e.message.toString())
            }
        }
    }
}