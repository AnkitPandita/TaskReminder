package com.example.taskreminder

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {
    private lateinit var rvTasks: RecyclerView
    private lateinit var gridLayoutManager: GridLayoutManager
    private lateinit var taskList: ArrayList<Task>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        rvTasks = findViewById(R.id.rv_tasks)
        val fab = findViewById<FloatingActionButton>(R.id.floatingActionButton)
        gridLayoutManager = GridLayoutManager(this, 2)
        rvTasks.layoutManager = gridLayoutManager

        taskList = ArrayList<Task>()

        val taskAdapter = TasksAdapter(taskList)
        rvTasks.adapter = taskAdapter

        fab.setOnClickListener {
            val intent = Intent(this, EditActivity::class.java)
            startActivity(intent)
        }
    }
}