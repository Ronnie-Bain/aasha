package com.example.aasha.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.setPadding
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.aasha.R
import com.example.aasha.databinding.ImgaeMessageBinding
import com.example.aasha.databinding.MessageBinding
import com.example.aasha.databinding.SenderImageLayoutBinding
import com.example.aasha.databinding.SenderMessageBinding
import com.example.aasha.model.Message
import com.example.aasha.model.MessageDiff
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.resources.MaterialResources.getDimensionPixelSize
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlin.properties.Delegates

private enum class EnumViewHolder {
    SENDER_MESSAGE_VIEW_HOLDER,
    MESSAGE_VIEW_HOLDER,
    IMAGE_VIEW_HOLDER,
    SENDER_IMAGE_VIEW_HOLDER
}

class MessageAdapter(
    private val options: FirebaseRecyclerOptions<Message>,
    private val userId: String,
    private val progressBar: ProgressBar
) : FirebaseRecyclerAdapter<Message, RecyclerView.ViewHolder>(options) {

    private lateinit var viewHolder: EnumViewHolder

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_SENDER_TEXT -> {
                val view = inflater.inflate(R.layout.sender_message, parent, false)
                SenderMessageViewHolder(
                    SenderMessageBinding.bind(view)
                )
            }
            VIEW_TYPE_TEXT -> {
                val view = inflater.inflate(R.layout.message, parent, false)
                MessageViewHolder(
                    MessageBinding.bind(view)
                )
            }
            VIEW_TYPE_IMAGE -> {
                val view = inflater.inflate(R.layout.imgae_message, parent, false)
                ImageMessageViewHolder(
                    ImgaeMessageBinding.bind(view)
                )
            }
            else -> {
                val view = inflater.inflate(R.layout.sender_image_layout, parent, false)
                SenderImageViewHolder(
                    SenderImageLayoutBinding.bind(view)
                )
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val optionSnapshot = options.snapshots[position]
        return if (optionSnapshot.text != null) {
            if (optionSnapshot.userId == userId) {
                viewHolder = EnumViewHolder.SENDER_MESSAGE_VIEW_HOLDER
                VIEW_TYPE_SENDER_TEXT
            } else {
                viewHolder = EnumViewHolder.MESSAGE_VIEW_HOLDER
                VIEW_TYPE_TEXT
            }
        } else {
            if (optionSnapshot.userId == userId) {
                viewHolder = EnumViewHolder.SENDER_IMAGE_VIEW_HOLDER
                VIEW_TYPE_SENDER_IMAGE
            } else {
                viewHolder = EnumViewHolder.IMAGE_VIEW_HOLDER
                VIEW_TYPE_IMAGE
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, model: Message) {
        val isSameUser =
            if (position > 0) model.userId == getItem(position - 1).userId else false
        val isSameDate =
            if (position > 0) model.date == getItem(position - 1).date else false

        when (viewHolder) {
            EnumViewHolder.SENDER_MESSAGE_VIEW_HOLDER ->
                (holder as SenderMessageViewHolder).bindData(model, isSameDate, isSameUser)
            EnumViewHolder.MESSAGE_VIEW_HOLDER ->
                (holder as MessageViewHolder).bindData(model, isSameDate, isSameUser)
            EnumViewHolder.IMAGE_VIEW_HOLDER ->
                (holder as ImageMessageViewHolder).bindData(model)
            EnumViewHolder.SENDER_IMAGE_VIEW_HOLDER ->
                (holder as SenderImageViewHolder).bindData(model)
        }
        progressBar.visibility = View.GONE
    }

    inner class MessageViewHolder(private val binding: MessageBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bindData(item: Message,
             isSameDate: Boolean,
             isSameUser: Boolean,
        ) {
            val text = item.text!!
            if (text.length < 20) {
                binding.messageLayout.orientation = LinearLayout.HORIZONTAL
                binding.messageText.setPadding(20)
            }
            binding.messageText.text = text
            binding.messageTime.text = item.time

            if (isSameUser && isSameDate) {
                binding.viewholderLayout.setPadding(0)
                binding.userImage.visibility = View.INVISIBLE
                binding.messageLayout.setBackgroundResource(R.drawable.message_background)
            } else {
                binding.viewholderLayout.setPadding(0, 16, 0, 0)
                if (item.photoUrl != null) {
                    loadImageIntoView(binding.userImage, item.photoUrl!!)
                } else {
                    binding.userImage.setImageResource(R.drawable.ic_user_profile)
                }
            }

            if (isSameDate) {
                binding.messageDate.visibility = View.GONE
            } else binding.messageDate.text = item.date.toString().replace('-',' ')
        }
    }

    inner class SenderMessageViewHolder(private val binding: SenderMessageBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bindData(item: Message,
                     isSameDate: Boolean,
                     isSameUser: Boolean,
        ) {
            val text = item.text!!
            if (text.length < 20) {
                binding.senderMessageLayout.orientation = LinearLayout.HORIZONTAL
                binding.senderMessageText.setPadding(20)
            }
            binding.senderMessageText.text = text
            binding.senderMessageTime.text = item.time

            if (isSameUser && isSameDate) {
                binding.senderViewholderLayout.setPadding(0)
                binding.senderMessageLayout.setBackgroundResource(R.drawable.message_blue_background)
                binding.senderUserImage.visibility = View.INVISIBLE
            } else {
                binding.senderViewholderLayout.setPadding(0, 16, 0, 0)
                if (item.photoUrl != null) {
                    loadImageIntoView(binding.senderUserImage, item.photoUrl!!)
                } else {
                    binding.senderUserImage.setImageResource(R.drawable.ic_user_profile)
                }
            }

            if (isSameDate) {
                binding.senderMessageDate.visibility = View.GONE
            } else binding.senderMessageDate.text = item.date.toString().replace('-',' ')
        }
    }

    inner class ImageMessageViewHolder(private val binding: ImgaeMessageBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bindData(item: Message) {
            loadImageIntoView(binding.sendImage, item.imageUrl!!)
            binding.imageMessageTime.text = item.time
            if (item.photoUrl != null) {
                loadImageIntoView(binding.senderImage, item.photoUrl!!)
            } else {
                binding.senderImage.setImageResource(R.drawable.ic_user_profile)
            }
        }
    }

    inner class SenderImageViewHolder(private val binding: SenderImageLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bindData(item: Message) {
            loadImageIntoView(binding.senderImage, item.imageUrl!!)
            binding.imageSenderMessageTime.text = item.time
            if (item.photoUrl != null) {
                loadImageIntoView(binding.senderProfile, item.photoUrl!!)
            } else {
                binding.senderProfile.setImageResource(R.drawable.ic_user_profile)
            }
        }
    }

    private fun loadImageIntoView(view: ImageView, url: String) {
        if (url.startsWith("gs://")) {
            val storageReference = Firebase.storage.getReferenceFromUrl(url)
            storageReference.downloadUrl
                .addOnSuccessListener { uri ->
                    val downloadedUrl = uri.toString()
                    Glide.with(view.context)
                        .load(downloadedUrl)
                        .into(view)
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Firebase photo download failed", e)
                }
        } else {
            Glide.with(view.context).load(url).into(view)
        }
    }

    companion object {
        private const val TAG = "MessageAdapter"
        private const val ANONYMOUS = "anonymous"
        private const val VIEW_TYPE_TEXT = 1
        private const val VIEW_TYPE_SENDER_TEXT = 2
        private const val VIEW_TYPE_IMAGE = 3
        private const val VIEW_TYPE_SENDER_IMAGE = 4
    }
}

