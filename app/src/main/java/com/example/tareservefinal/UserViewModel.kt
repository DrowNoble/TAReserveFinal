package com.example.tareservefinal

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.google.firebase.database.FirebaseDatabase

class UserViewModel(application: Application): AndroidViewModel(application){
    // TODO: Look to change this to private & move to 'User' Class Object
    var userId = "1160826215"
    var isTA = "0"
    var userToken = "null"

    init{
        //FirebaseDatabase.getInstance().setPersistenceEnabled(true)
    }

    // Setter for userID
    fun setUserID(userID: String) {
        userId = userID
    }

    // Setter for isTA
    fun setIsTA(TA: String) {
        isTA = TA
    }
}