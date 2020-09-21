import ch.bildspur.realsense.*;
import ch.bildspur.realsense.type.ColorScheme;

import org.intel.rs.types.Option;

RealSenseCamera camera = new RealSenseCamera(this);

void setup()
{
  size(640, 480);

  // enable color & depth stream
  camera.enableColorStream();

  // add colorizer
  camera.enableColorizer(ColorScheme.Warm);

  // align the streams
  camera.enableAlign();

  camera.start();

  // disable auto exposure
  camera.getRGBSensor().setOption(Option.EnableAutoExposure, 0.0f);
}

void draw()
{
  background(0);

  // read frames
  camera.readFrames();

  // show color images
  blendMode(BLEND);
  image(camera.getColorImage(), 0, 0);
}
