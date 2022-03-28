package com.mbanking.m_banking.utils

import com.mbanking.m_banking.model.User

interface IOnItemClickLongCallback {
    fun onItemLongClicked(user: User)
}