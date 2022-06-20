package com.thecodefire.attendancemonitor

class student {
    var name : String? = null
    var email : String? = null
    var address: String? = null
    var course: String? = null
    var dob: String? = null

    constructor(){}

    constructor(name: String?, email: String?, address: String?, course: String?, dob: String?) {
        this.name = name
        this.email = email
        this.address = address
        this.course = course
        this.dob = dob
    }
}