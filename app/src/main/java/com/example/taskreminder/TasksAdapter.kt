package com.example.taskreminder

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import java.text.SimpleDateFormat

/**
 *
 * @author Ankit Pandita, Samuel Garn, Scott Stahlman, Yosif Munther, Zaccary Hudson
 * This is the adapter for the recycler view in the MainActivity that displays all the saved tasks.
 */

class TasksAdapter(private val activity: Activity, private val taskList: ArrayList<Task>, private val sharedPreferences: SharedPreferences) :
    RecyclerView.Adapter<TasksAdapter.ViewHolder>() {

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.textView)
        val tvBody: TextView = view.findViewById(R.id.textView2)
        val tvDate: TextView = view.findViewById(R.id.textView3)
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.layout_item_rv_tasks, viewGroup, false)
        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    @SuppressLint("SimpleDateFormat")
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        val title = taskList[position].title
        val body = taskList[position].body
        val date = taskList[position].date
        if (title == null || title.contentEquals("")) {
            viewHolder.tvTitle.visibility = View.GONE
        } else {
            viewHolder.tvTitle.visibility = View.VISIBLE
            viewHolder.tvTitle.text = title
        }
        if (body == null || body.contentEquals("")) {
            viewHolder.tvBody.visibility = View.GONE
        } else {
            viewHolder.tvBody.visibility = View.VISIBLE
            viewHolder.tvBody.text = body
        }
        if (date == null) {
            viewHolder.tvDate.visibility = View.GONE
        } else {
            viewHolder.tvDate.visibility = View.VISIBLE
            val sdf = SimpleDateFormat("MM/dd/yyyy HH:mm")
            viewHolder.tvDate.text = sdf.format(date)
        }

        viewHolder.itemView.setOnClickListener {
            val intent = Intent(activity, EditActivity::class.java)
            intent.putExtra(EditActivity.KEY_TASK, taskList[position])
            taskList.removeAt(position)
            editSharedPref()
            intent.putExtra(EditActivity.KEY_POS, position)
            activity.startActivityForResult(intent, MainActivity.REQUEST_CODE)
        }

        viewHolder.itemView.setOnLongClickListener {
            val alert = AlertDialog.Builder(this.activity)
            alert.setMessage("Do you want to delete this task?")
            alert.setPositiveButton("Yes") { _, _ ->
                taskList.removeAt(position)
                editSharedPref()
                notifyDataSetChanged()
                Toast.makeText(activity, "Task deleted!", Toast.LENGTH_SHORT).show()
            }
            alert.setNegativeButton("No") { _, _ ->

            }
            alert.show()
            return@setOnLongClickListener true
        }

    }

    // for editing shared preferences
    private fun editSharedPref(){
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.putString(MainActivity.KEY_SP, Gson().toJson(taskList))
        editor.apply()
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = taskList.size

}
