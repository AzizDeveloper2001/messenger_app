package uz.pdp.messanger.Room.Dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import uz.pdp.messanger.Room.Entities.MyContactInfo

@Dao
interface MyContactDao {
    @Insert
    fun addContact(myContactInfo: MyContactInfo)
    @Query("select * from mycontactinfo")
    fun getAllContacts():List<MyContactInfo>
}