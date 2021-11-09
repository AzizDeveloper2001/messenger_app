package uz.pdp.messanger.models

class Groupmessage {
    var message: String?=null
    var sendUser: UserData? = null
    var date: Long? = null
    var key:String?=null
    var check:String?=null
    var type:String?=null

    constructor()
    constructor(message: String?, sendUser: UserData?, date: Long?, key:String?,check:String?,type:String?) {
        this.message = message
        this.sendUser = sendUser
        this.date = date
        this.key=key
        this.check=check
        this.type=type

    }

}