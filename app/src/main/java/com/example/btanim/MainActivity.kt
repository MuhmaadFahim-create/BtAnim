package com.example.btanim

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 96, 48, 48)
        }
        val info = TextView(this).apply {
            text = "ব্লুটুথ কানেক্ট হলে অ্যানিমেশন দেখাবে।\n\"Start\" চাপুন এবং সব পারমিশন দিন।"
            textSize = 18f
        }
        val start = Button(this).apply { text = "Start" }
        layout.addView(info)
        layout.addView(start)
        setContentView(layout)

        start.setOnClickListener {
            // ১) ব্লুটুথ ও নোটিফিকেশন পারমিশন
            val missing = mutableListOf<String>()
            if (Build.VERSION.SDK_INT >= 31 && !granted(Manifest.permission.BLUETOOTH_CONNECT))
                missing += Manifest.permission.BLUETOOTH_CONNECT
            if (Build.VERSION.SDK_INT >= 33 && !granted(Manifest.permission.POST_NOTIFICATIONS))
                missing += Manifest.permission.POST_NOTIFICATIONS
            if (missing.isNotEmpty()) {
                ActivityCompat.requestPermissions(this, missing.toTypedArray(), 1)
                info.text = "পারমিশন দিন, তারপর আবার Start চাপুন।"
                return@setOnClickListener
            }
            // ২) অন্য অ্যাপের ওপরে দেখানোর পারমিশন
            if (!Settings.canDrawOverlays(this)) {
                startActivity(
                    Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
                )
                info.text = "সুইচ চালু করে ব্যাক করুন, তারপর আবার Start চাপুন।"
                return@setOnClickListener
            }
            // ৩) সার্ভিস চালু
            ContextCompat.startForegroundService(this, Intent(this, BtService::class.java))
            info.text = "চালু আছে ✅ এখন ব্লুটুথ কানেক্ট করে দেখুন।"
        }
    }

    private fun granted(p: String) =
        ContextCompat.checkSelfPermission(this, p) == PackageManager.PERMISSION_GRANTED
}
