import ch.bildspur.realsense.*;

RealSenseCamera camera = new RealSenseCamera(this);

void setup()
{
  size(640, 480);

  // enable color & depth stream
  camera.enableColorStream();
  camera.enableDepthStream();

  // add colorizer
  camera.enableColorizer();

  // align the streams
  camera.enableAlign();

  camera.start();
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
