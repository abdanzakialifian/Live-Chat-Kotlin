package com.mbanking.m_banking.viewmodel

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.mbanking.m_banking.model.Chatting
import com.mbanking.m_banking.model.User
import com.mbanking.m_banking.utils.Global

class ChattingViewModel(val app: Application) : ViewModel() {

    private val _user = MutableLiveData<User>()
    val user: LiveData<User> = _user

    private val _userId = MutableLiveData<String>()
    val userId: LiveData<String> = _userId

    private val _listUser = MutableLiveData<List<User>>()
    val listUser: LiveData<List<User>> = _listUser

    private val _userSender = MutableLiveData<User>()
    var userSender: LiveData<User> = _userSender

    private val _userReceiver = MutableLiveData<User>()
    var userReceiver: LiveData<User> = _userReceiver

    private val _chatting = MutableLiveData<List<Chatting>>()
    val chatting: LiveData<List<Chatting>> = _chatting

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isEmailFound = MutableLiveData<Boolean>()
    val isEmailFound: LiveData<Boolean> = _isEmailFound

    private var sharedPreferences: SharedPreferences? = null

    fun getDataUser(userId: String) {
        val userRef = Firebase.database.reference.child("Users").child(userId)
        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val data = snapshot.getValue(User::class.java) as User
                    _user.value = data
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun getListUser(userId: String) {
        val userRef = Firebase.database.reference.child("Users")
        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val listUser = ArrayList<User>()
                    for (data in snapshot.children) {
                        if (data.key == userId) {
                            continue
                        } else {
                            val user = data.getValue(User::class.java) as User
                            listUser.add(user)
                            _listUser.value = listUser
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun checkEmail(email: String) {
        sharedPreferences = app.getSharedPreferences(Global.PREFS_NAME, Context.MODE_PRIVATE)
        val userRef = Firebase.database.reference.child("Users")
        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    snapshot.children.forEachIndexed { index, data ->
                        val emailList = data.child("email").value.toString()
                        val userId = data.child("id").value.toString()
                        val emailPref = sharedPreferences?.getString(Global.KEY_EMAIL_USER, null)

                        if (email == emailList && email != emailPref) {
                            _isEmailFound.value = true
                            _userId.value = userId
                            return
                        }

                        if (index == snapshot.children.count() - 1) {
                            _isEmailFound.value = false
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun sendMessage(senderId: String?, receiverId: String?, message: String?) {
        val userReference = Firebase.database.reference.child("Chatting")
        val map = HashMap<String, String>()
        map["senderId"] = senderId.toString()
        map["receiverId"] = receiverId.toString()
        map["message"] = message.toString()

        userReference.push().setValue(map)
    }

    fun readMessage(senderId: String, receiverId: String) {
        val userRef = Firebase.database.reference.child("Chatting")
        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val listChat = ArrayList<Chatting>()
                    listChat.clear()
                    for (data in snapshot.children) {
                        val chat = data.getValue(Chatting::class.java)

                        if (chat?.senderId == senderId && chat.receiverId == receiverId || chat?.senderId == receiverId && chat.receiverId == senderId) {
                            listChat.add(chat)
                            _chatting.value = listChat
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun checkUser(senderId: String?) {
        val ref = Firebase.database.reference.child("Chatting")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (data in snapshot.children) {
                        val receiver = data.child("receiverId").value.toString()
                        val idSender = data.child("senderId").value.toString()
                        if (senderId == receiver) {
                            checkUserSender(idSender)
                        }

                        if (senderId == idSender) {
                            checkUserReceiver(receiver)
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun checkUserSender(userId: String) {
        val userRef = Firebase.database.reference.child("Users").child(userId)
        _isLoading.value = true
        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    _isLoading.value = false
                    _userSender.value = snapshot.getValue(User::class.java)
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun checkUserReceiver(userId: String) {
        val userRef = Firebase.database.reference.child("Users").child(userId)
        _isLoading.value = true
        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    _isLoading.value = false
                    _userReceiver.value = snapshot.getValue(User::class.java)
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun deleteUserChatting(receiverId: String, senderId: String) {
        val chattingRef = Firebase.database.reference.child("Chatting")
        chattingRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (data in snapshot.children) {
                        val receiverIdUser = data.child("receiverId").value.toString()
                        val senderIdUser = data.child("senderId").value.toString()

                        if (receiverId == receiverIdUser && senderId == senderIdUser || receiverId == senderIdUser && senderId == receiverIdUser) {
                            data.ref.removeValue()

                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
}