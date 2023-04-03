package com.common.ak

import android.content.Context
import android.view.View
import android.widget.Toast
import unified.vpn.sdk.*
import java.util.*

fun stopAkService(callback: (call: String) -> Unit) {
    UnifiedSdk.getInstance().vpn.stop(TrackingConstants.GprReasons.M_UI,
        object : CompletableCallback {
            override fun complete() {
                logout(callback)
            }

            override fun error(e: VpnException) {
                logout(callback)
            }
        })
}

private fun logout(callback: (call: String) -> Unit) {
    try {
        UnifiedSdk.getInstance().backend.logout(object : CompletableCallback {
            override fun complete() {
                callback.invoke("complete")
            }

            override fun error(e: VpnException) {
                callback.invoke("error")
                callback.invoke(e.toString())
            }
        })
    } catch (e: Exception) {
        e.printStackTrace()
        callback.invoke("error")
        callback.invoke(e.toString())
    }
}

fun Context.startAkService(
    vpnUrl: String,
    carrierId: String,
    appName: String,
    virtualLocation:String,
    callback: (call: Calling) -> Unit
) {
    UnifiedSdk.getVpnState(object : unified.vpn.sdk.Callback<VpnState> {
        override fun success(vpnState: VpnState) {
            if (vpnState == VpnState.CONNECTED) {
                callback.invoke(Calling.RESUME_CONNECTION)
            } else {
//                createNotificationChannel()
                callback.invoke(Calling.CONNECTING)
                val clientInfo = ClientInfo.newBuilder()
                    .addUrl(vpnUrl)
                    .carrierId(carrierId)
                    .build()
                val transportConfigList: MutableList<TransportConfig> = ArrayList()
                transportConfigList.add(HydraTransportConfig.create())
                transportConfigList.add(OpenVpnTransportConfig.tcp())
                transportConfigList.add(OpenVpnTransportConfig.udp())
                UnifiedSdk.update(transportConfigList, CompletableCallback.EMPTY)

//                val notificationConfig = SdkNotificationConfig.newBuilder()
//                    .title(appName)
//                    .channelId("vpn")
//                    .build()

//                UnifiedSdk.update(notificationConfig)

                val config = UnifiedSdkConfig.newBuilder().build()
                UnifiedSdk.getInstance(clientInfo, config)
                login(virtualLocation,callback)
            }
        }

        override fun failure(error: VpnException) {
            callback.invoke(Calling.INT_FAIL)
        }

    })
}

fun login(virtualLocation:String,callback: (call: Calling) -> Unit) {
    val authMethod = AuthMethod.anonymous()
    UnifiedSdk.getInstance().backend
        .login(authMethod, object : unified.vpn.sdk.Callback<User> {
            override fun success(p0: User) {
                connectVPN(virtualLocation,callback)
            }

            override fun failure(p0: VpnException) {
                callback.invoke(Calling.LOGIN_FAIL)
            }
        })
}

fun connectVPN(virtualLocation:String,callback: (call: Calling) -> Unit) {
    val fallbackOrder: MutableList<String> = java.util.ArrayList()
    fallbackOrder.add(HydraTransport.TRANSPORT_ID)
    fallbackOrder.add(OpenVpnTransportConfig.tcp().name)
    fallbackOrder.add(OpenVpnTransportConfig.udp().name)
    val bypassDomains: List<String> = LinkedList()
    UnifiedSdk.getInstance().vpn.start(SessionConfig.Builder()
        .withReason(TrackingConstants.GprReasons.M_UI)
        .withTransportFallback(fallbackOrder)
        .withTransport(HydraTransport.TRANSPORT_ID)
        .withVirtualLocation(
            virtualLocation
        )
        .addDnsRule(TrafficRule.Builder.bypass().fromDomains(bypassDomains))
        .build(), object : CompletableCallback {
        override fun complete() {
            callback.invoke(Calling.CONNECTED)
        }

        override fun error(e: VpnException) {
            callback.invoke(Calling.FAIL)
        }
    })
}


