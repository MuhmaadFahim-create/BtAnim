package com.example.btanim

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.TextView

class BtService : Service() {

    private val handler = Handler(Looper.getMainLooper())

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action != BluetoothDevice.ACTION_ACL_CONNECTED) return
            val device: BluetoothDevice? = if (Build.VERSION.SDK_INT >= 33)
                intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
            else @Suppress("DEPRECATION") intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
            val name = try { device?.name } catch (e: SecurityException) { null } ?: "Bluetooth Device"
            showAnimation(name)
        }
    }

    override fun onCreate() {
        super.onCreate()
        val channelId = "bt_anim"
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(
            NotificationChannel(channelId, "BT Animation", NotificationManager.IMPORTANCE_MIN)
        )
        val notification = Notification.Builder(this, channelId)
            .setContentTitle("Bluetooth animation চালু আছে")
            .setSmallIcon(android.R.drawable.stat_sys_data_bluetooth)
            .build()
        startForeground(1, notification)

        registerReceiver(receiver, IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED))
    }

    private fun showAnimation(deviceName: String) {
        val wm = getSystemService(WINDOW_SERVICE) as WindowManager
        val density = resources.displayMetrics.density
        fun dp(v: Int) = (v * density).toInt()

        val root = FrameLayout(this).apply { setBackgroundColor(Color.parseColor("#99000000")) }

        val rings = (0 until 3).map {
            View(this).apply {
                background = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setStroke(dp(4), Color.parseColor("#2196F3"))
                }
                layoutParams = FrameLayout.LayoutParams(dp(120), dp(120), Gravity.CENTER)
                alpha = 0f
            }.also { root.addView(it) }
        }

        val label = TextView(this).apply {
            text = "🎧\n$deviceName\nConnected"
            setTextColor(Color.WHITE)
            textSize = 20f
            gravity = Gravity.CENTER
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER
            )
            scaleX = 0f; scaleY = 0f
        }
        root.addView(label)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )

        try { wm.addView(root, params) } catch (e: Exception) { return }

        label.animate().scaleX(1f).scaleY(1f).setDuration(400).start()
        rings.forEachIndexed { i, ring ->
            ring.alpha = 0.9f
            ring.animate()
                .scaleX(4f).scaleY(4f).alpha(0f)
                .setStartDelay(i * 350L).setDuration(1400).start()
        }

        handler.postDelayed({
            root.animate().alpha(0f).setDuration(300).withEndAction {
                try { wm.removeView(root) } catch (_: Exception) {}
            }.start()
        }, 3000)
    }

    override fun onDestroy() {
        unregisterReceiver(receiver)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
