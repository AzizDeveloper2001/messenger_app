package uz.pdp.messanger.fragments.menuFragments

import android.app.ProgressDialog
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.fragment.findNavController
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import uz.pdp.messanger.R
import uz.pdp.messanger.Room.Database.AppDatabase
import uz.pdp.messanger.adapters.MultiSeclectionAdapter
import uz.pdp.messanger.databinding.FragmentChennelBinding
import uz.pdp.messanger.databinding.FragmentCreateChennelBinding
import uz.pdp.messanger.models.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class CreateChennelFragment : Fragment() {
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

    lateinit var binding:FragmentCreateChennelBinding
    var uri:Uri?=null
    lateinit var multiSeclectionAdapter: MultiSeclectionAdapter
    lateinit var firebaseAuth: FirebaseAuth
    lateinit var appDatabase: AppDatabase
    lateinit var selections:ArrayList<String>
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= FragmentCreateChennelBinding.inflate(inflater,container,false)
        selections= ArrayList()
        firebaseAuth= FirebaseAuth.getInstance()
        appDatabase= AppDatabase.getInstance(requireContext())
        binding.image.setOnClickListener {
            getImage.launch("image/*")
        }

        binding.save.setOnClickListener {
            if(uri!=null && binding.name.text.isNotEmpty() && selections.size>0){
                creategroup()
            } else {
                Toast.makeText(requireContext(), "Select chennel image,enter chennel name and add member", Toast.LENGTH_SHORT).show()
            }

        }
        binding.back.setOnClickListener {
            findNavController().popBackStack()
        }
        return binding.root
    }
    private var getImage=
        registerForActivityResult(ActivityResultContracts.GetContent()){ it->

            if(it!=null){
                binding.image.setImageURI(it)
                uri=it


            }

        }

    private fun creategroup() {

        var progressDialog= ProgressDialog(requireContext())
        progressDialog.setMessage("Chennel is creating")
        progressDialog.show()
        var ref= FirebaseDatabase.getInstance().getReference("Chennels")
        var key=ref.push().key
        selections.add(firebaseAuth.currentUser?.uid!!)
        var stroage= FirebaseStorage.getInstance().getReference("ChennelProfilImages/${firebaseAuth.currentUser?.uid}/${System.currentTimeMillis()}")
        stroage.putFile(uri!!).addOnSuccessListener(object:
            OnSuccessListener<UploadTask.TaskSnapshot> {
            override fun onSuccess(p0: UploadTask.TaskSnapshot?) {
                var task=  p0?.metadata?.reference?.downloadUrl
                task?.addOnSuccessListener(object : OnSuccessListener<Uri> {
                    override fun onSuccess(p0: Uri?) {
                        var photoUrl=p0.toString()


                        var chennel= Chennel(binding.name.text.toString(),photoUrl,key,firebaseAuth.currentUser?.uid,selections)
                        ref.child(key!!).setValue(chennel).addOnCompleteListener{
                            if(it.isSuccessful){
                                progressDialog.dismiss()
                                Toast.makeText(requireContext(), "Successfully created", Toast.LENGTH_SHORT).show()
                                findNavController().popBackStack()
                            }
                        }
                    }

                })
            }

        })


    }

    override fun onResume() {
        super.onResume()
        var  contactlist= getContacts()
        var list=ArrayList<UserData>()
        var databaseReference= FirebaseDatabase.getInstance().getReference("Users")
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                list.clear()
                for(s in snapshot.children) {

                    val userData = s.getValue(UserData::class.java)
                    if(userData!=null){
                        for ( i in contactlist){
                            if (userData?.phonenumber==i.number && userData.uid!=firebaseAuth.currentUser?.uid && !list.contains(userData)){
                                list.add(userData)

                            }

                        }

                        var l=appDatabase.mycontactdao().getAllContacts()
                        for (a in l){
                            if(userData?.phonenumber==a.phonenumber && userData.uid!=firebaseAuth.currentUser?.uid && !list.contains(userData)){
                                userData.firstname=a.firstname
                                userData.lastname=a.secondname
                                list.add(userData)

                            }
                        }
                        var ref= FirebaseDatabase.getInstance().getReference("Message/${firebaseAuth.currentUser?.uid}/${userData?.uid}")
                        ref.addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                one@for (child in snapshot.children) {
                                    var message=child.getValue(Message::class.java)
                                    if(message!=null && !list.contains(userData)){
                                        list.add(userData)
                                        break@one
                                    }

                                }
                                selections=ArrayList<String>()
                                multiSeclectionAdapter= MultiSeclectionAdapter(list,object :MultiSeclectionAdapter.onItemClick{
                                    override fun OnItemClick(userData: UserData, position: Int) {
                                        if(selections.size>0 && selections.contains(userData.uid)){
                                            selections.remove(userData.uid)
                                        }
                                    }

                                    override fun onItemLongClick(
                                        userData: UserData,
                                        position: Int
                                    ): ArrayList<String> {
                                        if(!selections.contains(userData.uid)){
                                            selections.add(userData.uid!!)
                                        }

                                        return selections
                                    }

                                })
                                binding.rv.adapter=multiSeclectionAdapter


                            }

                            override fun onCancelled(error: DatabaseError) {
                                Toast.makeText(requireContext(), "${error.message}", Toast.LENGTH_SHORT).show()
                            }

                        })

                    }
                }




            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }
    private fun getContacts(): ArrayList<Contact> {
        val arrayList = ArrayList<Contact>()
        val contentResolver = requireActivity().contentResolver
        val cur =
            contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null)
        if ((cur?.count ?: 0) > 0) {
            while (cur != null && cur.moveToNext()) {
                val id = cur.getString(
                    cur.getColumnIndex(ContactsContract.Contacts._ID)
                )
                val name = cur.getString(
                    cur.getColumnIndex(
                        ContactsContract.Contacts.DISPLAY_NAME
                    )
                )
                if (cur.getInt(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                    val pCur = contentResolver.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                        arrayOf(id), null
                    )
                    while (pCur!!.moveToNext()) {
                        val phoneNo = pCur.getString(
                            pCur.getColumnIndex(
                                ContactsContract.CommonDataKinds.Phone.NUMBER
                            )
                        )
                        arrayList.add(Contact(name, phoneNo))
                    }
                    pCur.close()
                }

            }
            cur?.close()

        }
        return arrayList
    }

    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            CreateChennelFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}