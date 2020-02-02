import ch.bildspur.realsense.*;
import ch.bildspur.realsense.type.*;

import ch.bildspur.realsense.*;

RealSenseCamera camera = new RealSenseCamera(this);

void setup()
{
  size(640, 480);
  
  // enable depth stream
  camera.enableDepthStream(640, 480);
  
  // enable colorizer to display depth
  camera.enableColorizer(ColorScheme.Cold);
  
  camera.start();
}

void draw()
{
  background(0);
  
  // read frames
  camera.readFrames();
  
  // show color image
  image(camera.getDepthImage(), 0, 0);
}
