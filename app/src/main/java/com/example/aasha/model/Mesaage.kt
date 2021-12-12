package com.example.aasha.model

import androidx.recyclerview.widget.DiffUtil

class Message {
    var userId: String? = null
    var text: String? = null
    var name: String? = null
    var photoUrl: String? = null
    var imageUrl: String? = null
    var date: String? = null
    var time: String? = null

    // Empty constructor for firebase serialization
    constructor()

    constructor(userId: String?, text: String?, name: String?,
                photoUrl: String?, imageUrl: String?,
                date: String?, time: String?
    ) {
        this.userId = userId
        this.text = text
        this.name = name
        this.photoUrl = photoUrl
        this.imageUrl = imageUrl
        this.date = date
        this.time = time
    }
}

object MessageDiff : DiffUtil.ItemCallback<Message>() {

    override fun areContentsTheSame(oldItem: Message, newItem: Message) = oldItem.userId == newItem.userId
    override fun areItemsTheSame(oldItem: Message, newItem: Message) = oldItem === newItem
}
