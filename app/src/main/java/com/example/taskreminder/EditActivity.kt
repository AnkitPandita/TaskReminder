package com.example.taskreminder

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_edit.*
import java.util.*
import java.util.concurrent.TimeUnit

/**
 *
 * @author Ankit Pandita, Samuel Garn, Scott Stahlman, Yosif Munther, Zaccary Hudson
 * This is the activity where user edits the task details (title, note, and date & time)
 */

class EditActivity : AppCompatActivity(), DatePickerDialog.OnDateSetListener,
    TimePickerDialog.OnTimeSetListener {
    private lateinit var etTitle: EditText
    private lateinit var etBody: EditText
    private lateinit var btnDateTime: Button
    private lateinit var ibCross: ImageButton
    private var day = 0
    private var month: Int = 0
    private var year: Int = 0
    private var hour: Int = 0
    private var minute: Int = 0
    private var taskDay = 0
    private var taskMonth: Int = 0
    private var taskYear: Int = 0
    private var taskHour: Int = 0
    private var taskMinute: Int = 0
    private lateinit var calendar: Calendar
    private var oldTaskObj: Task? = null

    companion object {
        const val RESULT_CODE = 5000
        const val KEY_TASK = "task"
        const val KEY_POS = "pos"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)
        etTitle = et_title
        etBody = et_body
        calendar = Calendar.getInstance()
        ibCross = findViewById(R.id.ib_cross)
        ibCross.setOnClickListener {
            taskDay = 0
            taskMonth = 0
            taskYear = 0
            taskHour = 0
            taskMinute = 0
            btnDateTime.text = "Set Reminder"
            ibCross.visibility = View.GONE
        }
        btnDateTime = findViewById(R.id.btn_set_datetime)
        btnDateTime.setOnClickListener {
            day = calendar.get(Calendar.DAY_OF_MONTH)
            month = calendar.get(Calendar.MONTH)
            year = calendar.get(Calendar.YEAR)
            val datePickerDialog = DatePickerDialog(this, this, year, month, day)
            datePickerDialog.show()
        }
        if (intent.extras?.getSerializable(KEY_TASK) != null) {
            oldTaskObj = intent.extras?.getSerializable(KEY_TASK) as Task?
            etTitle.setText(oldTaskObj?.title)
            etBody.setText(oldTaskObj?.body)
            if (oldTaskObj?.date != null) {
                val date: Date = oldTaskObj!!.date!!
                taskDay = date.date
                taskMonth = date.month + 1
                taskYear = date.year + 1900
                taskHour = date.hours
                taskMinute = date.minutes
                val minStr: String
                if (taskMinute / 10 == 0) {
                    minStr = "0$taskMinute"
                } else {
                    minStr = "$taskMinute"
                }
                btnDateTime.text = "$taskMonth/$taskDay/$taskYear at $taskHour:$minStr"
            }
        }
    }

    // gets called after selecting date
    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        taskDay = dayOfMonth
        taskYear = year
        taskMonth = month + 1
        calendar = Calendar.getInstance()
        hour = calendar.get(Calendar.HOUR_OF_DAY)
        minute = calendar.get(Calendar.MINUTE)
        val timePickerDialog =
            TimePickerDialog(this, this, hour, minute, DateFormat.is24HourFormat(this))
        timePickerDialog.show()
        btnDateTime.text = "$taskMonth/$taskDay/$taskYear"
        ibCross.visibility = View.VISIBLE
    }

    // gets called after selecting time
    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        taskHour = hourOfDay
        taskMinute = minute
        val minStr: String
        if (taskMinute / 10 == 0) {
            minStr = "0$taskMinute"
        } else {
            minStr = "$taskMinute"
        }
        btnDateTime.text = "$taskMonth/$taskDay/$taskYear at $taskHour:$minStr"
        ibCross.visibility = View.VISIBLE
    }

    // gets called when pressed back
    override fun onBackPressed() {
        val intentBack = Intent()
        val title = etTitle.text.toString().trim()
        val body = etBody.text.toString().trim()
        val day = taskDay
        val year = taskYear
        val month = taskMonth
        val hour = taskHour
        val min = taskMinute

        if ((!title.contentEquals(""))
            || (!body.contentEquals(""))
        ) {
            val task: Task
            if (oldTaskObj != null) {
                task = oldTaskObj as Task
            } else {
                val id = UUID.randomUUID().toString()
                task = Task(id)
            }
            task.title = title
            task.body = body

            var dateTime: Date? = null
            val cal = Calendar.getInstance()
            if (day != 0 && year != 0 && month != 0) {
                cal.set(Calendar.DAY_OF_MONTH, day)
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, month - 1)
                cal.set(Calendar.HOUR_OF_DAY, hour)
                cal.set(Calendar.MINUTE, min)
                cal.set(Calendar.SECOND, 0)
                dateTime = cal.time
            }
            if (dateTime != null) {
                task.date = dateTime
                val customTime = cal.timeInMillis
                val currentTime = System.currentTimeMillis()
                if (customTime > currentTime) {
                    val notificationData =
                        Data.Builder().putString(NotifyWork.KEY_TAG, task.id)
                            .putString(NotifyWork.KEY_TITLE, task.title)
                            .putString(NotifyWork.KEY_SUBTITLE, task.body)
                            .putInt(NotifyWork.KEY_ID, customTime.toInt()).build()
                    val delay = customTime - currentTime
                    //cancelNotification(task.id)
                    scheduleNotification(delay, notificationData, task.id)
                    Log.d("Msg", "Success")
                } else {
                    Log.d("Msg", "Failed")
                    val errorNotificationSchedule = "Failed to schedule notification"
                    Snackbar.make(etBody, errorNotificationSchedule, Snackbar.LENGTH_LONG).show()
                }
            }
            intentBack.putExtra(KEY_TASK, task)
        }
        if (intent.extras?.getInt(KEY_POS) != null) {
            intentBack.putExtra(KEY_POS, intent.extras!!.getInt(KEY_POS))
        }
        setResult(RESULT_CODE, intentBack)
        finish()
    }

    // for scheduling notification
    private fun scheduleNotification(delay: Long, data: Data, workId: String) {
        val notificationWork = OneTimeWorkRequest.Builder(NotifyWork::class.java)
            .setInitialDelay(delay, TimeUnit.MILLISECONDS).setInputData(data).build()

        val instanceWorkManager = WorkManager.getInstance(this)
        instanceWorkManager.beginUniqueWork(
            workId,
            ExistingWorkPolicy.APPEND, notificationWork
        ).enqueue()
    }

    /*private fun cancelNotification(workId: String){
        val instanceWorkManager = WorkManager.getInstance(this)
        instanceWorkManager.cancelUniqueWork(workId)
    }*/

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
        }
        return true
    }

}