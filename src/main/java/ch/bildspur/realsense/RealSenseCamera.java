package ch.bildspur.realsense;

import org.librealsense.*;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PImage;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.file.Paths;
import java.util.List;

/**
 * Intel RealSense Camera
 */
public class RealSenseCamera implements PConstants {
    private static char MIN_DEPTH = 0;
    private static char MAX_DEPTH = 65535;

    // processing
    private PApplet parent;

    // realsense
    private Context context;
    private Pipeline pipeline;

    private Colorizer colorizer;
    private PointCloud pointCloud;

    private final int depthStreamIndex = 0;
    private final int colorStreamIndex = 0;
    private final int irStreamIndex = 1;

    // camera
    volatile private boolean running = false;

    private int width;
    private int height;
    private int fps;
    private boolean enableDepthStream;
    private boolean enableColorStream;
    private boolean enableIRStream;

    private PImage depthImage;
    private PImage colorImage;
    private PImage irImage;
    private char[] depthBuffer;

    /**
     * Create a new Intel RealSense camera.
     * @param parent Parent processing sketch.
     */
    public RealSenseCamera(PApplet parent)
    {
        this.parent = parent;
        parent.registerMethod("stop", this);

        // load native libs
        loadNativeLibraries();

        // create context
        context = Context.create();
    }

    /**
     * Start the camera.
     * @param width Width of the camera image.
     * @param height Height of the camera image.
     * @param fps Frames per second to capture.
     * @param enableDepthStream True if depth stream should be enabled.
     * @param enableColorStream True if color stream should be enabled.
     */
    public void start(int width, int height, int fps, boolean enableDepthStream, boolean enableColorStream, boolean enableIRStream)
    {
        DeviceList deviceList = this.context.queryDevices();
        List<Device> devices = deviceList.getDevices();

        if(devices.isEmpty())
            PApplet.println("RealSense: No device found!");

        this.start(devices.get(0), width, height, fps, enableDepthStream, enableColorStream, enableIRStream);
    }

    /**
     * Start the camera.
     * @param device Intel RealSense device to start.
     * @param width Width of the camera image.
     * @param height Height of the camera image.
     * @param fps Frames per second to capture.
     * @param enableDepthStream True if depth stream should be enabled.
     * @param enableColorStream True if color stream should be enabled.
     */
     public void start(Device device, int width, int height, int fps, boolean enableDepthStream, boolean enableColorStream, boolean enableIRStream)
     {
         if(running)
             return;

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

         // create buffer
         this.depthBuffer = new char[this.width * this.height];

         // create pipeline
         colorizer = new Colorizer();
         pointCloud = new PointCloud();
         pipeline = context.createPipeline();

         Config config = Config.create();
         config.enableDevice(device);

         if (this.enableDepthStream) {
             config.enableStream(Native.Stream.RS2_STREAM_DEPTH,
                     this.depthStreamIndex,
                     this.width,
                     this.height,
                     Native.Format.RS2_FORMAT_Z16,
                     this.fps);
         }

         if (this.enableColorStream) {
             config.enableStream(Native.Stream.RS2_STREAM_COLOR,
                     this.colorStreamIndex,
                     this.width,
                     this.height,
                     Native.Format.RS2_FORMAT_RGB8,
                     this.fps);
         }

         if (this.enableIRStream) {
             config.enableStream(Native.Stream.RS2_STREAM_INFRARED,
                     this.irStreamIndex,
                     this.width,
                     this.height,
                     Native.Format.RS2_FORMAT_Y8,
                     this.fps);
         }

         // start pipeline
         pipeline.startWithConfig(config);

         running = true;
     }

    /**
     * Read the camera frame buffers for all active streams.
     */
    public void readFrames()
    {
        FrameList frames = pipeline.waitForFrames(5000);

        for (int i = 0; i < frames.frameCount(); i++) {
            Frame frame = frames.frame(i);

            StreamProfile profile = frame.getStreamProfile();
            profile.getProfileData();

            if (profile.getStream() == Native.Stream.RS2_STREAM_DEPTH) {
                this.readDepthBuffer(frame);

                // check if colorized frame is available
                Frame colorizedFrame = colorizer.process(frame);

                if(colorizedFrame != null) {
                    this.readColorImage(colorizedFrame);
                    colorizedFrame.release();
                }

                // check if pointcloud is available
                Points points = pointCloud.process(frame);
                PApplet.println(points.getCount());
                points.release();
            }

            if(profile.getStream() == Native.Stream.RS2_STREAM_COLOR) {
                this.readColorImage(frame);
            }

            if(profile.getStream() == Native.Stream.RS2_STREAM_INFRARED) {
                this.readIRImage(frame);
            }

            frame.release();
        }

        frames.release();
    }

    /**
     * Returns true if a device is available.
     * @return True if device is available.
     */
    public boolean isCameraAvailable() {
        DeviceList deviceList = this.context.queryDevices();
        return deviceList.getDeviceCount() > 0;
    }

    /**
     * Stop the camera.
     */
    public void stop()
    {
        PApplet.println("cleanup realsense...");

        if(!running)
            return;

        // clean up
        pipeline.stop();
        context.delete();

        // set states
        running = false;
    }

    /**
     * Creates grayscale depth image from depth buffer (accessible through getDepthImage()).
     * @param minDepth Minimum depth value which translates to white.
     * @param maxDepth Maximum depth value which translates to black.
     */
    public void createDepthImage(int minDepth, int maxDepth)
    {
        this.depthImage.loadPixels();

        for (int i = 0; i < width * height; i++)
        {
            int grayScale = (int) PApplet.map(depthBuffer[i] & 0xFFFF, minDepth, maxDepth, 255, 0);
            grayScale = PApplet.constrain(grayScale, MIN_DEPTH, MAX_DEPTH);

            if (depthBuffer[i] > 0)
                depthImage.pixels[i] = toColor(grayScale);
            else
                depthImage.pixels[i] = toColor(0);
        }

        this.depthImage.updatePixels();
    }

    private void readIRImage(Frame frame)
    {
        ByteBuffer buffer = frame.getFrameData();
        irImage.loadPixels();

        for(int i = 0; i < width * height; i++)
        {
            int irvalue = buffer.get(i) & 0xFF;
            irImage.pixels[i] = toColor(irvalue);
        }

        irImage.updatePixels();
    }

    private void readDepthBuffer(Frame frame) {
        CharBuffer buffer = frame.getFrameData().asCharBuffer();
        buffer.get(depthBuffer);
    }

    private void readColorImage(Frame frame) {
        ByteBuffer buffer = frame.getFrameData();
        this.colorImage.loadPixels();

        for (int i = 0; i < frame.getStrideInBytes() * height; i += 3)
        {
            colorImage.pixels[i / 3] = toColor(buffer.get(i) & 0xFF, buffer.get(i + 1) & 0xFF, buffer.get(i + 2) & 0xFF);
        }

        this.colorImage.updatePixels();
    }

    private int toColor(int gray)
    {
        return toColor(gray, gray, gray);
    }

    private int toColor(int red, int green, int blue)
    {
        return -16777216 | red << 16 | green << 8 | blue;
    }

    private void loadNativeLibraries()
    {
        String os = System.getProperty("os.name").toLowerCase();
        String libPath = ProcessingUtils.getLibPath(this.parent);

        if (os.contains("win")) {
            Native.loadNativeLibraries(Paths.get(libPath,"native/windows-x64").toString());
        } else if (os.contains("mac")) {
            Native.loadNativeLibraries(Paths.get(libPath,"native/osx-x64").toString());
        } else if (os.contains("nix") || os.contains("nux") || os.contains("aix")) {
            Native.loadNativeLibraries(Paths.get(libPath,"native/linux-64").toString());
        } else {
            // Operating System not supported!
            PApplet.println("RealSense: Load the native libraries by your own.");
        }
    }

    /**
     * Check if the camera is already running.
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
     * @param x X coordinate.
     * @param y Y coordinate.
     * @return Depth value between 0 and 65535.
     */
    public int getDepth(int x, int y)
    {
        return depthBuffer[x + y * width] & 0xFFFF;
    }
}
