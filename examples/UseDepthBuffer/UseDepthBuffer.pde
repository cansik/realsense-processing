import ch.bildspur.realsense.*;

RealSenseCamera camera = new RealSenseCamera(this);

void setup()
{
  size(640, 480);

  pixelDensity(2);

  camera.enableDepthStream(640, 480);
  camera.start();
}

void draw()
{
  background(22);

  // read frames
  camera.readFrames();

  // read depth buffer
  short[][] data = camera.getDepthData();

  noFill();
  strokeWeight(1.0);

  // use it to display circles
  for (int y = 0; y < height; y += 10) {
    for (int x = 0; x < width; x += 10) {

      // get intensity
      int intensity = data[y][x];

      // map intensity (values between 0-65536)
      float d = map(intensity, 0, 5000, 10, 0);
      float c = map(intensity, 0, 5000, 255, 0);

      d = constrain(d, 0, 8);
      c = constrain(c, 0, 255);

      stroke(c, 255, 255);
      circle(x, y, d);
    }
  }
}
