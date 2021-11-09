package uz.pdp.messanger.fragments.menuFragments

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.shrikanthravi.library.NightModeButton
import kotlinx.coroutines.currentCoroutineContext
import uz.pdp.messanger.PasscodeActivity
import uz.pdp.messanger.R
import uz.pdp.messanger.RegisterActivity
import uz.pdp.messanger.Room.Database.AppDatabase
import uz.pdp.messanger.Room.Entities.Password
import uz.pdp.messanger.databinding.EditprofildialogBinding
import uz.pdp.messanger.databinding.FragmentSettingsBinding
import uz.pdp.messanger.databinding.SetpasswordDialogBinding
import uz.pdp.messanger.models.Message
import uz.pdp.messanger.models.UserData


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class SettingsFragment : Fragment() {

    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    lateinit var binding:FragmentSettingsBinding
    lateinit var firebaseAuth: FirebaseAuth
    lateinit var firebaseDatabase: FirebaseDatabase
    lateinit var database: DatabaseReference
    lateinit var appDatabase: AppDatabase
    lateinit var firebaseStorage: FirebaseStorage
    lateinit var storage:StorageReference
    lateinit var imageView:ImageView
    var photourl=""
    lateinit var currentuser:UserData
    var uri:Uri?=null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= FragmentSettingsBinding.inflate(inflater,container,false)
        appDatabase= AppDatabase.getInstance(requireContext())

        firebaseAuth= FirebaseAuth.getInstance()
        firebaseDatabase= FirebaseDatabase.getInstance()
        firebaseStorage= FirebaseStorage.getInstance()
        database=firebaseDatabase.getReference("Users")
        binding.apply {
            logout.setOnClickListener {
                var dialog=AlertDialog.Builder(requireContext())
                dialog.setMessage("Bu  sizning akoutingiz o'chirilishiga sabab bo'ladi")
                dialog.setPositiveButton("ok",object :DialogInterface.OnClickListener{
                    override fun onClick(dialog: DialogInterface?, which: Int) {

                        database.addListenerForSingleValueEvent(object :ValueEventListener{
                            override fun onDataChange(snapshot: DataSnapshot) {
                                for (child in snapshot.children) {
                                  var userData=  child.getValue(UserData::class.java)
                                    if(userData?.uid==firebaseAuth.currentUser?.uid){
                                      var   reference=firebaseDatabase.getReference("Users/${firebaseAuth.currentUser?.uid}/")
                                        userData?.on_of="delete"
                                        userData?.firstname="Delete Account"
                                        userData?.lastname=""
                                        reference.setValue(userData)
                                        firebaseAuth.signOut()
                                        if(appDatabase.passworddao().allPasswords().isNotEmpty()){
                                            appDatabase.passworddao().deletePassword(appDatabase.passworddao().allPasswords()[0])
                                            Toast.makeText(requireContext(), "Password successfully disabled", Toast.LENGTH_SHORT).show()
                                        }
                                        var intent= Intent(requireContext(),RegisterActivity::class.java)
                                        startActivity(intent)
                                        requireActivity()?.finish()
                                        break
                                    }


                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Toast.makeText(requireContext(), "${error.message}", Toast.LENGTH_SHORT).show()
                            }

                        })


                    }

                })
                dialog.setNegativeButton("No"
                ) { dialog, _ -> dialog.dismiss() }
                dialog.show()
            }







            passcode.setOnClickListener {
                setpasscode()
            }


            dis.setOnClickListener {
                if(appDatabase.passworddao().allPasswords().isNotEmpty()){
                    appDatabase.passworddao().deletePassword(appDatabase.passworddao().allPasswords()[0])
                    Toast.makeText(requireContext(), "Password successfully disabled", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "No password to disable", Toast.LENGTH_SHORT).show()
                }

            }
        }
        return binding.root
    }

//    private fun editprofile() {
//        val dialog= androidx.appcompat.app.AlertDialog.Builder(requireContext())
//        val dialogItemBinding= EditprofildialogBinding.inflate(layoutInflater)
//        storage=firebaseStorage.getReference("UserProfileImages/${firebaseAuth.currentUser?.uid}")
//        dialog.setView(dialogItemBinding.root)
//        var d=dialog.create()
//
//        database.addListenerForSingleValueEvent(object :ValueEventListener{
//            override fun onDataChange(snapshot: DataSnapshot) {
//
//               one@for (child in snapshot.children) {
//                    var userData=child.getValue(UserData::class.java)
//                    if(userData?.uid== firebaseAuth.currentUser?.uid){
//                        dialogItemBinding.firstname.setText(userData?.firstname)
//                        dialogItemBinding.lastname.setText(userData?.lastname)
//                        Glide.with(requireActivity()).load(userData?.photoUrl).apply(
//                            RequestOptions().placeholder(R.drawable.ic_man__1_))
//                            .into(dialogItemBinding.image)
//                        photourl=userData?.photoUrl!!
//                        currentuser=userData
//                        break@one
//                    }
//
//                }
//                dialogItemBinding.image.setOnClickListener {
//                    imageView=dialogItemBinding.image
//                    getImage.launch("image/*")
//
//                }
//                dialogItemBinding.save.setOnClickListener {
//
//                    if(dialogItemBinding.firstname.text!!.isNotEmpty() && dialogItemBinding.lastname.text!!.isNotEmpty()) {
//
//                        var progressDialog=ProgressDialog(requireContext())
//                        progressDialog.setMessage("Profil is updated")
//                        progressDialog.setCancelable(false)
//                        progressDialog.show()
//                        if(uri!=null){
//                            saveToStorage()
//                        }
//
//                        var photo=HashMap<String,String>()
//                        photo["photoUrl"] = photourl
//                        photo["firstname"] = dialogItemBinding.firstname.text.toString()
//                        photo["lastname"] = dialogItemBinding.lastname.text.toString()
//                        database.child(firebaseAuth.currentUser!!.uid).updateChildren(photo as Map<String, Any>).addOnCompleteListener {
//                            if(it.isSuccessful){
//                                progressDialog.dismiss()
//                                d.dismiss()
//                            }
//                        }
//
//
//                    }
//
//                }
//                d.window?.attributes?.windowAnimations=R.style.AnimationforDialog
//
//                d.show()
//
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//
//            }
//
//        })
//
//
//    }

    private fun saveToStorage() {
        storage=firebaseStorage.getReference("UserProfileImages/${firebaseAuth.currentUser?.uid}")

        storage.putFile(uri!!).addOnSuccessListener(object :
            OnSuccessListener<UploadTask.TaskSnapshot> {
            override fun onSuccess(p0: UploadTask.TaskSnapshot?) {
                var task=p0?.metadata?.reference?.downloadUrl
                task?.addOnSuccessListener(object : OnSuccessListener<Uri> {
                    override fun onSuccess(p0: Uri?) {
                         photourl=p0.toString()
                    }

                })
            }

        })
    }

    private fun setpasscode() {
        val dialog= androidx.appcompat.app.AlertDialog.Builder(requireContext())
        val dialogItemBinding= SetpasswordDialogBinding.inflate(layoutInflater)

        dialog.setView(dialogItemBinding.root)
        var d=dialog.create()
        dialogItemBinding.cancel.setOnClickListener {
            d.dismiss()
        }
        dialogItemBinding.save.setOnClickListener {
            if(dialogItemBinding.userpassword.text!!.isNotEmpty() && dialogItemBinding.userpasswordconfirm.text!!.isNotEmpty() &&
                    dialogItemBinding.userpasswordconfirm.text.toString()==dialogItemBinding.userpassword.text.toString() &&
                    dialogItemBinding.userpassword.text.toString().length==5){
                if(appDatabase.passworddao().allPasswords().isEmpty()){
                    appDatabase.passworddao().addPassword(Password(word = dialogItemBinding.userpassword.text.toString()))
                    Toast.makeText(requireContext(), "Password set successfully", Toast.LENGTH_SHORT).show()
                } else{
                   var password= appDatabase.passworddao().allPasswords()[0]
                    password.word=dialogItemBinding.userpassword.text.toString()
                    appDatabase.passworddao().updatePassword(password)
                    Toast.makeText(requireContext(), "Password was edited successfully", Toast.LENGTH_SHORT).show()
                }
                d.dismiss()
            }else {
                Toast.makeText(requireContext(), "Your password must be 5 length and you must confirm this", Toast.LENGTH_SHORT).show()
            }

        }
        d.window?.attributes?.windowAnimations=R.style.AnimationforDialog

        d.show()
    }
    private var getImage=
        registerForActivityResult(ActivityResultContracts.GetContent()) { it ->

            if (it != null) {
                imageView.setImageURI(it)
                uri = it


            }
        }
    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SettingsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}