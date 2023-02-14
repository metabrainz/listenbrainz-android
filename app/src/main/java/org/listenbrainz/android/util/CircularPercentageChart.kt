package org.listenbrainz.android.util

import CacheService
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import org.listenbrainz.android.App
import org.listenbrainz.android.R
import org.listenbrainz.android.data.di.user_profile
import org.listenbrainz.android.data.sources.Constants
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

    var imageBitmap: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.ic_user_profile)
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        var cache= App.context?.let { CacheService<Bitmap>(it, PROFILE_PIC) }
        var data= cache?.getBitmap()
        if (data != null) {
            try {
                imageBitmap = data
            } catch (e: Exception) {
                imageBitmap = BitmapFactory.decodeResource(resources, R.drawable.ic_user_profile)
            }
        }
            else{
                imageBitmap = BitmapFactory.decodeResource(resources, R.drawable.ic_user_profile)
            }
        drawCircle(canvas)
        drawImage(canvas)
        setPercentage()
    }

    private fun drawCircle(canvas: Canvas) {
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 20f
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
            addCircle(centerX.toFloat(), centerY.toFloat(), radius.toFloat() - 10, Path.Direction.CCW)
        }
        canvas.clipPath(path)
        canvas.drawBitmap(bitmap, centerX - bitmap.width / 2.toFloat(), centerY - bitmap.height / 2.toFloat(), paint)

    }

    private fun getResizedBitmap(bitmap: Bitmap, newWidth: Int, newHeight: Int): Bitmap {
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, false)
    }

    fun setPercentage() {
        var complete:Int =0
        var cache= App.context?.let { CacheService<user_profile>(it, Constants.PROFILE) }
            var list=cache?.getData(user_profile::class.java)
        if (list != null) {
            if(list.lastIndex>=0)
            {
                var cacheImage= App.context?.let { CacheService<Bitmap>(it, PROFILE_PIC) }
                var data= cacheImage?.getBitmap()
                var name= list[0].name
                var time= list[0].time
                var image=data

                if((image != null) && time?.toInt() ==0 && name=="Listener")
                    complete=1
                else if((image != null) && time?.toInt() !=0 && name!="Listener")
                    complete=3
                else if((image != null) && time?.toInt() !=0 && name=="Listener")
                    complete=2
                else if((image != null) && time?.toInt() ==0 && name!="Listener")
                    complete=2
                else if((image == null) && time?.toInt() !=0 && name !="Listener")
                    complete=2
                else if((image == null) && time?.toInt() ==0 && name=="Listener")
                    complete=0
                else if((image == null) && time?.toInt() !=0 && name=="Listener")
                    complete=1
                else if((image == null) && time?.toInt() ==0 && name!="Listener")
                    complete=1
            }

        }

        var percentage= ((100 / 3) * complete).toFloat()
        if(percentage>98)
            percentage=100f
        this.percentage = percentage
        invalidate()
    }
}