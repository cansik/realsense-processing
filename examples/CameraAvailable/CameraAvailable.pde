import ch.bildspur.realsense.*;

RealSenseCamera camera = new RealSenseCamera(this);

void setup()
{
  size(640, 480);
}

void draw()
{
  background(55);

  textSize(20);
  textAlign(CENTER, CENTER);

  if (camera.isCameraAvailable())
  {
    fill(100, 255, 100);
    text("camera available!", width / 2, height / 2);
  } else
  {
    fill(255, 100, 100);
    text("no camera available!", width / 2, height / 2);
  }
}
