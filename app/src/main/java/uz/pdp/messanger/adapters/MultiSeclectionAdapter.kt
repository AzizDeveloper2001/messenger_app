package uz.pdp.messanger.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import uz.pdp.messanger.R
import uz.pdp.messanger.databinding.ChatcontactsrvItemBinding
import uz.pdp.messanger.models.Message
import uz.pdp.messanger.models.UserData
import java.text.SimpleDateFormat

class MultiSeclectionAdapter(var list:ArrayList<UserData>, var listener:MultiSeclectionAdapter.onItemClick):RecyclerView.Adapter<MultiSeclectionAdapter.Vh>() {
    private var simpleDateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm")
    var firebaseAuth= FirebaseAuth.getInstance()
    var firebaseDatabase= FirebaseDatabase.getInstance()
    inner class Vh(var itemRvBinding: ChatcontactsrvItemBinding):
             RecyclerView.ViewHolder(itemRvBinding.root){
         fun onBind(userData: UserData,position: Int) {
             itemView.setOnLongClickListener(object :View.OnLongClickListener{
                 override fun onLongClick(v: View?): Boolean {
                     listener.onItemLongClick(userData,position)
                     itemRvBinding.root.setBackgroundColor(Color.parseColor("#E5E5E5"))
                     return true
                 }

             })

             itemView.setOnClickListener {
                 listener.OnItemClick(list[position],position)
                 itemRvBinding.root.setBackgroundColor(Color.TRANSPARENT)
             }




             Glide.with(itemRvBinding.root.context).load(userData.photoUrl).into(itemRvBinding.profileimage)
             itemRvBinding.username.text="${userData.lastname} ${userData.firstname}"
             if(userData.on_of=="Online"){
                 itemRvBinding.onOff.setImageResource(R.drawable.onlineback)
             } else{
                 itemRvBinding.onOff.setImageResource(R.drawable.offlineback)
             }

             var r=firebaseDatabase.getReference("Message/${firebaseAuth.currentUser?.uid}/${userData.uid}")
             var count=0
             r.addValueEventListener(object : ValueEventListener {

                 override fun onDataChange(snapshot: DataSnapshot) {
                     count=0
                     for (child in snapshot.children) {
                         var mesage=child.getValue(Message::class.java)
                         if (mesage != null) {
                             if(mesage.from==firebaseAuth.currentUser?.uid){
                                itemRvBinding.checked.visibility=View.VISIBLE
                                 if(mesage.check=="true"){
                                     itemRvBinding.checked.setImageResource(R.drawable.ic_double_check)
                                 }else{
                                     itemRvBinding.checked.setImageResource(R.drawable.ic_baseline_check_24)
                                     count++
                                 }
                                itemRvBinding.count.visibility=View.GONE
                             }
                             else if(mesage.to==firebaseAuth.currentUser?.uid) {

                                 itemRvBinding.checked.visibility=View.GONE
                                if(mesage.check=="false"){
                                    count++
                                     itemRvBinding.count.visibility=View.VISIBLE
                                     itemRvBinding.count.text=count.toString()

                                 }


                             }
                             if(mesage.type=="text"){
                                 if(mesage.message?.length!!<12){
                                     itemRvBinding.lasttext.text=mesage.message
                                 } else {
                                     itemRvBinding.lasttext.text=mesage.message!!.substring(0,10)+"..."
                                 }
                             } else if(mesage.type=="audio"){
                                 itemRvBinding.lasttext.text="Audio message"
                             } else if(mesage.type=="image"){
                                 itemRvBinding.lasttext.text="Image message"
                             }

                             var format=simpleDateFormat.format(mesage.date)
                             itemRvBinding.sana.text=format
                         }


                     }


                 }

                 override fun onCancelled(error: DatabaseError) {

                 }

             })
         }


     }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Vh {
        return Vh(ChatcontactsrvItemBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun onBindViewHolder(holder: Vh, position: Int) {
        holder.onBind(list[position],position)
    }

    override fun getItemCount(): Int {
        return list.size
    }
    interface onItemClick{
        fun OnItemClick(userData: UserData,position: Int)
        fun onItemLongClick(userData: UserData,position: Int):ArrayList<String>

    }
}