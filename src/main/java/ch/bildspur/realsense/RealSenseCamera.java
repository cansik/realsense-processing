package ch.bildspur.realsense;

import ch.bildspur.realsense.processing.*;
import ch.bildspur.realsense.sensor.RSDepthSensor;
import ch.bildspur.realsense.sensor.RSSensor;
import ch.bildspur.realsense.stream.DepthRSStream;
import ch.bildspur.realsense.stream.PoseRSStream;
import ch.bildspur.realsense.stream.RSStream;
import ch.bildspur.realsense.stream.VideoRSStream;
import ch.bildspur.realsense.type.*;
import org.intel.rs.Context;
import org.intel.rs.device.AdvancedDevice;
import org.intel.rs.device.Device;
import org.intel.rs.device.DeviceList;
import org.intel.rs.frame.*;
import org.intel.rs.pipeline.Config;
import org.intel.rs.pipeline.Pipeline;
import org.intel.rs.pipeline.PipelineProfile;
import org.intel.rs.processing.Align;
import org.intel.rs.sensor.Sensor;
import org.intel.rs.sensor.SensorList;
import org.intel.rs.stream.VideoStreamProfile;
import org.intel.rs.types.*;
import org.intel.rs.util.Utils;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PImage;
import processing.core.PVector;

import java.util.ArrayList;
import java.util.List;

public class RealSenseCamera implements PConstants {
    private final int defaultWidth = 640;
    private final int defaultHeight = 480;
    private final int defaultStreamIndex = 0;
    private final int defaultFrameRate = 30;

    // static
    private static Context context = new Context();

    // processing
    private final PApplet parent;

    // realsense
    private Config config = new Config();
    private Pipeline pipeline = new Pipeline(context);
    private PipelineProfile pipelineProfile;

    // streams
    private VideoRSStream colorStream = new VideoRSStream();
    private DepthRSStream depthStream = new DepthRSStream();
    private VideoRSStream firstIRStream = new VideoRSStream();
    private VideoRSStream secondIRStream = new VideoRSStream();
    private PoseRSStream poseStream = new PoseRSStream();

    // processors
    // todo: add syncer and pointcloud
    private RSColorizer colorizer = new RSColorizer();
    private RSProcessingBlock<Align> align = new RSProcessingBlock<>();

    // processor lists
    private final RSProcessingBlock[] blocks = {colorizer, align};
    private final List<RSFilterBlock> filters = new ArrayList<>();

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
     *
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

    /**
     * Enable a new pose stream with default configuration.
     */
    public void enablePoseStream() {
        // this does not work like the video streams (only uses type and format)
        poseStream.init(Stream.Pose, Format.SixDOF);
        config.enableStream(Stream.Pose, Format.SixDOF);
    }

    // Processors
    public RSColorizer enableColorizer() {
        return enableColorizer(ColorScheme.Jet);
    }

    public RSColorizer enableColorizer(ColorScheme scheme) {
        colorizer.init();
        colorizer.setColorScheme(scheme);

        return colorizer;
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

    public RSDecimationFilter addDecimationFilter() {
        return addDecimationFilter(2);
    }

    public RSDecimationFilter addDecimationFilter(int filterMagnitude) {
        RSDecimationFilter filter = new RSDecimationFilter();
        addFilter(filter);

        filter.setFilterMagnitude(filterMagnitude);
        return filter;
    }

    public RSDisparityTransform addDisparityTransform() {
        return addDisparityTransform(true);
    }

    public RSDisparityTransform addDisparityTransform(boolean depthToDisparity) {
        RSDisparityTransform filter = new RSDisparityTransform(depthToDisparity);
        addFilter(filter);
        return filter;
    }

    public RSHoleFillingFilter addHoleFillingFilter() {
        return addHoleFillingFilter(HoleFillingType.FarestFromAround);
    }

    public RSHoleFillingFilter addHoleFillingFilter(HoleFillingType fillingType) {
        RSHoleFillingFilter filter = new RSHoleFillingFilter();
        addFilter(filter);

        filter.setHoleFillingType(fillingType);
        return filter;
    }

    public RSSpatialFilter addSpatialFilter() {
        return addSpatialFilter(2, 0.5f, 20, 0);
    }

    public RSSpatialFilter addSpatialFilter(int filterMagnitude, float smoothAlpha, int smoothDelta, int holeFilling) {
        RSSpatialFilter filter = new RSSpatialFilter();
        addFilter(filter);

        filter.setMagnitude(filterMagnitude);
        filter.setSmoothAlpha(smoothAlpha);
        filter.setSmoothDelta(smoothDelta);
        filter.setHoleFilling(holeFilling);

        return filter;
    }

    public RSTemporalFilter addTemporalFilter() {
        return addTemporalFilter(0.4f, 20, PersistencyIndex.ValidIn2_Last4);
    }

    public RSTemporalFilter addTemporalFilter(float smoothAlpha, int smoothDelta, PersistencyIndex persistencyIndex) {
        RSTemporalFilter filter = new RSTemporalFilter();

        addFilter(filter);

        filter.setSmoothAlpha(smoothAlpha);
        filter.setSmoothDelta(smoothDelta);
        filter.setPersistencyIndex(persistencyIndex);

        return filter;
    }

    public RSUnitsTransform addUnitsTransform() {
        RSUnitsTransform filter = new RSUnitsTransform();
        addFilter(filter);

        // todo: are there no options available?

        return filter;
    }

    public RSZeroOrderInvalidationFilter addZeroOrderInvalidationFilter() {
        RSZeroOrderInvalidationFilter filter = new RSZeroOrderInvalidationFilter();
        addFilter(filter);

        // todo: are there no options available?

        return filter;
    }

    public RSThresholdFilter addThresholdFilter() {
        return addThresholdFilter(0.0f, 16.0f);
    }

    public RSThresholdFilter addThresholdFilter(float minDistance, float maxDistance) {
        RSThresholdFilter filter = new RSThresholdFilter();
        addFilter(filter);

        filter.setMinDistance(minDistance);
        filter.setMaxDistance(maxDistance);

        return filter;
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

        if (align.isEnabled()) {
            FrameList temp = align.getBlock().process(frames);
            frames.release();
            frames = temp;
        }

        // copy streams
        if (depthStream.isEnabled()) {
            DepthFrame frame = frames.getDepthFrame();

            // apply depth filter
            if (!filters.isEmpty()) {
                for (RSFilterBlock filter : filters) {
                    DepthFrame temp = filter.getBlock().process(frame);
                    frame.release();
                    frame = temp;
                }
            }

            // update colors if colorized is there
            if (colorizer.isEnabled()) {
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

        if (poseStream.isEnabled()) {
            PoseFrame frame = frames.getPoseFrame();

            frame.release();
        }
    }

    @SuppressWarnings("unchecked")
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

    private void checkRunning() {
        if (running)
            return;

        RuntimeException ex = new RuntimeException("Camera is not running.");
        System.err.println(ex.getMessage());
        throw ex;
    }

    // Camera control

    /**
     * Returns true if a device is available.
     *
     * @return True if device is available.
     */
    public static boolean isDeviceAvailable() {
        return getDeviceCount() > 0;
    }

    /**
     * Returns how many devices are connected.
     *
     * @return Returns how many devices are connected.
     */
    public static int getDeviceCount() {
        DeviceList deviceList = context.queryDevices();
        int count = deviceList.count();
        deviceList.release();
        return count;
    }

    /**
     * Returns a list of created devices.
     * All the devices are created, so it is mandatory to close the devices again to be reused by the library.
     *
     * @return List of created devices.
     */
    public static Device[] getDevices() {
        DeviceList deviceList = context.queryDevices();
        int count = deviceList.count();

        Device[] devices = new Device[count];
        for (int i = 0; i < devices.length; i++)
            devices[i] = deviceList.get(i);

        deviceList.release();
        return devices;
    }

    /**
     * Returns device used by pipeline.
     *
     * @return Returns device used by pipeline.
     */
    public Device getDevice() {
        checkRunning();
        return pipelineProfile.getDevice();
    }

    /**
     * Returns advanced device used by pipeline.
     *
     * @return Returns advanced device used by pipeline.
     */
    public AdvancedDevice getAdvancedDevice() {
        Device device = getDevice();
        // todo: check if device is advanced device?
        return AdvancedDevice.fromDevice(device);
    }

    /**
     * Starts the first camera.
     */
    public synchronized void start() {
        if (running)
            return;

        DeviceList deviceList = context.queryDevices();

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

        for (RSProcessingBlock block : blocks)
            block.release();

        for (RSFilterBlock filter : filters)
            filter.release();

        context.release();
    }

    // Image / Data Getters

    /**
     * Returns depth at specific position in the depth frame.
     * Returns -1 if no depth frame was captured.
     * Returns -2 if no frames were captured at all.
     * Returns -3 if image coordinates are out of range.
     *
     * @param x X coordinate.
     * @param y Y coordinate.
     * @return Distance value.
     */
    public float getDistance(int x, int y) {
        checkRunning();

        if (frames == null)
            return -2;

        DepthFrame depth = frames.getDepthFrame();

        if (depth == null)
            return -1;

        if (x < 0 || x >= depth.getWidth() || y < 0 || y > depth.getHeight())
            return -3;

        float distance = depth.getDistance(x, y);
        depth.release();
        return distance;
    }

    /**
     * Returns depth at specific position in the depth frame.
     * Returns null if something did not work.
     *
     * @param x X coordinate.
     * @param y Y coordinate.
     * @return 3D vector containing the vertex information.
     */
    public PVector getProjectedPoint(int x, int y) {
        checkRunning();

        if (frames == null)
            return null;

        DepthFrame depth = frames.getDepthFrame();

        if (depth == null)
            return null;

        // read depth
        if (x < 0 || x >= depth.getWidth() || y < 0 || y > depth.getHeight()) {
            depth.release();
            return null;
        }

        float distance = depth.getDistance(x, y);

        // project pixel
        VideoStreamProfile profile = depth.getProfileEx();
        Intrinsics intrinsics = profile.getIntrinsics();

        Pixel pixel = new Pixel(x, y);

        Vertex vertex = Utils.deprojectPixelToPoint(intrinsics, pixel, distance);
        PVector v = new PVector(vertex.getX(), vertex.getY(), vertex.getZ());

        // cleanup
        depth.release();
        profile.release();
        intrinsics.release();

        return v;
    }

    /**
     * Returns 2-dimensional short array which contains the depth buffer.
     *
     * @return Y / X short array of the raw depth buffer.
     */
    public short[][] getDepthData() {
        checkRunning();

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

    public Pose getPose() {
        return poseStream.getPose();
    }

    public void setJsonConfiguration(String config) {
        checkRunning();

        AdvancedDevice ad = getAdvancedDevice();
        ad.setJsonConfiguration(config);
    }

    public String getJsonConfiguration() {
        checkRunning();

        AdvancedDevice ad = getAdvancedDevice();
        return ad.getJsonConfiguration();
    }

    // Sensors
    public RSSensor getSensor(int index) {
        SensorList sensors = getDevice().querySensors();
        Sensor sensor = sensors.get(index);
        sensors.release();
        return new RSSensor(sensor);
    }

    public RSDepthSensor getDepthSensor() {
        SensorList sensors = getDevice().querySensors();
        Sensor sensor = null;

        for (Sensor s : sensors) {
            if (s.isExtendableTo(Extension.DepthSensor)) {
                sensor = s;
                break;
            }
        }

        sensors.release();
        return new RSDepthSensor(sensor);
    }

    public RSSensor getRGBSensor() {
        SensorList sensors = getDevice().querySensors();
        Sensor sensor = null;

        for (Sensor s : sensors) {
            if (s.getStreamProfiles().get(0).getStream() == Stream.Color) {
                sensor = s;
                break;
            }
        }

        sensors.release();
        return new RSSensor(sensor);
    }

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

    public RSColorizer getColorizer() {
        return colorizer;
    }
}
