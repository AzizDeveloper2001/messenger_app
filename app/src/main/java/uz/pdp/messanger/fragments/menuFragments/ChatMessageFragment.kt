package uz.pdp.messanger.fragments.menuFragments

import android.app.ProgressDialog
import android.content.Context
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Environment.getExternalStorageDirectory
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.devlomi.record_view.OnRecordListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import uz.pdp.messanger.R
import uz.pdp.messanger.Retrofit.ApiClient
import uz.pdp.messanger.Retrofit.ApiService
import uz.pdp.messanger.adapters.ChatMessageAdapter
import uz.pdp.messanger.databinding.FragmentChatMessageBinding
import uz.pdp.messanger.models.Message
import uz.pdp.messanger.models.NotificationModels.Notification
import uz.pdp.messanger.models.NotificationModels.RequestModel
import uz.pdp.messanger.models.NotificationModels.ResponseModel
import uz.pdp.messanger.models.UserData
import java.io.File
import java.text.SimpleDateFormat

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class ChatMessageFragment : Fragment() {
 
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    lateinit var binding:FragmentChatMessageBinding
    lateinit var chatMessageAdapter: ChatMessageAdapter
    lateinit var firebaseAuth: FirebaseAuth
    lateinit var firebaseDatabase: FirebaseDatabase
    lateinit var referenceCurrentUser: DatabaseReference
    lateinit var referenceuser:DatabaseReference
    lateinit var ref:DatabaseReference
    lateinit var userData: UserData
    lateinit var audiopath:String
    lateinit var mediarecorder: MediaRecorder
    lateinit var apiService: ApiService
     var uri:Uri?=null
    var resume=false
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
       binding= FragmentChatMessageBinding.inflate(inflater,container,false)
        firebaseAuth= FirebaseAuth.getInstance()
        firebaseDatabase= FirebaseDatabase.getInstance()
        userData=arguments?.getSerializable("userdata") as UserData
       Glide.with(binding.root.context).load(userData.photoUrl).apply(RequestOptions().placeholder(R.drawable.ic_man__1_)).into(binding.img)
        binding.name.text="${userData.firstname} ${userData.lastname}"

        binding.backone.setOnClickListener {
            findNavController().popBackStack()
            closeKeyboard()
        }
        referenceCurrentUser=firebaseDatabase.getReference("Message/${firebaseAuth.currentUser?.uid}/${userData.uid}")
        referenceuser=firebaseDatabase.getReference("Message/${userData.uid}/${firebaseAuth.currentUser?.uid}")
         binding.apply {
             var f=firebaseDatabase.getReference("Users/")
             f.addValueEventListener(object :ValueEventListener{
                 override fun onDataChange(snapshot: DataSnapshot) {
                     for (child in snapshot.children) {
                         var abonent=child.getValue(UserData::class.java)
                         if(abonent!=null && abonent.uid==userData.uid){
                              if(abonent.firstname=="Delete Account"){
                                 layout.visibility=View.GONE
                                 lay.visibility=View.GONE
                             }

                             if(abonent.on_of=="Online"){
                                 binding.lastseen.text = "Online"

                             }  else  {
                                 if(System.currentTimeMillis()-abonent.on_of!!.toLong()<60000){
                                     binding.lastseen.text="Last seen recently"
                                 } else {
                                     var simpleDateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm")

                                     val format = simpleDateFormat.format(abonent.on_of!!.toLong())
                                     binding.lastseen.text="Last seen at $format"


                                 }
                             }

                         }

                     }
                 }

                 override fun onCancelled(error: DatabaseError) {

                 }

             })
             sendbutton.visibility=View.GONE
             recordbtn.visibility=View.VISIBLE

             message.addTextChangedListener(object:TextWatcher{
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

             var list=ArrayList<Message>()
             chatMessageAdapter=ChatMessageAdapter(list,firebaseAuth.currentUser?.uid!!,userData)
             rv.layoutManager=LinearLayoutManager(requireContext())
             rv.adapter=chatMessageAdapter

                 referenceCurrentUser.addValueEventListener(object :ValueEventListener{
                     override fun onDataChange(snapshot: DataSnapshot) {
                         list.clear()
                         for (child in snapshot.children) {

                             var message=  child.getValue(Message::class.java)
                             if (message != null) {
                                 list.add(message)
                             }
                         }

                            chatMessageAdapter.notifyDataSetChanged()
//                         binding.rv.isNestedScrollingEnabled=false
                         binding.rv.smoothScrollToPosition(list.size)
//                         binding.rv.scrollToPosition(list.size)
                     }

                     override fun onCancelled(error: DatabaseError) {

                     }

                 })



             sendbutton.setOnClickListener {
                 Toast.makeText(requireContext(), "Message Send", Toast.LENGTH_SHORT).show()
                 var uid=userData.uid
                 var messagetext=  message.text.toString()
                 var key=referenceuser.push().key
                 var message=Message(messagetext,firebaseAuth.currentUser?.uid,uid,System.currentTimeMillis(),"false",key,"text")
                 referenceCurrentUser.child(key?:"").setValue(message)
                 referenceuser.child(key?:"").setValue(message)
                 sendbutton.visibility=View.GONE
                 recordbtn.visibility=View.VISIBLE
                 binding.message.setText("")

                 var r=FirebaseDatabase.getInstance().getReference("Tokens/$uid")
                 r.addValueEventListener(object :ValueEventListener{
                     override fun onDataChange(snapshot: DataSnapshot) {
                         var m=messagetext
                       if(m.length>15){
                           m=m.substring(0,14)+"..."
                       } else {
                           m=m
                       }
                         var to = snapshot.value.toString()
                         var notifmodel=Notification("${userData.firstname} ${userData.lastname}",m)
                         sendNotificationToUser(to,notifmodel)
                     }

                     override fun onCancelled(error: DatabaseError) {
                         Toast.makeText(requireContext(), "${error.message}", Toast.LENGTH_SHORT).show()
                     }

                 })

                 binding.rv.scrollToPosition(list.size)


             }
             recordbtn.setOnClickListener {
                 Toast.makeText(requireContext(), "Audio record", Toast.LENGTH_SHORT).show()
             }
         }
        recordAudio()
        imagesending()

        return binding.root
    }

    override fun onStop() {
        super.onStop()
        referenceCurrentUser.removeEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {

            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

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
           var refstorage=FirebaseStorage.getInstance().getReference("ChatSendImages/${firebaseAuth.currentUser?.uid}/${userData.uid}/${System.currentTimeMillis()}")
            refstorage.putFile(uri!!).addOnSuccessListener{
                var audiourl=it.storage.downloadUrl
                audiourl.addOnCompleteListener{ i->
                    if(i.isSuccessful){
                        var url=i.result.toString()
                        var reference=FirebaseDatabase.getInstance().getReference("Message/${firebaseAuth.currentUser?.uid}/${userData.uid}")
                        var refer=FirebaseDatabase.getInstance().getReference("Message/${userData.uid}/${firebaseAuth.currentUser?.uid}")
                        var key=reference.push().key
                        var message=Message(url,firebaseAuth.currentUser?.uid,userData.uid,System.currentTimeMillis(),"false",key,"image")
                        refer.child(key!!).setValue(message)
                        reference.child(key!!).setValue(message)
                        progressDialog.dismiss()
                        binding.rv.scrollToPosition(chatMessageAdapter.itemCount)

                        var r=FirebaseDatabase.getInstance().getReference("Tokens/${userData.uid}")
                        r.addValueEventListener(object :ValueEventListener{
                            override fun onDataChange(snapshot: DataSnapshot) {

                                var to = snapshot.value.toString()
                                var notifmodel=Notification("${userData.firstname} ${userData.lastname}","Image message")
                                sendNotificationToUser(to,notifmodel)
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Toast.makeText(requireContext(), "${error.message}", Toast.LENGTH_SHORT).show()
                            }

                        })

                    }
                }
            }


        }
    }
    fun recordAudio(){
        binding.recordbtn.setRecordView(binding.recordview)
        binding.recordbtn.isListenForRecord=false
        binding.recordbtn.setOnClickListener{
            binding.recordbtn.isListenForRecord=true
        }
        binding.recordview.setOnRecordListener(object :OnRecordListener{
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
         mediarecorder=MediaRecorder()
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
        var storagereference=FirebaseStorage.getInstance().getReference("RecordingVoiceMessages/${firebaseAuth.currentUser?.uid}/${userData.uid}/${System.currentTimeMillis()}")
        var uri= Uri.fromFile(File(audio))
        var progressDialog=ProgressDialog(requireContext())
        progressDialog.setMessage("Sending voice message")
        progressDialog.show()
        storagereference.putFile(uri).addOnSuccessListener{
            var audiourl=it.storage.downloadUrl
            audiourl.addOnCompleteListener{ i->
                if(i.isSuccessful){
                    var url=i.result.toString()
                    var reference=FirebaseDatabase.getInstance().getReference("Message/${firebaseAuth.currentUser?.uid}/${userData.uid}")
                    var refer=FirebaseDatabase.getInstance().getReference("Message/${userData.uid}/${firebaseAuth.currentUser?.uid}")
                    var key=reference.push().key
                    var message=Message(url,firebaseAuth.currentUser?.uid,userData.uid,System.currentTimeMillis(),"false",key,"audio")
                    refer.child(key!!).setValue(message)
                    reference.child(key!!).setValue(message)
                    progressDialog.dismiss()
                    binding.rv.scrollToPosition(chatMessageAdapter.itemCount)


                    var r=FirebaseDatabase.getInstance().getReference("Tokens/${userData.uid}")
                    r.addValueEventListener(object :ValueEventListener{
                        override fun onDataChange(snapshot: DataSnapshot) {

                            var to = snapshot.value.toString()
                            var notifmodel=Notification("${userData.firstname} ${userData.lastname}","Audio message")
                            sendNotificationToUser(to,notifmodel)
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(requireContext(), "${error.message}", Toast.LENGTH_SHORT).show()
                        }

                    })
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

    private fun sendNotificationToUser(token:String,notification: Notification) {
        val requestmodel= RequestModel(notification=notification,to=token)

        apiService= ApiClient.apiService
        apiService.sendNotification(requestmodel).enqueue(object : Callback<ResponseModel?> {
            override fun onResponse(
                call: Call<ResponseModel?>,
                response: Response<ResponseModel?>
            ) {
                Toast.makeText(requireContext(), "Send message", Toast.LENGTH_SHORT).show()
            }

            override fun onFailure(call: Call<ResponseModel?>, t: Throwable) {
                Toast.makeText(requireContext(), "${t.message}", Toast.LENGTH_SHORT).show()
            }


        })
    }

    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ChatMessageFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}