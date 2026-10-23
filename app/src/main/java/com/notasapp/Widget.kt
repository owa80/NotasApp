package com.notasapp

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StrikethroughSpan
import android.widget.RemoteViews
import android.widget.RemoteViewsService

// Un proveedor por tamaño, así el lanzador coloca el widget ocupando
// exactamente las celdas elegidas (columnas x filas).
open class NotasWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        ids.forEach { Widgets.update(context, manager, it, javaClass) }
        manager.notifyAppWidgetViewDataChanged(ids, R.id.list)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == Widgets.ACTION_TAB) {
            val id = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
            if (id == AppWidgetManager.INVALID_APPWIDGET_ID) return
            Widgets.setTab(context, id, intent.getIntExtra(Widgets.EXTRA_TAB, 0))
            val manager = AppWidgetManager.getInstance(context)
            Widgets.update(context, manager, id, javaClass)
            manager.notifyAppWidgetViewDataChanged(id, R.id.list)
        }
    }

    override fun onDeleted(context: Context, ids: IntArray) {
        val prefs = Widgets.prefs(context).edit()
        ids.forEach { prefs.remove("tab_$it") }
        prefs.apply()
    }
}

class NotasWidget2x2 : NotasWidget()
class NotasWidget2x3 : NotasWidget()
class NotasWidget3x2 : NotasWidget()
class NotasWidget3x3 : NotasWidget()
class NotasWidget3x4 : NotasWidget()

object Widgets {
    const val ACTION_TAB = "com.notasapp.ACTION_TAB"
    const val EXTRA_TAB = "tab"
    const val EXTRA_TASK = "task"
    const val EXTRA_ACTION = "action"
    const val ACTION_TOGGLE = "toggle"
    const val ACTION_MENU = "menu"
    const val ACTION_DELETE = "delete"

    // Tamaños disponibles (columnas x filas) y el proveedor de cada uno.
    val SIZES: Map<Pair<Int, Int>, Class<out NotasWidget>> = linkedMapOf(
        (2 to 2) to NotasWidget2x2::class.java,
        (2 to 3) to NotasWidget2x3::class.java,
        (3 to 2) to NotasWidget3x2::class.java,
        (3 to 3) to NotasWidget3x3::class.java,
        (3 to 4) to NotasWidget3x4::class.java
    )

    private val TAB_IDS = intArrayOf(R.id.tab0, R.id.tab1, R.id.tab2)

    fun prefs(context: Context) = context.getSharedPreferences("widgets", Context.MODE_PRIVATE)

    fun tab(context: Context, id: Int) = prefs(context).getInt("tab_$id", 0)

    fun setTab(context: Context, id: Int, tab: Int) {
        prefs(context).edit().putInt("tab_$id", tab).apply()
    }

    // Vuelve a dibujar todos los widgets después de cualquier cambio en las tareas.
    fun refreshAll(context: Context) {
        val manager = AppWidgetManager.getInstance(context)
        SIZES.values.forEach { cls ->
            val ids = manager.getAppWidgetIds(ComponentName(context, cls))
            if (ids.isNotEmpty()) {
                ids.forEach { update(context, manager, it, cls) }
                manager.notifyAppWidgetViewDataChanged(ids, R.id.list)
            }
        }
    }

    fun update(context: Context, manager: AppWidgetManager, id: Int, cls: Class<*>) {
        val views = RemoteViews(context.packageName, R.layout.widget)
        val current = tab(context, id)

        TAB_IDS.forEachIndexed { index, viewId ->
            val selected = index == current
            views.setInt(viewId, "setBackgroundResource", if (selected) R.drawable.tab_selected else R.drawable.tab_normal)
            views.setTextColor(viewId, if (selected) Color.parseColor("#3E2723") else Color.parseColor("#8D6E63"))
            val intent = Intent(context, cls)
                .setAction(ACTION_TAB)
                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id)
                .putExtra(EXTRA_TAB, index)
            views.setOnClickPendingIntent(
                viewId,
                PendingIntent.getBroadcast(context, id * 10 + index, intent, immutable())
            )
        }

        views.setOnClickPendingIntent(
            R.id.btn_add,
            PendingIntent.getActivity(context, id * 10 + 5, fromWidget(TaskEditActivity.newIntent(context, current, false)), immutable())
        )
        views.setOnClickPendingIntent(
            R.id.btn_mic,
            PendingIntent.getActivity(context, id * 10 + 6, fromWidget(TaskEditActivity.newIntent(context, current, true)), immutable())
        )
        views.setOnClickPendingIntent(
            R.id.empty,
            PendingIntent.getActivity(context, id * 10 + 7, fromWidget(TaskEditActivity.newIntent(context, current, false)), immutable())
        )

        val service = Intent(context, NotasWidgetService::class.java)
            .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id)
        service.data = Uri.parse(service.toUri(Intent.URI_INTENT_SCHEME))
        @Suppress("DEPRECATION")
        views.setRemoteAdapter(R.id.list, service)
        views.setEmptyView(R.id.list, R.id.empty)

        // Plantilla común para las filas; cada fila añade la tarea y la acción.
        val template = Intent(context, TaskActionActivity::class.java)
        val mutable = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_MUTABLE else 0
        views.setPendingIntentTemplate(
            R.id.list,
            PendingIntent.getActivity(context, id, template, PendingIntent.FLAG_UPDATE_CURRENT or mutable)
        )

        manager.updateAppWidget(id, views)
    }

    private fun fromWidget(intent: Intent) =
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)

    private fun immutable() = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE

    fun dotFor(priority: Int) = when (priority) {
        0 -> R.drawable.dot_red
        1 -> R.drawable.dot_yellow
        else -> R.drawable.dot_green
    }

    fun struck(text: String, done: Boolean): CharSequence {
        if (!done) return text
        return SpannableString(text).apply {
            setSpan(StrikethroughSpan(), 0, text.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }
}

class NotasWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsService.RemoteViewsFactory =
        Factory(applicationContext, intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, 0))

    private class Factory(private val context: Context, private val widgetId: Int) : RemoteViewsService.RemoteViewsFactory {
        private var items: List<Task> = emptyList()

        override fun onCreate() {}

        override fun onDataSetChanged() {
            items = Db.get(context).tasks(Widgets.tab(context, widgetId))
        }

        override fun onDestroy() {}

        override fun getCount() = items.size

        override fun getViewAt(position: Int): RemoteViews {
            val views = RemoteViews(context.packageName, R.layout.widget_row)
            val task = items.getOrNull(position) ?: return views
            views.setImageViewResource(R.id.dot, Widgets.dotFor(task.priority))
            views.setTextViewText(R.id.text, Widgets.struck(task.text, task.done))
            views.setTextColor(R.id.text, Color.parseColor(if (task.done) "#9E8F6A" else "#3E2723"))
            views.setImageViewResource(R.id.check, if (task.done) R.drawable.ic_check_on else R.drawable.ic_check_off)

            views.setOnClickFillInIntent(R.id.check, fillIn(task.id, Widgets.ACTION_TOGGLE))
            views.setOnClickFillInIntent(R.id.delete, fillIn(task.id, Widgets.ACTION_DELETE))
            views.setOnClickFillInIntent(R.id.text, fillIn(task.id, Widgets.ACTION_MENU))
            views.setOnClickFillInIntent(R.id.dot, fillIn(task.id, Widgets.ACTION_MENU))
            return views
        }

        private fun fillIn(taskId: Long, action: String) =
            Intent().putExtra(Widgets.EXTRA_TASK, taskId).putExtra(Widgets.EXTRA_ACTION, action)

        override fun getLoadingView(): RemoteViews? = null

        override fun getViewTypeCount() = 1

        override fun getItemId(position: Int) = items.getOrNull(position)?.id ?: position.toLong()

        override fun hasStableIds() = true
    }
}
