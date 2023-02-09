package org.listenbrainz.android.util

import CacheService
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import org.listenbrainz.android.App
import org.listenbrainz.android.R
import org.listenbrainz.android.data.sources.Constants.PROFILE_PIC

class CircularPercentageChart @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    var radius = 0
    var centerX = 0
    var centerY = 0
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val rect = RectF()
    private var percentage = 0f

    var imageBitmap: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.profile_photo)
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        var cache= App.context?.let { CacheService<Bitmap>(it, PROFILE_PIC) }
        var data= cache?.getBitmap()
        if (data != null) {
            try {
                imageBitmap = data
            } catch (e: Exception) {
                imageBitmap = BitmapFactory.decodeResource(resources, R.drawable.profile_photo)
            }
        }
            else{
                imageBitmap = BitmapFactory.decodeResource(resources, R.drawable.profile_photo)
            }
        drawCircle(canvas)
        drawImage(canvas)
    }

    private fun drawCircle(canvas: Canvas) {
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 15f
        paint.color = Color.parseColor("#8F00FF")

        radius = (width / 2) - 10
        centerX = width / 2
        centerY = height / 2
        rect.set(
            centerX - radius.toFloat(),
            centerY - radius.toFloat(),
            centerX + radius.toFloat(),
            centerY + radius.toFloat()
        )
        canvas.drawArc(rect, -90f, 360 * (percentage / 100), false, paint)
    }

    private fun drawImage(canvas: Canvas) {
        val bitmap = getResizedBitmap(imageBitmap, width, height)
        val centerX = width / 2
        val centerY = height / 2
        val path = Path().apply {
            addCircle(centerX.toFloat(), centerY.toFloat(), radius.toFloat() - 8, Path.Direction.CCW)
        }
        canvas.clipPath(path)
        canvas.drawBitmap(bitmap, centerX - bitmap.width / 2.toFloat(), centerY - bitmap.height / 2.toFloat(), paint)

    }

    private fun getResizedBitmap(bitmap: Bitmap, newWidth: Int, newHeight: Int): Bitmap {
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, false)
    }

    fun setPercentage(percentage: Float) {
        this.percentage = percentage
        invalidate()
    }
}