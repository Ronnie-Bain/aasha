package com.example.aasha

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.FileProvider
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.aasha.adapter.MessageAdapter
import com.example.aasha.databinding.ActivityMainBinding
import com.example.aasha.model.Message
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity(),
    PopupMenu.OnMenuItemClickListener,
    ProfileDialogFragment.NoticeDialogListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseDatabase
    private lateinit var adapter: MessageAdapter

    private val openDocument = registerForActivityResult(OpenDocumentContract()) { uri ->
        if (uri != null) onImageSelected(uri)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

        //createNotificationChannel()
        /*if (BuildConfig.DEBUG) {
            Firebase.auth.useEmulator("10.0.2.2", 9099)
            Firebase.database.useEmulator("10.0.2.2", 9000)
            Firebase.storage.useEmulator("10.0.2.2", 9199)
        }*/

        auth = Firebase.auth
        if (auth.currentUser == null) {
            startActivity(Intent(this, SignInActivity::class.java))
            finish()
            return
        }
        db = Firebase.database
        val messageRef = db.reference.child(MESSAGE_CHILD)

        val options = FirebaseRecyclerOptions.Builder<Message>()
            .setQuery(messageRef, Message::class.java)
            .build()

        adapter = MessageAdapter(options, auth.currentUser!!.uid, binding.progressBar)
        val manager = LinearLayoutManager(this)
        manager.stackFromEnd = true
        binding.chatRecyclerView.layoutManager = manager
        binding.chatRecyclerView.adapter = adapter
        binding.chatRecyclerView.itemAnimator = null
        adapter.registerAdapterDataObserver(
            ScrollToBottomObserver(binding.chatRecyclerView, adapter, manager)
        )

        val uri = getUserProfilePicture()
        val userProfileImageView = binding.userProfile
        if (uri != null) {
            Glide.with(userProfileImageView.context)
                .load(uri)
                .into(userProfileImageView)
        } else {
            userProfileImageView.setImageResource(R.drawable.ic_user_profile)
        }

        binding.messageTypeBox.addTextChangedListener(
            ButtonObserver(binding.messageSendButton)
        )

        binding.messageSendButton.setOnClickListener {
            val message = Message(
                auth.currentUser!!.uid,
                binding.messageTypeBox.text.toString(),
                getUserName(),
                getUserProfilePicture(),
                null,
                SimpleDateFormat("dd-MMMM-yy", Locale.getDefault()).format(Date()),
                SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
            )
            //messageRef.push().setValue(message)
            db.reference.child(MESSAGE_CHILD).push().setValue(message)
            binding.messageTypeBox.setText("")
        }

        binding.imageSendButton.setOnClickListener {
            openDocument.launch(arrayOf("image/*"))
        }

        binding.optionsMenu.setOnClickListener {
            showPopup(binding.optionsMenu)
        }

        binding.userProfile.setOnClickListener {
            ProfileDialogFragment().show(supportFragmentManager, "ProfileDialog")
        }
    }

    override fun onStart() {
        super.onStart()
        if (auth.currentUser == null) {
            startActivity(Intent(this, SignInActivity::class.java))
            finish()
            return
        }
    }

    public override fun onPause() {
        adapter.stopListening()
        super.onPause()
    }

    public override fun onResume() {
        super.onResume()
        adapter.startListening()
    }

    private fun showPopup(view: View) {
        PopupMenu(this, view).apply {
            // MainActivity implements OnMenuItemClickListener
            setOnMenuItemClickListener(this@MainActivity)
            inflate(R.menu.options_menu)
            show()
        }
    }

    override fun onMenuItemClick(p0: MenuItem?): Boolean {
        return when (p0?.itemId) {
            R.id.mute_chat -> {
                Toast.makeText(
                    this,
                    "Notification service is yet to be implemented",
                    Toast.LENGTH_LONG
                ).show()
                true
            }
            R.id.users -> {
                goToConnUsersActivity()
                true
            }
            R.id.share -> {
                launchShareIntent()
                true
            }
            else -> false
        }
    }

    private fun launchShareIntent() {
        val app: ApplicationInfo = applicationContext.applicationInfo
        val originalApk = app.publicSourceDir
        try {
            //Make new directory in new location
            var tempFile: File = File(externalCacheDir.toString() + "/ExtractedApk")
            //If directory doesn't exists create new
            if (!tempFile.isDirectory) if (!tempFile.mkdirs()) return
            //rename apk file to app name
            tempFile = File(tempFile.path + "/" + getString(app.labelRes).replace(" ", "") + ".apk")
            //If file doesn't exists create new
            if (!tempFile.exists()) {
                if (!tempFile.createNewFile()) {
                    return
                }
            }
            //Copy file to new location
            val inp: InputStream = FileInputStream(originalApk)
            val out: OutputStream = FileOutputStream(tempFile)
            val buf = ByteArray(1024)
            var len: Int
            while (inp.read(buf).also { len = it } > 0) {
                out.write(buf, 0, len)
            }
            inp.close()
            out.close()
            //Open share dialog
            val intent = Intent(Intent.ACTION_SEND)
            //MIME type for apk, might not work in bluetooth share as it doesn't support apk MIME type

            intent.type = "application/vnd.android.package-archive"
            intent.putExtra(
                Intent.EXTRA_STREAM, FileProvider.getUriForFile(
                    this, BuildConfig.APPLICATION_ID + ".provider", File(tempFile.path)
                )
            )
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(intent)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun onDialogPositiveClick(
        dialog: ProfileDialogFragment,
        userName: String?,
        textView: TextView,
        userImageUrl: Uri?,
        imageView: ImageView
    ) {
        val userKey = auth.currentUser!!.uid
        // Updating the current user name
        if (!userName.isNullOrBlank()) {
            // Updating auth current user
            val userUpdates = userProfileChangeRequest {
                displayName = userName
            }
            auth.currentUser!!.updateProfile(userUpdates)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Update the user database
                        db.reference.child(USER_LIST_CHILD)
                            .child(userKey)
                            .child(USER_NAME)
                            .setValue(userName)
                            .addOnSuccessListener {
                                Toast.makeText(
                                    this,
                                    "User updated",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        textView.text = userName
                    }
                }
                .addOnFailureListener {
                    Log.e(TAG, "Updating user name failed", it)
                    Toast.makeText(
                        this,
                        "There was an error updating user name",
                        Toast.LENGTH_LONG
                    ).show()
                }
        }

        // Updating the current user image
        if (userImageUrl != null) {
            val userUpdates = userProfileChangeRequest {
                photoUri = userImageUrl
            }
            auth.currentUser!!.updateProfile(userUpdates)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        db.reference.child(USER_LIST_CHILD)
                            .child(userKey)
                            .child(USER_PHOTO)
                            .setValue(userImageUrl)
                            .addOnSuccessListener {
                                Toast.makeText(
                                    this,
                                    "User data updated",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }
                }
                .addOnFailureListener {
                    Log.e(TAG, "Updating user image failed", it)
                    Toast.makeText(
                        this,
                        "Unable to update user profile image",
                        Toast.LENGTH_LONG
                    ).show()
                }
        }
    }

    override fun onDialogNeutralClick(dialog: ProfileDialogFragment) {
        AuthUI.getInstance().signOut(this)
        startActivity(Intent(this, SignInActivity::class.java))
        finish()
    }

    override fun getUserObject() = auth.currentUser!!

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun onImageSelected(uri: Uri) {
        Log.d(TAG, "OnImageSelected uri: $uri")
        val user = auth.currentUser
        val tempMessage = Message(auth.currentUser!!.uid,
                null,
                getUserName(),
                getUserProfilePicture(),
                LOADING_IMAGE_URL,
                SimpleDateFormat("dd-MMMM-yy", Locale.getDefault()).format(Date()),
                SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        )
        db.reference
            .child(MESSAGE_CHILD)
            .push()
            .setValue(
                tempMessage,
                DatabaseReference.CompletionListener { databaseError, databaseReference ->
                    if (databaseError != null) {
                        Log.w(TAG, "Unable to write database.", databaseError.toException())
                        return@CompletionListener
                    }
                    val key = databaseReference.key
                    Log.w(TAG, "Key: $key")
                    val storageReference = Firebase.storage
                        .getReference(user!!.uid)
                        .child(key!!)
                        .child(uri.lastPathSegment!!)
                    putImageInStorage(storageReference, uri, key)
                }
            )
    }

    private fun putImageInStorage(storageReference: StorageReference, uri: Uri, key: String?) {
        storageReference.putFile(uri)
            .addOnSuccessListener(this) { taskSnapshot -> // After the image loads, get a public download url to put it to a message
                taskSnapshot.metadata!!.reference!!.downloadUrl
                    .addOnSuccessListener { uri ->
                        val message = Message(auth.currentUser!!.uid,
                            null,
                            getUserName(),
                            getUserProfilePicture(),
                            uri.toString(),
                            SimpleDateFormat("dd-MMMM-yy", Locale.getDefault()).format(Date()),
                            SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
                        )
                        db.reference
                            .child(MESSAGE_CHILD)
                            .child(key!!)
                            .setValue(message)
                    }
            }
            .addOnFailureListener(this) { e ->
                Log.w(TAG, "Unable upload file to database", e)
            }
    }

    private fun getUserName(): String? {
        val user = auth.currentUser
        return if (user != null) user.displayName else ANONYMOUS
    }

    private fun getUserProfilePicture() = auth.currentUser?.photoUrl?.toString()

    private fun goToConnUsersActivity() {
        val intent = Intent(this, ConnectedUsersActivity::class.java)
        startActivity(intent)
    }

    companion object {
        private const val TAG = "MainActivity"
        private const val USER_LIST_CHILD = "users"
        private const val USER_PHOTO = "userPhotoUrl"
        private const val USER_NAME = "name"
        const val MESSAGE_CHILD = "messages"
        const val  ANONYMOUS = "anonymous"
        const val CHANNEL_ID = "Notification Channel"
        private const val LOADING_IMAGE_URL = "https://www.google.com/images/spin-32.gif"
    }
}