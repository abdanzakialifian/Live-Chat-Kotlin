package com.mbanking.m_banking.view.activity

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.mbanking.m_banking.R
import com.mbanking.m_banking.databinding.ActivityContainerBinding
import com.mbanking.m_banking.utils.Global.KEY_EMAIL_USER
import com.mbanking.m_banking.utils.Global.KEY_ID_USER
import com.mbanking.m_banking.utils.Global.KEY_IMAGE_USER
import com.mbanking.m_banking.utils.Global.KEY_NAME_USER
import com.mbanking.m_banking.utils.Global.KEY_SIGN_IN
import com.mbanking.m_banking.utils.Global.PREFS_NAME
import com.mbanking.m_banking.view.activity.ChattingUserActivity.Companion.EXTRA_NOTIF
import com.mbanking.m_banking.view.fragment.ChattingFragment
import com.mbanking.m_banking.view.fragment.HomeFragment
import com.mbanking.m_banking.view.fragment.TransferFragment
import com.mbanking.m_banking.viewmodel.ContainerViewModel
import com.mbanking.m_banking.viewmodel.ContainerViewModelFactory

class ContainerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityContainerBinding
    private lateinit var containerViewModel: ContainerViewModel
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var name: String
    private lateinit var email: String
    private lateinit var image: String
    private lateinit var id: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContainerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        containerViewModel = obtainViewModel(application)
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        image = sharedPreferences.getString(KEY_IMAGE_USER, null) as String
        name = sharedPreferences.getString(KEY_NAME_USER, null) as String
        email = sharedPreferences.getString(KEY_EMAIL_USER, null) as String
        id = sharedPreferences.getString(KEY_ID_USER, null) as String

        getDataUser()

        bottomNavigationItem()

        loadingLogout()

        binding.btnLogout.setOnClickListener {
            alertSignOut()
        }
    }

    @SuppressLint("InflateParams")
    private fun alertSignOut() {
        AlertDialog.Builder(this, R.style.CustomAlertDialog).apply {
            val inflater = layoutInflater
            val alertLayout = inflater.inflate(R.layout.custom_alert_dialog_logout, null)
            setView(alertLayout)
            setPositiveButton(resources.getString(R.string.yes)) { _, _ ->
                containerViewModel.googleSignInClient.revokeAccess()
                    .addOnCompleteListener {
                        if (it.isComplete) {
                            containerViewModel.updateData(email, id, image, name)
                            val editor = sharedPreferences.edit()
                            editor.putBoolean(KEY_SIGN_IN, false)
                            editor.apply()
                            Intent(this@ContainerActivity, LoginActivity::class.java).apply {
                                startActivity(this)
                                finish()
                            }
                        }
                    }
            }
            setNegativeButton(resources.getString(R.string.no)) { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
            show()
        }
    }

    private fun getDataUser() {
        Glide.with(this)
            .load(image)
            .circleCrop()
            .into(binding.imgProfile)
        binding.tvGreetings.text = containerViewModel.greetings
        binding.tvNameUser.text = name
    }

    private fun bottomNavigationItem() {
        val isNotif = intent.getBooleanExtra(EXTRA_NOTIF, false)
        val homeFragment = HomeFragment()
        val chattingFragment = ChattingFragment()
        val transferFragment = TransferFragment()

        if (isNotif) {
            setCurrentFragment(chattingFragment)
            binding.bottomNavView.selectedItemId = R.id.chatting
            binding.btnLogout.visibility = View.GONE
        } else {
            setCurrentFragment(homeFragment)
        }

        binding.bottomNavView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> {
                    setCurrentFragment(homeFragment)
                    binding.btnLogout.visibility = View.VISIBLE
                }
                R.id.chatting -> {
                    setCurrentFragment(chattingFragment)
                    binding.btnLogout.visibility = View.GONE
                }
                R.id.transfer -> {
                    setCurrentFragment(transferFragment)
                    binding.btnLogout.visibility = View.GONE
                }
            }
            true
        }
    }

    private fun setCurrentFragment(fragment: Fragment) {
        supportFragmentManager.commit {
            replace(R.id.fragment_view, fragment)
        }
    }

    private fun obtainViewModel(app: Application): ContainerViewModel {
        val factory = ContainerViewModelFactory(app)
        return ViewModelProvider(this, factory)[ContainerViewModel::class.java]
    }

    private fun loadingLogout() {
        containerViewModel.isLoading.observe(this) { isLoading ->
            if (isLoading) {
                binding.loadingAnimation.visibility = View.VISIBLE
            } else {
                binding.loadingAnimation.visibility = View.GONE
            }
        }
    }
}