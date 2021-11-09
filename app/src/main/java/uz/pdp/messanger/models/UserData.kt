package uz.pdp.messanger.models

import java.io.Serializable

class UserData:Serializable {
    var photoUrl: String? = null
    var firstname: String? = null
    var lastname: String? = null
    var uid: String? = null
    var phonenumber:String?=null
    var on_of:String?=null




    constructor()
    constructor(
        photoUrl: String?,
        firstname: String?,
        lastname: String?,
        uid: String?,
        phonenumber:String?=null,
        on_of: String?,

        ) {
        this.photoUrl = photoUrl
        this.firstname = firstname
        this.lastname = lastname
        this.uid = uid
        this.phonenumber=phonenumber
        this.on_of = on_of

    }
}