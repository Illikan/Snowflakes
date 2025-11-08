package com.example.snowflakes

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.AsyncTask
import android.os.Build
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.annotation.RequiresApi
import kotlin.random.Random

data class Snowflake(var x: Float, var y: Float, val velocity: Float, val radius: Float, val color: Int)
lateinit var snow: Array<Snowflake>
val paint = Paint()
var h = 1000; var w = 1000

open class Snowflakes(ctx: Context) : View(ctx) {
    private lateinit var moveTask: MoveTask
    private var isAnimationRunning = false
    private val random = Random(0)

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(Color.BLUE)

        for (s in snow) {
            paint.color = s.color
            canvas.drawCircle(s.x, s.y, s.radius, paint)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        h = bottom - top
        w = right - left

        snow = Array(10) {
            val randomFactor = random.nextFloat()
            val shade = (200 + 55 * randomFactor).toInt()
            Snowflake(
                x = random.nextFloat() * w,
                y = random.nextFloat() * h,
                velocity = 15 + 10 * randomFactor,
                radius = 30 + 20 * randomFactor,
                color = Color.rgb(shade, shade, shade)
            )
        }
        Log.d("mytag", "snow: " + snow.contentToString())
    }

    fun moveSnowflakes() {
        for (s in snow) {
            val speedFactor = (h - s.y) / h.toFloat()
            s.y += s.velocity * speedFactor * 0.5f

            if (s.y > h - s.radius) {
                s.y = 0f
                s.x = random.nextFloat() * w
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event?.action == MotionEvent.ACTION_DOWN) {
            if (!isAnimationRunning) {
                moveTask = MoveTask(this)
                moveTask.execute(50)
                isAnimationRunning = true
            }
        }
        return true
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        if (::moveTask.isInitialized) {
            moveTask.cancel(true)
        }
    }

    class MoveTask(private val s: Snowflakes) : AsyncTask<Int, Unit, Int>() {
        override fun doInBackground(vararg params: Int?): Int {
            val delay = params[0] ?: 200
            while (!isCancelled) {
                try {
                    Thread.sleep(delay.toLong())
                    publishProgress()
                } catch (e: InterruptedException) {
                    break
                }
            }
            return 0
        }

        override fun onProgressUpdate(vararg values: Unit?) {
            s.moveSnowflakes()
            s.invalidate()
        }
    }
}
