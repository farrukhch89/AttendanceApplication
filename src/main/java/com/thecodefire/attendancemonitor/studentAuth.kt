package com.thecodefire.attendancemonitor

class studentAuth {
    var stuName: String? = null
    var stuEmail: String? = null
    var stuStatus: String? = null

    constructor(){}

    constructor(stuName: String?, stuEmail: String?, stuStatus: String?) {
        this.stuName = stuName
        this.stuEmail = stuEmail
        this.stuStatus = stuStatus
    }
}