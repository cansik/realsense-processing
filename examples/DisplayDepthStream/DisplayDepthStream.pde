import ch.bildspur.realsense.*;

RealSenseCamera camera = new RealSenseCamera(this);

void setup()
{
  size(640, 480);
  
  // width, height, fps, depth-stream, color-stream
  camera.start(640, 480, 30, true, false);
}

void draw()
{
  background(0);
  
  // read frames
  camera.readFrames();
  
  // create grayscale image form depth buffer
  // min and max depth
  camera.createDepthImage(0, 3000);
  
  // show color image
  image(camera.getDepthImage(), 0, 0);
}
