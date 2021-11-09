package uz.pdp.messanger.fragments

import android.os.Bundle
import android.os.CountDownTimer
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
import uz.pdp.messanger.R
import uz.pdp.messanger.databinding.FragmentGroupBinding
import uz.pdp.messanger.databinding.FragmentSmsRecievBinding
import java.util.concurrent.TimeUnit

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class SmsRecievFragment : Fragment() {
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

    lateinit var binding: FragmentSmsRecievBinding
    lateinit var firebaseAuth:FirebaseAuth
    private  val TAG = "SmsRecievFragment"
    lateinit var storedVerificationId:String
    lateinit var resendToken:PhoneAuthProvider.ForceResendingToken
    lateinit var phoneNumber: String
     var min=60
    var time=false
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
       binding= FragmentSmsRecievBinding.inflate(inflater,container,false)
        firebaseAuth= FirebaseAuth.getInstance()
       phoneNumber=arguments?.getString("phone")!!
        sendVerificationCode(phoneNumber?:"")


        binding.next.setOnClickListener {
            var sms=binding.phone.text.toString()
            if(sms.length==6){
                var credential=PhoneAuthProvider.getCredential(storedVerificationId,sms)
                signInWithPhoneAuthCredential(credential,phoneNumber!!)
            }
        }
        binding.sendagain.setOnClickListener {
            if(time){
                resendCode(phoneNumber!!)

            } else{
                Toast.makeText(requireContext(), "Wait please!", Toast.LENGTH_SHORT).show()
            }

        }
        return binding.root



    }



    fun gettime(){
        binding.time.visibility=View.VISIBLE
        min=60
        var countDownTimer=object :CountDownTimer(61000,1000){
            override fun onTick(millisUntilFinished: Long) {
                min -= 1
                binding.time.text="00:$min"
            }

            override fun onFinish() {
                binding.time.text="00:00"
                binding.time.visibility=View.INVISIBLE
                time=true
            }

        }
        countDownTimer.start()
    }
    fun resendCode(phoneNumber: String){
        val options = PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber(phoneNumber)       // Phone number to verify
            .setTimeout(59L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(requireActivity())                 // Activity (for callback binding)
            .setCallbacks(callbacks)
            .setForceResendingToken(resendToken)// OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }
    fun sendVerificationCode(phoneNumber:String){
        val options = PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber(phoneNumber)       // Phone number to verify
            .setTimeout(59L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(requireActivity())                 // Activity (for callback binding)
            .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private val  callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            // This callback will be invoked in two situations:
            // 1 - Instant verification. In some cases the phone number can be instantly
            //     verified without needing to send or enter a verification code.
            // 2 - Auto-retrieval. On some devices Google Play services can automatically
            //     detect the incoming verification SMS and perform verification without
            //     user action.
            Log.d(TAG, "onVerificationCompleted:$credential")
            signInWithPhoneAuthCredential(credential,phoneNumber)

        }

        override fun onVerificationFailed(e: FirebaseException) {
            // This callback is invoked in an invalid request for verification is made,
            // for instance if the the phone number format is not valid.
            Log.w(TAG, "onVerificationFailed", e)

            if (e is FirebaseAuthInvalidCredentialsException) {
                // Invalid request
            } else if (e is FirebaseTooManyRequestsException) {
                // The SMS quota for the project has been exceeded
            }

            // Show a message and update the UI
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken
        ) {
            // The SMS verification code has been sent to the provided phone number, we
            // now need to ask the user to enter the code and then construct a credential
            // by combining the code with a verification ID.
            Log.d(TAG, "onCodeSent:$verificationId")
                      binding.tv.visibility=View.VISIBLE
            binding.tv.text="SMS $phoneNumber raqamiga jo’natildi"
                       gettime()

            // Save verification ID and resending token so we can use them later
            storedVerificationId = verificationId
            resendToken = token
        }
    }
    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential,phoneNumber: String) {
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                gettime()
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Toast.makeText(requireContext(), "Successful", Toast.LENGTH_SHORT).show()
                    var bundle=Bundle()
                    bundle.putString("phone",phoneNumber)
                    findNavController().navigate(R.id.createAccountFragment,bundle)
                    Log.d(TAG, "signInWithCredential:success")

                    val user = task.result?.user
                } else {
                    // Sign in failed, display a message and update the UI
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                    }
                    // Update UI
                }
            }
    }
    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SmsRecievFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}