package av.hms.push.notif

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.huawei.agconnect.AGConnectOptionsBuilder
import com.huawei.hms.aaid.HmsInstanceId
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
                    val appId = AGConnectOptionsBuilder().build(this@MainActivity).getString("client/app_id")
//IQAAAACy0yxKAAAN997X2zLweXDY9gezcLGOqp4-Kcr8z5HIp4dflFU-cyDWylUHaeNYmZbprY0FvpqxLGBqbOFCEzlyAjthP-qCNnOKOVGEWJDTUQ
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
        if (currentPushToken != null) {
            HmsInstanceId.getInstance(this).deleteToken(currentPushToken)
            currentPushToken = null
            Log.d(TAG, "PushToken: DELETED!")
        }
    }

    override fun onDestroy() {
//        deleteToken()
        super.onDestroy()
    }

}