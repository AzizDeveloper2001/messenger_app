package uz.pdp.messanger.fragments.menuFragments

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.provider.ContactsContract
import android.renderscript.Sampler
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.navigation.fragment.findNavController
import com.github.florent37.runtimepermission.kotlin.askPermission
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.messaging.FirebaseMessaging
import uz.pdp.messanger.R
import uz.pdp.messanger.RegisterActivity
import uz.pdp.messanger.Room.Database.AppDatabase
import uz.pdp.messanger.adapters.ChatconAdap
import uz.pdp.messanger.adapters.ChatcontactAdapter
import uz.pdp.messanger.databinding.FragmentChatContactsBinding
import uz.pdp.messanger.models.Contact
import uz.pdp.messanger.models.Message
import uz.pdp.messanger.models.UserData

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class ChatContactsFragment : Fragment() {
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

    lateinit var binding:FragmentChatContactsBinding
    lateinit var databaseReference: DatabaseReference
    lateinit var firebaseDatabase: FirebaseDatabase
    lateinit var chatcontactAdapter: ChatconAdap
    lateinit var firebaseAuth:FirebaseAuth
    lateinit var appDatabase: AppDatabase
    lateinit var list:ArrayList<UserData>
    lateinit var main:ArrayList<UserData>
    var f=false
    lateinit var messagelist:ArrayList<String>
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= FragmentChatContactsBinding.inflate(inflater,container,false)
        appDatabase= AppDatabase.getInstance(requireContext())
        firebaseDatabase= FirebaseDatabase.getInstance()
        firebaseAuth= FirebaseAuth.getInstance()
        databaseReference=firebaseDatabase.getReference("Users/")
        main=ArrayList()
        getmessageToken()

        binding.add.setOnClickListener {
            findNavController().navigate(R.id.contactsFragment)

        }
        binding.searchview.setOnCloseListener(object:SearchView.OnCloseListener{
            override fun onClose(): Boolean {
                binding.bar.visibility=View.VISIBLE
                f=false
                return false
            }

        })

        binding.searchview.setOnQueryTextListener(object :SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {

                TODO("Not yet implemented")
            }

            override fun onQueryTextChange(newText: String?): Boolean {

                var newList=ArrayList<UserData>()
                for (a in list){
                    if(a.lastname.toString().lowercase().contains(newText?.lowercase()!!)){
                        newList.add(a)
                    }
                }
                var adap=ChatconAdap(newList,object :ChatconAdap.onItemClick{
                    override fun OnItemClick(userData: UserData, position: Int) {
                        var bundle=Bundle()
                        bundle.putSerializable("userdata",userData)
                        findNavController().navigate(R.id.chatMessageFragment,bundle)
                    }

                })
                binding.rv.adapter=adap

                return true
            }

        })
        binding.searchview.setOnSearchClickListener(object :View.OnClickListener{
            override fun onClick(v: View?) {
                binding.bar.visibility=View.INVISIBLE
                f=true
            }

        })


        return binding.root
    }

    private fun getmessageToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {

                return@OnCompleteListener
            }
            // Get new FCM registration token
            val token = task.result

            val ref=FirebaseDatabase.getInstance().getReference("Tokens")
            ref.child(FirebaseAuth.getInstance().currentUser?.uid!!).setValue(token)


        })

    }

    
    override fun onResume() {
        super.onResume()
        if(f){
            binding.bar.visibility=View.INVISIBLE
        } else {
            binding.bar.visibility=View.VISIBLE
        }
        permisssion()
    }

    private fun permisssion() {
        askPermission(
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ){
          var  contactlist= getContacts()
             list=ArrayList<UserData>()
            chatcontactAdapter= ChatconAdap(list,object :ChatconAdap.onItemClick{
                override fun OnItemClick(userData: UserData, position: Int) {
                    var bundle=Bundle()
                    bundle.putSerializable("userdata",userData)
                    findNavController().navigate(R.id.chatMessageFragment,bundle)
                }

            })
            binding.rv.adapter=chatcontactAdapter
            loadMessage()



        }.onDeclined {
            if(it.hasDenied()){
                val dialog= AlertDialog.Builder(requireContext())
                dialog.setCancelable(false)
                dialog.setMessage("Ushbu dasturdan foydalanish uchun barcha so'rovlarga ruxsat berishingiz kerak")
                dialog.setPositiveButton("Ruxsat so'rash",object : DialogInterface.OnClickListener{
                    override fun onClick(dialog: DialogInterface?, which: Int) {
                        dialog?.dismiss()
                        it.askAgain()

                    }

                })
                dialog.show()
            } else if(it.hasForeverDenied()){
                val dialog= AlertDialog.Builder(requireContext())
                dialog.setCancelable(false)
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

    private fun loadMessage() {
        messagelist= ArrayList()
        var ref=FirebaseDatabase.getInstance().getReference("Message/${firebaseAuth.currentUser?.uid}")
        ref.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for (child in snapshot.children) {
                    messagelist.add(child.key!!)

                }
                loadUsers()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "${error.message}", Toast.LENGTH_SHORT).show()
            }

        })
    }

    private fun loadUsers() {
        databaseReference.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                list.clear()
                for (child in snapshot.children) {
                    var userData=child.getValue(UserData::class.java)
                    if(userData!=null){
                        if(messagelist.contains(userData.uid)){
                            list.add(userData)
                        }
                    }

                }
                if(list.isEmpty()){
                    binding.text.visibility=View.VISIBLE
                } else {
                    binding.text.visibility=View.GONE
                }
                chatcontactAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "${error.message}", Toast.LENGTH_SHORT).show()
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
            ChatContactsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}