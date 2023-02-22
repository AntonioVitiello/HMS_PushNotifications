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