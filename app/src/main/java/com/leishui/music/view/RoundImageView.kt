package com.leishui.music.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.ImageView

open class RoundImageView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ImageView(context, attrs, defStyleAttr) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private lateinit var rawBitmap: Bitmap
    private lateinit var shader: Shader
    private var mMatrix = Matrix()


    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas?) {
        if (drawable != null) {
            val rawBitmap = getBitmap(drawable)
            if (rawBitmap != null) {
                val viewWidth = width
                val viewHeight = height
                val viewMinSize = Math.min(viewWidth, viewHeight)
                this.rawBitmap = rawBitmap
                shader = BitmapShader(this.rawBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
                mMatrix.setScale(
                    (viewMinSize / rawBitmap.width).toFloat(),
                    (viewMinSize / rawBitmap.height).toFloat()
                )
                shader.setLocalMatrix(mMatrix)
                paint.shader = shader
                val radius = viewMinSize / 2.0f
                canvas?.drawCircle(radius, radius, radius, paint)
            } else {
                super.onDraw(canvas)
            }
        }
    }

    private fun getBitmap(drawable: Drawable): Bitmap? {
        if (drawable is BitmapDrawable) {
            return drawable.bitmap
        } else if (drawable is ColorDrawable) {
            val rect = drawable.getBounds()
            val width = rect.right - rect.left
            val height = rect.bottom - rect.top
            val color = drawable.color
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            canvas.drawARGB(Color.alpha(color), Color.red(color), Color.green(color), Color.blue(color))
            return bitmap
        } else {
            return null
        }
    }
}