package uz.pdp.messanger.models

import java.io.Serializable

class Chennel :Serializable {
    var groupname: String? = null
    var groupimage: String? = null
    var key:String?=null
    var admin:String?=null
    var members:ArrayList<String>?=null


    constructor()
    constructor(groupname: String?, groupimage: String?, key: String?,admin:String?,members:ArrayList<String>?) {
        this.groupname = groupname
        this.groupimage = groupimage
        this.key=key
        this.admin=admin
        this.members=members

    }
}