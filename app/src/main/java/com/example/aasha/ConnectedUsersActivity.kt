package com.example.aasha

import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.aasha.SignInActivity.Companion.USER_LIST_CHILD
import com.example.aasha.adapter.UserListAdapter
import com.example.aasha.databinding.ActivityConnectedUsersBinding
import com.example.aasha.model.User
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class ConnectedUsersActivity : AppCompatActivity() {

    private lateinit var db: FirebaseDatabase
    private lateinit var auth: FirebaseAuth
    private lateinit var adapter: UserListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityConnectedUsersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = Firebase.database
        auth = Firebase.auth
        val userRf = db.reference.child(USER_LIST_CHILD)

        val options = FirebaseRecyclerOptions.Builder<User>()
            .setQuery(userRf, User::class.java)
            .build()

        adapter = UserListAdapter(options, auth.currentUser!!.displayName)
        val manager = LinearLayoutManager(this)
        manager.stackFromEnd = true
        binding.totalUsersRecyclerView.layoutManager = manager
        binding.totalUsersRecyclerView.adapter = adapter
    }

    public override fun onPause() {
        adapter.stopListening()
        super.onPause()
    }

    public override fun onResume() {
        super.onResume()
        adapter.startListening()
    }
}