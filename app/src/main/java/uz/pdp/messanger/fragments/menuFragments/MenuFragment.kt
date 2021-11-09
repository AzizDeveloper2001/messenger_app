package uz.pdp.messanger.fragments.menuFragments

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import uz.pdp.messanger.R
import uz.pdp.messanger.databinding.FragmentMenuBinding
import uz.pdp.messanger.databinding.SetpasswordDialogBinding
import uz.pdp.messanger.models.UserData

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class MenuFragment : Fragment() {
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

    lateinit var binding:FragmentMenuBinding
    lateinit var firebaseDatabase: FirebaseDatabase
    lateinit var firebaseAuth: FirebaseAuth
    lateinit var reference: DatabaseReference
    lateinit var currentAccount:UserData
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
       binding= FragmentMenuBinding.inflate(inflater,container,false)
        firebaseAuth= FirebaseAuth.getInstance()
        firebaseDatabase= FirebaseDatabase.getInstance()
        reference=firebaseDatabase.getReference("Users")
        reference.addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                one@for (child in snapshot.children) {
                    var user=child.getValue(UserData::class.java)
                    if(user!=null && firebaseAuth.currentUser?.uid==user.uid){
                      currentAccount=user
                        break@one
                    }

                }
                binding.number.text=currentAccount.phonenumber
                Glide.with(requireActivity()).load(currentAccount.photoUrl).apply(RequestOptions().placeholder(R.drawable.ic_man__1_))
                    .into(binding.profileimage)
                binding.profilename.text="${currentAccount.firstname} ${currentAccount.lastname}"
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "${error.message}", Toast.LENGTH_SHORT).show()
            }

        })
        binding.apply {
            creategroup.setOnClickListener {
                findNavController().navigate(R.id.createGroupFragment,null,navOptions {
                    anim {
                        enter = android.R.anim.fade_in
                        exit = android.R.animator.fade_out
                    }
                })
            }
            chennels.setOnClickListener {
                findNavController().navigate(R.id.createChennelFragment,null,navOptions {
                    anim {
                        enter = android.R.anim.fade_in
                        exit = android.R.animator.fade_out
                    }
                })
            }
            calls.setOnClickListener {
                findNavController().navigate(R.id.callsFragment,null, navOptions {
                    anim {
                        enter = android.R.anim.fade_in
                        exit = android.R.animator.fade_out
                    }
                })
            }
            contacts.setOnClickListener {
                findNavController().navigate(R.id.contactsFragment,null,navOptions {
                    anim {
                        enter = android.R.anim.fade_in
                        exit = android.R.animator.fade_out
                    }
                })
            }
            settings.setOnClickListener {
                findNavController().navigate(R.id.settingsFragment,null,navOptions {
                    anim {
                        enter = android.R.anim.fade_in
                        exit = android.R.animator.fade_out
                    }
                })
            }
        }
        return binding.root
    }



    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MenuFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}