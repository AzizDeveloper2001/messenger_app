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
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.devlomi.record_view.OnRecordListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import uz.pdp.messanger.R
import uz.pdp.messanger.adapters.GroupMessageAdapter
import uz.pdp.messanger.databinding.FragmentGroupMessageBinding
import uz.pdp.messanger.models.Groupmessage
import uz.pdp.messanger.models.Groups
import uz.pdp.messanger.models.Message
import uz.pdp.messanger.models.UserData
import java.io.File

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class GroupMessageFragment : Fragment() {
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

    lateinit var binding:FragmentGroupMessageBinding
    lateinit var groupMessageAdapter: GroupMessageAdapter
    lateinit var firebaseAuth: FirebaseAuth
    lateinit var firebaseDatabase: FirebaseDatabase
    lateinit var reference: DatabaseReference
    lateinit var group:Groups
    lateinit var senderUser:UserData
    lateinit var list:ArrayList<Groupmessage>
    lateinit var mediarecorder:MediaRecorder
    lateinit var audiopath:String
    var uri:Uri?=null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
       binding= FragmentGroupMessageBinding.inflate(inflater,container,false)
        firebaseAuth= FirebaseAuth.getInstance()
        firebaseDatabase= FirebaseDatabase.getInstance()
        group=arguments?.getSerializable("group") as Groups
        Glide.with(requireActivity()).load(group.groupimage).apply(RequestOptions().placeholder(R.drawable.group_off))
            .into(binding.img)
        binding.backone.setOnClickListener {
            findNavController().popBackStack()
            closeKeyboard()
        }
        binding.lastseen.text="${group.members?.size} members"
        binding.name.text="${group.groupname}"
        reference=firebaseDatabase.getReference("Groups/${group.key}/groupmessages/")
        list= ArrayList()
        reference.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                list.clear()
                for (child in snapshot.children) {
                    var groupmessage=child.getValue(Groupmessage::class.java)
                    if(groupmessage!=null){
                        list.add(groupmessage)
                    }

                }
                groupMessageAdapter= GroupMessageAdapter(list,firebaseAuth.currentUser!!.uid,group)
                binding.rv.adapter=groupMessageAdapter
                binding.rv.smoothScrollToPosition(groupMessageAdapter.itemCount)
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
        var storagereference= FirebaseStorage.getInstance().getReference("GroupVoiceMessages/${group.key}/${firebaseAuth.currentUser?.uid}/${System.currentTimeMillis()}")
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
                    var message= Groupmessage(url,senderUser,System.currentTimeMillis(),key,"false","audio")
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
            var progressDialog=ProgressDialog(requireContext())
            progressDialog.setMessage("Sending image...")
            progressDialog.show()
            var refstorage=FirebaseStorage.getInstance().getReference("GroupChatImages/${group.groupimage}/${firebaseAuth.currentUser?.uid}/${System.currentTimeMillis()}")
            refstorage.putFile(uri!!).addOnSuccessListener{
                var audiourl=it.storage.downloadUrl
                audiourl.addOnCompleteListener{
                    if(it.isSuccessful){
                        var url=it.result.toString()
                        var key=reference.push().key
                        var groupmessage=Groupmessage(url,senderUser,System.currentTimeMillis(),key,"false","image")
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
            GroupMessageFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}