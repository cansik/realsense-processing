package ch.bildspur.realsense

import org.librealsense.Config
import org.librealsense.Context
import org.librealsense.Native
import org.librealsense.Pipeline
import processing.core.PImage
import java.nio.file.Paths
import processing.core.PApplet
import processing.core.PConstants

class RealSenseCamera(val applet : PApplet) {
    init {
        // loading native libs
        System.load(Paths.get("lib/realsense/librealsense2.dylib").toAbsolutePath().toString())
        System.load(Paths.get("lib/realsense/libnative.dylib").toAbsolutePath().toString())
    }

    private lateinit var context : Context
    private lateinit var pipeline : Pipeline

    private val width = 1280
    private val height = 720

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
        //config.enableStream(Native.Stream.RS2_STREAM_COLOR, 0, width, height, Native.Format.RS2_FORMAT_RGB8, 30)
        config.enableStream(Native.Stream.RS2_STREAM_DEPTH, 0, width, height, Native.Format.RS2_FORMAT_Z16, 30)

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
            val buffer = frame.frameData

            // update pixels
            (0 until width * height).forEach {
                // add grayscale value
                image.pixels[it] = buffer[it].toInt() //applet.color(buffer[it].toInt())
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