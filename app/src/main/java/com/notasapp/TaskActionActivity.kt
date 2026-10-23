package com.notasapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

// Pantalla transparente que se abre al tocar una tarea en el widget.
class TaskActionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val db = Db.get(this)
        val task = db.task(intent.getLongExtra(Widgets.EXTRA_TASK, -1))
        if (task == null) {
            finish()
            return
        }
        if (intent.getStringExtra(Widgets.EXTRA_ACTION) == Widgets.ACTION_TOGGLE) {
            db.setDone(task.id, !task.done)
            Widgets.refreshAll(this)
            finish()
            return
        }
        if (savedInstanceState == null) {
            if (intent.getStringExtra(Widgets.EXTRA_ACTION) == Widgets.ACTION_DELETE) {
                TaskActions.askDelete(this, task, onChanged = {}, onClosed = { finish() })
            } else {
                TaskActions.show(this, task, onChanged = {}, onClosed = { finish() })
            }
        }
    }
}
