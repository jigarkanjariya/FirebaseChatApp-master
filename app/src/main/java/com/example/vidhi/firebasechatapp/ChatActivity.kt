package com.example.vidhi.firebasechatapp

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.database.Cursor
import android.graphics.Bitmap
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.preference.PreferenceManager
import android.provider.ContactsContract
import android.provider.MediaStore
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.widget.*
import com.bumptech.glide.Glide
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelection
import com.google.android.exoplayer2.ui.SimpleExoPlayerView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.makeramen.roundedimageview.RoundedImageView
import com.ravikoradiya.zoomableimageview.ZoomableImageView
import org.jetbrains.anko.toast
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat


class ChatActivity : AppCompatActivity() {
    val auth = FirebaseAuth.getInstance()
    var idSev: String = ""
    var idLocal: String = ""
    var mAdapter: FirebaseRecyclerAdapter<Chat, RecyclerView.ViewHolder>? = null;
    var fPath: Uri? = null
    var dbRefChat = FirebaseDatabase.getInstance().getReference("Chat")
    var threadId: String? = null
    var storageReference: StorageReference? = null
    val output_formats = intArrayOf(MediaRecorder.OutputFormat.MPEG_4, MediaRecorder.OutputFormat.THREE_GPP)
    var vcfFile: File? = null
    var mRecordbtn: ImageView? = null
    var recorder: MediaRecorder? = null
    var mFilename: String? = null
    val currentFormat = 0
    var storageRef = FirebaseStorage.getInstance().getReference("Store")
    var userId: String? = ""

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        threadId = intent.getStringExtra("threadId")
//        println(threadId + ".......................................")

        var userName = intent.getStringExtra("uName")
        var userPhoto = intent.getStringExtra("photoUrl")
        userId = intent.getStringExtra("userId")
        var newRef = dbRefChat.child(threadId)

        val imgUser = findViewById(R.id.iv_user) as RoundedImageView
        val nameUser = findViewById(R.id.tv_user_name) as TextView
        val rvMsg = findViewById(R.id.task_list) as RecyclerView
        val etMsg = findViewById(R.id.add_task_box) as EditText
        val btnMsg = findViewById(R.id.add_task_button) as Button
        val btnFile = findViewById(R.id.btn_add_file) as Button
        val customLayout = findViewById(R.id.customLayout) as FrameLayout
        val img = findViewById(R.id.btnIMG) as ImageView
        val file = findViewById(R.id.btnFILE) as ImageView
        mRecordbtn = findViewById(R.id.btnVOICE) as ImageView
        val contact = findViewById(R.id.btnCONTACT) as ImageView
        val camera = findViewById(R.id.btnCAMERA) as ImageView
        val location = findViewById(R.id.btnLOCATION) as ImageView
        val toolbar = findViewById(R.id.toolbar) as android.support.v7.widget.Toolbar
        val layoutGone = findViewById(R.id.tvLinear) as LinearLayout
        val layoutlast = findViewById(R.id.layout_last) as LinearLayout
        val ulastseen = findViewById(R.id.tv_lastseen) as TextView
        val tvTyping = findViewById(R.id.tv_typing) as TextView
        val ivDeletechat = findViewById(R.id.iv_deletechat) as ImageView


// Notificaion managaer
        val sharedPref: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val editor = sharedPref.edit()
        editor.putString("userId", userId)
        editor.commit()


            var userIdSet: Set<String?>? = null
            if(userId.equals("")){
            userIdSet = sharedPref.getStringSet("userIdSet", userIdSet)
            userIdSet.remove(userId)
            }


        val idEditor = sharedPref.edit()
        idEditor.putStringSet("userIdSet", userIdSet)
        idEditor.commit()

        //for visiblity of layout
        layoutlast.setOnClickListener {

            customLayout.visibility = View.GONE
        }
        layoutGone.setOnClickListener {

            customLayout.visibility = View.GONE
            layoutGone.visibility = View.GONE

        }
        ///pic nd name
        setSupportActionBar(toolbar)
        Glide.with(imgUser.getContext())
                .load(userPhoto)
                .placeholder(R.mipmap.ic_launcher_round)
                .into(imgUser)

        nameUser.setText(userName)
        setSupportActionBar(toolbar)
        val ab = supportActionBar
        ab!!.setHomeAsUpIndicator(R.mipmap.ic_action_navigation_arrow_back)
        ab.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)


        ///delete chat
        ivDeletechat.setOnClickListener {


            val subdialog: AlertDialog.Builder = AlertDialog.Builder(this@ChatActivity)
            subdialog.setTitle("Are you want to sure delete chat ?").setPositiveButton("Yes", { update: DialogInterface?, which: Int ->
                dbRefChat.child(threadId).removeValue()


            }).setNegativeButton("No", { updatee: DialogInterface?, which: Int -> })

            val display = subdialog
            display.create()
            display.show()
            false


        }


        //for "typing.."  message
        etMsg.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
//                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
//                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.

                if (count > 0) {
                    val dbThreaddetais = FirebaseDatabase.getInstance().getReference("Thread-Details")
                    dbThreaddetais.run { child(threadId).child("typing").child(auth.currentUser?.uid).setValue(true) }
                } else {
                    FirebaseDatabase.getInstance().getReference("Thread-Details").child(threadId).child("typing").child(auth.currentUser?.uid).removeValue()
                }
            }

        })


        //for lastseen and online offline
        val dbonline = FirebaseDatabase.getInstance().getReference("User").child(userId)

        dbonline.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {
//                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onDataChange(data: DataSnapshot?) {
//                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.


                val userData = data?.getValue(User::class.java)
                if (!userData?.online.isNullOrEmpty()) {
                    ulastseen.setText("online")
                } else if (userData?.lastSeen != null) {
                    val sfd: SimpleDateFormat = SimpleDateFormat("dd-MM-yy HH:mm")
                    var formatedTime = sfd.format(userData?.lastSeen)
                    ulastseen.setText(formatedTime)
                }


            }

        })

///for green symbol online offline
        val thread = FirebaseDatabase.getInstance().getReference("Thread-Details").child(threadId).child("typing").child(userId)
        thread.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {
//                            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onDataChange(p0: DataSnapshot?) {
                val chatref = p0?.getValue(Boolean::class.java)
                println(".....++++++++++++++++" + chatref)

                if (p0?.getValue(Boolean::class.java) == true) {

                    tvTyping.visibility = View.VISIBLE

                } else {
                    tvTyping.visibility = View.GONE
                }
            }


        })


        val mAuth = FirebaseAuth.getInstance()
        val currentUser = mAuth.currentUser?.uid
        idLocal = mAuth.currentUser!!.uid

        PICK_IMAGE_REQUEST



        rvMsg.setHasFixedSize(true)
        rvMsg.isNestedScrollingEnabled

        fun ChatActivity.getAdapterItemCount() = mAdapter!!.itemCount
        rvMsg.layoutManager = LinearLayoutManager(this@ChatActivity) as RecyclerView.LayoutManager?
        mAdapter = object : FirebaseRecyclerAdapter<Chat, RecyclerView.ViewHolder>(Chat::class.java,
                R.layout.list_msg, RecyclerView.ViewHolder::class.java, newRef) {
            @SuppressLint("Range")
            public override fun populateViewHolder(viewholder: RecyclerView.ViewHolder?, chat: Chat?, position: Int) {
///................................message set here...................................................................

                if (!chat?.msg.isNullOrEmpty()) {

                    val viewHolder1 = viewholder as myHolder
                    viewHolder1.tvMsg.setText(chat?.msg)


                    val sTime = chat?.time
                    val sfd: SimpleDateFormat = SimpleDateFormat("dd-MM-yy HH:mm")
                    var formatedTime = sfd.format(sTime)
                    idSev = chat?.userId!!
                    viewHolder1.tvTime.setText(formatedTime)
                    if (idSev.equals(idLocal)) {
                        viewHolder1.linear.gravity = Gravity.END
                        viewHolder1.tvMsg.setBackgroundResource(R.drawable.et_shape)
                    } else {
                        viewHolder1.linear.gravity = Gravity.START
                        viewHolder1.tvMsg.setBackgroundResource(R.drawable.et_shape2)
                    }

                    viewHolder1.tvMsg.setOnLongClickListener {

                        val subdialog: AlertDialog.Builder = AlertDialog.Builder(this@ChatActivity)
                        subdialog.setTitle("Are you want to sure delete?").setPositiveButton("Yes", { update: DialogInterface?, which: Int ->

                            dbRefChat?.child(threadId)
                                    ?.child(getRef(viewHolder1.adapterPosition).key)
                                    ?.removeValue()
                            storageReference!!.child(threadId!!)
                                    .child(getRef(viewHolder1.adapterPosition).key)
                                    .delete().addOnSuccessListener(object : OnSuccessListener<Void> {
                                override fun onSuccess(p0: Void?) {
                                    Toast.makeText(applicationContext, "Message deleted", Toast.LENGTH_SHORT).show()
                                }

                            })


                        }).setNegativeButton("No", { updatee: DialogInterface?, which: Int -> })

                        val display = subdialog
                        display.create()
                        display.show()
                        false


                    }


                }
//                   .......................Img path here.............................................................
                else if (!chat?.imgPath.isNullOrEmpty()) {

                   // toast("holder 1 selected")

                    val viewHolder2 = viewholder as myHolder1

                    val ivPath = chat?.imgPath

                    Glide.with(viewHolder2.ivImg.getContext())
                            .load(ivPath)
                            .into(viewHolder2.ivImg);

                    viewHolder2.ivImg.setOnLongClickListener {


                        val subdialog: AlertDialog.Builder = AlertDialog.Builder(this@ChatActivity)
                        subdialog.setTitle("Are you want to sure delete?").setPositiveButton("Yes", { update: DialogInterface?, which: Int ->

                            dbRefChat?.child(threadId)
                                    ?.child(getRef(viewHolder2.adapterPosition).key)
                                    ?.removeValue()
                            storageReference!!.child(threadId!!)
                                    .child(getRef(viewHolder2.adapterPosition).key)
                                    .delete().addOnSuccessListener(object : OnSuccessListener<Void> {
                                override fun onSuccess(p0: Void?) {
                                    Toast.makeText(applicationContext, "Image deleted", Toast.LENGTH_SHORT).show()
                                }

                            })

                        }).setNegativeButton("No", { updatee: DialogInterface?, which: Int -> })

                        val display = subdialog
                        display.create()
                        display.show()
                        false
                    }

                    val sTime = chat?.time
                    val sfd: SimpleDateFormat = SimpleDateFormat("dd-MM-yy HH:mm")
                    var formatedTime = sfd.format(sTime)
                    idSev = chat?.userId!!
                    viewHolder2.tvTime.setText(formatedTime)
                    if (idSev.equals(idLocal)) {
                        viewHolder2.linear.gravity = Gravity.END
                        viewHolder2.ivImg.setBackgroundResource(R.drawable.et_shape)
                    } else {
                        viewHolder2.linear.gravity = Gravity.START
                        viewHolder2.ivImg.setBackgroundResource(R.drawable.et_shape2)
                    }

                }

                //................................Audio path here................................................................................
                else if (!chat?.audioPath.isNullOrEmpty()) {

                    val viewHolder3 = viewholder as myHolder2



                    if (idSev.equals(idLocal)) {
                        viewHolder3.voice.gravity = Gravity.END
                        viewHolder3.simpleExoPlayerView.setBackgroundResource(R.drawable.et_shape)

                    } else {
                        viewHolder3.voice.gravity = Gravity.START
                        viewHolder3.simpleExoPlayerView.setBackgroundResource(R.drawable.et_shape2)
                    }

                    val sTime = chat?.time
                    val sfd: SimpleDateFormat = SimpleDateFormat("dd-MM-yy HH:mm")
                    var formatedTime = sfd.format(sTime)
                    idSev = chat?.userId!!
                    viewHolder3.tvTime.setText(formatedTime)

                    //Exo player starts here
                    fun buildHttpDataSourceFactor(bandwidthMeter: DefaultBandwidthMeter): DefaultHttpDataSourceFactory {
                        return DefaultHttpDataSourceFactory(Util.getUserAgent(this@ChatActivity,
                                getString(R.string.app_name)), bandwidthMeter)
                    }

                    var videoTrackSelectionFactory: TrackSelection.Factory = AdaptiveTrackSelection.Factory(bandwidthMeter)
                    var player: SimpleExoPlayer = ExoPlayerFactory.newSimpleInstance(this@ChatActivity,
                            DefaultTrackSelector(videoTrackSelectionFactory),
                            DefaultLoadControl())

                    viewHolder3.simpleExoPlayerView.player = player

                    var dataSourceFactory: DataSource.Factory = DefaultDataSourceFactory(this@ChatActivity,
                            bandwidthMeter,
                            buildHttpDataSourceFactor(bandwidthMeter))
                    var videoSource: MediaSource = ExtractorMediaSource(Uri.parse(chat.audioPath), dataSourceFactory,
                            DefaultExtractorsFactory(), Handler(), null)
                    player.prepare(videoSource)

                    //Exo player end here............

                    viewHolder3.voice.setOnClickListener {

                        val subdialog: AlertDialog.Builder = AlertDialog.Builder(this@ChatActivity)
                        subdialog.setTitle("Are you want to sure delete?").setPositiveButton("Yes", { update: DialogInterface?, which: Int ->

                            dbRefChat?.child(threadId)
                                    ?.child(getRef(viewHolder3.adapterPosition).key)
                                    ?.removeValue()
                            storageReference!!.child(threadId!!)
                                    .child(getRef(viewHolder3.adapterPosition).key)
                                    .delete().addOnSuccessListener(object : OnSuccessListener<Void> {
                                override fun onSuccess(p0: Void?) {
                                    Toast.makeText(applicationContext, "Audio Succesfully deleted", Toast.LENGTH_SHORT).show()
                                }

                            })

                        }).setNegativeButton("No", { updatee: DialogInterface?, which: Int -> })

                        val display = subdialog
                        display.create()
                        display.show()
                    }

                }
//                ...............................filepath set here.....................................................................
                else if (!chat?.filePath.isNullOrEmpty()) {

                    val viewHolder4 = viewholder as myHolder3
                    val sTime = chat?.time
                    val sfd: SimpleDateFormat = SimpleDateFormat("dd-MM-yy HH:mm")
                    var formatedTime = sfd.format(sTime)
                    idSev = chat?.userId!!
                    viewHolder4.tvTime.setText(formatedTime)

                    if (idSev.equals(idLocal)) {
                        viewHolder4.fileLayout.gravity = Gravity.END
                        viewHolder4.sublayout.gravity = Gravity.END
                        viewHolder4.file.setBackgroundResource(R.drawable.et_shape)
                    } else {
                        viewHolder4.fileLayout.gravity = Gravity.START
                        viewHolder4.sublayout.gravity = Gravity.START
                        viewHolder4.file.setBackgroundResource(R.drawable.et_shape2)
                    }


                    viewHolder4.download.setOnClickListener {
                        toast("Download Starting....")
                        var dwnld = storageRef.child(threadId + "/" + getRef(viewHolder4.adapterPosition).key)
                        println(">>>>>>>>>>>>>>>>>>>>>>" + dwnld)
                        storageRef.
                                child(threadId + "/" + getRef(viewHolder4.adapterPosition).key).
                                downloadUrl.addOnSuccessListener(object : OnSuccessListener<Uri> {
                            override fun onSuccess(p0: Uri?) {
//                                TODO("not implemented") //To change body of created functions use File | Settings | File Templates
// ...........................................here get http url........:p0.
                                toast("Downloaded")
                                val uri = Uri.parse(p0.toString())
                                println("???????????????????" + uri)
                                val target = Intent(Intent.ACTION_VIEW)
                                target.setData(uri)
                                target.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                                startActivity(target)


                            }


                        })


                    }

/////////////////////////...............FILE LAYOUTS SET HERE.................................
                    viewHolder4.fileLayout.setOnLongClickListener {

                        val subdialog: AlertDialog.Builder = AlertDialog.Builder(this@ChatActivity)
                        subdialog.setTitle("Are you want to sure delete?").setPositiveButton("Yes", { update: DialogInterface?, which: Int ->

                            dbRefChat?.child(threadId)
                                    ?.child(getRef(viewHolder4.adapterPosition).key)
                                    ?.removeValue()
                            storageReference!!.child(threadId!!)
                                    .child(getRef(viewHolder4.adapterPosition).key)
                                    .delete().addOnSuccessListener(object : OnSuccessListener<Void> {
                                override fun onSuccess(p0: Void?) {
                                    Toast.makeText(applicationContext, "File Succesfully deleted", Toast.LENGTH_SHORT).show()
                                }

                            })


                        }).setNegativeButton("No", { updatee: DialogInterface?, which: Int -> })

                        val display = subdialog
                        display.create()
                        display.show()
                        false


                    }
                }
                //............................contact set here....................................................................
                else if (!chat?.contact?.name.isNullOrEmpty()) {

                    val viewHolder5 = viewholder as myHolder4
                    val sTime = chat?.time
                    val sfd: SimpleDateFormat = SimpleDateFormat("dd-MM-yy HH:mm")
                    var formatedTime = sfd.format(sTime)
                    idSev = chat?.userId!!
                    viewHolder5.tvTime.setText(formatedTime)
                    viewHolder5.tvName.setText(chat.contact?.name)
                    viewHolder5.tvNumber.setText(chat.contact?.number)

                    if (idSev.equals(idLocal)) {
                        viewHolder5.tvLayout.gravity = Gravity.END
                    } else {
                        viewHolder5.tvLayout.gravity = Gravity.START
                    }

                    viewHolder5.tvName.setOnLongClickListener {

                        val subdialog: AlertDialog.Builder = AlertDialog.Builder(this@ChatActivity)
                        subdialog.setTitle("Are you want to sure delete?").setPositiveButton("Yes", { update: DialogInterface?, which: Int ->

                            //                            val dlt = dbRefChat.child(threadId).child(getRef(viewHolder5.adapterPosition).key).removeValue()
//                            println(dlt)


                            dbRefChat?.child(threadId)
                                    ?.child(getRef(viewHolder5.adapterPosition).key)
                                    ?.removeValue()
                            storageReference!!.child(threadId!!)
                                    .child(getRef(viewHolder5.adapterPosition).key)
                                    .delete().addOnSuccessListener(object : OnSuccessListener<Void> {
                                override fun onSuccess(p0: Void?) {
                                    Toast.makeText(applicationContext, "Contact succesfully deleted", Toast.LENGTH_SHORT).show()
                                }

                            })


                        }).setNegativeButton("No", { updatee: DialogInterface?, which: Int -> })

                        val display = subdialog
                        display.create()
                        display.show()
                        false

                    }


                }
//                .............................location set here.........................................
                else {


                    val viewHolder6 = viewholder as myHolder5
                    val sTime = chat?.time
                    val sfd: SimpleDateFormat = SimpleDateFormat("dd-MM-yy HH:mm")
                    var formatedTime = sfd.format(sTime)
                    idSev = chat?.userId!!
                    viewHolder6.tvTime.setText(formatedTime)

                    if (idSev.equals(idLocal)) {
                        viewHolder6.mvLayout.gravity = Gravity.END
                    } else {
                        viewHolder6.mvLayout.gravity = Gravity.START
                    }


                    val mlat = chat.location?.lat
                    val mlong = chat.location?.long
                    viewHolder6.mvMap.setOnClickListener {


                        val intent = Intent(Intent.ACTION_VIEW,
                                Uri.parse("https//www.google.com/maps/search/?api=1&query=$mlat,$mlong"))
                        intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity")
                        startActivity(intent)

                    }


                    viewHolder6.mvMap.setOnLongClickListener {

                        val subdialog: AlertDialog.Builder = AlertDialog.Builder(this@ChatActivity)
                        subdialog.setTitle("Are you want to sure delete?").setPositiveButton("Yes", { update: DialogInterface?, which: Int ->

                            //                            val dlt = dbRefChat.child(threadId).child(getRef(viewHolder6.adapterPosition).key).removeValue()
//                            println(dlt)

                            dbRefChat?.child(threadId)
                                    ?.child(getRef(viewHolder6.adapterPosition).key)
                                    ?.removeValue()
                            storageReference!!.child(threadId!!)
                                    .child(getRef(viewHolder6.adapterPosition).key)
                                    .delete().addOnSuccessListener(object : OnSuccessListener<Void> {
                                override fun onSuccess(p0: Void?) {
                                    Toast.makeText(applicationContext, "Location succesfully deleted", Toast.LENGTH_SHORT).show()
                                }

                            })


                        }).setNegativeButton("No", { updatee: DialogInterface?, which: Int -> })

                        val display = subdialog
                        display.create()
                        display.show()
                        false

                    }


                }


            }

            //custom holderfor functionality
            override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {

                if (viewType == 0) {
                    val userType1: View = LayoutInflater.from(parent?.getContext())
                            .inflate(R.layout.list_msg, parent, false);
                    return myHolder(userType1)
                } else if (viewType == 1) {
                    val userType2: View = LayoutInflater.from(parent?.getContext())
                            .inflate(R.layout.list_img, parent, false);
                    return myHolder1(userType2)
                } else if (viewType == 2) {
                    val userType3: View = LayoutInflater.from(parent?.getContext())
                            .inflate(R.layout.list_voice, parent, false);
                    return myHolder2(userType3)
                } else if (viewType == 3) {
                    val userType4: View = LayoutInflater.from(parent?.getContext()).inflate(R.layout.list_file, parent, false)
                    return myHolder3(userType4)
                } else if (viewType == 4) {
                    val userType5: View = LayoutInflater.from(parent?.getContext()).inflate(R.layout.list_contact, parent, false)
                    return myHolder4(userType5)
                } else {
                    val userType6: View = LayoutInflater.from(parent?.getContext()).inflate(R.layout.list_map, parent, false)
                    return myHolder5(userType6)
                }


                return super.onCreateViewHolder(parent, viewType)
            }
            //item view selection for multiiple adapter and holder

            override fun getItemViewType(position: Int): Int {
                val chat: Chat = getItem(position)
                if (!chat.msg.isNullOrEmpty()) {
                    return 0
                } else if (!chat.imgPath.isNullOrEmpty()) {
                    return 1
                } else if (!chat.audioPath.isNullOrEmpty()) {
                    return 2
                } else if (!chat.filePath.isNullOrEmpty()) {
                    return 3
                } else if (!chat?.contact?.name.isNullOrEmpty()) {
                    return 4
                } else {
                    return 5

                }

            }

        }

        //adapter for all action here

        rvMsg.adapter = mAdapter
        var adapterDataObserver: RecyclerView.AdapterDataObserver = object : RecyclerView.AdapterDataObserver() {

            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                rvMsg.scrollToPosition(getAdapterItemCount() - 1)
            }
        }
        mAdapter?.registerAdapterDataObserver(adapterDataObserver)

        btnMsg.setOnClickListener {
            var task = etMsg.text.toString()

            if (task.equals(null) || task.equals("")) {

                Toast.makeText(this@ChatActivity, "Please Enter Msg", Toast.LENGTH_LONG).show()
            } else {
                //database entry starts here..........
                val key = dbRefChat.push().key
                val msg = etMsg.text.toString()
                val ctime: Long = System.currentTimeMillis()
                val chat = Chat(msg, ctime, currentUser)

                dbRefChat.run { child(threadId).child(key).setValue(chat) }
                println("MSG........." + currentUser)
                etMsg.text.clear()
                rvMsg.adapter = mAdapter

            }
        }
        mFilename = Environment.getExternalStorageDirectory().absolutePath
        mFilename += "/recorded_audio.3gp"
        storageReference = FirebaseStorage.getInstance().getReference("Store")

        btnFile.setOnClickListener {

            customLayout.visibility = View.VISIBLE
            layoutGone.visibility = View.VISIBLE

            //recording starts here
            mRecordbtn?.setOnTouchListener(object : View.OnTouchListener {


                override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                    if (event?.action == MotionEvent.ACTION_DOWN) {
                        startRecording()
                        Toast.makeText(applicationContext, "Record Start", Toast.LENGTH_SHORT).show()
                    } else if (event?.action == MotionEvent.ACTION_UP) {
                        stopRecording()
                        Toast.makeText(applicationContext, "Record Stop", Toast.LENGTH_SHORT).show()

                        if (!mFilename.equals("")) {
                            var progressDialog = ProgressDialog(this@ChatActivity)
                            val keyId = dbRefChat.push().key
                            val audioStorage = storageReference!!.child(threadId!!).child(keyId!!)
                            println(audioStorage)
                            audioStorage.putFile(Uri.fromFile(File(mFilename))).addOnProgressListener { taskSnapshot ->
                                //calculating progress percentage
                                val progress = 100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount

                                //displaying percentage in progress dialog
                                progressDialog.setMessage("Uploading " + progress.toInt() + "%...")
                                progressDialog.show()
                            }

                                    .addOnSuccessListener(object : OnSuccessListener<UploadTask.TaskSnapshot> {
                                        override fun onSuccess(p0: UploadTask.TaskSnapshot?) {
                                            val url: String? = p0?.downloadUrl.toString()
                                            val time: Long = System.currentTimeMillis()
                                            val imgMsg = Chat(time = time, audioPath = url, userId = idLocal)
                                            dbRefChat?.run { child(threadId).child(keyId).setValue(imgMsg) }
                                            progressDialog.dismiss()

                                            println("................in success listener ....................")

                                        }
                                    })
                        }

                    }
                    return true
                }

            })

            //records end here

            img.setOnClickListener {

                customLayout.visibility = View.GONE

                val intent = Intent()
                intent.type = "image/*"
                intent.action = Intent.ACTION_PICK
                startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST)

            }
            file.setOnClickListener {
                customLayout.visibility = View.GONE
                val intent = Intent()
                intent.action = Intent.ACTION_GET_CONTENT
                intent.type = "contact/*"
                startActivityForResult(intent, 4)

            }
            contact.setOnClickListener {
                customLayout.visibility = View.GONE
                vcfFile = File(this.getExternalFilesDir(null), "generated.vcf")

                val intent = Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
                startActivityForResult(intent, 5)
            }
            camera.setOnClickListener {

                customLayout.visibility = View.GONE
                val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                startActivityForResult(cameraIntent, 1)

            }

            location.setOnClickListener {
                //                customLayout.visibility = View.GONE
                val mFusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this@ChatActivity)
                mFusedLocationClient.lastLocation
                        .addOnSuccessListener(this) { location ->
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                println("....................locaton.......................")
                                println(location.latitude)
                                println(location.longitude)
                                Toast.makeText(applicationContext, "latitude" + location.latitude, Toast.LENGTH_SHORT).show()
                                Toast.makeText(applicationContext, "longitude" + location.longitude, Toast.LENGTH_SHORT).show()
                                val lat = location.latitude
                                val long = location.longitude
                                val key1 = dbRefChat?.push()?.key
                                val time: Long = System.currentTimeMillis()
                                val location = Location(lat, long)
                                val locationmsg = Chat(time = time, location = location, userId = idLocal)
                                dbRefChat?.run { child(threadId).child(key1).setValue(locationmsg) }

                            }
                        }

            }


        }

    }

    //back button works here
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        when (item?.itemId) {
            android.R.id.home -> {
                onBackPressed()
            }
        }

        return super.onOptionsItemSelected(item)
    }
//intent fire according to the request code and data base entries here

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            val image = data?.getExtras()?.get("data") as Bitmap
            val byte: ByteArrayOutputStream = ByteArrayOutputStream()
            image.compress(Bitmap.CompressFormat.JPEG, 100, byte)

            if (image != null) {
                //displaying a progress dialog while upload is going on
                var progressDialog = ProgressDialog(this)
                progressDialog.setTitle("Uploading")
                progressDialog.show()


                val keyId = dbRefChat?.push()?.key
                val riversRef = storageReference?.child(threadId!!)?.child(keyId!!)


                val baos: ByteArrayOutputStream = ByteArrayOutputStream()
                image.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                val bytes: ByteArray = baos.toByteArray()
                riversRef?.putBytes(bytes)?.addOnProgressListener { taskSnapshot ->
                    //calculating progress percentage
                    val progress = 100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount

                    //displaying percentage in progress dialog
                    progressDialog.setMessage("Uploading " + progress.toInt() + "%...")
                }

                        ?.addOnSuccessListener(object : OnSuccessListener<UploadTask.TaskSnapshot> {
                            override fun onSuccess(p0: UploadTask.TaskSnapshot?) {
                                val url: String? = p0?.downloadUrl.toString()
                                val time: Long = System.currentTimeMillis()
                                val imgMsg = Chat(time = time, imgPath = url, userId = idLocal)
                                dbRefChat?.run { child(threadId).child(keyId).setValue(imgMsg) }
                                progressDialog.dismiss()

                            }
                        })

            }


        } else if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            fPath = data.data

            val pd = ProgressDialog(this)
            val progress = 100.0
            pd.setMessage("Uploading....")

            pd.setMessage("Uploading " + progress.toInt() + "%...")

            if (fPath != null) {
                pd.show()
                val keyId = dbRefChat?.push()?.key
                val childRef = storageReference?.child(threadId!!)?.child(keyId!!)
                //uploading the image
                val uploadTask = childRef?.putFile(fPath!!)
                uploadTask?.addOnSuccessListener(object : OnSuccessListener<UploadTask.TaskSnapshot> {

                    override fun onSuccess(p0: UploadTask.TaskSnapshot?) {
//                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.

                        pd.dismiss()
                        Toast.makeText(this@ChatActivity, "Upload successful", Toast.LENGTH_SHORT).show()
                        val pathNew: String? = p0!!.downloadUrl.toString()
                        val ctime: Long = System.currentTimeMillis()
                        val imgMsg = Chat(time = ctime, imgPath = pathNew, userId = idLocal)
                        dbRefChat?.run { child(threadId).child(keyId).setValue(imgMsg) }


                    }
                })
            }


        } else if (requestCode == 4 && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            fPath = data.data

            val pd = ProgressDialog(this)
            val progress = 100.0
            pd.setMessage("Uploading....")

            pd.setMessage("Uploading " + progress.toInt() + "%...")

            if (fPath != null) {
                pd.show()

                val keyId = dbRefChat.push().key
                val childRef = storageRef.child(threadId!!).child(keyId)
                //uploading the image
                val uploadTask = childRef.putFile(fPath!!)


                uploadTask.addOnSuccessListener(object : OnSuccessListener<UploadTask.TaskSnapshot> {

                    override fun onSuccess(p0: UploadTask.TaskSnapshot?) {
//                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.

                        pd.dismiss()

                        Toast.makeText(this@ChatActivity, "Upload successful", Toast.LENGTH_SHORT).show()

                        val pathNew: String? = p0!!.downloadUrl.toString()
                        val ctime: Long = System.currentTimeMillis()
                        val imgMsg = Chat(time = ctime, filePath = pathNew, userId = idLocal)
                        dbRefChat.run { child(threadId).child(keyId).setValue(imgMsg) }
                    }

                }).addOnFailureListener(OnFailureListener { e ->
                    pd.dismiss()

                    Toast.makeText(this@ChatActivity, "Upload Failed -> " + e, Toast.LENGTH_SHORT).show()
                    Log.d(".....", e.toString())
                })
            }


        } else if (requestCode == 5 && resultCode == Activity.RESULT_OK && data != null && data.data != null) {

            try {

                val contactUri: Uri? = data.data
                println(".............................." + contactUri)

                if (contactUri != null) {
                    //displaying a progress dialog while upload is going on
                    var progressDialog = ProgressDialog(this)
                    progressDialog.setTitle("Uploading")
//                    progressDialog.show()

                    val phone: Cursor = getContentResolver().query(contactUri, null, null, null, null);
                    if (phone.moveToFirst()) {
                        val contactNumberName: String? = phone.getString(phone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                        val contactNumber: String? = phone.getString(phone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))

                        val key1 = dbRefChat?.push()?.key

                        val time: Long = System.currentTimeMillis()
                        val contact = Contact(contactNumberName, contactNumber)
                        val contactMsg = Chat(time = time, contact = contact, userId = idLocal)
                        dbRefChat?.run { child(threadId).child(key1).setValue(contactMsg) }
                        progressDialog.dismiss()

                    }

                }
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }

    }


//    override fun onStop() {
//        super.onStop()
//        FirebaseDatabase.getInstance().getReference("Thread-Details").child(threadId).child("typing").child(auth.currentUser?.uid).removeValue()
//
//    }
////////////////////////////HOLDER FOR MSG

    class myHolder(v: View) : RecyclerView.ViewHolder(v) {

        internal var tvMsg: TextView
        internal var tvTime: TextView
        internal var linear: LinearLayout

        init {
            tvMsg = v.findViewById(R.id.tvMsg) as TextView
            tvTime = v.findViewById(R.id.tvTime) as TextView
            linear = v.findViewById(R.id.linear) as LinearLayout

        }

    }
    /////////FOr sending voice message

    fun startRecording() {
        recorder = MediaRecorder()
        recorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
        recorder?.setOutputFormat(output_formats[currentFormat])
        recorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
        recorder?.setOutputFile(mFilename)
//        recorder.setOnErrorListener(errorListener)
//        recorder.setOnInfoListener(infoListener)

        try {
            recorder?.prepare()
            recorder?.start()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }


    }

    fun stopRecording() {

        if (null != recorder) {
            try {
                recorder?.stop()
            } catch (stopException: RuntimeException) {
                //handle cleanup here
            }

//            recorder?.stop();
            recorder?.reset();
            recorder?.release();

            recorder = null;
        }


    }
    ////////////functions complete here

    //////////////////////////HOLDER FOR IMAGE SENDING
    class myHolder1(v: View) : RecyclerView.ViewHolder(v) {

        internal var tvTime: TextView
        internal var linear: LinearLayout
        internal var ivImg: ZoomableImageView//img for pic

        init {

            tvTime = v.findViewById(R.id.tvTime) as TextView
            linear = v.findViewById(R.id.linear) as LinearLayout
            ivImg = v.findViewById(R.id.iv_zoom) as ZoomableImageView

        }

    }

    ///////////////////////////HOLDER FOR VOICE MESSAGE
    class myHolder2(v: View) : RecyclerView.ViewHolder(v) {

        internal var tvTime: TextView
        internal var voice: LinearLayout
        internal var simpleExoPlayerView: SimpleExoPlayerView

        init {

            tvTime = v.findViewById(R.id.tvTime) as TextView
            voice = v.findViewById(R.id.linear) as LinearLayout
            simpleExoPlayerView = v.findViewById(R.id.mainSimpleExoPlayer) as SimpleExoPlayerView

        }

    }

    /////////////////////////////HOLDER FOR FILE
    class myHolder3(v: View) : RecyclerView.ViewHolder(v) {

        internal var tvTime: TextView
        internal var file: ImageView
        internal var download: ImageView
        internal var fileLayout: LinearLayout
        internal var sublayout: LinearLayout

        init {

            tvTime = v.findViewById(R.id.tvTime) as TextView
            download = v.findViewById(R.id.ivDownload) as ImageView
            file = v.findViewById(R.id.ivFile) as ImageView
            fileLayout = v.findViewById(R.id.layoutFile) as LinearLayout
            sublayout = v.findViewById(R.id.sublayoutfile) as LinearLayout

        }
    }

    //////////////////////////////////HOLDER FOR CONTACT
    class myHolder4(v: View) : RecyclerView.ViewHolder(v) {
        internal var tvName: TextView
        internal var tvNumber: TextView
        internal var tvTime: TextView
        internal var tvLayout: LinearLayout

        init {

            tvName = v.findViewById(R.id.tvName) as TextView
            tvNumber = v.findViewById(R.id.tvNumber) as TextView
            tvTime = v.findViewById(R.id.tvTime) as TextView
            tvLayout = v.findViewById(R.id.layout_contact) as LinearLayout
        }

    }

    //////////////////////////////////HOLDER FOR MAP
    class myHolder5(v: View) : RecyclerView.ViewHolder(v) {

        internal var mvMap: ImageView
        internal var mvLayout: LinearLayout
        internal var tvTime: TextView

        init {

            mvMap = v.findViewById(R.id.mvMap) as ImageView
            mvLayout = v.findViewById(R.id.linear_map) as LinearLayout
            tvTime = v.findViewById(R.id.tvTime) as TextView
        }

    }

//
//    override fun onStop() {
//        super.onStop()
//        FirebaseDatabase.getInstance().getReference("Thread-Details").child(threadId).child("typing").child(auth.currentUser?.uid).removeValue()
//        val sharedPref = PreferenceManager.getDefaultSharedPreferences(applicationContext)
//        sharedPref.edit().remove("userId").commit()
//    }
//
//    override fun onStart() {
//        super.onStart()
//        val sharedPref: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
//        val editor = sharedPref.edit()
//        editor.putString("userId", userId)
//        editor.commit()
//    }

    override fun onResume() {
        super.onResume()
        val sharedPref: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val editor = sharedPref.edit()
        editor.putString("userId", userId)
        editor.commit()
    }

    override fun onPause() {
        super.onPause()
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        sharedPref.edit().remove("userId").commit()
    }


    companion object {

        private val bandwidthMeter = DefaultBandwidthMeter()
    }

    internal var PICK_IMAGE_REQUEST = 111
}



