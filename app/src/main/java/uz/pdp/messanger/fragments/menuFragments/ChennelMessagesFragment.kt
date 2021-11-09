package uz.pdp.messanger.fragments.menuFragments

import android.app.ProgressDialog
import android.content.Context
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.size
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.devlomi.record_view.OnRecordListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import uz.pdp.messanger.R
import uz.pdp.messanger.adapters.ChennelMessageAdapter
import uz.pdp.messanger.adapters.GroupMessageAdapter
import uz.pdp.messanger.databinding.FragmentChennelMessagesBinding
import uz.pdp.messanger.models.Chennel
import uz.pdp.messanger.models.ChennelMessage
import uz.pdp.messanger.models.Groupmessage
import uz.pdp.messanger.models.UserData
import java.io.File

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class ChennelMessagesFragment : Fragment() {
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

    lateinit var binding:FragmentChennelMessagesBinding
    lateinit var chennel:Chennel
    lateinit var firebaseDatabase: FirebaseDatabase
    lateinit var firebaseAuth: FirebaseAuth
    lateinit var reference: DatabaseReference
    lateinit var senderUser: UserData
    lateinit var list:ArrayList<ChennelMessage>
    lateinit var mediarecorder: MediaRecorder
    lateinit var audiopath:String
    lateinit var chennelMessageAdapter: ChennelMessageAdapter
    var uri: Uri?=null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
       binding= FragmentChennelMessagesBinding.inflate(inflater,container,false)
        chennel=arguments?.getSerializable("chennel") as Chennel
        firebaseAuth= FirebaseAuth.getInstance()
        firebaseDatabase= FirebaseDatabase.getInstance()
        reference=firebaseDatabase.getReference("Chennels/${chennel.key}/chennelmessages")
        Glide.with(requireActivity()).load(chennel.groupimage).apply(RequestOptions().placeholder(R.drawable.group_off))
            .into(binding.img)

        binding.backone.setOnClickListener {
            findNavController().popBackStack()
            closeKeyboard()
        }
        binding.lastseen.text="${chennel.members?.size} subscribers"
        binding.name.text=chennel.groupname
        binding.apply {
            if(chennel.admin==firebaseAuth.currentUser?.uid){
                layout.visibility=View.VISIBLE
                lay.visibility=View.VISIBLE
            } else {
                layout.visibility=View.GONE
                lay.visibility=View.GONE

            }
        }



        list= ArrayList()
        chennelMessageAdapter = ChennelMessageAdapter(list,firebaseAuth.currentUser!!.uid,chennel)
        binding.rv.adapter=chennelMessageAdapter
        reference.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                list.clear()
                for (child in snapshot.children) {
                    var chennelmessage=child.getValue(ChennelMessage::class.java)
                    if(chennelmessage!=null){
                        list.add(chennelmessage)
                    }

                }
               chennelMessageAdapter.notifyDataSetChanged()
                binding.rv.smoothScrollToPosition(chennelMessageAdapter.itemCount)

            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "${error.message}", Toast.LENGTH_SHORT).show()
            }

        })
        binding.apply {
            sendbutton.setOnClickListener {
                if(message.text.isNotEmpty()){
                    sendTextMessage(message.text.toString())
                }
            }
            message.addTextChangedListener(object: TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if(message.text.toString()!=""){
                        sendbutton.visibility=View.VISIBLE
                        recordbtn.visibility=View.GONE
                    }
                    else if(message.text.toString() ==""){
                        sendbutton.visibility=View.GONE
                        recordbtn.visibility=View.VISIBLE
                    }
                }

                override fun afterTextChanged(s: Editable?) {

                }

            })
        }
        var f=firebaseDatabase.getReference("Users/")
        f.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                one@for (child in snapshot.children) {
                    var userData=child.getValue(UserData::class.java)
                    if(userData?.uid==firebaseAuth.currentUser?.uid){
                        senderUser=userData!!
                        break@one
                    }


                }

            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "${error.message}", Toast.LENGTH_SHORT).show()
            }

        })
        recordAudio()
        imagesending()
        return binding.root

    }


    private fun sendTextMessage(sendttext:String) {
        var f=firebaseDatabase.getReference("Users/")
        f.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                one@for (child in snapshot.children) {
                    var userData=child.getValue(UserData::class.java)
                    if(userData?.uid==firebaseAuth.currentUser?.uid){
                        senderUser=userData!!
                        break@one
                    }


                }
                var key=reference.push().key
                var groupmessage=Groupmessage(sendttext,senderUser,System.currentTimeMillis(),key,"false","text")
                reference.child(key!!).setValue(groupmessage)
                Toast.makeText(requireContext(), "Send message", Toast.LENGTH_SHORT).show()
                binding.message.setText("")
                binding.sendbutton.visibility=View.GONE
                binding.recordbtn.visibility=View.VISIBLE
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "${error.message}", Toast.LENGTH_SHORT).show()
            }

        })


    }
    fun recordAudio(){
        binding.recordbtn.setRecordView(binding.recordview)
        binding.recordbtn.isListenForRecord=false
        binding.recordbtn.setOnClickListener{
            binding.recordbtn.isListenForRecord=true
        }
        binding.recordview.setOnRecordListener(object : OnRecordListener {
            override fun onStart() {

                setuprecording()
                mediarecorder.prepare()
                mediarecorder.start()
                binding.layout.visibility=View.GONE
                binding.recordview.visibility=View.VISIBLE

            }

            override fun onCancel() {

                mediarecorder.reset()
                mediarecorder.release()
                var file= File(audiopath)
                if(!file.exists())
                    file.delete()
                binding.layout.visibility=View.VISIBLE
                binding.recordview.visibility=View.GONE



            }

            override fun onFinish(recordTime: Long) {
                mediarecorder.stop()
                mediarecorder.release()
                binding.layout.visibility=View.VISIBLE
                binding.recordview.visibility=View.GONE
                sendrecordingMessage(audiopath)
            }

            override fun onLessThanSecond() {
                mediarecorder.reset()
                mediarecorder.release()
                var file= File(audiopath)
                if(!file.exists())
                    file.delete()
                binding.layout.visibility=View.VISIBLE
                binding.recordview.visibility=View.GONE

            }

        })
    }


    fun setuprecording(){
        mediarecorder= MediaRecorder()
        mediarecorder.setAudioSource(MediaRecorder.AudioSource.MIC)
        mediarecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
        mediarecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB)
        var file = File("${requireActivity().externalCacheDir?.absolutePath}/${System.currentTimeMillis()}.3gp")
        audiopath = Environment.getExternalStorageDirectory().absolutePath + "/recording.mp3"
        if(!file.exists()){
            file.mkdirs()

            mediarecorder.setOutputFile(audiopath)
        }
    }

    fun sendrecordingMessage(audio:String){
        var storagereference= FirebaseStorage.getInstance().getReference("ChennelVoiceMessage/${chennel.key}/${firebaseAuth.currentUser?.uid}/${System.currentTimeMillis()}")
        var uri= Uri.fromFile(File(audio))
        var progressDialog= ProgressDialog(requireContext())
        progressDialog.setMessage("Sending voice message")
        progressDialog.show()
        storagereference.putFile(uri).addOnSuccessListener{
            var audiourl=it.storage.downloadUrl
            audiourl.addOnCompleteListener{
                if(it.isSuccessful){
                    var url=it.result.toString()
                    var key=reference.push().key
                    var message= ChennelMessage(url,senderUser,System.currentTimeMillis(),key,"false","audio")
                    reference.child(key!!).setValue(message)
                    progressDialog.dismiss()
                }
            }
        }
    }

    fun imagesending(){
        binding.file.setOnClickListener {
            getimage.launch("image/*")
        }
    }
    private val getimage=registerForActivityResult(ActivityResultContracts.GetContent()){ it->

        if(it!=null){
            uri=it
            var progressDialog= ProgressDialog(requireContext())
            progressDialog.setMessage("Sending image...")
            progressDialog.show()
            var refstorage= FirebaseStorage.getInstance().getReference("ChennelSendImages/${chennel.groupimage}/${firebaseAuth.currentUser?.uid}/${System.currentTimeMillis()}")
            refstorage.putFile(uri!!).addOnSuccessListener{
                var audiourl=it.storage.downloadUrl
                audiourl.addOnCompleteListener{
                    if(it.isSuccessful){
                        var url=it.result.toString()
                        var key=reference.push().key
                        var groupmessage=ChennelMessage(url,senderUser,System.currentTimeMillis(),key,"false","image")
                        reference.child(key!!).setValue(groupmessage)
                        progressDialog.dismiss()

                    }
                }
            }


        }
    }
    fun closeKeyboard(){
        if(requireActivity().currentFocus!=null){
            var im=requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            im.hideSoftInputFromWindow(requireActivity().currentFocus?.windowToken,0)
        }
    }

    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ChennelMessagesFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}