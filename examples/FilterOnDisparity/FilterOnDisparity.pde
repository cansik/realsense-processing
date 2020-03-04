import ch.bildspur.realsense.*;
import ch.bildspur.realsense.type.*;
import ch.bildspur.realsense.processing.RSFilterBlock;

import org.intel.rs.processing.DisparityTransform;

RealSenseCamera camera = new RealSenseCamera(this);

RSFilterBlock toDisparityTransform = new RSFilterBlock();
RSFilterBlock fromDisparityTransform = new RSFilterBlock();

void setup()
{
  size(640, 480);
  
  // enable depth stream
  camera.enableDepthStream(640, 480);
  
  // enable colorizer to display depth
  camera.enableColorizer(ColorScheme.Cold);
  
  // init disparity transforms
  toDisparityTransform.init(new DisparityTransform(true));
  fromDisparityTransform.init(new DisparityTransform(false));
  
  // add filters
  camera.addFilter(toDisparityTransform);
  camera.addSpatialFilter();
  camera.addTemporalFilter();
  camera.addFilter(fromDisparityTransform);
  
  // start camera
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
