package uz.pdp.messanger.fragments

import android.Manifest
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.navigation.fragment.findNavController
import com.github.florent37.runtimepermission.kotlin.askPermission
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.SuccessContinuation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import uz.pdp.messanger.MainActivity
import uz.pdp.messanger.PasscodeActivity
import uz.pdp.messanger.R
import uz.pdp.messanger.databinding.FragmentCreateAccountBinding
import uz.pdp.messanger.databinding.FragmentGoogleSignInBinding
import uz.pdp.messanger.models.UserData
import java.io.File
import java.io.FileOutputStream

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class CreateAccountFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    lateinit var binding: FragmentCreateAccountBinding
     var uri:Uri?=null
    lateinit var firebaseAuth: FirebaseAuth
    lateinit var firebasedatabase: FirebaseDatabase
    lateinit var reference: DatabaseReference
    lateinit var storage: FirebaseStorage
    lateinit var refstorage: StorageReference
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
       binding= FragmentCreateAccountBinding.inflate(inflater,container, false)
        firebaseAuth= FirebaseAuth.getInstance()
        firebasedatabase= FirebaseDatabase.getInstance()
        storage= FirebaseStorage.getInstance()
        reference=firebasedatabase.getReference("Users")
        var phonenumber=arguments?.getString("phone")
        binding.apply {
            image.setOnClickListener {
                getPermisssion()
            }
            save.setOnClickListener {
                if(uri!=null && firstname.text.isNotEmpty() && lastname.text.isNotEmpty()){
                    var fname=firstname.text.toString()
                    var lname=lastname.text.toString()

                    var progressDialog= ProgressDialog(requireContext())
                    progressDialog.setCancelable(false)
                    progressDialog.setMessage("Account is creating...")
                    progressDialog.show()
                    var imageurl="UserProfileImages/"+firebaseAuth.currentUser?.uid
                      refstorage=storage.getReference(imageurl)
                    refstorage.putFile(uri!!).addOnSuccessListener(object :OnSuccessListener<UploadTask.TaskSnapshot>{
                        override fun onSuccess(p0: UploadTask.TaskSnapshot?) {
                            var task=p0?.metadata?.reference?.downloadUrl
                            task?.addOnSuccessListener(object : OnSuccessListener<Uri>{
                                override fun onSuccess(p0: Uri?) {
                                    var photoUrl=p0.toString()
                                    var userData=UserData(photoUrl,fname,lname,firebaseAuth.currentUser!!.uid,phonenumber,"online")
                                    reference.child(firebaseAuth.currentUser!!.uid).setValue(userData).addOnCompleteListener {
                                        if(it.isSuccessful){
                                            progressDialog.dismiss()
                                            val intent=Intent(requireContext(),PasscodeActivity::class.java)
                                            startActivity(intent)
                                            requireActivity().finish()

                                        }
                                        else {
                                            Toast.makeText(
                                                requireContext(),
                                                it.exception?.message,
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                }

                            })
                        }

                    })

                }
            }
        }
        binding.back.setOnClickListener {
            findNavController().popBackStack()
        }
        return binding.root
    }




    private fun getPermisssion() {
        askPermission(
            Manifest.permission.READ_EXTERNAL_STORAGE){
            getImage.launch("image/*")
        }.onDeclined {
            if(it.hasDenied()){
                val dialog= AlertDialog.Builder(requireContext())
                dialog.setMessage("Ushbu dasturdan foydalanish uchun berilgan so'rovlarga ruxsat berishingiz kerak")
                dialog.setCancelable(false)
                dialog.setPositiveButton("Ruxsat berish", DialogInterface.OnClickListener { dialog, which ->
                    dialog.dismiss()
                    it.askAgain()
                })
                dialog.show()

            } else if(it.hasForeverDenied()){
                val dialog= AlertDialog.Builder(requireContext())

                dialog.setMessage("Dasturdan foydalanish uchun  sozlamalar bo'limidan barcha so'rovlarga ruxsat berishingiz kerak")
                dialog.setPositiveButton("Ok",object : DialogInterface.OnClickListener{
                    override fun onClick(dialog: DialogInterface?, which: Int) {
                        dialog?.dismiss()
                        it.goToSettings()
                    }

                })
                dialog.show()
            }
        }
    }


    private var getImage=
        registerForActivityResult(ActivityResultContracts.GetContent()){ it->

            if(it!=null){
                binding.image.setImageURI(it)
                uri=it


            }

        }
    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            CreateAccountFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}