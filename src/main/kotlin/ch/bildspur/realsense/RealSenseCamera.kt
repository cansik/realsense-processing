package ch.bildspur.realsense

import org.librealsense.Config
import org.librealsense.Context
import org.librealsense.Native
import org.librealsense.Pipeline
import processing.core.PApplet
import processing.core.PConstants
import processing.core.PImage
import kotlin.math.absoluteValue

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

    fun readFrame() : PImage
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

            // update pixels
            image.loadPixels()
            (0 until width * height).forEach {
                // add grayscale value
                val depth = buffer[it]
                image.pixels[it] = applet.color(Sketch.map(depth.toInt().absoluteValue, 0, 65536, 0, 255))
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