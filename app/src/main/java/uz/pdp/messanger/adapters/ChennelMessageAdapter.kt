package uz.pdp.messanger.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import uz.pdp.messanger.R
import uz.pdp.messanger.databinding.SendFromMeBinding
import uz.pdp.messanger.databinding.SendmeBinding
import uz.pdp.messanger.models.Chennel
import uz.pdp.messanger.models.ChennelMessage
import uz.pdp.messanger.models.Message
import uz.pdp.messanger.models.UserData
import java.text.SimpleDateFormat

class ChennelMessageAdapter(var list: ArrayList<ChennelMessage>, var currentuserid:String, var chennel: Chennel):RecyclerView.Adapter<RecyclerView.ViewHolder>(){
    private var simpleDateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm")
    var firebaseDatabase=FirebaseDatabase.getInstance()
    lateinit var ref:DatabaseReference
    var firebaseAuth=FirebaseAuth.getInstance()
    inner class Sendtext(var itemRvBinding: SendmeBinding):
             RecyclerView.ViewHolder(itemRvBinding.root){
         fun onBind(message: ChennelMessage) {
             if(message.type=="text"){
                 itemRvBinding.image.visibility=View.GONE
                 itemRvBinding.audio.visibility=View.GONE
                 itemRvBinding.text.visibility=View.VISIBLE
                 itemRvBinding.messagetext.text = message.message
                 val format = simpleDateFormat.format(message.date)
                 itemRvBinding.date.text = format.substring(11)
                 if(message.check=="true"){
                     itemRvBinding.check.setImageResource(R.drawable.ic_double_check)
                 } else {
                     itemRvBinding.check.setImageResource(R.drawable.ic_baseline_check_24)
                 }

             } else if(message.type=="audio"){
                 itemRvBinding.image.visibility=View.GONE
                 itemRvBinding.audio.visibility=View.VISIBLE
                 itemRvBinding.text.visibility=View.GONE
                 val format = simpleDateFormat.format(message.date)
                 itemRvBinding.dateaudi.text = format.substring(11)
                 if(message.check=="true"){
                     itemRvBinding.checkaudio.setImageResource(R.drawable.ic_double_check)
                 } else {
                     itemRvBinding.checkaudio.setImageResource(R.drawable.ic_baseline_check_24)
                 }
                 itemRvBinding.voicePlayerView.setAudio(message.message)
             } else if(message.type=="image"){
                 itemRvBinding.image.visibility=View.VISIBLE
                 itemRvBinding.audio.visibility=View.GONE
                 itemRvBinding.text.visibility=View.GONE
                 val format = simpleDateFormat.format(message.date)
                 itemRvBinding.dateimage.text = format.substring(11)
                 if(message.check=="true"){
                     itemRvBinding.checkimage.setImageResource(R.drawable.ic_double_check)
                 } else {
                     itemRvBinding.checkimage.setImageResource(R.drawable.ic_baseline_check_24)
                 }
                 Picasso.get().load(message.message).into(itemRvBinding.placeholder)
             }

//             ref=firebaseDatabase.getReference("Message/${userData.uid}/${firebaseAuth.currentUser?.uid}/${message.key}/check")
//             ref.setValue("true")
//

         }

     }
     inner class Recievetext(var itemRvBinding: SendFromMeBinding):
             RecyclerView.ViewHolder(itemRvBinding.root){
         fun onBind(message: ChennelMessage) {

             ref=firebaseDatabase.getReference("Chennels/${chennel.key}/chennelmessages/${message.key}/check")
             ref.setValue("true")


             if(message.type=="text"){
                 itemRvBinding.image.visibility=View.GONE
                 itemRvBinding.audio.visibility=View.GONE
                 itemRvBinding.text.visibility=View.VISIBLE
                 itemRvBinding.messagetext.text = message.message
                 val format = simpleDateFormat.format(message.date)
                 itemRvBinding.date.text = format.substring(11)

             } else if(message.type=="audio"){
                 itemRvBinding.image.visibility=View.GONE
                 itemRvBinding.audio.visibility=View.VISIBLE
                 itemRvBinding.text.visibility=View.GONE
                itemRvBinding.voicePlayerView.setAudio(message.message)
                 val format = simpleDateFormat.format(message.date)
                 itemRvBinding.dateaudio.text = format.substring(11)
             }
             else if(message.type=="image"){
//                 val circularProgressDrawable = CircularProgressDrawable(itemRvBinding.root.context)
//                 circularProgressDrawable.strokeWidth = 5f
//                 circularProgressDrawable.centerRadius = 30f
//                 circularProgressDrawable.start()

                 itemRvBinding.image.visibility=View.VISIBLE
                 itemRvBinding.audio.visibility=View.GONE
                 itemRvBinding.text.visibility=View.GONE
                 val format = simpleDateFormat.format(message.date)
                 itemRvBinding.dateimage.text = format.substring(11)
                 Glide.with(itemRvBinding.root.context)
                     .load(message.message)
                     .apply(
                         RequestOptions().placeholder(R.drawable.placeholder))
                     .into(itemRvBinding.placeholder)
             }
         }

             }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == 0) {
            return Sendtext(
                SendmeBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        } else {
            return Recievetext(
                SendFromMeBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is Sendtext) {
            holder.onBind(list[position])
        } else {
            val toVh = holder as Recievetext
            toVh.onBind(list[position])
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun getItemViewType(position: Int): Int {
        return if(list[position].sendUser?.uid==currentuserid){
            0
        } else 1
    }

}