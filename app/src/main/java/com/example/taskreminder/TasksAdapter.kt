package com.example.taskreminder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat

class TasksAdapter(private val taskList: List<Task>) :
    RecyclerView.Adapter<TasksAdapter.ViewHolder>() {

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView
        val textView2: TextView
        val textView3: TextView

        init {
            // Define click listener for the ViewHolder's View.
            textView = view.findViewById(R.id.textView)
            textView2 = view.findViewById(R.id.textView2)
            textView3 = view.findViewById(R.id.textView3)
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.layout_item_rv_tasks, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        val title = taskList.get(position).title
        val body = taskList.get(position).body
        val date = taskList.get(position).date
        if (title == null || title.contentEquals("")) {
            viewHolder.textView.visibility = View.GONE
        } else {
            viewHolder.textView.text = title
        }
        if (body == null || body.contentEquals("")) {
            viewHolder.textView2.visibility = View.GONE
        } else {
            viewHolder.textView2.text = body
        }
        if (date == null) {
            viewHolder.textView3.visibility = View.GONE
        } else {
            val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm")
            //Log.d("Hiiii", sdf.format(cal.time))
            viewHolder.textView3.text = sdf.format(date)
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = taskList.size

}
