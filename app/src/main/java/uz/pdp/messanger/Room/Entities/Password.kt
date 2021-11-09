package uz.pdp.messanger.Room.Entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Password (
    @PrimaryKey(autoGenerate = true)
    val id:Int=0,
    var word:String
        )