package com.notasapp

import android.app.Activity
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import androidx.appcompat.app.AlertDialog

// Menú de una tarea, compartido por la app y por el widget.
object TaskActions {

    fun show(activity: Activity, task: Task, onChanged: () -> Unit, onClosed: () -> Unit = {}) {
        val db = Db.get(activity)
        val labels = listOf(
            if (task.done) "Marcar como pendente" else "Marcar como feita",
            "Editar",
            "Cambiar a cor",
            "Subir",
            "Baixar",
            "Mover a outra lapela",
            "Borrar"
        )
        var followUp = false

        fun changed() {
            Widgets.refreshAll(activity)
            onChanged()
        }

        AlertDialog.Builder(activity)
            .setTitle(task.text)
            .setItems(labels.toTypedArray()) { _, which ->
                when (which) {
                    0 -> { db.setDone(task.id, !task.done); changed() }
                    1 -> activity.startActivity(TaskEditActivity.editIntent(activity, task.id))
                    2 -> { followUp = true; choosePriority(activity, task, ::changed, onClosed) }
                    3 -> { db.move(task.id, -1); changed() }
                    4 -> { db.move(task.id, 1); changed() }
                    5 -> { followUp = true; chooseTab(activity, task, ::changed, onClosed) }
                    6 -> { followUp = true; confirmDelete(activity, task, ::changed, onClosed) }
                }
            }
            .setOnDismissListener { if (!followUp) onClosed() }
            .show()
    }

    private fun choosePriority(activity: Activity, task: Task, changed: () -> Unit, onClosed: () -> Unit) {
        val items = PRIORITY_NAMES.mapIndexed { i, name -> colored("●  $name", PRIORITY_COLORS[i]) }
        AlertDialog.Builder(activity)
            .setTitle("Cor da prioridade")
            .setSingleChoiceItems(items.toTypedArray(), task.priority) { dialog, which ->
                Db.get(activity).setPriority(task.id, which)
                changed()
                dialog.dismiss()
            }
            .setOnDismissListener { onClosed() }
            .show()
    }

    private fun chooseTab(activity: Activity, task: Task, changed: () -> Unit, onClosed: () -> Unit) {
        AlertDialog.Builder(activity)
            .setTitle("Mover a")
            .setSingleChoiceItems(TAB_NAMES.toTypedArray(), task.tab) { dialog, which ->
                if (which != task.tab) {
                    Db.get(activity).moveToTab(task.id, which)
                    changed()
                }
                dialog.dismiss()
            }
            .setOnDismissListener { onClosed() }
            .show()
    }

    private fun confirmDelete(activity: Activity, task: Task, changed: () -> Unit, onClosed: () -> Unit) {
        AlertDialog.Builder(activity)
            .setTitle("Borrar a tarefa?")
            .setMessage(task.text)
            .setPositiveButton("Borrar") { _, _ ->
                Db.get(activity).delete(task.id)
                changed()
            }
            .setNegativeButton("Cancelar", null)
            .setOnDismissListener { onClosed() }
            .show()
    }

    fun colored(text: String, color: Int): CharSequence =
        SpannableString(text).apply {
            setSpan(ForegroundColorSpan(color), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
}
