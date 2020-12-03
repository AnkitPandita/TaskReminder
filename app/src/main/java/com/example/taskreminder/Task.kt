package com.example.taskreminder

import java.io.Serializable
import java.util.*

class Task(var id: String) : Serializable {
    var title: String? = null
    var body: String? = null
    var date: Date? = null
}