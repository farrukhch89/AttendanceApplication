package com.thecodefire.attendancemonitor

class Attendance {
    var date: String? = null
    var status: String? = null

    constructor(){}

    constructor(date: String?, status: String?) {
        this.date = date
        this.status = status
    }
}