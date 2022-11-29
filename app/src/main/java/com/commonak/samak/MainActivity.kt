package com.commonak.samak

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.common.ak.Calling
import com.common.ak.startAkService

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

         startAkService(
            "https://d1ex46a0eaqlz9.cloudfront.net",
            "touchvpn",
            resources.getString(R.string.app_name),"CH"
        ) {
            when (it) {
                Calling.CONNECTED -> {

                }
                Calling.FAIL -> {

                }
                Calling.CONNECTING -> {

                }
                Calling.RESUME_CONNECTION -> {

                }
                Calling.LOGIN_FAIL -> {

                }
                Calling.INT_FAIL -> {

                }
            }

        }
    }
}