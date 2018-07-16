package ch.bildspur.realsense

import processing.core.PApplet

class Sketch : PApplet() {

    override fun settings() {
        size(500, 500)
    }

    lateinit var cam : RealSenseCamera

    override fun setup() {
        cam = RealSenseCamera()
        cam.start()
    }

    override fun draw() {
        background(0)
    }

    fun startSketch() {
        runSketch()
    }
}