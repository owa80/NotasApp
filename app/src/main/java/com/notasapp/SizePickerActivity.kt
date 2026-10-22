package com.notasapp

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

// Elige el tamaño del post-it arrastrando sobre la rejilla y lo coloca en la pantalla de inicio.
class SizePickerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_size_picker)
        title = "Tamaño del widget"

        val grid = findViewById<GridPickerView>(R.id.grid)
        val label = findViewById<TextView>(R.id.size_label)
        val place = findViewById<Button>(R.id.btn_place)

        fun show(columns: Int, rows: Int) {
            val available = Widgets.SIZES.containsKey(columns to rows)
            label.text = if (available) "$columns x $rows" else "$columns x $rows (no disponible)"
            place.isEnabled = available
        }
        grid.onChange = ::show
        show(grid.columns, grid.rows)

        place.setOnClickListener { pin(grid.columns, grid.rows) }
    }

    private fun pin(columns: Int, rows: Int) {
        val cls = Widgets.SIZES[columns to rows] ?: return
        val manager = AppWidgetManager.getInstance(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && manager.isRequestPinAppWidgetSupported) {
            manager.requestPinAppWidget(ComponentName(this, cls), null, null)
            Toast.makeText(this, "Confirma para añadir el post-it $columns x $rows", Toast.LENGTH_LONG).show()
        } else {
            AlertDialog.Builder(this)
                .setTitle("Añadir el widget")
                .setMessage(
                    "Mantén pulsado un hueco libre de la pantalla de inicio, entra en Widgets, " +
                        "busca NotasApp y arrastra \"Notas $columns x $rows\" a la esquina superior derecha."
                )
                .setPositiveButton("Entendido", null)
                .show()
        }
    }
}
