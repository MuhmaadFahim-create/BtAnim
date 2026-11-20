package com.example.btanim

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
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
            text = "\"Enable\" চাপুন এবং সব পারমিশন দিন।\nএকবার সেটআপ হলে অ্যাপ খুলে রাখতে হবে না।"
            textSize = 18f
        }
        val enable = Button(this).apply { text = "Enable" }
        layout.addView(info)
        layout.addView(enable)
        setContentView(layout)

        enable.setOnClickListener {
            if (Build.VERSION.SDK_INT >= 31 &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this, arrayOf(Manifest.permission.BLUETOOTH_CONNECT), 1
                )
                info.text = "পারমিশন দিন, তারপর আবার Enable চাপুন।"
                return@setOnClickListener
            }
            if (!Settings.canDrawOverlays(this)) {
                startActivity(
                    Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
                )
                info.text = "সুইচ চালু করে ব্যাক করুন, তারপর আবার Enable চাপুন।"
                return@setOnClickListener
            }
            val pm = getSystemService(PowerManager::class.java)
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                startActivity(
                    Intent(
                        Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                        Uri.parse("package:$packageName")
                    )
                )
                info.text = "Allow দিন, তারপর আবার Enable চাপুন।"
                return@setOnClickListener
            }
            info.text = "সব প্রস্তুত ✅ এখন ব্লুটুথ কানেক্ট করলেই অ্যানিমেশন দেখাবে।\nঅ্যাপ বন্ধ করে দিতে পারেন।"
        }
    }
}
