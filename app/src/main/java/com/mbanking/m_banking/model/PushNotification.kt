package com.mbanking.m_banking.model

data class PushNotification(
    var data: NotificationData? = null,
    var to: String? = null
)
