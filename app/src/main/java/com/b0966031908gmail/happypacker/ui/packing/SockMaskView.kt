package com.b0966031908gmail.happypacker.ui.packing

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.b0966031908gmail.happypacker.R

/**
 * 襪子預覽容器（讓作品貼合襪子形狀）
 */
class SockMaskView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var maskBitmap: Bitmap? = null
    private val maskPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
    }

    init {
        // 啟用硬體加速
        setLayerType(LAYER_TYPE_HARDWARE, null)

        // 載入襪子遮罩圖
        loadMask()
    }

    /**
     * 載入襪子遮罩
     */
    private fun loadMask() {
        try {
            // 這裡使用襪子模板作為遮罩
            val drawable = ContextCompat.getDrawable(context, R.drawable.sock_template)
            drawable?.let {
                maskBitmap = Bitmap.createBitmap(
                    it.intrinsicWidth,
                    it.intrinsicHeight,
                    Bitmap.Config.ARGB_8888
                )
                val canvas = Canvas(maskBitmap!!)
                it.setBounds(0, 0, canvas.width, canvas.height)
                it.draw(canvas)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun dispatchDraw(canvas: Canvas) {
        // 創建離屏緩衝區
        val count = canvas.saveLayer(0f, 0f, width.toFloat(), height.toFloat(), null)

        // 繪製子 View（作品圖案）
        super.dispatchDraw(canvas)

        // 應用襪子遮罩
        maskBitmap?.let { mask ->
            val scaledMask = Bitmap.createScaledBitmap(mask, width, height, true)
            canvas.drawBitmap(scaledMask, 0f, 0f, maskPaint)
            scaledMask.recycle()
        }

        canvas.restoreToCount(count)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        maskBitmap?.recycle()
        maskBitmap = null
    }
}