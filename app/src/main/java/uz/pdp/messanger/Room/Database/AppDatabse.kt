package uz.pdp.messanger.Room.Database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import uz.pdp.messanger.Room.Dao.MyContactDao
import uz.pdp.messanger.Room.Dao.PasswordDao
import uz.pdp.messanger.Room.Entities.MyContactInfo
import uz.pdp.messanger.Room.Entities.Password

@Database(entities = [MyContactInfo::class,Password::class],version = 1)
    abstract class AppDatabase:RoomDatabase() {
    abstract fun mycontactdao():MyContactDao
    abstract fun passworddao():PasswordDao
        companion object{
            private var appDatabase:AppDatabase?=null
            @Synchronized
            fun getInstance(context: Context):AppDatabase{
                if(appDatabase==null){
                    appDatabase= Room.databaseBuilder(context,AppDatabase::class.java,"my_db")
                        .fallbackToDestructiveMigration()
                        .allowMainThreadQueries()
                        .build()


                }
                return appDatabase!!
            }
        }
    }
