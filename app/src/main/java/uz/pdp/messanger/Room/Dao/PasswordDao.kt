package uz.pdp.messanger.Room.Dao

import androidx.room.*
import uz.pdp.messanger.Room.Entities.Password
@Dao
interface PasswordDao {
    @Insert
   fun  addPassword(password:Password)
   @Query("select * from password")
   fun allPasswords():List<Password>
   @Update
   fun updatePassword(password: Password)
   @Delete
   fun deletePassword(password: Password)
}