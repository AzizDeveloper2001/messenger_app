package uz.pdp.messanger.adapters

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
import uz.pdp.messanger.databinding.CallItemBinding
import uz.pdp.messanger.databinding.ChatcontactsrvItemBinding
import uz.pdp.messanger.models.Message
import uz.pdp.messanger.models.UserData
import java.text.SimpleDateFormat

class CallAdapter(var list:ArrayList<UserData>, var listener:CallAdapter.onItemClick):RecyclerView.Adapter<CallAdapter.Vh>() {
    private var simpleDateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm")
    var firebaseAuth= FirebaseAuth.getInstance()
    var firebaseDatabase= FirebaseDatabase.getInstance()
    inner class Vh(var itemRvBinding: CallItemBinding):
             RecyclerView.ViewHolder(itemRvBinding.root){
         fun onBind(userData: UserData,position: Int) {
             Glide.with(itemRvBinding.root.context).load(userData.photoUrl).apply(RequestOptions().placeholder(R.drawable.ic_man__1_)).into(itemRvBinding.profileimage)
             itemRvBinding.username.text="${userData.lastname} ${userData.firstname}"
             itemRvBinding.lasttext.text="${userData.phonenumber}"
             itemRvBinding.callbtn.setOnClickListener {
                 listener.OnItemClick(list[position],position)
             }

         }


     }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Vh {
        return Vh(CallItemBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun onBindViewHolder(holder: Vh, position: Int) {
        holder.onBind(list[position],position)
    }

    override fun getItemCount(): Int {
        return list.size
    }
    interface onItemClick{
        fun OnItemClick(userData: UserData,position: Int)

    }

    fun updatelist(calls:ArrayList<UserData>){
        list= ArrayList()
        list.addAll(calls)
        notifyDataSetChanged()
    }

}