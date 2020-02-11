import ch.bildspur.realsense.*;
import ch.bildspur.realsense.type.ColorScheme;

import org.intel.rs.device.AdvancedDevice;

RealSenseCamera camera = new RealSenseCamera(this);

void setup()
{
  size(640, 480);

  // load json config from file
  String jsonConfig = String.join("\n", loadStrings("RawStereoConfig.json"));

  // enable color & depth stream
  camera.enableColorStream();
  camera.enableDepthStream();

  // add colorizer
  camera.enableColorizer(ColorScheme.Warm);

  // align the streams
  camera.enableAlign();

  camera.start();
  camera.setJsonConfiguration(jsonConfig);
}

void draw()
{
  background(0);

  // read frames
  camera.readFrames();

  // show color images
  blendMode(BLEND);
  image(camera.getColorImage(), 0, 0);

  blendMode(MULTIPLY);
  image(camera.getDepthImage(), 0, 0);
}
