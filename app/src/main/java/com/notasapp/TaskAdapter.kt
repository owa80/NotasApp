package com.notasapp

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TaskAdapter(
    private val onClick: (Task) -> Unit,
    private val onToggle: (Task) -> Unit,
    private val onDelete: (Task) -> Unit,
    private val onStartDrag: (RecyclerView.ViewHolder) -> Unit
) : RecyclerView.Adapter<TaskAdapter.Holder>() {

    val items = mutableListOf<Task>()

    @SuppressLint("NotifyDataSetChanged")
    fun submit(tasks: List<Task>) {
        items.clear()
        items.addAll(tasks)
        notifyDataSetChanged()
    }

    fun swap(from: Int, to: Int) {
        val task = items.removeAt(from)
        items.add(to, task)
        notifyItemMoved(from, to)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        Holder(LayoutInflater.from(parent.context).inflate(R.layout.row_task, parent, false))

    override fun getItemCount() = items.size

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: Holder, position: Int) {
        val task = items[position]
        holder.dot.setImageResource(Widgets.dotFor(task.priority))
        holder.text.text = Widgets.struck(task.text, task.done)
        holder.text.alpha = if (task.done) 0.5f else 1f
        holder.check.setImageResource(if (task.done) R.drawable.ic_check_on else R.drawable.ic_check_off)
        holder.itemView.setOnClickListener { onClick(task) }
        holder.check.setOnClickListener { onToggle(task) }
        holder.delete.setOnClickListener { onDelete(task) }
        holder.handle.setOnTouchListener { _, event ->
            if (event.actionMasked == MotionEvent.ACTION_DOWN) onStartDrag(holder)
            false
        }
    }

    class Holder(view: View) : RecyclerView.ViewHolder(view) {
        val handle: ImageView = view.findViewById(R.id.handle)
        val dot: ImageView = view.findViewById(R.id.dot)
        val text: TextView = view.findViewById(R.id.text)
        val check: ImageView = view.findViewById(R.id.check)
        val delete: ImageView = view.findViewById(R.id.delete)
    }
}
