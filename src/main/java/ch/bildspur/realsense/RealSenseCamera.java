package ch.bildspur.realsense;

import ch.bildspur.realsense.processing.RSFilterBlock;
import ch.bildspur.realsense.processing.RSProcessingBlock;
import ch.bildspur.realsense.stream.DepthRSStream;
import ch.bildspur.realsense.stream.RSStream;
import ch.bildspur.realsense.stream.VideoRSStream;
import ch.bildspur.realsense.type.*;
import org.intel.rs.Context;
import org.intel.rs.device.AdvancedDevice;
import org.intel.rs.device.Device;
import org.intel.rs.device.DeviceList;
import org.intel.rs.frame.DepthFrame;
import org.intel.rs.frame.Frame;
import org.intel.rs.frame.FrameList;
import org.intel.rs.frame.VideoFrame;
import org.intel.rs.pipeline.Config;
import org.intel.rs.pipeline.Pipeline;
import org.intel.rs.pipeline.PipelineProfile;
import org.intel.rs.processing.*;
import org.intel.rs.types.Format;
import org.intel.rs.types.Option;
import org.intel.rs.types.Stream;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PImage;

import java.util.ArrayList;
import java.util.List;

public class RealSenseCamera implements PConstants {
    private final int defaultWidth = 640;
    private final int defaultHeight = 480;
    private final int defaultStreamIndex = 0;
    private final int defaultFrameRate = 30;

    // processing
    private PApplet parent;

    // realsense
    private Context context = new Context();
    private Config config = new Config();
    private Pipeline pipeline = new Pipeline(context);
    private PipelineProfile pipelineProfile;

    // streams
    private VideoRSStream colorStream = new VideoRSStream();
    private DepthRSStream depthStream = new DepthRSStream();
    private VideoRSStream firstIRStream = new VideoRSStream();
    private VideoRSStream secondIRStream = new VideoRSStream();

    // processors
    // todo: add syncer and pointcloud
    private RSProcessingBlock<Colorizer> colorizer = new RSProcessingBlock<>();
    private RSProcessingBlock<Align> align = new RSProcessingBlock<>();

    // filter processors
    private RSFilterBlock decimationFilter = new RSFilterBlock();
    private RSFilterBlock disparityTransform = new RSFilterBlock();
    private RSFilterBlock holeFillingFilter = new RSFilterBlock();
    private RSFilterBlock spatialFilter = new RSFilterBlock();
    private RSFilterBlock temporalFilter = new RSFilterBlock();
    private RSFilterBlock thresholdFilter = new RSFilterBlock();
    private RSFilterBlock unitsTransform = new RSFilterBlock();
    private RSFilterBlock zeroOrderInvalidationFilter = new RSFilterBlock();

    // processor lists
    private RSProcessingBlock[] blocks = {colorizer, align};
    private List<RSFilterBlock> filters = new ArrayList<>();

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

    /**
     * Enable a new RealSense camera stream.
     * @param stream The stream to enable.
     */
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

    /**
     * Enable a new depth stream with default configuration.
     */
    public void enableDepthStream() {
        enableDepthStream(defaultWidth, defaultHeight);
    }


    public void enableDepthStream(int width, int height) {
        enableDepthStream(width, height, defaultFrameRate);
    }

    public void enableDepthStream(int width, int height, int fps) {
        depthStream.init(Stream.Depth, defaultStreamIndex, width, height, Format.Z16, fps);
        enableStream(depthStream);
    }

    /**
     * Enable a new color stream with default configuration.
     */
    public void enableColorStream() {
        enableColorStream(defaultWidth, defaultHeight);
    }

    public void enableColorStream(int width, int height) {
        enableColorStream(width, height, defaultFrameRate);
    }

    public void enableColorStream(int width, int height, int fps) {
        colorStream.init(Stream.Color, defaultStreamIndex, width, height, Format.Rgb8, fps);
        enableStream(colorStream);
    }

    /**
     * Enable a new infrared stream with default configuration.
     */
    public void enableIRStream() {
        enableIRStream(defaultWidth, defaultHeight);
    }

    public void enableIRStream(int width, int height) {
        enableIRStream(width, height, defaultFrameRate);
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
        colorizer.setOption(Option.ColorScheme, scheme.getIndex());
    }

    public void enableAlign() {
        enableAlign(StreamType.Color);
    }

    public void enableAlign(StreamType streamType) {
        align.init(new Align(streamType.getStream()));
    }

    // Filters

    public void addFilter(RSFilterBlock filter) {
        filters.add(filter);
    }

    public void addDecimationFilter() {
        addDecimationFilter(2);
    }

    public void addDecimationFilter(int filterMagnitude) {
        decimationFilter.init(new DecimationFilter());
        addFilter(decimationFilter);

        decimationFilter.setOption(Option.FilterMagnitude, filterMagnitude);
    }

    public void addDisparityTransform() {
        addDisparityTransform(true);
    }

    public void addDisparityTransform(boolean depthToDisparity) {
        disparityTransform.init(new DisparityTransform(depthToDisparity));
        addFilter(disparityTransform);
    }

    public void addHoleFillingFilter() {
        addHoleFillingFilter(HoleFillingType.FarestFromAround);
    }

    public void addHoleFillingFilter(HoleFillingType fillingType) {
        holeFillingFilter.init(new HoleFillingFilter());
        addFilter(holeFillingFilter);

        holeFillingFilter.setOption(Option.HolesFill, fillingType.getIndex());
    }

    public void addSpatialFilter() {
        addSpatialFilter(2, 0.5f, 20, 0);
    }

    public void addSpatialFilter(int filterMagnitude, float smoothAlpha, int smoothDelta, int holeFilling) {
        spatialFilter.init(new SpatialFilter());
        addFilter(spatialFilter);

        spatialFilter.setOption(Option.FilterMagnitude, filterMagnitude);
        spatialFilter.setOption(Option.FilterSmoothAlpha, smoothAlpha);
        spatialFilter.setOption(Option.FilterSmoothDelta, smoothDelta);
        spatialFilter.setOption(Option.HolesFill, holeFilling);
    }

    public void addTemporalFilter() {
        addTemporalFilter(0.4f, 20, PersistencyIndex.ValidIn2_Last4);
    }

    public void addTemporalFilter(float smoothAlpha, int smoothDelta, PersistencyIndex persistencyIndex) {
        temporalFilter.init(new TemporalFilter());
        addFilter(temporalFilter);

        temporalFilter.setOption(Option.FilterSmoothAlpha, smoothAlpha);
        temporalFilter.setOption(Option.FilterSmoothDelta, smoothDelta);
        temporalFilter.setOption(Option.FilterOption, persistencyIndex.getIndex());
    }

    public void addUnitsTransform() {
        unitsTransform.init(new UnitsTransform());
        addFilter(unitsTransform);

        // todo: are there no options available?
    }

    public void addZeroOrderInvalidationFilter() {
        zeroOrderInvalidationFilter.init(new ZeroOrderInvalidationFilter());
        addFilter(zeroOrderInvalidationFilter);

        // todo: are there no options available?
    }

    public void addThresholdFilter(float minDistance, float maxDistance) {
        thresholdFilter.init(new ThresholdFilter());
        addFilter(thresholdFilter);

        thresholdFilter.setOption(Option.MinDistance, minDistance);
        thresholdFilter.setOption(Option.MaxDistance, maxDistance);
    }

    public void clearFilters() {
        filters.clear();
    }

    // Frame Handling

    /**
     * Read the camera frame buffers for all enabled streams.
     * Applies filters and processors to the streams.
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

            // apply depth filter
            if(!filters.isEmpty()) {
                for(RSFilterBlock filter : filters) {
                    DepthFrame temp = filter.getBlock().process(frame);
                    frame.release();
                    frame = temp;
                }
            }

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
    public boolean isDeviceAvailable() {
        return getDeviceCount() > 0;
    }

    /**
     * Returns how many devices are connected.
     *
     * @return Returns how many devices are connected.
     */
    public int getDeviceCount() {
        DeviceList deviceList = this.context.queryDevices();
        int count = deviceList.count();
        deviceList.release();
        return count;
    }

    /**
     * Returns the first device of the device list.
     * The device has to be closed by the user or used afterwards.
     * @return First device of the device list.
     */
    public Device getDevice() {
        DeviceList deviceList = this.context.queryDevices();
        Device first = deviceList.get(0);
        deviceList.release();
        return first;
    }

    /**
     * Returns the first advanced device of the device list.
     * The device has to be closed by the user or used afterwards.
     * @return First advanced device of the device list.
     */
    public AdvancedDevice getAdvancedDevice() {
        Device device = getDevice();
        return AdvancedDevice.fromDevice(device);
    }

    /**
     * Returns a list of created devices.
     * All the devices are created, so it is mandatory to close the devices again to be reused by the library.
     * @return List of created devices.
     */
    public Device[] getDevices() {
        DeviceList deviceList = this.context.queryDevices();
        int count = deviceList.count();

        Device[] devices = new Device[count];
        for(int i = 0; i < devices.length; i++)
            devices[i] = deviceList.get(i);

        deviceList.release();
        return devices;
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
        start(device.getSerialNumber());
    }

    /**
     * Starts the camera.
     *
     * @param serialNumber Camera device serial number.
     */
    public synchronized void start(String serialNumber) {
        if (running)
            return;

        // set device
        config.enableDevice(serialNumber);

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

        for(RSFilterBlock filter : filters)
            filter.release();

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

    /**
     * Returns 2-dimensional short array which contains the depth buffer.
     * @return Y / X short array of the raw depth buffer.
     */
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

    public Context getContext() {
        return context;
    }

    public Config getConfig() {
        return config;
    }

    public Pipeline getPipeline() {
        return pipeline;
    }

    public PipelineProfile getPipelineProfile() {
        return pipelineProfile;
    }

    public FrameList getFrames() {
        return frames;
    }

    public boolean isRunning() {
        return running;
    }
}
