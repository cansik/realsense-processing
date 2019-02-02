RealSenseCamera camera;

void setup()
{
  size(640, 480);

  // setup realsense camera
  // width, height, fps, enableDepthStream, enableColorStream
  camera = new RealSenseCamera(640, 480, 30, true, true);
  camera.setup();

  // set depth far plane
  camera.setDepthLevelHigh(5000);
}

void draw()
{
  background(0);

  // read streams
  camera.readStreams();

  // show depth stream
  image(camera.getDepthImage(), 0, 0);

  // show color stream
  //image(camera.getColorImage(), 0, 0);

  // show fps
  surface.setTitle("FPS: " + frameRate);
}
