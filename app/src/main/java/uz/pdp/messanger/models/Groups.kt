package uz.pdp.messanger.models

import java.io.Serializable

class Groups :Serializable {
    var groupname: String? = null
    var groupimage: String? = null
    var key:String?=null
    var members:ArrayList<String>?=null


    constructor()
    constructor(groupname: String?, groupimage: String?, key: String?,members:ArrayList<String>?) {
        this.groupname = groupname
        this.groupimage = groupimage
        this.key=key
        this.members=members

    }
}