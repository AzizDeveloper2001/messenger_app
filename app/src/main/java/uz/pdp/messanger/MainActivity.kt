package uz.pdp.messanger

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import uz.pdp.messanger.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    lateinit var binding:ActivityMainBinding
    lateinit var navController:NavController
    lateinit var reference: DatabaseReference
    lateinit var firebaseDatabase:FirebaseDatabase
    lateinit var firebaseAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseAuth= FirebaseAuth.getInstance()
        firebaseDatabase= FirebaseDatabase.getInstance()
        navController=findNavController(R.id.my_nav)

binding.bottommenu.itemIconTintList=null
       binding.bottommenu.setupWithNavController(navController)


        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.chatContactsFragment -> {showBottomNav()}
                R.id.groupFragment -> showBottomNav()
                R.id.chennelFragment -> showBottomNav()
                R.id.menuFragment -> showBottomNav()
                else -> hideBottomNav()
            }
        }
    }


    override fun onResume() {
        super.onResume()
        reference=firebaseDatabase.getReference("Users/${firebaseAuth.currentUser?.uid}/on_of")
        reference.setValue("Online")
    }

    override fun onStop() {
        super.onStop()
        reference.setValue(System.currentTimeMillis().toString())
    }
    private fun showBottomNav() {
        binding.bottommenu.visibility = View.VISIBLE

    }

    private fun hideBottomNav() {
        binding.bottommenu.visibility = View.GONE

    }

    override fun onSupportNavigateUp(): Boolean {
        navController.navigateUp()
        return super.onSupportNavigateUp()
    }
}