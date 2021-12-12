package com.example.aasha.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.aasha.R
import com.example.aasha.databinding.UserBinding
import com.example.aasha.model.User
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions

class UserListAdapter(
    private val options: FirebaseRecyclerOptions<User>,
    private val userName: String?
) : FirebaseRecyclerAdapter<User, RecyclerView.ViewHolder>(options) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.user, parent, false)
        return ViewHolder(
            UserBinding.bind(view)
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, model: User) {
        (holder as ViewHolder).bindData(model)
    }

    inner class ViewHolder(private val binding: UserBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bindData(item: User) {
            binding.signedUserName.text = if (item.name == userName) YOU else item.name
            binding.singedUserJoin.text = item.joinDate
            Glide.with(binding.signedUserProfile.context)
                .load(item.userPhotoUrl!!)
                .into(binding.signedUserProfile)
        }
    }

    companion object {
        private const val YOU = "You"
    }
}