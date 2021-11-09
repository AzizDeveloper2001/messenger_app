package uz.pdp.messanger.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.database.*
import uz.pdp.messanger.MainActivity
import uz.pdp.messanger.PasscodeActivity
import uz.pdp.messanger.R
import uz.pdp.messanger.databinding.FragmentAuthenticationBinding
import uz.pdp.messanger.models.UserData
import java.util.concurrent.TimeUnit

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class AuthenticationFragment : Fragment() {
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

    lateinit var binding: FragmentAuthenticationBinding
    lateinit var firebaseAuth: FirebaseAuth
    lateinit var firebaseDatabase: FirebaseDatabase
    lateinit var ref:DatabaseReference
    private  val TAG = "SmsRecievFragment"
    lateinit var storedVerificationId:String
    lateinit var resendToken:PhoneAuthProvider.ForceResendingToken

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= FragmentAuthenticationBinding.inflate(inflater,container,false)
        firebaseAuth= FirebaseAuth.getInstance()
//        firebaseDatabase= FirebaseDatabase.getInstance()
//        ref=firebaseDatabase.getReference("Users")

        if(firebaseAuth.currentUser!=null){
            val intent=Intent(requireContext(),PasscodeActivity::class.java)
                            startActivity(intent)
                            requireActivity().finish()
        }
//            ref.addValueEventListener(object :ValueEventListener{
//                override fun onDataChange(snapshot: DataSnapshot) {
//                    for (child in snapshot.children) {
//                       var userData= child.getValue(UserData::class.java)
//                        if( firebaseAuth.currentUser!=null && userData?.uid==firebaseAuth.currentUser?.uid){
//                            val intent=Intent(requireContext(),MainActivity::class.java)
//                            startActivity(intent)
//                            requireActivity().finish()
//                        }
//
//                    }
//                }
//
//                override fun onCancelled(error: DatabaseError) {
//                    Toast.makeText(requireContext(), "${error.message}", Toast.LENGTH_SHORT).show()
//                }
//
//            })


        binding.apply {
            next.setOnClickListener {
                if(binding.phone.text.toString().length==9){
                    var code= binding.code.text
                    var number=binding.phone.text.toString()
                    var phonenumber="$code$number"
                    var bundle=Bundle()
                    bundle.putString("phone",phonenumber)
                    findNavController().navigate(R.id.smsRecievFragment,bundle)
                } else {
                    Toast.makeText(requireContext(), "Wrong number", Toast.LENGTH_SHORT).show()
                }

            }
            googlesignIn.setOnClickListener {
                findNavController().navigate(R.id.googleSignInFragment)
            }
        }
        return binding.root
    }

    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AuthenticationFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}