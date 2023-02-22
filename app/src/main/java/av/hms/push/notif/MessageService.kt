package av.hms.push.notif

import android.util.Log
import com.huawei.hms.push.HmsMessageService
import com.huawei.hms.push.RemoteMessage
import java.util.*

/**
 * Created by Antonio Vitiello on 13/02/2023.
 */
class MessageService : HmsMessageService() {
    companion object {
        const val TAG = "AAA"
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // Check if message contains a notification payload.
        try {

            Log.d(TAG, "onMessageReceived: $remoteMessage")
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
            Log.e(TAG, "HMS_Error: while decoding notification, remoteMessage:${remoteMessage.data}", exc)
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "onNewToken:\n[$token]")
    }

}