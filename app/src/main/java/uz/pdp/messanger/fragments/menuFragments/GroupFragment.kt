package uz.pdp.messanger.fragments.menuFragments

import android.os.Bundle
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
import uz.pdp.messanger.adapters.ChatconAdap
import uz.pdp.messanger.adapters.GroupAdapter
import uz.pdp.messanger.databinding.FragmentGroupBinding
import uz.pdp.messanger.models.Groups
import uz.pdp.messanger.models.UserData

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class GroupFragment : Fragment() {
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

    lateinit var binding:FragmentGroupBinding
    lateinit var groupAdapter: GroupAdapter
    lateinit var firebaseAuth: FirebaseAuth
    lateinit var firebaseDatabase: FirebaseDatabase
    lateinit var reference: DatabaseReference
    lateinit var list:ArrayList<Groups>
    var f=false
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
       binding= FragmentGroupBinding.inflate(inflater,container,false)
        firebaseAuth= FirebaseAuth.getInstance()
        firebaseDatabase= FirebaseDatabase.getInstance()
        reference=firebaseDatabase.getReference("Groups/")


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
                var newList=ArrayList<Groups>()
                for (a in list){
                    if(a.groupname.toString().lowercase().contains(newText?.lowercase()!!)){
                        newList.add(a)
                    }
                }
                var adap= GroupAdapter(newList,object : GroupAdapter.onItemClick{
                    override fun OnItemClick(groups: Groups, position: Int) {
                        var bundle=Bundle()
                        bundle.putSerializable("group",groups)
                        findNavController().navigate(R.id.groupMessageFragment,bundle)
                    }

                })
                binding.rv.adapter=adap

                return true
            }

        })
        binding.searchview.setOnSearchClickListener(object :View.OnClickListener{
            override fun onClick(v: View?) {
                f=true

                binding.bar.visibility=View.INVISIBLE
            }

        })










        return binding.root
    }

    override fun onResume() {
        super.onResume()

        if(f){
            binding.bar.visibility=View.INVISIBLE
        } else {
            binding.bar.visibility=View.VISIBLE
        }
        list= ArrayList()
        groupAdapter= GroupAdapter(list,object :GroupAdapter.onItemClick{
            override fun OnItemClick(group: Groups, position: Int) {
                var bundle=Bundle()
                bundle.putSerializable("group",group)
                findNavController().navigate(R.id.groupMessageFragment,bundle)
            }

        })
        binding.rv.adapter=groupAdapter
        reference.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                list.clear()
                for (child in snapshot.children) {

                    var group=child.getValue(Groups::class.java)
                    if(group!=null ){
                        one@for (member in group.members!!) {
                            if(member==firebaseAuth.currentUser?.uid){
                                list.add(group)
                                break@one
                            }

                        }
                    }

                }
                groupAdapter.notifyDataSetChanged()


            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "${error.message}", Toast.LENGTH_SHORT).show()
            }

        })


    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            GroupFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}