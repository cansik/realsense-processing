import ch.bildspur.realsense.*;

RealSenseCamera camera = new RealSenseCamera(this);

void setup()
{
  size(640, 480);

  // width, height, fps, depth-stream, color-stream
  camera.start(640, 480, 30, true, true);
}

void draw()
{
  background(0);

  // read frames
  camera.readFrames();

  // show color image
  image(camera.getColorImage(), 0, 0);
  
  fill(0, 255, 255);
  textSize(20);
  textAlign(RIGHT, BOTTOM);
  text(camera.getDepth(mouseX, mouseY), mouseX, mouseY);
}
