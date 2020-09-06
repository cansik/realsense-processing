import ch.bildspur.realsense.*;
import ch.bildspur.realsense.type.*;

import ch.bildspur.realsense.processing.RSFilterBlock;
import org.intel.rs.processing.ThresholdFilter;
import org.intel.rs.types.Option;

RealSenseCamera camera = new RealSenseCamera(this);

RSFilterBlock thresholdFilter = new RSFilterBlock();

float minDistance = 0.0f;
float maxDistance = 4.0f;
float size = 0.5f;

boolean filterOn = false;

void setup()
{
  size(1280, 720, FX2D);

  // enable depth stream
  camera.enableDepthStream(1280, 720);

  // enable colorizer to display depth
  camera.enableColorizer(ColorScheme.Warm);

  // add threshold filter
  thresholdFilter.init(new ThresholdFilter());
  camera.addFilter(thresholdFilter);

  camera.start();
}

void draw()
{
  background(0);

  // adjust filter
  float filterCenter = map(mouseX, 0, height, minDistance, maxDistance);

  if (filterOn) {
    thresholdFilter.setOption(Option.MinDistance, 
      constrain(filterCenter - (size * 0.5f), minDistance, maxDistance - size));

    thresholdFilter.setOption(Option.MaxDistance, 
      constrain(filterCenter + (size * 0.5f), minDistance + size, maxDistance));
  }
  // read frames
  camera.readFrames();

  // show color image
  image(camera.getDepthImage(), 0, 0, width, height);
}

void keyPressed() {
  thresholdFilter.setOption(Option.MinDistance, minDistance);
  thresholdFilter.setOption(Option.MaxDistance, maxDistance);

  filterOn = !filterOn;
}
