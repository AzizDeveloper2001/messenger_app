package uz.pdp.messanger.Services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import uz.pdp.messanger.R


class FirebaseNotificationService:FirebaseMessagingService() {
    private  val TAG = "FirebaseNotificationSer"
    override fun onMessageReceived(p0: RemoteMessage) {
        super.onMessageReceived(p0)
        val notificationManager=getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val builder=NotificationCompat.Builder(this,"channel_id")
        builder.setContentTitle("${p0.notification?.title}")
            .setContentText("${p0.notification?.body}")
            .setSmallIcon(R.mipmap.ic_launcher)
            val notification=builder.build()

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            val importance=NotificationManager.IMPORTANCE_DEFAULT
            val notificationChannel=NotificationChannel("channel_id","name",importance)
            notificationChannel.description="desc"

            notificationManager.createNotificationChannel(notificationChannel)

        }
        Log.d(TAG, "onMessageReceived: ${p0.notification?.title}")
        notificationManager.notify(1,notification)
    }

    override fun onNewToken(p0: String) {
         updateToken(p0)
        super.onNewToken(p0)
    }
    fun updateToken( token:String){
        var currentuser=FirebaseAuth.getInstance().currentUser
        var reference=FirebaseDatabase.getInstance().getReference("Tokens")

        reference.child(currentuser?.uid!!).setValue(token)

    }
}