package com.igent.mobile.telemetry

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Log
import com.igent.mobile.data.preferences.PreferencesDataSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.net.HttpURLConnection
import java.net.NetworkInterface
import java.net.URL
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TelemetryDiscoveryManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferencesDataSource: PreferencesDataSource
) {
    private val TAG = "TelemetryDiscoveryManager"
    private val nsdManager = context.getSystemService(Context.NSD_SERVICE) as? NsdManager
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _discoveryStatus = MutableStateFlow("Idle")
    val discoveryStatus: StateFlow<String> = _discoveryStatus.asStateFlow()

    private val isDiscovering = AtomicBoolean(false)
    private var discoveryListener: NsdManager.DiscoveryListener? = null

    companion object {
        const val SERVICE_TYPE = "_igent-telemetry._tcp."
    }

    fun startDiscovery() {
        if (!isDiscovering.compareAndSet(false, true)) return
        if (nsdManager == null) {
            Log.w(TAG, "NsdManager not available, running fallback subnet scan")
            scope.launch { scanFallbackSubnets() }
            return
        }

        _discoveryStatus.value = "Discovering via mDNS..."
        discoveryListener = object : NsdManager.DiscoveryListener {
            override fun onDiscoveryStarted(regType: String) {
                Log.d(TAG, "mDNS discovery started for type: $regType")
            }

            override fun onServiceFound(service: NsdServiceInfo) {
                Log.d(TAG, "mDNS service found: ${service.serviceName}")
                if (service.serviceType.contains("igent-telemetry")) {
                    try {
                        nsdManager.resolveService(service, object : NsdManager.ResolveListener {
                            override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                                Log.w(TAG, "mDNS resolve failed: errorCode=$errorCode")
                            }

                            override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
                                Log.d(TAG, "mDNS service resolved: ${serviceInfo.host}:${serviceInfo.port}")
                                val host = serviceInfo.host?.hostAddress ?: return
                                val port = serviceInfo.port
                                val url = "http://$host:$port/"
                                scope.launch {
                                    preferencesDataSource.saveTelemetryBaseUrl(url)
                                    _discoveryStatus.value = "Discovered via mDNS: $url"
                                }
                            }
                        })
                    } catch (e: Exception) {
                        Log.e(TAG, "Error resolving mDNS service", e)
                    }
                }
            }

            override fun onServiceLost(service: NsdServiceInfo) {
                Log.d(TAG, "mDNS service lost: ${service.serviceName}")
            }

            override fun onDiscoveryStopped(serviceType: String) {
                Log.d(TAG, "mDNS discovery stopped: $serviceType")
                isDiscovering.set(false)
            }

            override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
                Log.e(TAG, "mDNS start discovery failed: errorCode=$errorCode")
                try {
                    nsdManager.stopServiceDiscovery(this)
                } catch (ignored: Exception) {}
                isDiscovering.set(false)
                scope.launch { scanFallbackSubnets() }
            }

            override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
                Log.e(TAG, "mDNS stop discovery failed: errorCode=$errorCode")
                isDiscovering.set(false)
            }
        }

        try {
            nsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener)
            
            // Timeout after 4 seconds to trigger fallback subnet scanning if no server found
            scope.launch {
                delay(4000L)
                if (_discoveryStatus.value.startsWith("Discovering")) {
                    stopDiscovery()
                    scanFallbackSubnets()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception starting mDNS discovery", e)
            isDiscovering.set(false)
            scope.launch { scanFallbackSubnets() }
        }
    }

    fun stopDiscovery() {
        try {
            discoveryListener?.let {
                nsdManager?.stopServiceDiscovery(it)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping discovery", e)
        } finally {
            discoveryListener = null
            isDiscovering.set(false)
        }
    }

    suspend fun scanFallbackSubnets() {
        _discoveryStatus.value = "Scanning local subnets & bootstrap URLs..."
        
        // 1. Check bootstrap / default URLs first
        val bootstrapUrls = listOf(
            "http://10.0.2.2:3000/",
            "http://localhost:3000/",
            "http://10.0.1.1:3000/"
        )
        for (urlStr in bootstrapUrls) {
            if (testServerUrl(urlStr)) {
                preferencesDataSource.saveTelemetryBaseUrl(urlStr)
                _discoveryStatus.value = "Discovered via bootstrap: $urlStr"
                return
            }
        }

        // 2. Scan device local subnet
        val localIp = getLocalIpAddress()
        if (localIp != null) {
            val subnetPrefix = localIp.substringBeforeLast(".") + "."
            Log.d(TAG, "Scanning subnet: ${subnetPrefix}x on port 3000")
            
            val deferreds = (1..254).map { i ->
                scope.async(Dispatchers.IO) {
                    val candidateUrl = "http://$subnetPrefix$i:3000/"
                    if (testServerUrl(candidateUrl)) {
                        candidateUrl
                    } else {
                        null
                    }
                }
            }

            val foundUrl = deferreds.awaitAll().filterNotNull().firstOrNull()
            if (foundUrl != null) {
                preferencesDataSource.saveTelemetryBaseUrl(foundUrl)
                _discoveryStatus.value = "Discovered via subnet scan: $foundUrl"
                return
            }
        }

        _discoveryStatus.value = "Fallback discovery completed (Server not found)"
    }

    private fun testServerUrl(urlStr: String): Boolean {
        return try {
            val url = URL(urlStr)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 400
                readTimeout = 400
                requestMethod = "GET"
            }
            val responseCode = connection.responseCode
            connection.disconnect()
            responseCode in 200..499
        } catch (e: Exception) {
            false
        }
    }

    private fun getLocalIpAddress(): String? {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val intf = interfaces.nextElement()
                val addrs = intf.inetAddresses
                while (addrs.hasMoreElements()) {
                    val addr = addrs.nextElement()
                    if (!addr.isLoopbackAddress && addr is java.net.Inet4Address) {
                        val hostAddress = addr.hostAddress
                        if (hostAddress != null && !hostAddress.startsWith("169.254")) {
                            return hostAddress
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting local IP", e)
        }
        return null
    }
}
