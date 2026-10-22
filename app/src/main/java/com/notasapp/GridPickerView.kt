package com.notasapp

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

// Rejilla de 3 columnas x 4 filas: se arrastra el dedo para elegir el tamaño,
// igual que al insertar una tabla en Office.
class GridPickerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    var columns = 3
        private set
    var rows = 3
        private set
    var onChange: ((Int, Int) -> Unit)? = null

    private val density = resources.displayMetrics.density
    private val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#FFF176") }
    private val empty = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#EEEEEE") }
    private val border = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2 * density
        color = Color.parseColor("#BDBDBD")
    }
    private val selectedBorder = Paint(border).apply { color = Color.parseColor("#F9A825") }
    private val rect = RectF()

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        setMeasuredDimension(width, width * MAX_ROWS / MAX_COLUMNS)
    }

    override fun onDraw(canvas: Canvas) {
        val cell = width.toFloat() / MAX_COLUMNS
        val gap = 6 * density
        val radius = 10 * density
        for (r in 0 until MAX_ROWS) {
            for (c in 0 until MAX_COLUMNS) {
                // Las columnas se cuentan desde la derecha: el widget se dibuja arriba a la derecha.
                val left = width - (c + 1) * cell
                rect.set(left + gap, r * cell + gap, left + cell - gap, (r + 1) * cell - gap)
                val selected = c < columns && r < rows
                canvas.drawRoundRect(rect, radius, radius, if (selected) fill else empty)
                canvas.drawRoundRect(rect, radius, radius, if (selected) selectedBorder else border)
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                parent?.requestDisallowInterceptTouchEvent(true)
                val cell = width.toFloat() / MAX_COLUMNS
                val c = ((width - event.x) / cell).toInt().coerceIn(0, MAX_COLUMNS - 1) + 1
                val r = (event.y / cell).toInt().coerceIn(0, MAX_ROWS - 1) + 1
                if (c != columns || r != rows) {
                    columns = c
                    rows = r
                    invalidate()
                    onChange?.invoke(columns, rows)
                }
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    companion object {
        const val MAX_COLUMNS = 3
        const val MAX_ROWS = 4
    }
}
