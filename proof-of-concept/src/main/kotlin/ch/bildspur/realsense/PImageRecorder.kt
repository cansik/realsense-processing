package ch.bildspur.realsense

import processing.core.PImage

class PImageRecorder(val sketch : Sketch) {
    val frames = ArrayList<PImage>(600)

    fun capture(frame : PImage)
    {
        frames.add(frame)
    }

    fun save(movieFileName : String)
    {
        frames.forEachIndexed { i, f ->
            f.save(sketch.savePath("frames/${("%04.0f".format(i.toDouble()))}.png"))
        }

        // release buffer
        frames.clear()
    }
}