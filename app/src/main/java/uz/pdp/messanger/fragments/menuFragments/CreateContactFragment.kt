package uz.pdp.messanger.fragments.menuFragments

import android.os.Bundle
import android.provider.ContactsContract
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import uz.pdp.messanger.R
import uz.pdp.messanger.Room.Database.AppDatabase
import uz.pdp.messanger.Room.Entities.MyContactInfo
import uz.pdp.messanger.adapters.ChatconAdap
import uz.pdp.messanger.adapters.MultiSeclectionAdapter
import uz.pdp.messanger.databinding.FragmentCreateContactBinding
import uz.pdp.messanger.models.Contact
import uz.pdp.messanger.models.Message
import uz.pdp.messanger.models.UserData
import kotlin.math.ln

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class CreateContactFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    lateinit var binding:FragmentCreateContactBinding
    lateinit var appDatabase: AppDatabase
    lateinit var ref:DatabaseReference
    lateinit var firebaseDatabase: FirebaseDatabase
    lateinit var firebaseAuth: FirebaseAuth
    lateinit var multiSeclectionAdapter: MultiSeclectionAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= FragmentCreateContactBinding.inflate(inflater,container,false)
        firebaseAuth= FirebaseAuth.getInstance()
        firebaseDatabase= FirebaseDatabase.getInstance()
        ref=firebaseDatabase.getReference("Users")
        appDatabase= AppDatabase.getInstance(requireContext())
        binding.apply {
            save.setOnClickListener {
                if(firstname.text.isNotEmpty() && lastname.text.isNotEmpty() && phone.text.toString().length==9){
                   var number=code.text.toString()+phone.text.toString()
                    savecontact(firstname.text.toString(),lastname.text.toString(),number)
                }

            }
        }

        binding.back.setOnClickListener {
            findNavController().popBackStack()
        }

        return binding.root
    }

    private fun savecontact(fname:String,lname:String,phone:String) {
          ref.addListenerForSingleValueEvent(object :ValueEventListener{
              var f=false
              override fun onDataChange(snapshot: DataSnapshot) {
                 one@for (child in snapshot.children) {
                      var userData=child.getValue(UserData::class.java)
                      if(userData?.phonenumber==phone){
                          f=true
                          break@one
                      }

                  }
                  if(f){
                      appDatabase.mycontactdao().addContact(MyContactInfo(firstname = fname,secondname = lname,phonenumber = phone))

                      binding.firstname.setText("")
                      binding.lastname.setText("")
                      binding.phone.setText("")
                      Toast.makeText(requireContext(), "Successfully added!", Toast.LENGTH_SHORT).show()
                  } else {
                      Toast.makeText(requireContext(), "This account is not on Messenger", Toast.LENGTH_SHORT).show()
                  }
              }

              override fun onCancelled(error: DatabaseError) {
                  Toast.makeText(requireContext(), "${error.message}", Toast.LENGTH_SHORT).show()
              }

          })

    }



    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            CreateContactFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}