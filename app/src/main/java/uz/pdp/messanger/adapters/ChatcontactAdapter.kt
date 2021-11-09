package uz.pdp.messanger.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import uz.pdp.messanger.R
import uz.pdp.messanger.databinding.ChatcontactsrvItemBinding
import uz.pdp.messanger.models.UserData

class ChatcontactAdapter(var list:ArrayList<UserData>,var listener:ChatcontactAdapter.onItemClick):RecyclerView.Adapter<ChatcontactAdapter.Vh>() {
     inner class Vh(var itemRvBinding: ChatcontactsrvItemBinding):
             RecyclerView.ViewHolder(itemRvBinding.root){
         fun onBind(userData: UserData,position: Int) {
             Glide.with(itemRvBinding.root.context).load(userData.photoUrl).into(itemRvBinding.profileimage)
             itemRvBinding.username.text="${userData.lastname} ${userData.firstname}"
             if(userData.on_of=="Online"){
                 itemRvBinding.onOff.setImageResource(R.drawable.onlineback)
             } else{
                 itemRvBinding.onOff.setImageResource(R.drawable.offlineback)
             }
             itemView.setOnClickListener {
                 listener.OnItemClick(list[position],position)
             }
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

    }
}