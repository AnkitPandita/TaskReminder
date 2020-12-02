package com.example.taskreminder

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.format.DateFormat
import android.view.MenuItem
import android.view.View
import android.widget.*
import kotlinx.android.synthetic.main.activity_edit.*
import java.util.*

class EditActivity : AppCompatActivity(), DatePickerDialog.OnDateSetListener,
    TimePickerDialog.OnTimeSetListener {
    private lateinit var etTitle: EditText
    private lateinit var etBody: EditText
    lateinit var btnDateTime: Button
    lateinit var ibCross: ImageButton
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

    companion object {
        const val RESULT_CODE = 5000
        const val KEY_TITLE = "title"
        const val KEY_BODY = "body"
        const val KEY_DAY = "day"
        const val KEY_YEAR = "year"
        const val KEY_MONTH = "month"
        const val KEY_HOUR = "hour"
        const val KEY_MIN = "min"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)
        etTitle = et_title
        etBody = et_body
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
            calendar = Calendar.getInstance()
            day = calendar.get(Calendar.DAY_OF_MONTH)
            month = calendar.get(Calendar.MONTH)
            year = calendar.get(Calendar.YEAR)
            val datePickerDialog = DatePickerDialog(this, this, year, month, day)
            datePickerDialog.show()
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
        var minStr: String
        if (myMinute / 10 == 0) {
            minStr = "0$myMinute"
        } else {
            minStr = "$myMinute"
        }
        btnDateTime.text = "$myMonth/$myDay/$myYear at $myHour:$minStr"
        ibCross.visibility = View.VISIBLE
    }

    override fun onBackPressed() {
        var intent = Intent()
        intent.putExtra(KEY_TITLE, etTitle.text.toString().trim())
        intent.putExtra(KEY_BODY, etBody.text.toString().trim())
        intent.putExtra(KEY_DAY, myDay)
        intent.putExtra(KEY_YEAR, myYear)
        intent.putExtra(KEY_MONTH, myMonth)
        intent.putExtra(KEY_HOUR, myHour)
        intent.putExtra(KEY_MIN, myMinute)
        setResult(RESULT_CODE, intent)
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
        }
        return true
    }

    /*fun DatePicker.getDate(): Date {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, dayOfMonth)
        return calendar.time
    }*/
}