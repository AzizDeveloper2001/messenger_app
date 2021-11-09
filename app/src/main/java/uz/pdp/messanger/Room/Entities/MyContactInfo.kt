package uz.pdp.messanger.Room.Entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity
data class MyContactInfo(
    @PrimaryKey(autoGenerate = true)
    val id:Int=0,
    var firstname:String,
    var secondname:String,
    var phonenumber:String
): Serializable

