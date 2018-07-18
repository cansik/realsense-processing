package ch.bildspur.realsense

import org.librealsense.Config
import org.librealsense.Context
import org.librealsense.Native
import org.librealsense.Pipeline
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
    private val streamIndex = 0

    private val image = PImage(width, height, PConstants.RGB)

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
        config.enableStream(Native.Stream.RS2_STREAM_DEPTH, streamIndex, width, height, Native.Format.RS2_FORMAT_Z16, fps)

        Thread.sleep(1000) // CONCURRENCY BUG SOMEWHERE!

        println("starting device...")
        pipeline.startWithConfig(config)

        println("started!")
    }

    fun readDepthImage() : PImage
    {
        val frames = pipeline.waitForFrames(5000)

        for (i in 0 until frames.frameCount()) {
            val frame = frames.frame(i)
            val buffer = frame.frameData.asCharBuffer()

            if(0 == Native.rs2IsFrameExtendableTo(frame.ptr, Native.Extension.RS2_EXTENSION_DEPTH_FRAME.ordinal)) {
                println("No extension depth frame!")
                frame.release()
                continue
            }

            image.loadPixels()
            (0 until width * height).forEach {
                val depth = buffer[it].toInt()
                val grayScale = Sketch.map(depth, 0, 65536 / 50, 255, 0)

                if(depth > 0)
                    image.pixels[it] = applet.color(grayScale, 0, 100)
                else
                    image.pixels[it] = applet.color(0)
            }
            image.updatePixels()
            frame.release()
        }
        frames.release()

        return image
    }

    fun stop()
    {
    }
}