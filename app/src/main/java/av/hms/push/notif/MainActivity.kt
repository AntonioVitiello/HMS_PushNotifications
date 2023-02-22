package av.hms.push.notif

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.huawei.agconnect.AGConnectOptionsBuilder
import com.huawei.hms.aaid.HmsInstanceId
import com.huawei.hms.api.HuaweiApiAvailability
import com.huawei.hms.push.HmsMessaging
import com.huawei.hms.push.HmsMessaging.DEFAULT_TOKEN_SCOPE

class MainActivity : AppCompatActivity() {
    private var currentPushToken: String? = null

    companion object {
        const val TAG = "AAA"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        handleIntent(intent)
        logPushToken()
    }

    fun logPushToken() {
        object : Thread() {
            override fun run() {
                try {
                    // Obtain the app ID from the agconnect-services.json file
                    val appId = AGConnectOptionsBuilder().build(this@MainActivity).getString("client/app_id")
                    currentPushToken = HmsInstanceId.getInstance(this@MainActivity).getToken(appId, DEFAULT_TOKEN_SCOPE)
                    Log.d(TAG, "appId: $appId")
                    Log.d(TAG, "PushToken:\n[$currentPushToken]")
                } catch (e: Exception) {
                    Log.e(TAG, "getToken failed", e)
                }
            }
        }.start()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent != null) {
            handleIntent(intent)
        }
    }

    private fun handleIntent(intent: Intent) {
        intent.extras?.let { bundle ->
            val myKey = "antonio"
            val myValue = bundle.getString(myKey)
            Log.i(TAG, "PushNotification key-value data: [$myKey] -> [$myValue]")

            bundle.keySet()?.forEach { key ->
                val value = bundle.getString(key)
                Log.i(TAG, "PushNotification key-value data: [$key] -> [$value]")
            }
        }
    }

    fun deleteToken() {
        object : Thread() {
            override fun run() {
                try {
                    currentPushToken?.let { pushToken ->
                        HmsInstanceId.getInstance(this@MainActivity).deleteToken(pushToken)
                        currentPushToken = null
                    } ?: run {
                        // Obtain the app ID from the agconnect-services.json file
                        val appId = AGConnectOptionsBuilder().build(this@MainActivity).getString("client/app_id")
                        HmsInstanceId.getInstance(this@MainActivity).deleteToken(appId, DEFAULT_TOKEN_SCOPE)
                    }
                    Log.i(TAG, "PushToken deleted successfully")
                } catch (exc: Exception) {
                    Log.e(TAG, "Delete PushToken failed", exc)
                }
            }
        }.start()
    }

    /**
     * Disables the display of notification messages.
     * This method applies to notification messages but not data messages.
     * It is the app that determines whether to enable or disable data messaging.
     * This is useful for: Do not delete push tokens frequently!
     * https://developer.huawei.com/consumer/en/doc/development/HMSCore-References/hmsmessaging-0000001050255650#section943612533595
     * https://developer.huawei.com/consumer/en/doc/development/HMSCore-Guides/android-client-dev-0000001050042041
     */
    fun disableNotificationMessages() {
        HmsMessaging.getInstance(this).turnOffPush().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d(TAG, "turnOffPush successfully.")
            } else {
                Log.e(TAG, "turnOffPush failed.")
            }
        }
    }

    /**
     * Enables the display of notification messages.
     * This method applies to notification messages but not data messages.
     * It is the app that determines whether to enable or disable data messaging.
     * https://developer.huawei.com/consumer/en/doc/development/HMSCore-References/hmsmessaging-0000001050255650#section943612533595
     */
    fun enableNotificationMessages() {
        HmsMessaging.getInstance(this).turnOnPush().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d(TAG, "turnOnPush successfully.")
            } else {
                Log.e(TAG, "turnOnPush failed.")
            }
        }
    }

    override fun onDestroy() {
//        deleteToken()
        super.onDestroy()
    }

    fun isHmsAvailable(): Boolean {
        val resultCode = HuaweiApiAvailability.getInstance().isHuaweiMobileServicesAvailable(this)
        return com.huawei.hms.api.ConnectionResult.SUCCESS == resultCode
    }

    fun disableHmsPushIfAvailable() {
        if (isHmsAvailable()) {
            HmsMessaging.getInstance(this).isAutoInitEnabled = false
        }
    }

}