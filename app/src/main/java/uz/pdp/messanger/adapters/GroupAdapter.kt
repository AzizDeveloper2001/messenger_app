package uz.pdp.messanger.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import uz.pdp.messanger.R
import uz.pdp.messanger.databinding.ChatcontactsrvItemBinding
import uz.pdp.messanger.models.Groupmessage
import uz.pdp.messanger.models.Groups
import uz.pdp.messanger.models.Message
import uz.pdp.messanger.models.UserData
import java.text.SimpleDateFormat

class GroupAdapter(var list:ArrayList<Groups>, var listener:GroupAdapter.onItemClick):RecyclerView.Adapter<GroupAdapter.Vh>() {
    private var simpleDateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm")
    var firebaseAuth= FirebaseAuth.getInstance()
    var firebaseDatabase= FirebaseDatabase.getInstance()
    inner class Vh(var itemRvBinding: ChatcontactsrvItemBinding):
             RecyclerView.ViewHolder(itemRvBinding.root){
         fun onBind(group: Groups, position: Int) {
             Glide.with(itemRvBinding.root.context).load(group.groupimage).apply(RequestOptions().placeholder(R.drawable.ic_man__1_)).into(itemRvBinding.profileimage)
             itemRvBinding.username.text="${group.groupname}"
            itemRvBinding.onOff.visibility=View.INVISIBLE
             itemView.setOnClickListener {
                 listener.OnItemClick(list[position],position)
             }
             var r=firebaseDatabase.getReference("Groups/${group.key}/groupmessages/")
             var count=0
             r.addListenerForSingleValueEvent(object : ValueEventListener {

                 override fun onDataChange(snapshot: DataSnapshot) {
                     count=0
                     for (child in snapshot.children) {
                         var mesage=child.getValue(Groupmessage::class.java)
                         if (mesage != null) {
                             if(mesage.sendUser?.uid==firebaseAuth.currentUser?.uid){
                                itemRvBinding.checked.visibility=View.VISIBLE
                                 if(mesage.check=="true"){
                                     itemRvBinding.checked.setImageResource(R.drawable.ic_double_check)
                                 }else{
                                     itemRvBinding.checked.setImageResource(R.drawable.ic_baseline_check_24)
                                     count++
                                 }
                                itemRvBinding.count.visibility=View.GONE
                             }
                             else {

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
                             var a=format.substring(0,2)
                             var d=System.currentTimeMillis()
                             var f=simpleDateFormat.format(d)
                             var b=f.substring(0,2)
                             if(b.toInt()-a.toInt()==0){
                                 itemRvBinding.sana.text=format.substring(11)
                             } else if(b.toInt()-a.toInt()==1) {
                                 itemRvBinding.sana.text = "kecha"
                             } else {
                                 itemRvBinding.sana.text=format.substring(0,10)
                             }
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
        fun OnItemClick(userData: Groups,position: Int)

    }
    @SuppressLint("NotifyDataSetChanged")
    fun updatelist(grouplist:ArrayList<Groups>){
        list= ArrayList()
        list.addAll(grouplist)
        notifyDataSetChanged()
    }
}