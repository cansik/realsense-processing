package ch.bildspur.realsense;

import org.intel.rs.Context;
import org.intel.rs.device.Device;
import org.intel.rs.device.DeviceList;
import org.intel.rs.frame.DepthFrame;
import org.intel.rs.frame.Frame;
import org.intel.rs.frame.FrameList;
import org.intel.rs.frame.VideoFrame;
import org.intel.rs.option.CameraOption;
import org.intel.rs.pipeline.Config;
import org.intel.rs.pipeline.Pipeline;
import org.intel.rs.pipeline.PipelineProfile;
import org.intel.rs.processing.Colorizer;
import org.intel.rs.types.Format;
import org.intel.rs.types.Option;
import org.intel.rs.types.Stream;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PImage;

import java.nio.ByteBuffer;

/**
 * Intel RealSense Camera
 */
public class RealSenseCamera implements PConstants {
    // processing
    private PApplet parent;

    // realsense
    private Context context;
    private Pipeline pipeline;
    private PipelineProfile pipelineProfile;

    private Colorizer colorizer;

    private final int depthStreamIndex = 0;
    private final int colorStreamIndex = 0;
    private final int irStreamIndex = 1;

    // camera
    private volatile boolean running = false;

    private int width;
    private int height;
    private int fps;

    private boolean enableDepthStream;
    private boolean enableColorStream;
    private boolean enableIRStream;

    private PImage depthImage;
    private PImage colorImage;
    private PImage irImage;

    private FrameList frames;

    /**
     * Create a new Intel RealSense camera.
     *
     * @param parent Parent processing sketch.
     */
    public RealSenseCamera(PApplet parent) {
        this.parent = parent;

        // register shutdown handler
        Runtime.getRuntime().addShutdownHook(new Thread(this::release));

        // create context
        context = new Context();
    }

    /**
     * Start the camera.
     *
     * @param width             Width of the camera image.
     * @param height            Height of the camera image.
     * @param fps               Frames per second to capture.
     * @param enableDepthStream True if depth stream should be enabled.
     * @param enableColorStream True if color stream should be enabled.
     * @param enableIRStream    True if first IR stream should be enabled.
     */
    public void start(int width, int height, int fps, boolean enableDepthStream, boolean enableColorStream, boolean enableIRStream) {
        DeviceList deviceList = this.context.queryDevices();

        if (deviceList.count() == 0) {
            PApplet.println("RealSense: No device found!");
            return;
        }

        this.start(deviceList.get(0), width, height, fps, enableDepthStream, enableColorStream, enableIRStream);
    }

    /**
     * Start the camera.
     *
     * @param device            Intel RealSense device to start.
     * @param width             Width of the camera image.
     * @param height            Height of the camera image.
     * @param fps               Frames per second to capture.
     * @param enableDepthStream True if depth stream should be enabled.
     * @param enableColorStream True if color stream should be enabled.
     */
    public void start(Device device, int width, int height, int fps, boolean enableDepthStream, boolean enableColorStream) {

    }

    /**
     * Start the camera.
     *
     * @param device            Intel RealSense device to start.
     * @param width             Width of the camera image.
     * @param height            Height of the camera image.
     * @param fps               Frames per second to capture.
     * @param enableDepthStream True if depth stream should be enabled.
     * @param enableColorStream True if color stream should be enabled.
     * @param enableIRStream    True if first IR stream should be enabled.
     */
    public void start(Device device, int width, int height, int fps, boolean enableDepthStream, boolean enableColorStream, boolean enableIRStream) {
        if (running)
            return;

        pipeline = new Pipeline(context);

        // set configuration
        this.width = width;
        this.height = height;
        this.fps = fps;
        this.enableDepthStream = enableDepthStream;
        this.enableColorStream = enableColorStream;
        this.enableIRStream = enableIRStream;

        // create images
        this.depthImage = new PImage(this.width, this.height, PConstants.RGB);
        this.colorImage = new PImage(this.width, this.height, PConstants.RGB);
        this.irImage = new PImage(this.width, this.height, PConstants.RGB);

        // create pipeline
        colorizer = new Colorizer();

        Config config = new Config();
        config.enableDevice(device.getSerialNumber());

        // set color scheme settings
        CameraOption colorScheme = colorizer.getOptions().get(Option.ColorScheme);
        colorScheme.setValue(2);

        if (this.enableDepthStream) {
            config.enableStream(Stream.Depth,
                    this.depthStreamIndex,
                    this.width,
                    this.height,
                    Format.Z16,
                    this.fps);
        }

        if (this.enableColorStream) {
            config.enableStream(Stream.Color,
                    this.colorStreamIndex,
                    this.width,
                    this.height,
                    Format.Rgb8,
                    this.fps);
        }

        if (this.enableIRStream) {
            config.enableStream(Stream.Infrared,
                    this.irStreamIndex,
                    this.width,
                    this.height,
                    Format.Y8,
                    this.fps);
        }

        // start pipeline
        pipelineProfile = pipeline.start(config);

        running = true;
    }

    /**
     * Read the camera frame buffers for all active streams.
     */
    public void readFrames() {
        // release previous
        if (frames != null)
            frames.release();

        frames = pipeline.waitForFrames();

        if (this.enableDepthStream) {
            DepthFrame frame = frames.getDepthFrame();
            VideoFrame coloredFrame = colorizer.colorize(frame);

            coloredFrame.copyTo(depthImage.pixels);
            depthImage.updatePixels();

            coloredFrame.release();
            frame.release();
        }

        if (this.enableColorStream) {
            VideoFrame frame = frames.getColorFrame();
            frame.copyTo(this.colorImage.pixels);
            this.colorImage.updatePixels();
            frame.release();
        }

        if (this.enableIRStream) {
            Frame frame = frames.getFirstOrDefault(Stream.Infrared);
            readIRImage(frame);
            frame.release();
        }
    }

    /**
     * Returns true if a device is available.
     *
     * @return True if device is available.
     */
    public boolean isCameraAvailable() {
        DeviceList deviceList = this.context.queryDevices();
        return deviceList.count() > 0;
    }

    /**
     * Stop the camera.
     */
    public void stop() {
        if (!running)
            return;

        if (frames != null)
            frames.release();

        // clean up
        pipeline.stop();
        pipeline.release();

        // set states
        running = false;
    }

    public synchronized void release() {
        stop();

        colorizer.release();
        context.release();
    }

    private void readIRImage(Frame frame) {
        ByteBuffer buffer = frame.getData();
        for (int i = 0; i < width * height; i++) {
            int irvalue = buffer.get(i) & 0xFF;
            irImage.pixels[i] = toColor(irvalue);
        }

        irImage.updatePixels();
    }

    private int toColor(int gray) {
        return toColor(gray, gray, gray);
    }

    private int toColor(int red, int green, int blue) {
        return -16777216 | red << 16 | green << 8 | blue;
    }

    /**
     * Check if the camera is already running.
     *
     * @return True if the camera is already running.
     */
    public boolean isRunning() {
        return running;
    }

    public PImage getDepthImage() {
        return depthImage;
    }

    public PImage getColorImage() {
        return colorImage;
    }

    public PImage getIRImage() {
        return irImage;
    }

    /**
     * Returns depth at specific position.
     * Returns -1 if no depth frame was captured.
     * Returns -2 if no frames were captured at all.
     *
     * @param x X coordinate.
     * @param y Y coordinate.
     * @return Distance value.
     */
    public float getDistance(int x, int y) {
        if (frames == null)
            return -2;

        DepthFrame depth = frames.getDepthFrame();

        if (depth == null)
            return -1;

        return depth.getDistance(x, y);
    }
}
