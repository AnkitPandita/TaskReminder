package com.example.taskreminder

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlin.collections.ArrayList

/**
 *
 * @author Ankit Pandita, Samuel Garn, Scott Stahlman, Yosif Munther, Zaccary Hudson
 * This is the home screen of the application where all the saved tasks will be displayed.
 */

class MainActivity : AppCompatActivity() {
    private lateinit var rvTasks: RecyclerView
    private lateinit var gridLayoutManager: GridLayoutManager
    private lateinit var taskList: ArrayList<Task>
    private lateinit var taskAdapter: TasksAdapter
    private lateinit var sharedPreferences: SharedPreferences

    companion object {
        const val REQUEST_CODE = 100
        const val KEY_SP = "keySP"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        rvTasks = findViewById(R.id.rv_tasks)
        val fab = findViewById<FloatingActionButton>(R.id.floatingActionButton)
        gridLayoutManager = GridLayoutManager(this, 2)
        rvTasks.layoutManager = gridLayoutManager
        sharedPreferences = getSharedPreferences(
            getString(R.string.preference_file_key), Context.MODE_PRIVATE
        )

        val strTaskList = sharedPreferences.getString(KEY_SP, null)
        if (strTaskList != null) {
            val itemType = object : TypeToken<ArrayList<Task>>() {}.type
            taskList = Gson().fromJson<ArrayList<Task>>(strTaskList, itemType)
        } else {
            taskList = ArrayList()
        }
        taskAdapter = TasksAdapter(this, taskList, sharedPreferences)
        rvTasks.adapter = taskAdapter

        fab.setOnClickListener {
            val intentEdit = Intent(this, EditActivity::class.java)
            startActivityForResult(intentEdit, REQUEST_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE && resultCode == EditActivity.RESULT_CODE) {
            if (data?.extras?.getSerializable(EditActivity.KEY_TASK) != null) {
                val task: Task = data.extras?.getSerializable(EditActivity.KEY_TASK) as Task
                if (data.extras?.getInt(EditActivity.KEY_POS)!=null){
                    taskList.add(data.extras!!.getInt(EditActivity.KEY_POS), task)
                } else {
                    taskList.add(0, task)
                }
                val editor: SharedPreferences.Editor = sharedPreferences.edit()
                editor.putString(KEY_SP, Gson().toJson(taskList))
                editor.apply()
                taskAdapter.notifyDataSetChanged()
            }
        }
    }

}