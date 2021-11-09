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
import uz.pdp.messanger.adapters.ChennelAdapter
import uz.pdp.messanger.databinding.FragmentChennelBinding
import uz.pdp.messanger.models.Chennel
import uz.pdp.messanger.models.UserData


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class ChennelFragment : Fragment() {

    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    lateinit var binding:FragmentChennelBinding
    lateinit var firebaseAuth: FirebaseAuth
    lateinit var firebaseDatabase: FirebaseDatabase
    lateinit var reference: DatabaseReference
    lateinit var chennelAdapter: ChennelAdapter
    lateinit var list:ArrayList<Chennel>
    var f=false
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= FragmentChennelBinding.inflate(inflater,container,false)
        firebaseAuth= FirebaseAuth.getInstance()
        firebaseDatabase= FirebaseDatabase.getInstance()
        reference=firebaseDatabase.getReference("Chennels")


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
                var newList=ArrayList<Chennel>()
                for (a in list){
                    if(a.groupname.toString().lowercase().contains(newText?.lowercase()!!)){
                        newList.add(a)
                    }
                }
                chennelAdapter= ChennelAdapter(newList,object:ChennelAdapter.onItemClick{
                    override fun OnItemClick(chennel: Chennel, position: Int) {
                        var bundle=Bundle()
                        bundle.putSerializable("chennel",chennel)
                        findNavController().navigate(R.id.chennelMessagesFragment,bundle)
                    }

                })
                binding.rv.adapter=chennelAdapter

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

    override fun onResume() {
        super.onResume()
        if(f){
            binding.bar.visibility=View.INVISIBLE
        } else {
            binding.bar.visibility=View.VISIBLE
        }
        list= ArrayList()
        chennelAdapter= ChennelAdapter(list,object:ChennelAdapter.onItemClick{
            override fun OnItemClick(chennel: Chennel, position: Int) {
                var bundle=Bundle()
                bundle.putSerializable("chennel",chennel)
                findNavController().navigate(R.id.chennelMessagesFragment,bundle)
            }

        })
        binding.rv.adapter=chennelAdapter
        reference.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                list.clear()
                for (child in snapshot.children) {
                    var chennel=child.getValue(Chennel::class.java)
                    if(chennel!=null){
                        one@for(member in chennel.members!!){
                            if(member==firebaseAuth.currentUser?.uid){
                                list.add(chennel)
                                break@one
                            }
                        }
                    }

                }
                chennelAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "${error.message}", Toast.LENGTH_SHORT).show()
            }

        })
    }

    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ChennelFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}