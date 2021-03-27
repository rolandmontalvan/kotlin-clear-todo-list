package douzifly.list.notification

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.support.v7.app.NotificationCompat
import douzifly.list.R
import douzifly.list.ui.home.MainActivity
import douzifly.list.utils.logd

/**
 * Created by air on 15/10/20.
 */
object ClearNotification {

    val EXTRA_THING_ID = "thingId"

    fun show(context: Context, title: String, color: Int, id: Int) {
        val intent = Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setClass(context, MainActivity::class.java);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        intent.putExtra(EXTRA_THING_ID, id)
        "pending intent thingid: $id".logd("MainActivity")
        val pi = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT)
        val notification = NotificationCompat.Builder(context)
                .setColor(color)
                .setContentText(title)
                .setContentTitle(context.resources.getString(R.string.reminder))
                .setSmallIcon(R.drawable.ic_small_icon)
                .setAutoCancel(true)
                .setContentIntent(pi)
                .setDefaults(Notification.DEFAULT_ALL)
                .build()
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(id, notification)
    }

}