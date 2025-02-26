package com.example.usergrocerymart.viewmodels

import android.app.Activity
import androidx.lifecycle.ViewModel
import com.example.usergrocerymart.mesg
import com.example.usergrocerymart.models.Users
import com.google.firebase.FirebaseException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.concurrent.TimeUnit


class authviewmodel : ViewModel() {
private val _verificationId= MutableStateFlow<String?>(null )
    private val _otpsend= MutableStateFlow(false)
    val otpsent=_otpsend
    private val _signindone= MutableStateFlow(false)
    val signindone=_signindone
    private val _currentuser= MutableStateFlow(false)
    val currentuser=_currentuser
    init{
        mesg.getAuthInstance().currentUser?.let {
            _currentuser.value=true
        }
    }
    fun sendotp(userNumber: String,activity:Activity ){

      val  callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {


            }

            override fun onVerificationFailed(e: FirebaseException) {

            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken,
            ) {
                _verificationId.value=verificationId
                    _otpsend.value=true

            }
        }
        val options = PhoneAuthOptions.newBuilder(mesg.getAuthInstance())
            .setPhoneNumber("+977$userNumber") // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(activity) // Activity (for callback binding)
            .setCallbacks(callbacks) // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)


    }
     fun signInWithPhoneAuthCredential(otp: String, userNumber: String, user: Users) {
        val credential = PhoneAuthProvider.getCredential(_verificationId.value?:"", otp)
        mesg.getAuthInstance().signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    FirebaseDatabase.getInstance().getReference("All Users").child("Users").child(user.uid!!).setValue(user)
                    _signindone.value=true

                } else {

                }
            }
    }

}