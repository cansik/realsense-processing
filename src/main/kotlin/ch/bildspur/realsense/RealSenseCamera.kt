package ch.bildspur.realsense

import org.librealsense.*
import processing.core.PApplet
import processing.core.PConstants
import processing.core.PImage

class RealSenseCamera(val applet : PApplet) {
    private lateinit var context : Context
    private lateinit var pipeline : Pipeline

    // parameters
    private val width = 1280
    private val height = 720
    private val fps = 30
    private val depthStreamIndex = 0
    private val infraredStreamIndex = 0
    private val colorStreamIndex = 0

    val depthImage = PImage(width, height, PConstants.RGB)
    val colorImage = PImage(width, height, PConstants.RGB)

    fun start()
    {
        context = Context.create()

        // find device
        val deviceList = context.queryDevices()
        val devices = deviceList.devices

        val device = devices[0]

        println("device found: ${device.name()}")

        // setup pipeline
        println("setting up pipeline")
        pipeline = context.createPipeline()
        val config = Config.create()
        config.enableDevice(device)
        config.enableStream(Native.Stream.RS2_STREAM_DEPTH, depthStreamIndex, width, height, Native.Format.RS2_FORMAT_Z16, fps)
        config.enableStream(Native.Stream.RS2_STREAM_COLOR, colorStreamIndex, width, height, Native.Format.RS2_FORMAT_RGB8, fps)
        //config.enableStream(Native.Stream.RS2_STREAM_INFRARED, infraredStreamIndex, width, height, Native.Format.RS2_FORMAT_RGB8, fps)

        Thread.sleep(1000) // CONCURRENCY BUG SOMEWHERE!

        println("starting device...")
        pipeline.startWithConfig(config)

        println("started!")
    }

    fun readStreams()
    {
        val frames = pipeline.waitForFrames(5000)

        for (i in 0 until frames.frameCount()) {
            val frame = frames.frame(i)

            if(frame.isExtendableTo(Native.Extension.RS2_EXTENSION_DEPTH_FRAME))
                readDepthImage(frame)
            else
                readColorImage(frame)

            frame.release()
        }
        frames.release()
    }

    private fun readDepthImage(frame : Frame)
    {
        val buffer = frame.frameData.asCharBuffer()

        depthImage.loadPixels()
        (0 until width * height).forEach { i ->
            val depth = buffer[i].toInt()

            val depthLevel = 50
            val grayScale = Sketch.map(depth, 0, 65536 / depthLevel, 255, 0)

            if(depth > 0)
                depthImage.pixels[i] = applet.color(grayScale.clamp(0, 255))
            else
                depthImage.pixels[i] = applet.color(0)
        }
        depthImage.updatePixels()
    }

    private fun readColorImage(frame : Frame)
    {
        val buffer = frame.frameData

        colorImage.loadPixels()
        (0 until width * height step 3).forEach { i ->
            colorImage.pixels[i] = buffer[i].toInt() shl 16 or buffer[i + 1].toInt() shl 8 or buffer[i + 2].toInt()
        }
        colorImage.updatePixels()
    }

    fun stop()
    {
    }
}

private fun Int.clamp(min: Int, max: Int): Int {
    return Math.max(Math.min(max, this), min)
}
