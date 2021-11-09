package uz.pdp.messanger.models

class Message {

    var message: String?=null
    var from: String? = null
    var to: String? = null
    var date: Long? = null
    var check:String?=null
    var key:String?=null
    var type:String?=null

    constructor()
    constructor(message: String?, from: String?, to: String?, date: Long?,check:String,key:String?,type:String) {
        this.message = message
        this.from = from
        this.to = to
        this.date = date
        this.check=check
        this.key=key
        this.type=type
    }
}