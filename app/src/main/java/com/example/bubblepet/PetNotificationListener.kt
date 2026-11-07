package com.example.bubblepet
import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
class PetNotificationListener : NotificationListenerService() {
 override fun onNotificationPosted(sbn: StatusBarNotification) {
  if (sbn.packageName != "com.kakao.talk") return
  val extras = sbn.notification.extras
  val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString().orEmpty()
  val body = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString().orEmpty()
  if (body.isBlank()) return
  PetOverlay.show(applicationContext, "$title: $body".take(120))
 }
}