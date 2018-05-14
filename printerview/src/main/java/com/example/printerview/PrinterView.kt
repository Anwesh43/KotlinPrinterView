package com.example.printerview

/**
 * Created by anweshmishra on 14/05/18.
 */

import android.content.Context
import android.graphics.*
import android.view.View
import android.view.MotionEvent

class PrinterView (ctx : Context) : View(ctx) {

    private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val renderer : Renderer = Renderer(this)

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas, paint)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
            }
        }
        return true
    }

    data class State(var prevScale : Float = 0f, var dir : Float = 0f, var j : Int = 0) {

        val scales : Array<Float> = arrayOf(0f, 0f)

        fun update(stopcb : (Float) -> Unit) {
            scales[j] += dir * 0.1f
            if (Math.abs(scales[j] - prevScale) > 1) {
                prevScale = scales[j] + dir
                j += dir.toInt()
                if (j == scales.size || j == -1) {
                    j -= dir.toInt()
                    dir = 0f
                    prevScale = scales[j]
                    stopcb(prevScale)
                }
            }
        }

        fun startUpdating(startcb : () -> Unit) {
            if (dir == 0f) {
                dir = 1 - 2 * prevScale
                startcb()
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(updatecb : () -> Unit) {
            if (animated) {
                updatecb()
                try {
                    Thread.sleep(50)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }

    }

    data class PrinterShape(var i : Int, val state : State = State()) {

        fun draw(canvas : Canvas, paint : Paint) {
            val w : Float = canvas.width.toFloat()
            val h : Float = canvas.height.toFloat()
            val pSize : Float = Math.min(w, h)/5
            val paperHeight : Float = Math.min(w, h)/3
            val gap : Float = paperHeight/4
            paint.strokeWidth = paperHeight/20
            paint.strokeCap = Paint.Cap.ROUND
            paint.color = Color.WHITE
            canvas.save()
            canvas.translate(w/2, h/2)
            paint.style = Paint.Style.STROKE
            canvas.drawRoundRect(RectF(-pSize, -pSize/2, pSize, -pSize/2 ), pSize/10, pSize/10, paint)
            paint.style = Paint.Style.FILL
            canvas.drawRoundRect(RectF(-pSize/2, -pSize/4, pSize/2, -pSize/4), pSize/10, pSize/10, paint)
            canvas.save()
            canvas.translate(-pSize/3, -pSize/4)
            paint.style = Paint.Style.STROKE
            canvas.drawRect(0f, -paperHeight * state.scales[0], 2 * pSize/3, 0f, paint)
            var y : Float = -paperHeight + gap
            for (i in 0..2) {
                canvas.drawLine( -pSize/4, y, pSize/4, y, paint)
                y += gap
            }
            canvas.restore()
            canvas.restore()
        }

        fun update(stopcb : (Float) -> Unit) {
            state.update(stopcb)
        }

        fun startUpdating(startcb : () -> Unit) {
            state.startUpdating(startcb)
        }
    }

    data class Renderer(var view : PrinterView) {

        private val animator : Animator = Animator(view)

        private val printerShape : PrinterShape = PrinterShape(0)

        fun render(canvas : Canvas, paint : Paint) {
            canvas.drawColor(Color.parseColor("#212121"))
            printerShape.draw(canvas, paint)
            animator.animate {
                printerShape.update {
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            printerShape.startUpdating {
                animator.start()
            }
        }

    }
}
