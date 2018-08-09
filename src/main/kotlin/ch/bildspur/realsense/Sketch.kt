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

    val rec = PImageRecorder(this)

    var recording = false

    var camMode = 1

    override fun setup() {
        cam.start()
    }

    override fun draw() {
        cam.readStreams()

        background(0)

        image(when(camMode)
        {
            0 -> cam.depthImage
            1 -> cam.colorImage
            else -> cam.depthImage
        }, 0f, 0f)

        if(recording)
        {
            rec.capture(cam.depthImage.copy())
        }
    }

    override fun stop()
    {
        cam.stop()
    }

    fun startSketch() {
        runSketch()
    }

    override fun keyPressed() {
        super.keyPressed()

        when(key)
        {
            ' ' ->  camMode = (camMode + 1) % 2
            'r' -> {
                println("recording...")
                recording = true
            }
            's' -> {
                println("stopped recording!")
                recording = false
                rec.save("test")
            }
        }
    }
}