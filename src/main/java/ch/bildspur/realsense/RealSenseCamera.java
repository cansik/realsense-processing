package ch.bildspur.realsense;

import ch.bildspur.realsense.stream.DepthRealSenseStream;
import ch.bildspur.realsense.stream.RealSenseStream;
import ch.bildspur.realsense.stream.VideoRealSenseStream;
import org.intel.rs.Context;
import org.intel.rs.device.Device;
import org.intel.rs.device.DeviceList;
import org.intel.rs.frame.FrameList;
import org.intel.rs.pipeline.Config;
import org.intel.rs.pipeline.Pipeline;
import org.intel.rs.pipeline.PipelineProfile;
import org.intel.rs.processing.Colorizer;
import org.intel.rs.types.Format;
import org.intel.rs.types.Stream;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PImage;

public class RealSenseCamera implements PConstants {
    // processing
    private PApplet parent;

    // realsense
    private Context context = new Context();
    private Config config = new Config();
    private Pipeline pipeline = new Pipeline(context);
    private PipelineProfile pipelineProfile;

    // streams
    VideoRealSenseStream colorStream = new VideoRealSenseStream();
    DepthRealSenseStream depthStream = new DepthRealSenseStream();
    VideoRealSenseStream firstIRStream = new VideoRealSenseStream();
    VideoRealSenseStream secondIRStream = new VideoRealSenseStream();

    // processors
    Colorizer colorizer;

    // internal objects
    private FrameList frames;

    // camera
    private volatile boolean running = false;

    /**
     * Create a new Intel RealSense camera.
     *
     * @param parent Parent processing sketch.
     */
    public RealSenseCamera(PApplet parent) {
        this.parent = parent;

        // register shutdown handler
        Runtime.getRuntime().addShutdownHook(new Thread(this::release));
    }

    // Streams

    public void enableStream(RealSenseStream stream) {
        config.enableStream(
                stream.getStreamType(),
                stream.getIndex(),
                stream.getWidth(),
                stream.getHeight(),
                stream.getFormat(),
                stream.getFps()
        );
    }

    public void enableDepthStream() {
        enableDepthStream(640, 480);
    }

    public void enableDepthStream(int width, int height) {
        enableDepthStream(width, height, 30);
    }

    public void enableDepthStream(int width, int height, int fps) {
        depthStream.init(Stream.Depth, 0, width, height, Format.Z16, fps);
        enableStream(depthStream);
    }

    public void enableColorStream() {
        enableColorStream(640, 480);
    }

    public void enableColorStream(int width, int height) {
        enableColorStream(width, height, 30);
    }

    public void enableColorStream(int width, int height, int fps) {
        colorStream.init(Stream.Color, 0, width, height, Format.Rgb8, fps);
        enableStream(colorStream);
    }

    public void enableIRStream() {
        enableIRStream(640, 480);
    }

    public void enableIRStream(int width, int height) {
        enableIRStream(width, height, 30);
    }

    public void enableIRStream(int width, int height, int fps) {
        enableIRStream(width, height, fps, IRStream.First);
    }

    public void enableIRStream(int width, int height, int fps, IRStream irStream) {
        int streamIndex = irStream == IRStream.First ? 1 : 2;
        VideoRealSenseStream stream = irStream == IRStream.First ? firstIRStream : secondIRStream;

        stream.init(Stream.Infrared, streamIndex, width, height, Format.Y8, fps);
        enableStream(stream);
    }

    // Processors



    // Frame Handling
    /**
     * Read the camera frame buffers for all enabled streams.
     */
    public void readFrames() {
        // release previous
        if (frames != null)
            frames.release();

        // read frames from camera
        frames = pipeline.waitForFrames();
    }

    // Camera control

    /**
     * Returns true if a device is available.
     *
     * @return True if device is available.
     */
    public boolean isCameraAvailable() {
        return getCameraCount() > 0;
    }

    /**
     * Returns how many devices are connected.
     *
     * @return Returns how many devices are connected.
     */
    public int getCameraCount() {
        DeviceList deviceList = this.context.queryDevices();
        int count = deviceList.count();
        deviceList.release();
        return count;
    }

    /**
     * Starts the first camera.
     */
    public synchronized void start() {
        if (running)
            return;

        DeviceList deviceList = this.context.queryDevices();

        if (deviceList.count() == 0) {
            PApplet.println("RealSense: No device found!");
            return;
        }

        this.start(deviceList.get(0));
        deviceList.release();
    }

    /**
     * Starts the camera.
     * @param device Camera device to start.
     */
    public synchronized void start(Device device) {
        if (running)
            return;

        // set device
        config.enableDevice(device.getSerialNumber());

        // start pipeline
        pipelineProfile = pipeline.start(config);

        running = true;
    }

    /**
     * Stops the camera.
     */
    public synchronized void stop() {
        if (!running)
            return;

        if (frames != null)
            frames.release();

        // clean up
        pipeline.stop();
        pipeline.release();

        pipeline = new Pipeline(context);

        // set states
        running = false;
    }

    /**
     * Resets the stream and processor settings.
     */
    public synchronized void reset() {
        stop();

        config.release();
        config = new Config();
    }

    /**
     * Frees all the resources acquired by the camera.
     */
    public synchronized void release() {
        stop();

        config.release();
        pipeline.release();
        colorizer.release();
        context.release();
    }

    // Image / Data Getters
    public short[][] getDepth() {
        return depthStream.getData();
    }

    public PImage getDepthImage() {
        return depthStream.getImage();
    }

    public PImage getColorImage() {
        return colorStream.getImage();
    }

    public PImage getIRImage() {
        return getIRImage(IRStream.First);
    }

    public PImage getIRImage(IRStream irStream) {
        VideoRealSenseStream stream = irStream == IRStream.First ? firstIRStream : secondIRStream;
        return stream.getImage();
    }

    // Methods to keep old API valid (will be removed soon!)

    // Other Getters
}
