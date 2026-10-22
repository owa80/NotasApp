package com.notasapp

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.speech.RecognizerIntent
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

// Alta y edición de una tarea, por texto o por voz.
class TaskEditActivity : AppCompatActivity() {
    private lateinit var input: EditText
    private lateinit var priorityGroup: RadioGroup
    private lateinit var tabGroup: RadioGroup
    private var taskId = -1L

    private val priorityIds = intArrayOf(R.id.prio_high, R.id.prio_medium, R.id.prio_low)
    private val tabIds = intArrayOf(R.id.tab_work, R.id.tab_personal, R.id.tab_other)

    private val speech = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val text = result.data
            ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            ?.firstOrNull()
            ?: return@registerForActivityResult
        val current = input.text.toString().trim()
        val joined = if (current.isEmpty()) text.replaceFirstChar { it.uppercase() } else "$current $text"
        input.setText(joined)
        input.setSelection(joined.length)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_edit)

        input = findViewById(R.id.input)
        priorityGroup = findViewById(R.id.priority_group)
        tabGroup = findViewById(R.id.tab_group)
        priorityIds.forEachIndexed { i, id ->
            findViewById<RadioButton>(id).buttonTintList = ColorStateList.valueOf(PRIORITY_COLORS[i])
        }

        taskId = intent.getLongExtra(EXTRA_ID, -1)
        val task = if (taskId >= 0) Db.get(this).task(taskId) else null
        val title = findViewById<TextView>(R.id.title)
        if (savedInstanceState == null) {
            if (task != null) {
                title.text = "Editar tarea"
                input.setText(task.text)
                input.setSelection(task.text.length)
                priorityGroup.check(priorityIds[task.priority])
                tabGroup.check(tabIds[task.tab])
            } else {
                title.text = "Nueva tarea"
                priorityGroup.check(R.id.prio_medium)
                tabGroup.check(tabIds[intent.getIntExtra(EXTRA_TAB, 0).coerceIn(0, 2)])
            }
        }

        findViewById<ImageButton>(R.id.btn_mic).setOnClickListener { listen() }
        findViewById<Button>(R.id.btn_cancel).setOnClickListener { finish() }
        findViewById<Button>(R.id.btn_save).setOnClickListener { save() }

        if (savedInstanceState == null && intent.getBooleanExtra(EXTRA_VOICE, false)) listen()
    }

    private fun listen() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            .putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            .putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-ES")
            .putExtra(RecognizerIntent.EXTRA_PROMPT, "Dicta la tarea")
        try {
            speech.launch(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "Este móvil no tiene reconocimiento de voz", Toast.LENGTH_LONG).show()
        }
    }

    private fun save() {
        val text = input.text.toString().trim()
        if (text.isEmpty()) {
            Toast.makeText(this, "Escribe o dicta la tarea", Toast.LENGTH_SHORT).show()
            return
        }
        val priority = priorityIds.indexOf(priorityGroup.checkedRadioButtonId).coerceAtLeast(0)
        val tab = tabIds.indexOf(tabGroup.checkedRadioButtonId).coerceAtLeast(0)
        val db = Db.get(this)
        val task = if (taskId >= 0) db.task(taskId) else null
        if (task != null) {
            db.update(task.id, text, priority)
            if (task.tab != tab) db.moveToTab(task.id, tab)
        } else {
            db.add(tab, text, priority)
        }
        Widgets.refreshAll(this)
        finish()
    }

    companion object {
        private const val EXTRA_ID = "id"
        private const val EXTRA_TAB = "tab"
        private const val EXTRA_VOICE = "voice"

        fun newIntent(context: Context, tab: Int, voice: Boolean): Intent =
            Intent(context, TaskEditActivity::class.java)
                .putExtra(EXTRA_TAB, tab)
                .putExtra(EXTRA_VOICE, voice)

        fun editIntent(context: Context, id: Long): Intent =
            Intent(context, TaskEditActivity::class.java).putExtra(EXTRA_ID, id)
    }
}
