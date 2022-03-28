package com.mbanking.m_banking.utils

import com.google.android.gms.auth.api.signin.GoogleSignInClient

interface IOnSignInStartedListener {
    fun onSignInStarted(client: GoogleSignInClient)
}