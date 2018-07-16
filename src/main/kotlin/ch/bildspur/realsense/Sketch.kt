package ch.bildspur.realsense

import processing.core.PApplet

class Sketch : PApplet() {

    override fun settings() {
        size(640, 480)
    }

    lateinit var cam : RealSenseCamera

    override fun setup() {
        cam = RealSenseCamera()
        cam.start()
    }

    override fun draw() {
        background(0)
        image(cam.readFrame(),0f, 0f)
    }

    override fun stop()
    {
        cam.stop()
    }

    fun startSketch() {
        runSketch()
    }
}