package com.b0966031908gmail.happypacker.utils

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class CanvasView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var drawPath = Path()
    private var drawPaint = Paint()
    private var canvasPaint = Paint(Paint.DITHER_FLAG)

    private var drawCanvas: Canvas? = null
    private var canvasBitmap: Bitmap? = null

    private val paths = mutableListOf<PathData>()
    private var currentColor = Color.BLACK
    private var currentStrokeWidth = 20f
    private var isEraserMode = false
    private var fillMode = false

    data class PathData(
        val path: Path,
        val paint: Paint
    )

    init {
        setupDrawing()
        setLayerType(LAYER_TYPE_SOFTWARE, null)  // 關鍵：啟用軟體渲染，支援透明
    }

    private fun setupDrawing() {
        drawPaint.apply {
            color = currentColor
            isAntiAlias = true
            strokeWidth = currentStrokeWidth
            style = Paint.Style.STROKE
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        // 關鍵：使用 ARGB_8888 支援透明度
        canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        drawCanvas = Canvas(canvasBitmap!!)
        drawCanvas?.drawColor(Color.WHITE)
    }

    override fun onDraw(canvas: Canvas) {
        // 先畫白色背景
        canvas.drawColor(Color.WHITE)
        // 再畫 bitmap（可能有透明區域）
        canvas.drawBitmap(canvasBitmap!!, 0f, 0f, canvasPaint)

        // 畫當前路徑
        for (pathData in paths) {
            canvas.drawPath(pathData.path, pathData.paint)
        }

        canvas.drawPath(drawPath, drawPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val touchX = event.x
        val touchY = event.y

        // 填充模式
        if (fillMode && event.action == MotionEvent.ACTION_DOWN) {
            floodFill(touchX.toInt(), touchY.toInt(), currentColor)
            return true
        }

        // 繪圖模式
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                drawPath.moveTo(touchX, touchY)
            }
            MotionEvent.ACTION_MOVE -> {
                drawPath.lineTo(touchX, touchY)
            }
            MotionEvent.ACTION_UP -> {
                val paint = Paint(drawPaint)
                paths.add(PathData(Path(drawPath), paint))
                drawCanvas?.drawPath(drawPath, drawPaint)
                drawPath.reset()
            }
            else -> return false
        }

        invalidate()
        return true
    }

    fun setColor(color: Int) {
        currentColor = color
        drawPaint.color = currentColor
        isEraserMode = false
        drawPaint.xfermode = null
        drawPaint.strokeWidth = currentStrokeWidth
    }

    fun setStrokeWidth(width: Float) {
        currentStrokeWidth = width
        if (isEraserMode) {
            drawPaint.strokeWidth = currentStrokeWidth * 2.5f
        } else {
            drawPaint.strokeWidth = currentStrokeWidth
        }
    }

    fun clearCanvas() {
        paths.clear()
        drawPath.reset()
        canvasBitmap?.eraseColor(Color.TRANSPARENT)  // 清成透明
        drawCanvas?.drawColor(Color.WHITE)  // 再填白色
        invalidate()
    }

    fun setEraser() {
        isEraserMode = true
        // 關鍵：使用 PorterDuff.Mode.CLEAR 實現真正的擦除
        drawPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        drawPaint.strokeWidth = currentStrokeWidth * 2.5f
    }

    fun setPen() {
        isEraserMode = false
        drawPaint.xfermode = null
        drawPaint.color = currentColor
        drawPaint.strokeWidth = currentStrokeWidth
    }

    fun setFillMode(enabled: Boolean) {
        fillMode = enabled
    }

    fun fillWithColor(color: Int) {
        drawCanvas?.drawColor(color)
        invalidate()
    }

    fun floodFill(x: Int, y: Int, fillColor: Int) {
        if (canvasBitmap == null) return

        val bitmap = canvasBitmap!!
        if (x < 0 || x >= bitmap.width || y < 0 || y >= bitmap.height) return

        val targetColor = bitmap.getPixel(x, y)
        if (targetColor == fillColor) return

        val queue = mutableListOf<Point>()
        queue.add(Point(x, y))

        val visited = mutableSetOf<String>()

        while (queue.isNotEmpty()) {
            val point = queue.removeAt(0)
            val px = point.x
            val py = point.y

            if (px < 0 || px >= bitmap.width || py < 0 || py >= bitmap.height) continue

            val key = "$px,$py"
            if (visited.contains(key)) continue
            visited.add(key)

            if (bitmap.getPixel(px, py) != targetColor) continue

            bitmap.setPixel(px, py, fillColor)

            queue.add(Point(px + 1, py))
            queue.add(Point(px - 1, py))
            queue.add(Point(px, py + 1))
            queue.add(Point(px, py - 1))
        }

        invalidate()
    }

    fun getBitmap(): Bitmap? {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)

        // 畫上所有內容
        canvas.drawBitmap(canvasBitmap!!, 0f, 0f, canvasPaint)

        for (pathData in paths) {
            canvas.drawPath(pathData.path, pathData.paint)
        }

        return bitmap
    }
}