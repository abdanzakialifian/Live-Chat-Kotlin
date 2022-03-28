package com.mbanking.m_banking.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    val email: String? = null,
    val id: String? = null,
    val image: String? = null,
    val name: String? = null
) : Parcelable
