package com.mbanking.m_banking.view.fragment

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import com.mbanking.m_banking.R
import com.mbanking.m_banking.databinding.FragmentTransferBinding
import java.text.NumberFormat
import java.util.*

class TransferFragment : Fragment() {

    private var _binding: FragmentTransferBinding? = null
    private val binding get() = _binding!!
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private var result = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTransferBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseAnalytics = Firebase.analytics
        binding.layoutTransfer.apply {
            edtBank.setOnClickListener {
                alertDialogBank()
            }
            edtBank.addTextChangedListener(editTextWatcher)
            edtNoRek.addTextChangedListener(editTextWatcher)
            edtAmount.addTextChangedListener(editAmountWatcher)
            btnSend.setOnClickListener {
                val inputManager =
                    context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputManager.hideSoftInputFromWindow(
                    view.windowToken,
                    InputMethodManager.HIDE_NOT_ALWAYS
                )
                alertDialogConfirm()
            }
        }
    }

    private val editAmountWatcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            binding.layoutTransfer.edtAmount.removeTextChangedListener(this)
            try {
                result = p0.toString().replace("[Rp. ]".toRegex(), "")
                val rpFormat = if (result.isEmpty()) {
                    ""
                } else formatRupiah(result.toDouble())
                binding.layoutTransfer.edtAmount.setText(rpFormat)
                binding.layoutTransfer.edtAmount.setSelection(rpFormat.length)
                binding.layoutTransfer.edtAmount.addTextChangedListener(this)
            } catch (nfe: NumberFormatException) {
            }

            val bank = binding.layoutTransfer.edtBank.text.toString()
            val noRek = binding.layoutTransfer.edtNoRek.text.toString()

            if (bank.isNotEmpty() && noRek.isNotEmpty() && result.isNotEmpty() && result.toInt() != 0 && result.toInt() >= 10000) {
                binding.layoutTransfer.btnSend.isEnabled = true
                binding.layoutTransfer.btnSend.setBackgroundResource(R.color.green_700)
            } else {
                binding.layoutTransfer.btnSend.isEnabled = false
                binding.layoutTransfer.btnSend.setBackgroundResource(R.color.main_background)
            }
        }

        override fun afterTextChanged(p0: Editable?) {}
    }

    private val editTextWatcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

        override fun afterTextChanged(p0: Editable?) {
            val bank = binding.layoutTransfer.edtBank.text.toString()
            val noRek = binding.layoutTransfer.edtNoRek.text.toString()

            if (bank.isNotEmpty() && noRek.isNotEmpty() && result.isNotEmpty() && result.toInt() != 0 && result.toInt() >= 10000) {
                binding.layoutTransfer.btnSend.isEnabled = true
                binding.layoutTransfer.btnSend.setBackgroundResource(R.color.green_700)
            } else {
                binding.layoutTransfer.btnSend.isEnabled = false
                binding.layoutTransfer.btnSend.setBackgroundResource(R.color.main_background)
            }
        }
    }

    private fun formatRupiah(number: Double): String {
        val localeId = Locale("IND", "ID")
        val numberFormat = NumberFormat.getCurrencyInstance(localeId)
        val formatRupiah = numberFormat.format(number)
        val split = formatRupiah.split(",")
        val length = split[0].length
        return split[0].substring(0, 2) + ". " + split[0].substring(2, length)
    }

    private fun alertDialogBank() {
        val items = resources.getStringArray(R.array.items)
        val sortedAsc =
            items.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { v -> v.toString() })
        AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog).apply {
            setTitle(resources.getString(R.string.choose_bank))
            setItems(sortedAsc.toTypedArray()) { _, which ->
                binding.layoutTransfer.edtBank.setText(items[which])
            }
            show()
        }
    }

    private fun alertDialogConfirm() {
        val bank = binding.layoutTransfer.edtBank.text
        val noRek = binding.layoutTransfer.edtNoRek.text
        val profit = (result.toInt() / 100) * 1
        AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog).apply {
            setTitle(resources.getString(R.string.confirmation))
            setMessage(
                """
                Bank : $bank
                
                No Rek : $noRek
                
                Jumlah : $result
            """.trimIndent()
            )
            setPositiveButton(resources.getString(R.string.ok)) { _, _ ->
                binding.loadingAnimation.visibility = View.VISIBLE
                Handler(Looper.getMainLooper()).postDelayed({
                    alertDialogSuccess()
                    addValueAndProfit(result.toLong(), profit.toString().toLong())
                    binding.loadingAnimation.visibility = View.GONE
                }, SUCCESS_DELAYED)
            }
            setNegativeButton(resources.getString(R.string.cancel)) { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
            show()
        }
    }

    @SuppressLint("InflateParams")
    private fun alertDialogSuccess() {
        AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog).apply {
            val inflater = layoutInflater
            val alertLayout = inflater.inflate(R.layout.custom_alert_dialog_success, null)
            setView(alertLayout)
            setPositiveButton(resources.getString(R.string.ok)) { dialogInterface, _ ->
                dialogInterface.dismiss()
                binding.layoutTransfer.edtBank.setText("")
                binding.layoutTransfer.edtNoRek.setText("")
                binding.layoutTransfer.edtAmount.setText("")
            }
            show()
        }
    }

    private fun addValueAndProfit(amount: Long, profit: Long) {
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.PURCHASE) {
            param(FirebaseAnalytics.Param.CURRENCY, "USD")
            param(FirebaseAnalytics.Param.VALUE, amount)
        }
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.AD_IMPRESSION) {
            param(FirebaseAnalytics.Param.CURRENCY, "USD")
            param(FirebaseAnalytics.Param.VALUE, profit)
        }
    }


    override fun onResume() {
        super.onResume()
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, "Transfer")
            param(FirebaseAnalytics.Param.SCREEN_CLASS, "TransferFragment")
        }
    }

    companion object {
        const val SUCCESS_DELAYED = 2000L
    }
}