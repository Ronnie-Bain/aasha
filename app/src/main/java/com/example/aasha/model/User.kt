package com.example.aasha.model

class User {
    var name: String? = null
    var userPhotoUrl: String? = null
    var joinDate: String? = null

    // Empty constructor for firebase serialization
    constructor()

    constructor(name: String?, photoUrl: String?, joinDate: String?) {
        this.name = name
        this.userPhotoUrl = photoUrl
        this.joinDate = joinDate
    }
}