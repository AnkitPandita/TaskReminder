package com.example.taskreminder

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import ru.ifr0z.notify.work.NotifyWork
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {
    private lateinit var rvTasks: RecyclerView
    private lateinit var gridLayoutManager: GridLayoutManager
    private lateinit var taskList: ArrayList<Task>
    private lateinit var taskAdapter: TasksAdapter
    private lateinit var sharedPref: SharedPreferences

    companion object {
        const val REQUEST_CODE = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        rvTasks = findViewById(R.id.rv_tasks)
        val fab = findViewById<FloatingActionButton>(R.id.floatingActionButton)
        gridLayoutManager = GridLayoutManager(this, 2)
        rvTasks.layoutManager = gridLayoutManager
        sharedPref = getSharedPreferences(
            getString(R.string.preference_file_key), Context.MODE_PRIVATE
        )
        taskList = ArrayList<Task>()

        taskAdapter = TasksAdapter(taskList)
        rvTasks.adapter = taskAdapter

        fab.setOnClickListener {
            val intent = Intent(this, EditActivity::class.java)
            startActivityForResult(intent, 100)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE && resultCode == EditActivity.RESULT_CODE) {
            val title = data?.extras?.getString(EditActivity.KEY_TITLE)
            val body = data?.extras?.getString(EditActivity.KEY_BODY)
            val day = data?.extras?.getInt(EditActivity.KEY_DAY)
            val year = data?.extras?.getInt(EditActivity.KEY_YEAR)
            val month = data?.extras?.getInt(EditActivity.KEY_MONTH)
            val hour = data?.extras?.getInt(EditActivity.KEY_HOUR)
            val min = data?.extras?.getInt(EditActivity.KEY_MIN)
            val cal = Calendar.getInstance();
            if (day != null) {
                cal.set(Calendar.DAY_OF_MONTH, day)
            }
            if (year != null) {
                cal.set(Calendar.YEAR, year)
            }
            if (month != null) {
                cal.set(Calendar.MONTH, month - 1)
            }
            if (hour != null) {
                cal.set(Calendar.HOUR, hour)
            }
            if (min != null) {
                cal.set(Calendar.MINUTE, min)
            }
            cal.set(Calendar.SECOND, 0)
            // val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
            // Log.d("Hiiii", sdf.format(cal.time))
            val dateTime = cal.time
            if (title != null || body != null) {
                val task = Task(UUID.randomUUID().toString());
                task.title = title
                task.body = body
                task.date = dateTime
                taskList.add(task)
                taskAdapter.notifyDataSetChanged()
            }
            val customTime = cal.timeInMillis
            val currentTime = System.currentTimeMillis()
            if (customTime > currentTime) {
                val notificationData = Data.Builder().putInt(NotifyWork.NOTIFICATION_ID, 0).build()
                val delay = customTime - currentTime
                scheduleNotification(delay, notificationData)
                Log.d("Msg","Success")
            } else {
                Log.d("Msg","Failed")
                // val errorNotificationSchedule = getString(R.string.notification_schedule_error)
                // Snackbar.make(coordinator_l, errorNotificationSchedule, Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun scheduleNotification(delay: Long, data: Data) {
        val notificationWork = OneTimeWorkRequest.Builder(NotifyWork::class.java)
            .setInitialDelay(delay, TimeUnit.MILLISECONDS).setInputData(data).build()

        val instanceWorkManager = WorkManager.getInstance(this)
        instanceWorkManager.beginUniqueWork(
            NotifyWork.NOTIFICATION_WORK,
            ExistingWorkPolicy.REPLACE, notificationWork
        ).enqueue()
    }

}