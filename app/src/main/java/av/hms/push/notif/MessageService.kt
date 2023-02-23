package av.hms.push.notif

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.huawei.hms.push.HmsMessageService
import com.huawei.hms.push.RemoteMessage
import org.json.JSONObject
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

/**
 * Created by Antonio Vitiello on 13/02/2023.
 */
class MessageService : HmsMessageService() {
    private lateinit var idChannel: String
//    private val notificheRepository = NotificheRepository()

    companion object {
        const val TAG = "AAA"
        private const val PAYLOAD_KEY = "payload"
        private const val NOTIFICATION_ID_KEY = "id"
        const val KEY_EXTRA_INTENT_NOTIFICATION_ID = "notification id"
        private val atomicInteger = AtomicInteger()
    }

    override fun onCreate() {
        super.onCreate()

        //Create NotificationChannel and register the channel with the system
        idChannel = getString(R.string.default_notification_channel_id)
        createChannel(idChannel, getString(R.string.default_notification_channel_name))

        Log.d(TAG, "HMS_DEBUG onCreate!!")
    }

    private fun createChannel(channelId: String, channelName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                enableLights(true)
                //notificationChannel.lightColor = Color.RED
                enableVibration(true)
                description = getString(R.string.user_visible_channel_description)
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "HMS_DEBUG onMessageReceived!!")

        try {
            logNotification(remoteMessage)

            //eg. key-value pair is:  payload = {"dettaglio":false,"id":"26531700"}
            val payload: JSONObject = JSONObject(remoteMessage.dataOfMap[PAYLOAD_KEY] ?: "{}")
            Log.d(TAG, "HMS_DEBUG Push PAYLOAD: $payload")
            if (!payload.isNull(NOTIFICATION_ID_KEY)) {
                val notificationId = payload.getInt(NOTIFICATION_ID_KEY)
                Log.d(TAG, "HMS_DEBUG Push notification id: $notificationId")
            }

            //after parsing show notification in notification drawer (cassetto delle notifiche)
            val title = remoteMessage.notification?.title ?: getString(R.string.app_name)
            val body = remoteMessage.notification?.body ?: getString(R.string.default_notification_channel_name)
            val notificationId = atomicInteger.getAndIncrement()
            showNotification(title, body, notificationId)

        } catch (exc: Exception) {
            Log.e(TAG, "HMS_DEBUG Error: while decoding notification, remoteMessage:${remoteMessage.data}", exc)
        }
    }

    private fun logNotification(remoteMessage: RemoteMessage) {
        Log.d(TAG, "HMS_DEBUG onMessageReceived!!")
        try {
            Log.d(
                TAG,
                """HMS_DEBUG REMOTE MESSAGE DATA:
                    data:${remoteMessage.data}
                    from: ${remoteMessage.from}
                    to: ${remoteMessage.to}
                    messageId: ${remoteMessage.messageId}
                    sentTime: ${remoteMessage.sentTime}
                    dataMap: ${remoteMessage.dataOfMap}
                    messageType: ${remoteMessage.messageType}
                    ttl: ${remoteMessage.ttl}
                    token: ${remoteMessage.token}
                    analyticInfoMap: ${remoteMessage.analyticInfoMap}
                     """.trimIndent()
            )
            Log.d(
                TAG,
                """HMS_DEBUG NOTIFICATION DATA:
                     ImageUrl: ${remoteMessage.notification.imageUrl}
                     Title: ${remoteMessage.notification.title}
                     TitleLocalizationKey: ${remoteMessage.notification.titleLocalizationKey}
                     TitleLocalizationArgs: ${Arrays.toString(remoteMessage.notification.titleLocalizationArgs)}
                     Body: ${remoteMessage.notification.body}
                     BodyLocalizationKey: ${remoteMessage.notification.bodyLocalizationKey}
                     BodyLocalizationArgs: ${Arrays.toString(remoteMessage.notification.bodyLocalizationArgs)}
                     Icon: ${remoteMessage.notification.icon}
                     Sound: ${remoteMessage.notification.sound}
                     Tag: ${remoteMessage.notification.tag}
                     Color: ${remoteMessage.notification.color}
                     ClickAction: ${remoteMessage.notification.clickAction}
                     ChannelId: ${remoteMessage.notification.channelId}
                     Link: ${remoteMessage.notification.link}
                     NotifyId: ${remoteMessage.notification.notifyId}
                      """
            )
        } catch (exc: Exception) {
            Log.e(TAG, "HMS_DEBUG Error: while decoding notification, remoteMessage:${remoteMessage.data}", exc)
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "HMS_DEBUG onNewToken:\n[$token]")
    }

    private fun showNotification(title: String, messageBody: String, notificationId: Int) {

        //Create Notification Intent
        val contentIntent = Intent(this, NotificationTapActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra(KEY_EXTRA_INTENT_NOTIFICATION_ID, notificationId.toString())
        }

        //Create PendingIntent that will start a new activity
        val pendingFlags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val pendingIntent = PendingIntent.getActivity(this, notificationId, contentIntent, pendingFlags)
        val messageText = "$messageBody\n${notificationId}"

        //Create Notification for the specific channel_id and sets fields
        val builder = NotificationCompat.Builder(this, idChannel)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setSmallIcon(R.drawable.ic_push_notification)
            .setContentTitle(title)
            .setContentText(messageText)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setStyle(NotificationCompat.BigTextStyle().bigText(messageText))
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))

        //Colorized icon for Android 10 and above
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
            val purple700 = ContextCompat.getColor(this, R.color.purple_700)
            builder.apply {
                setSmallIcon(R.drawable.ic_push_notification)
                setColorized(true)
                color = purple700
            }
        }

        //Post notification to be shown in the status bar
        val notificationManager = NotificationManagerCompat.from(this)
        notificationManager.notify(notificationId, builder.build())
    }

    override fun onDestroy() {
        super.onDestroy()
        println("HMS_DEBUG onDestroy!!")
        Log.d(TAG, "HMS_DEBUG onDestroy!!")
    }

}