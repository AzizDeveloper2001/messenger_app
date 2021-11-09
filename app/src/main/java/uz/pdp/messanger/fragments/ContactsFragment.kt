package uz.pdp.messanger.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import uz.pdp.messanger.R
import uz.pdp.messanger.Room.Database.AppDatabase
import uz.pdp.messanger.adapters.CallAdapter
import uz.pdp.messanger.adapters.ChatconAdap
import uz.pdp.messanger.databinding.FragmentContactsBinding
import uz.pdp.messanger.models.Contact
import uz.pdp.messanger.models.Message
import uz.pdp.messanger.models.UserData


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class ContactsFragment : Fragment() {
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

    lateinit var binding:FragmentContactsBinding
    lateinit var chatconAdap: ChatconAdap
    lateinit var databaseReference: DatabaseReference
    lateinit var firebaseDatabase: FirebaseDatabase
    lateinit var firebaseAuth: FirebaseAuth
    lateinit var appDatabase: AppDatabase
    lateinit var list:ArrayList<UserData>
    var f=false
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= FragmentContactsBinding.inflate(inflater,container,false)
        firebaseAuth= FirebaseAuth.getInstance()
        firebaseDatabase= FirebaseDatabase.getInstance()
        databaseReference=firebaseDatabase.getReference("Users/")
        appDatabase= AppDatabase.getInstance(requireContext())
        binding.backIcon.setOnClickListener {
            findNavController().popBackStack()
        }


        binding.searchview.setOnCloseListener(object: SearchView.OnCloseListener{
            override fun onClose(): Boolean {
                binding.bar.visibility=View.VISIBLE
                f=false
                return false
            }

        })

        binding.searchview.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
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

          binding.add.setOnClickListener {
              findNavController().navigate(R.id.createContactFragment)
          }
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        if(f){
            binding.bar.visibility=View.INVISIBLE
        } else {
            binding.bar.visibility=View.VISIBLE
        }
        setRv()
    }
    private fun setRv() {
        var  contactlist= getContacts()
       list=ArrayList<UserData>()
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
                    }
                }
                chatconAdap= ChatconAdap(list,object :ChatconAdap.onItemClick{
                    override fun OnItemClick(userData: UserData, position: Int) {
                        var bundle=Bundle()
                        bundle.putSerializable("userdata",userData)
                        findNavController().navigate(R.id.chatMessageFragment,bundle)
                    }

                })
                binding.rv.adapter=chatconAdap





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
            ContactsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}