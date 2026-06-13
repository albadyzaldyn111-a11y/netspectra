package com.example.vpn

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.data.database.NetSpectraDatabase
import com.example.data.model.FirewallRule
import com.example.data.repository.NetSpectraRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.FileInputStream

class FirewallVpnService : VpnService() {
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)
    private var vpnInterface: ParcelFileDescriptor? = null
    private lateinit var repository: NetSpectraRepository
    private var connectionThread: Thread? = null
    private lateinit var connectivityManager: ConnectivityManager

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            Log.d("FirewallVpnService", "Network changed, re-evaluating firewall rules")
            startVpn()
        }
        override fun onLost(network: Network) {
            super.onLost(network)
            Log.d("FirewallVpnService", "Network lost, re-evaluating firewall rules")
            startVpn()
        }
    }

    private val screenReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_SCREEN_ON, Intent.ACTION_SCREEN_OFF -> {
                    Log.d("FirewallVpnService", "Screen state changed: ${intent.action}, re-applying rules")
                    startVpn()
                }
            }
        }
    }

    companion object {
        const val ACTION_START = "com.example.vpn.START"
        const val ACTION_STOP = "com.example.vpn.STOP"
        const val ACTION_RESTART = "com.example.vpn.RESTART"
        const val CHANNEL_ID = "NetSpectraFirewallVpnChannel"
        const val NOTIFICATION_ID = 4552

        @Volatile
        var isServiceRunning = false
            private set
    }

    override fun onCreate() {
        super.onCreate()
        val database = NetSpectraDatabase.getDatabase(this)
        repository = NetSpectraRepository(database.firewallRuleDao(), database.dataLimitDao())
        connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        // Track network changes to apply rules like cell-only & wifi-only dynamically
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(request, networkCallback)

        // Register screen state listeners for background blocker
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
        }
        registerReceiver(screenReceiver, filter)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: ACTION_START
        Log.d("FirewallVpnService", "Service invoked with Action: $action")
        when (action) {
            ACTION_START -> {
                showNotification()
                startVpn()
            }
            ACTION_STOP -> {
                stopVpnService()
            }
            ACTION_RESTART -> {
                startVpn()
            }
        }
        return START_STICKY
    }

    private fun showNotification() {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "NetSpectra Firewall Service",
                NotificationManager.IMPORTANCE_LOW
            )
            manager.createNotificationChannel(channel)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("NetSpectra جدار ناري نشط")
            .setContentText("جاري حظر التطبيقات المحددة وتحليل الاستهلاك...")
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    private fun startVpn() {
        scope.launch {
            try {
                val rules = repository.getAllRules()
                val isWifi = isWifiConnected(connectivityManager)
                val isMobile = isMobileConnected(connectivityManager)

                val powerManager = getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
                val isInteractive = powerManager.isInteractive

                val builder = Builder()
                    .setSession("NetSpectra Firewall")
                    .setMtu(1500)
                    .addAddress("10.8.0.2", 32)
                    .addRoute("0.0.0.0", 0)

                var addedAny = false

                for (rule in rules) {
                    var shouldBlock = false

                    if (rule.isBlocked) {
                        shouldBlock = true
                    } else if (rule.wifiOnly && isMobile) {
                        // App should only run on WiFi, but we are on mobile-data -> Box/Block it
                        shouldBlock = true
                    } else if (rule.mobileOnly && isWifi) {
                        // App should only run on cellular, but we are on WiFi -> Box/Block it
                        shouldBlock = true
                    } else if (rule.backgroundBlocked && !isInteractive) {
                        // Background blocker: block app internet when screen is off
                        shouldBlock = true
                    }

                    if (shouldBlock) {
                        try {
                            builder.addAllowedApplication(rule.packageName)
                            addedAny = true
                            Log.d("FirewallVpnService", "Routing ${rule.packageName} to discard interface")
                        } catch (e: Exception) {
                            Log.e("FirewallVpnService", "Could not route ${rule.packageName}", e)
                        }
                    }
                }

                // Default dummy container if no app is blocked
                if (!addedAny) {
                    builder.addAllowedApplication(packageName)
                }

                // Establish VPN safely
                synchronized(this@FirewallVpnService) {
                    val oldInterface = vpnInterface
                    vpnInterface = builder.establish()
                    oldInterface?.close()
                }

                isServiceRunning = true

                // Start draining input streams
                connectionThread?.interrupt()
                connectionThread = Thread {
                    val fd = vpnInterface ?: return@Thread
                    val inputStream = FileInputStream(fd.fileDescriptor)
                    val buffer = ByteArray(32768)
                    try {
                        while (!Thread.currentThread().isInterrupted) {
                            val bytesRead = inputStream.read(buffer)
                            if (bytesRead <= 0) {
                                Thread.sleep(50)
                            }
                        }
                    } catch (e: java.io.IOException) {
                        Log.e("FirewallVpnService", "TUN Stream error: ${e.message}")
                    } catch (e: InterruptedException) {
                        // Thread closed
                    }
                }.apply {
                    name = "NetSpectraTunnelDrainer"
                    start()
                }

            } catch (e: Exception) {
                Log.e("FirewallVpnService", "Failed establishing VPN routing", e)
            }
        }
    }

    private fun stopVpnService() {
        isServiceRunning = false
        connectionThread?.interrupt()
        connectionThread = null
        synchronized(this) {
            vpnInterface?.close()
            vpnInterface = null
        }
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun isWifiConnected(cm: ConnectivityManager): Boolean {
        val activeNet = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(activeNet) ?: return false
        return caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    }

    private fun isMobileConnected(cm: ConnectivityManager): Boolean {
        val activeNet = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(activeNet) ?: return false
        return caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
    }

    override fun onDestroy() {
        super.onDestroy()
        connectivityManager.unregisterNetworkCallback(networkCallback)
        try {
            unregisterReceiver(screenReceiver)
        } catch (e: Exception) {
            // Safe ignore
        }
        scope.cancel()
        isServiceRunning = false
    }
}
