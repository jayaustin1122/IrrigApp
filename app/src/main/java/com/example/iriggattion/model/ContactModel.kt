package com.example.iriggattion.model

class ContactModel {
    var fullName: String = ""
    var phone: String = ""

    constructor(name: String, contact: String) {
        this.fullName = name
        this.phone = contact
    }
    constructor()
}