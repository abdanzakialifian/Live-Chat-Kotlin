package com.mbanking.m_banking.view.fragment

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Application
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.mbanking.m_banking.R
import com.mbanking.m_banking.databinding.FragmentChattingBinding
import com.mbanking.m_banking.model.User
import com.mbanking.m_banking.other.FirebaseService
import com.mbanking.m_banking.utils.Global.KEY_ID_USER
import com.mbanking.m_banking.utils.Global.PREFS_NAME
import com.mbanking.m_banking.utils.IOnItemClickCallback
import com.mbanking.m_banking.utils.IOnItemClickLongCallback
import com.mbanking.m_banking.view.activity.ChattingUserActivity
import com.mbanking.m_banking.view.activity.ChattingUserActivity.Companion.EXTRA_ID
import com.mbanking.m_banking.view.activity.ChattingUserActivity.Companion.EXTRA_NOTIF
import com.mbanking.m_banking.view.activity.ContainerActivity
import com.mbanking.m_banking.view.adapter.ListChattingAdapter
import com.mbanking.m_banking.view.adapter.ListContactAdapter
import com.mbanking.m_banking.viewmodel.ChattingViewModel
import com.mbanking.m_banking.viewmodel.ChattingViewModelFactory
import java.util.regex.Pattern

@SuppressLint("NotifyDataSetChanged")
class ChattingFragment : Fragment() {
    private var _binding: FragmentChattingBinding? = null
    private val binding get() = _binding!!
    private lateinit var chattingViewModel: ChattingViewModel
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private lateinit var listChattingAdapter: ListChattingAdapter
    private lateinit var listContactAdapter: ListContactAdapter
    private lateinit var senderId: String
    private val userListChatting = ArrayList<User>()
    private val userList = mutableSetOf<User>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChattingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        firebaseAnalytics = Firebase.analytics
        chattingViewModel = obtainViewModel(activity?.application)
        sharedPreferences = activity?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)!!
        senderId = sharedPreferences.getString(KEY_ID_USER, null) as String
        FirebaseService.sharedPreferences =
            activity?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        FirebaseInstallations.getInstance().getToken(true).addOnSuccessListener {
            FirebaseService.tokens = it.token
        }
        FirebaseMessaging.getInstance().subscribeToTopic("/topics/$senderId")

        chattingViewModel.getListUser(senderId)
        chattingViewModel.listUser.observe(viewLifecycleOwner) { listUser ->
            userList.addAll(listUser)
        }
        chattingViewModel.checkUser(senderId)

        binding.fabSendMessage.setOnClickListener {
            alertDialogListEmail()
        }

        binding.fabSendMessage.setOnLongClickListener {
            alertDialogInputEmail()
            true
        }

        showUserSender()
        showUserReceiver()
        checkEmailUser()
    }

    private fun loadingData() {
        chattingViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                binding.shimmerLayout.visibility = View.VISIBLE
                binding.shimmerLayout.startShimmer()
            } else {
                binding.shimmerLayout.visibility = View.GONE
                binding.shimmerLayout.stopShimmer()
            }
        }
    }

    private fun showUserSender() {
        chattingViewModel.userSender.observe(viewLifecycleOwner) { user ->
            userListChatting.add(0, user)
            listChattingAdapter =
                ListChattingAdapter(
                    userListChatting.distinct(),
                    object : IOnItemClickCallback {
                        override fun onItemClicked(user: User) {
                            Intent(requireContext(), ChattingUserActivity::class.java).apply {
                                putExtra(EXTRA_ID, user.id)
                                startActivity(this)
                            }
                        }
                    },
                    object : IOnItemClickLongCallback {
                        override fun onItemLongClicked(user: User) {
                            alertDialogDelete(user)
                        }
                    })
            binding.apply {
                rvListChatting.layoutManager = LinearLayoutManager(requireContext())
                rvListChatting.adapter = listChattingAdapter
                listChattingAdapter.notifyDataSetChanged()
                rvListChatting.setHasFixedSize(true)
                srcUserChatting.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String?): Boolean {
                        listChattingAdapter.filter.filter(query)
                        return true
                    }

                    override fun onQueryTextChange(newText: String?): Boolean {
                        listChattingAdapter.filter.filter(newText)
                        return true
                    }
                })
            }
        }
        loadingData()
    }

    private fun showUserReceiver() {
        chattingViewModel.userReceiver.observe(viewLifecycleOwner) { user ->
            userListChatting.add(0, user)
            listChattingAdapter =
                ListChattingAdapter(
                    userListChatting.distinct(),
                    object : IOnItemClickCallback {
                        override fun onItemClicked(user: User) {
                            Intent(requireContext(), ChattingUserActivity::class.java).apply {
                                putExtra(EXTRA_ID, user.id)
                                startActivity(this)
                            }
                        }
                    },
                    object : IOnItemClickLongCallback {
                        override fun onItemLongClicked(user: User) {
                            alertDialogDelete(user)
                        }
                    })
            binding.apply {
                rvListChatting.layoutManager = LinearLayoutManager(requireContext())
                rvListChatting.adapter = listChattingAdapter
                listChattingAdapter.notifyDataSetChanged()
                rvListChatting.setHasFixedSize(true)
                srcUserChatting.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String?): Boolean {
                        listChattingAdapter.filter.filter(query)
                        return true
                    }

                    override fun onQueryTextChange(newText: String?): Boolean {
                        listChattingAdapter.filter.filter(newText)
                        return true
                    }
                })
            }
        }
        loadingData()
    }

    @SuppressLint("InflateParams")
    private fun alertDialogListEmail() {
        Dialog(requireContext(), R.style.CustomAlertDialog).apply {
            setContentView(R.layout.dialog_box_users)
            listContactAdapter = ListContactAdapter(object : IOnItemClickCallback {
                override fun onItemClicked(user: User) {
                    Intent(context, ChattingUserActivity::class.java).apply {
                        putExtra(EXTRA_ID, user.id)
                        startActivity(this)
                    }
                    hide()
                }
            })
            val sortAsc = userList.toList()
                .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { v -> v.name.toString() })
            listContactAdapter.setListContact(sortAsc)
            val recyclerView = this.findViewById<RecyclerView>(R.id.rv_list_users)
            recyclerView.layoutManager = LinearLayoutManager(context)
            recyclerView.adapter = listContactAdapter
            listContactAdapter.notifyDataSetChanged()
            show()
            val layoutParams = WindowManager.LayoutParams()
            layoutParams.copyFrom(window?.attributes)
            layoutParams.height = 1000
            window?.attributes = layoutParams
        }
    }

    @SuppressLint("InflateParams")
    private fun alertDialogInputEmail() {
        AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog).apply {
            setTitle("Email")
            val inflater = layoutInflater
            val dialogLayout = inflater.inflate(R.layout.custom_alert_dialog_email, null)
            val edtEmail = dialogLayout.findViewById<EditText>(R.id.edt_email)
            setView(dialogLayout)
            setPositiveButton("Cari") { _, _ ->
                val email = edtEmail.text.toString().trim()
                if (EMAIL_ADDRESS_PATTERN.matcher(email).matches()) {
                    chattingViewModel.checkEmail(email)
                } else {
                    Toast.makeText(context, "Email tidak valid.", Toast.LENGTH_SHORT).show()
                }
            }
            setNegativeButton("Cancel") { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
            show()
        }
    }

    private fun alertDialogDelete(user: User) {
        AlertDialog.Builder(context, R.style.CustomAlertDialog).apply {
            setTitle(resources.getString(R.string.delete))
            setMessage(resources.getString(R.string.are_you_sure_delete_it))
            setPositiveButton(resources.getString(R.string.yes)) { _, _ ->
                chattingViewModel.deleteUserChatting(user.id.toString(), senderId)
                val indexUser = userListChatting.toSet().toList().indexOf(user)
                listChattingAdapter.notifyItemRemoved(indexUser)
                restartActivity()
            }
            setNegativeButton(resources.getString(R.string.cancel)) { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
            show()
        }
    }

    private fun restartActivity() {
        Handler(Looper.getMainLooper()).post {
            Intent(requireActivity(), ContainerActivity::class.java).apply {
                putExtra(EXTRA_NOTIF, true)
                addFlags(
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                            or Intent.FLAG_ACTIVITY_NO_ANIMATION
                )
                requireActivity().overridePendingTransition(0, 0)
                requireActivity().finish()
                requireActivity().overridePendingTransition(0, 0)
                startActivity(this)
            }
        }
    }

    private fun checkEmailUser() {
        chattingViewModel.isEmailFound.observe(viewLifecycleOwner) { isEmail ->
            if (isEmail) {
                chattingViewModel.userId.observe(viewLifecycleOwner) { userId ->
                    Intent(requireContext(), ChattingUserActivity::class.java).apply {
                        putExtra(EXTRA_ID, userId)
                        startActivity(this)
                    }
                }
            } else {
                Toast.makeText(
                    requireContext(),
                    resources.getString(R.string.email_not_found),
                    Toast.LENGTH_SHORT
                )
                    .show()
            }
        }
    }

    private fun obtainViewModel(app: Application?): ChattingViewModel {
        val factory = ChattingViewModelFactory(app!!)
        return ViewModelProvider(viewModelStore, factory)[ChattingViewModel::class.java]
    }

    override fun onResume() {
        super.onResume()
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, "ListChatting")
            param(FirebaseAnalytics.Param.SCREEN_CLASS, "ChattingFragment")
        }
    }

    companion object {
        val EMAIL_ADDRESS_PATTERN: Pattern = Pattern.compile(
            "^(([\\w-]+\\.)+[\\w-]+|([a-zA-Z]|[\\w-]{2,}))@"
                    + "((([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                    + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\."
                    + "([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                    + "[0-9]{1,2}|25[0-5]|2[0-4][0-9]))|"
                    + "([a-zA-Z]+[\\w-]+\\.)+[a-zA-Z]{2,4})$"
        )
    }
}