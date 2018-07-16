package ch.bildspur.realsense

import org.librealsense.Config
import org.librealsense.Context
import org.librealsense.Native
import java.nio.file.Paths

class RealSenseCamera {

    fun start()
    {
        println("starting camera...")

        // loading native libs
        System.load(Paths.get("lib/realsense/librealsense2.dylib").toAbsolutePath().toString())
        System.load(Paths.get("lib/realsense/libnative.dylib").toAbsolutePath().toString())

        val context = Context.create()
        val deviceList = context.queryDevices()
        val devices = deviceList.devices

        val device = devices[0]

        println("device found: ${device.name()}")

        val pipeline = context.createPipeline()
        val config = Config.create()
        config.enableDevice(device)
        config.enableStream(Native.Stream.RS2_STREAM_DEPTH, 0, 640, 480, Native.Format.RS2_FORMAT_Z16, 30)
        pipeline.startWithConfig(config)

        while (true) {
            val frames = pipeline.waitForFrames(5000)

            for (i in 0 until frames.frameCount()) {
                val frame = frames.frame(i)
                val buffer = frame.frameData

                // -- use ByteBuffer here
                println("frame incoming...")

                frame.release()
            }
            frames.release()
        }
    }

    fun stop()
    {

    }
}