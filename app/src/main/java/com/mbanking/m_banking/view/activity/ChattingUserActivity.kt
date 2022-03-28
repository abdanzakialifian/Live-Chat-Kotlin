package com.mbanking.m_banking.view.activity

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import com.mbanking.m_banking.databinding.ActivityChattingUserBinding
import com.mbanking.m_banking.model.NotificationData
import com.mbanking.m_banking.model.PushNotification
import com.mbanking.m_banking.utils.Global.KEY_ID_USER
import com.mbanking.m_banking.utils.Global.KEY_NAME_USER
import com.mbanking.m_banking.utils.Global.PREFS_NAME
import com.mbanking.m_banking.view.adapter.ChattingAdapter
import com.mbanking.m_banking.viewmodel.ChattingViewModel
import com.mbanking.m_banking.viewmodel.ChattingViewModelFactory
import com.mbanking.m_banking.viewmodel.NotificationViewModel

class ChattingUserActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChattingUserBinding
    private lateinit var chattingViewModel: ChattingViewModel
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var adapter: ChattingAdapter
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private val notificationViewModel by viewModels<NotificationViewModel>()
    private var topic = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChattingUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAnalytics = Firebase.analytics
        chattingViewModel = obtainViewModel(application)
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val receiverId = intent.getStringExtra(EXTRA_ID) as String
        val senderId = sharedPreferences.getString(KEY_ID_USER, null)

        binding.btnBack.setOnClickListener {
            Intent(this, ContainerActivity::class.java).apply {
                putExtra(EXTRA_NOTIF, true)
                startActivity(this)
                finish()
            }
        }

        chattingViewModel.readMessage(senderId.toString(), receiverId)
        chattingViewModel.getDataUser(receiverId)
        getDataUser(senderId)
        listChatting()
    }

    private fun listChatting() {
        chattingViewModel.chatting.observe(this@ChattingUserActivity) { chatting ->
            adapter = ChattingAdapter()
            adapter.setListChat(chatting)
            binding.rvChatting.layoutManager = LinearLayoutManager(this@ChattingUserActivity)
            binding.rvChatting.adapter = adapter
            binding.rvChatting.setHasFixedSize(true)
        }
    }

    private fun getDataUser(senderId: String?) {
        chattingViewModel.user.observe(this) { user ->
            binding.apply {
                Glide.with(this@ChattingUserActivity)
                    .load(user.image)
                    .circleCrop()
                    .into(imgUser)
                tvName.text = user.name
                btnSend.setOnClickListener {
                    chattingViewModel.checkUser(senderId)
                    sendMessageAndNotification(senderId, user.id)
                }
            }
        }
    }

    private fun sendMessageAndNotification(senderId: String?, userId: String?) {
        val message = binding.edtMessage.text.toString()
        val id = sharedPreferences.getString(KEY_ID_USER, null)
        val name = sharedPreferences.getString(KEY_NAME_USER, null)
        chattingViewModel.sendMessage(senderId, userId, message)
        topic = "/topics/$userId"
        val pushNotification =
            PushNotification(NotificationData(name, message, id), topic)
        notificationViewModel.sendNotification(pushNotification)
        binding.edtMessage.setText("")
    }

    private fun obtainViewModel(app: Application): ChattingViewModel {
        val factory = ChattingViewModelFactory(app)
        return ViewModelProvider(this, factory)[ChattingViewModel::class.java]
    }

    override fun onResume() {
        super.onResume()
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, "Chatting")
            param(FirebaseAnalytics.Param.SCREEN_CLASS, "ChattingUserActivity")
        }
    }

    companion object {
        const val EXTRA_ID = "extra_id"
        const val EXTRA_NOTIF = "extra_notif"
    }
}