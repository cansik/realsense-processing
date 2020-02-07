import ch.bildspur.realsense.*;
import ch.bildspur.realsense.type.ColorScheme;

import org.intel.rs.device.AdvancedDevice;

RealSenseCamera camera = new RealSenseCamera(this);

void setup()
{
  size(640, 480);

  // load json config from file
  String jsonConfig = String.join("\n", loadStrings("RawStereoConfig.json"));

  // get first device and load configuration
  AdvancedDevice device = camera.getAdvancedDevice();
  device.setAdvancedModeEnabled(true);
  device.setJsonConfiguration(jsonConfig);

  // enable color & depth stream
  camera.enableColorStream();
  camera.enableDepthStream();

  // add colorizer
  camera.enableColorizer(ColorScheme.Bio);

  // align the streams
  camera.enableAlign();

  camera.start(device);
}

void draw()
{
  background(0);

  // read frames
  camera.readFrames();

  // show color images
  tint(255, 255);
  image(camera.getColorImage(), 0, 0);

  tint(255, 100);
  image(camera.getDepthImage(), 0, 0);
}
