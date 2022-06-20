package com.thecodefire.attendancemonitor

class Lecturer {
    var address: String? = null
    var email: String? = null
    var faculty: String? = null
    var name: String? = null

    constructor(){}

    constructor(name: String?, email: String?, address: String?, faculty: String?) {
        this.name = name
        this.email = email
        this.address = address
        this.faculty = faculty
    }
}