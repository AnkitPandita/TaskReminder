package com.example.taskreminder

import java.io.Serializable
import java.util.*

/**
 *
 * @author Ankit Pandita, Samuel Garn, Scott Stahlman, Yosif Munther, Zaccary Hudson
 * This is the model class for a task object
 */

class Task(var id: String) : Serializable {
    var title: String? = null
    var body: String? = null
    var date: Date? = null
}