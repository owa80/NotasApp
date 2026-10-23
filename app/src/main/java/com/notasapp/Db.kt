package com.notasapp

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

// Pestañas del post-it: 0 = Trabajo, 1 = Personal, 2 = Otros.
val TAB_NAMES = listOf("Traballo", "Persoal", "Outros")

// Prioridades de mayor a menor: 0 = alta (rojo), 1 = media (amarillo), 2 = baja (verde).
val PRIORITY_NAMES = listOf("Alta (vermello)", "Media (amarelo)", "Baixa (verde)")
val PRIORITY_COLORS = intArrayOf(0xFFE53935.toInt(), 0xFFFFA000.toInt(), 0xFF43A047.toInt())

data class Task(
    val id: Long,
    val tab: Int,
    val text: String,
    val priority: Int,
    val done: Boolean,
    val position: Int
)

class Db private constructor(context: Context) :
    SQLiteOpenHelper(context, "notas.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """CREATE TABLE tasks (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                tab INTEGER NOT NULL,
                text TEXT NOT NULL,
                priority INTEGER NOT NULL,
                done INTEGER NOT NULL DEFAULT 0,
                position INTEGER NOT NULL
            )"""
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}

    fun tasks(tab: Int): List<Task> {
        val out = mutableListOf<Task>()
        readableDatabase.rawQuery(
            "SELECT id, tab, text, priority, done, position FROM tasks WHERE tab = ? ORDER BY position, id",
            arrayOf(tab.toString())
        ).use { c ->
            while (c.moveToNext()) {
                out += Task(c.getLong(0), c.getInt(1), c.getString(2), c.getInt(3), c.getInt(4) != 0, c.getInt(5))
            }
        }
        return out
    }

    fun task(id: Long): Task? =
        readableDatabase.rawQuery(
            "SELECT id, tab, text, priority, done, position FROM tasks WHERE id = ?",
            arrayOf(id.toString())
        ).use { c ->
            if (c.moveToFirst()) Task(c.getLong(0), c.getInt(1), c.getString(2), c.getInt(3), c.getInt(4) != 0, c.getInt(5))
            else null
        }

    // Las tareas nuevas van al final: se ordenan por orden de entrada de arriba hacia abajo.
    fun add(tab: Int, text: String, priority: Int): Long {
        val values = ContentValues().apply {
            put("tab", tab)
            put("text", text)
            put("priority", priority)
            put("done", 0)
            put("position", nextPosition(tab))
        }
        return writableDatabase.insert("tasks", null, values)
    }

    fun update(id: Long, text: String, priority: Int) {
        set(id, ContentValues().apply {
            put("text", text)
            put("priority", priority)
        })
    }

    fun setDone(id: Long, done: Boolean) = set(id, ContentValues().apply { put("done", if (done) 1 else 0) })

    fun setPriority(id: Long, priority: Int) = set(id, ContentValues().apply { put("priority", priority) })

    fun moveToTab(id: Long, tab: Int) {
        set(id, ContentValues().apply {
            put("tab", tab)
            put("position", nextPosition(tab))
        })
    }

    fun delete(id: Long) {
        writableDatabase.delete("tasks", "id = ?", arrayOf(id.toString()))
    }

    fun deleteDone(tab: Int) {
        writableDatabase.delete("tasks", "tab = ? AND done = 1", arrayOf(tab.toString()))
    }

    // Sube (delta = -1) o baja (delta = 1) una tarea un puesto dentro de su pestaña.
    fun move(id: Long, delta: Int) {
        val task = task(id) ?: return
        val ids = tasks(task.tab).map { it.id }.toMutableList()
        val from = ids.indexOf(id)
        val to = from + delta
        if (from < 0 || to < 0 || to >= ids.size) return
        ids.removeAt(from)
        ids.add(to, id)
        reorder(ids)
    }

    fun reorder(ids: List<Long>) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            ids.forEachIndexed { index, id ->
                db.update("tasks", ContentValues().apply { put("position", index) }, "id = ?", arrayOf(id.toString()))
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    private fun set(id: Long, values: ContentValues) {
        writableDatabase.update("tasks", values, "id = ?", arrayOf(id.toString()))
    }

    private fun nextPosition(tab: Int): Int =
        readableDatabase.rawQuery(
            "SELECT COALESCE(MAX(position), -1) + 1 FROM tasks WHERE tab = ?",
            arrayOf(tab.toString())
        ).use { c -> if (c.moveToFirst()) c.getInt(0) else 0 }

    companion object {
        @Volatile
        private var instance: Db? = null

        fun get(context: Context): Db =
            instance ?: synchronized(this) {
                instance ?: Db(context.applicationContext).also { instance = it }
            }
    }
}
