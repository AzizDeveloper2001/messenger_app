package uz.pdp.messanger

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.hanks.passcodeview.PasscodeView
import uz.pdp.messanger.Room.Database.AppDatabase
import uz.pdp.messanger.databinding.ActivityPasscodeBinding

class PasscodeActivity : AppCompatActivity() {
    lateinit var binding:ActivityPasscodeBinding
    lateinit var appDatabase: AppDatabase
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityPasscodeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        appDatabase= AppDatabase.getInstance(this)
        if(appDatabase.passworddao().allPasswords().isEmpty()){
            val intent=Intent(this@PasscodeActivity,MainActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            val p=appDatabase.passworddao().allPasswords()[0].word
            binding.passcodeView.setLocalPasscode(p).setListener(object :PasscodeView.PasscodeViewListener{
                override fun onFail() {
                    Toast.makeText(this@PasscodeActivity, "Wrong password", Toast.LENGTH_SHORT).show()
                }

                override fun onSuccess(number: String?) {
                    val intent=Intent(this@PasscodeActivity,MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }

            })
        }


    }
}