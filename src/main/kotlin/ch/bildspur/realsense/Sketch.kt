package ch.bildspur.realsense

import processing.core.PApplet
import kotlin.math.roundToInt

class Sketch : PApplet() {
    companion object {
        @JvmStatic
        fun map(value: Double, start1: Double, stop1: Double, start2: Double, stop2: Double): Double {
            return start2 + (stop2 - start2) * ((value - start1) / (stop1 - start1))
        }

        @JvmStatic
        fun map(value: Int, start1: Int, stop1: Int, start2: Int, stop2: Int): Int {
            return map(value.toDouble(), start1.toDouble(), stop1.toDouble(), start2.toDouble(), stop2.toDouble()).roundToInt()
        }
    }

    override fun settings() {
        size(1280, 720)
    }

    val cam = RealSenseCamera(this)

    override fun setup() {
        cam.start()
    }

    override fun draw() {
        background(0)
        image(cam.readDepthImage(),0f, 0f)
    }

    override fun stop()
    {
        cam.stop()
    }

    fun startSketch() {
        runSketch()
    }
}