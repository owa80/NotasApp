package com.notasapp

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout

class MainActivity : AppCompatActivity() {
    private lateinit var tabs: TabLayout
    private lateinit var empty: TextView
    private lateinit var adapter: TaskAdapter
    private lateinit var touchHelper: ItemTouchHelper

    private val currentTab get() = tabs.selectedTabPosition.coerceAtLeast(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tabs = findViewById(R.id.tabs)
        empty = findViewById(R.id.tv_empty)
        TAB_NAMES.forEach { tabs.addTab(tabs.newTab().setText(it)) }
        tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) = refresh()
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        adapter = TaskAdapter(
            onClick = { TaskActions.show(this, it, onChanged = { refresh() }) },
            onToggle = {
                Db.get(this).setDone(it.id, !it.done)
                Widgets.refreshAll(this)
                refresh()
            },
            onDelete = { TaskActions.askDelete(this, it, onChanged = { refresh() }) },
            onStartDrag = { touchHelper.startDrag(it) }
        )
        val list = findViewById<RecyclerView>(R.id.list)
        list.layoutManager = LinearLayoutManager(this)
        list.adapter = adapter

        // Arrastrar para mover una tarea arriba o abajo.
        touchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0) {
            override fun onMove(rv: RecyclerView, from: RecyclerView.ViewHolder, to: RecyclerView.ViewHolder): Boolean {
                adapter.swap(from.bindingAdapterPosition, to.bindingAdapterPosition)
                return true
            }

            override fun onSwiped(holder: RecyclerView.ViewHolder, direction: Int) {}

            override fun clearView(rv: RecyclerView, holder: RecyclerView.ViewHolder) {
                super.clearView(rv, holder)
                Db.get(this@MainActivity).reorder(adapter.items.map { it.id })
                Widgets.refreshAll(this@MainActivity)
            }
        })
        touchHelper.attachToRecyclerView(list)

        findViewById<ImageButton>(R.id.btn_add).setOnClickListener {
            startActivity(TaskEditActivity.newIntent(this, currentTab, false))
        }
        findViewById<ImageButton>(R.id.btn_mic).setOnClickListener {
            startActivity(TaskEditActivity.newIntent(this, currentTab, true))
        }
        findViewById<Button>(R.id.btn_widget).setOnClickListener {
            startActivity(Intent(this, SizePickerActivity::class.java))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.add(0, MENU_CLEAR_DONE, 0, "Borrar as feitas")
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId != MENU_CLEAR_DONE) return super.onOptionsItemSelected(item)
        val tab = currentTab
        val count = Db.get(this).tasks(tab).count { it.done }
        if (count == 0) {
            AlertDialog.Builder(this)
                .setMessage("Non hai tarefas feitas en ${TAB_NAMES[tab]}.")
                .setPositiveButton("Entendido", null)
                .show()
        } else {
            AlertDialog.Builder(this)
                .setTitle("Borrar as tarefas feitas?")
                .setMessage("Borraranse $count tarefas feitas de ${TAB_NAMES[tab]}.")
                .setPositiveButton("Borrar") { _, _ ->
                    Db.get(this).deleteDone(tab)
                    Widgets.refreshAll(this)
                    refresh()
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
        return true
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun refresh() {
        adapter.submit(Db.get(this).tasks(currentTab))
        empty.visibility = if (adapter.items.isEmpty()) View.VISIBLE else View.GONE
    }

    companion object {
        private const val MENU_CLEAR_DONE = 1
    }
}
