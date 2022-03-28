package com.mbanking.m_banking.utils

import com.mbanking.m_banking.model.PushNotification
import com.mbanking.m_banking.utils.Global.CONTENT_TYPE
import com.mbanking.m_banking.utils.Global.SERVER_KEY
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface INotificationApi {
    @Headers("Authorization: key=$SERVER_KEY", "Content-type:$CONTENT_TYPE")
    @POST("fcm/send")
    suspend fun postNotification(
        @Body notification: PushNotification
    ): Response<ResponseBody>
}