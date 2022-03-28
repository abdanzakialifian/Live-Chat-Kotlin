package com.mbanking.m_banking.utils

import com.mbanking.m_banking.model.User

interface IOnItemClickCallback {
    fun onItemClicked(user: User)
}