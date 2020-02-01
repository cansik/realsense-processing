package ch.bildspur.realsense;

import ch.bildspur.realsense.processing.RSProcessingBlock;
import ch.bildspur.realsense.stream.DepthRSStream;
import ch.bildspur.realsense.stream.RSStream;
import ch.bildspur.realsense.stream.VideoRSStream;
import ch.bildspur.realsense.type.ColorScheme;
import ch.bildspur.realsense.type.IRStream;
import ch.bildspur.realsense.type.StreamType;
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
import org.intel.rs.processing.Align;
import org.intel.rs.processing.Colorizer;
import org.intel.rs.types.Format;
import org.intel.rs.types.Option;
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
    VideoRSStream colorStream = new VideoRSStream();
    DepthRSStream depthStream = new DepthRSStream();
    VideoRSStream firstIRStream = new VideoRSStream();
    VideoRSStream secondIRStream = new VideoRSStream();

    // processors
    RSProcessingBlock<Colorizer> colorizer = new RSProcessingBlock<>();
    RSProcessingBlock<Align> align = new RSProcessingBlock<>();

    RSProcessingBlock[] blocks = {colorizer, align};

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

    public void enableStream(RSStream stream) {
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
        VideoRSStream stream = irStream == IRStream.First ? firstIRStream : secondIRStream;

        stream.init(Stream.Infrared, streamIndex, width, height, Format.Y8, fps);
        enableStream(stream);
    }

    // Processors
    public void enableColorizer() {
        enableColorizer(ColorScheme.Jet);
    }

    public void enableColorizer(ColorScheme scheme) {
        colorizer.init(new Colorizer());

        // set color scheme settings
        CameraOption colorScheme = colorizer.getBlock().getOptions().get(Option.ColorScheme);
        colorScheme.setValue(scheme.getIndex());
    }

    public void enableAlign() {
        enableAlign(StreamType.Color);
    }

    public void enableAlign(StreamType streamType) {
        align.init(new Align(streamType.getStream()));
    }

    // Frame Handling

    /**
     * Read the camera frame buffers for all enabled streams.
     */
    public void readFrames() {
        // release previous (needed for depth extraction)
        if (frames != null)
            frames.release();

        // read frames from camera
        frames = pipeline.waitForFrames();

        if(align.isEnabled()) {
            FrameList temp = align.getBlock().process(frames);
            frames.release();
            frames = temp;
        }

        // copy streams
        if (depthStream.isEnabled()) {
            DepthFrame frame = frames.getDepthFrame();

            // todo: decimate frame if needed

            // update colors if colorized is there
            if(colorizer.isEnabled()) {
                VideoFrame coloredFrame = colorizer.getBlock().colorize(frame);
                depthStream.copyPixels(coloredFrame);
                coloredFrame.release();
            }

            frame.release();
        }

        if (colorStream.isEnabled()) {
            VideoFrame frame = frames.getColorFrame();
            colorStream.copyPixels(frame);
            frame.release();
        }

        if (firstIRStream.isEnabled()) {
            VideoFrame frame = getStreamByIndex(frames, Stream.Infrared, Format.Any, firstIRStream.getIndex());
            firstIRStream.copyPixels(frame);
            frame.release();
        }

        if (secondIRStream.isEnabled()) {
            VideoFrame frame = getStreamByIndex(frames, Stream.Infrared, Format.Any, secondIRStream.getIndex());
            secondIRStream.copyPixels(frame);
            frame.release();
        }
    }

    private <T extends Frame> T getStreamByIndex(FrameList frames, Stream stream, Format format, int index) {
        for (Frame frame : frames) {
            if (frame.getProfile().getStream() == stream
                    && (Format.Any == format || frame.getProfile().getFormat() == format)
                    && frame.getProfile().getIndex() == index) {
                return (T) frame;
            }
            frame.release();
        }
        return null;
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
     *
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

        for(RSProcessingBlock block : blocks)
            block.release();

        context.release();
    }

    // Image / Data Getters
    /**
     * Returns depth at specific position in the depth frame.
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

        float distance = depth.getDistance(x, y);
        depth.release();
        return distance;
    }

    public short[][] getDepthData() {
        DepthFrame frame = frames.getDepthFrame();
        depthStream.updateDepthData(frame);
        frame.release();
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
        VideoRSStream stream = irStream == IRStream.First ? firstIRStream : secondIRStream;
        return stream.getImage();
    }

    // Methods to keep old API valid (will be removed soon!)

    // Other Getters
}
