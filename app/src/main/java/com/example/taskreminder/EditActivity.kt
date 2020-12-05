package com.example.taskreminder

import android.app.DatePickerDialog
import android.app.PendingIntent
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
    private var myDay = 0
    private var myMonth: Int = 0
    private var myYear: Int = 0
    private var myHour: Int = 0
    private var myMinute: Int = 0
    private lateinit var calendar: Calendar
    private var oldTaskObj: Task? = null
    private lateinit var pendingIntent: PendingIntent

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
            myDay = 0
            myMonth = 0
            myYear = 0
            myHour = 0
            myMinute = 0
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
                myDay = date.date
                myMonth = date.month + 1
                myYear = date.year + 1900
                myHour = date.hours
                myMinute = date.minutes
                val minStr: String
                if (myMinute / 10 == 0) {
                    minStr = "0$myMinute"
                } else {
                    minStr = "$myMinute"
                }
                btnDateTime.text = "$myMonth/$myDay/$myYear at $myHour:$minStr"
            }
        }
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        myDay = dayOfMonth
        myYear = year
        myMonth = month + 1
        calendar = Calendar.getInstance()
        hour = calendar.get(Calendar.HOUR_OF_DAY)
        minute = calendar.get(Calendar.MINUTE)
        val timePickerDialog =
            TimePickerDialog(this, this, hour, minute, DateFormat.is24HourFormat(this))
        timePickerDialog.show()
        btnDateTime.text = "$myMonth/$myDay/$myYear"
        ibCross.visibility = View.VISIBLE
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        myHour = hourOfDay
        myMinute = minute
        val minStr: String
        if (myMinute / 10 == 0) {
            minStr = "0$myMinute"
        } else {
            minStr = "$myMinute"
        }
        btnDateTime.text = "$myMonth/$myDay/$myYear at $myHour:$minStr"
        ibCross.visibility = View.VISIBLE
    }

    override fun onBackPressed() {
        val intentBack = Intent()
        val title = etTitle.text.toString().trim()
        val body = etBody.text.toString().trim()
        val day = myDay
        val year = myYear
        val month = myMonth
        val hour = myHour
        val min = myMinute

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
                // val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
                // Log.d("Date", sdf.format(cal.time))
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