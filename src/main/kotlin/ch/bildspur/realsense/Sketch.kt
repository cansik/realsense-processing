package ch.bildspur.realsense

import processing.core.PApplet

class Sketch : PApplet() {

    override fun settings() {
        size(1280, 720)
    }

    val cam = RealSenseCamera(this)

    override fun setup() {
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