import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.librealsense.Config;
import org.librealsense.Context;
import org.librealsense.Device;
import org.librealsense.DeviceList;
import org.librealsense.Frame;
import org.librealsense.FrameList;
import org.librealsense.Pipeline;
import org.librealsense.Native.Extension;
import org.librealsense.Native.Format;
import org.librealsense.Native.Stream;
import processing.core.PImage;

public final class RealSenseCamera {
  private Context context;
  private Pipeline pipeline;
  private final int depthStreamIndex = 0;
  private final int colorStreamIndex = 0;
  private final PImage depthImage;
  private final PImage colorImage;
  private int depthLevelLow = 0;
  private int depthLevelHigh = 65536;
  private final int width;
  private final int height;
  private final int fps;
  private final boolean enableDepthStream;
  private final boolean enableColorStream;

  public final PImage getDepthImage() {
    return this.depthImage;
  }

  public final PImage getColorImage() {
    return this.colorImage;
  }

  public final int getDepthLevelLow() {
    return this.depthLevelLow;
  }

  public final void setDepthLevelLow(int value) {
    this.depthLevelLow = value;
  }

  public final int getDepthLevelHigh() {
    return this.depthLevelHigh;
  }

  public final void setDepthLevelHigh(int value) {
    this.depthLevelHigh = value;
  }

  public RealSenseCamera(int width, int height, int fps, boolean enableDepthStream, boolean enableColorStream) {
    this.width = width;
    this.height = height;
    this.fps = fps;
    this.enableDepthStream = enableDepthStream;
    this.enableColorStream = enableColorStream;
    this.context = Context.create();
    this.depthImage = new PImage(this.width, this.height, PConstants.RGB);
    this.colorImage = new PImage(this.width, this.height, PConstants.RGB);
  }

  public final boolean checkIfDeviceIsAvailable() {
    DeviceList deviceList = this.context.queryDevices();
    List devices = deviceList.getDevices();
    Collection var3 = (Collection)devices;
    return !var3.isEmpty();
  }

  public final void setup() {
    println("RS: starting camera...");
    DeviceList deviceList = this.context.queryDevices();
    List devices = deviceList.getDevices();
    println("RS: device count: " + devices.size());

    if (devices.size() == 0)
    {
      println("RS: no cameras found!");
      return;
    }

    Device device = (Device)devices.get(0);
    println("RS: device found: " + device.name());
    println("RS: setting up pipeline");
    Pipeline var10001 = this.context.createPipeline();
    this.pipeline = var10001;
    Config config = Config.create();
    config.enableDevice(device);
    if (this.enableDepthStream) {
      config.enableStream(Stream.RS2_STREAM_DEPTH, this.depthStreamIndex, this.width, this.height, Format.RS2_FORMAT_Z16, this.fps);
    }

    if (this.enableColorStream) {
      config.enableStream(Stream.RS2_STREAM_COLOR, this.colorStreamIndex, this.width, this.height, Format.RS2_FORMAT_RGB8, this.fps);
    }

    println("RS: starting device...");
    Pipeline pipeline = this.pipeline;

    pipeline.startWithConfig(config);
    println("RS: started!");
  }

  public final void readStreams() {
    FrameList frames = pipeline.waitForFrames(5000);
    int i = 0;

    for (int var3 = frames.frameCount(); i < var3; ++i) {
      Frame frame = frames.frame(i);
      if (frame.isExtendableTo(Extension.RS2_EXTENSION_DEPTH_FRAME)) {
        this.readDepthImage(frame);
      } else {
        this.readColorImage(frame);
      }

      frame.release();
    }

    frames.release();
  }

  private final void readDepthImage(Frame frame) {
    CharBuffer buffer = frame.getFrameData().asCharBuffer();
    this.depthImage.loadPixels();

    for (int i = 0; i < width * height; i++)
    {
      int depth = buffer.get(i) & 0xFFFF;
      int grayScale = this.clamp((int)map(depth, depthLevelLow, depthLevelHigh, 255, 0), 0, 255);

      if (depth > 0)
        depthImage.pixels[i] = color(grayScale);
      else
        depthImage.pixels[i] = color(0);
    }

    this.depthImage.updatePixels();
  }

  private final void readColorImage(Frame frame) {
    ByteBuffer buffer = frame.getFrameData();
    this.colorImage.loadPixels();

    for (int i = 0; i < frame.getStrideInBytes() * height; i += 3)
    {
      colorImage.pixels[i / 3] = color(buffer.get(i) & 0xFF, buffer.get(i + 1) & 0xFF, buffer.get(i + 2) & 0xFF);
    }

    this.colorImage.updatePixels();
  }

  public final void stop() {
  }

  private final int clamp(int $receiver, int min, int max) {
    return Math.max(Math.min(max, $receiver), min);
  }

  public final int getWidth() {
    return this.width;
  }

  public final int getHeight() {
    return this.height;
  }

  public final int getFps() {
    return this.fps;
  }

  public final boolean getEnableDepthStream() {
    return this.enableDepthStream;
  }

  public final boolean getEnableColorStream() {
    return this.enableColorStream;
  }
}
