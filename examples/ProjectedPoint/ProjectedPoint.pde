import ch.bildspur.realsense.*;

RealSenseCamera camera = new RealSenseCamera(this);

void setup()
{
  size(640, 480);

  // enable color & depth stream
  camera.enableColorStream();
  camera.enableDepthStream();
  
  // align the streams to get accurate results
  camera.enableAlign();

  camera.start();
  
  noCursor();
}

void draw()
{
  background(0);

  // read frames
  camera.readFrames();

  // show color image
  image(camera.getColorImage(), 0, 0);
  
  // measure distnace
  PVector vertex = camera.getProjectedPoint(mouseX, mouseY);
  String vertexText = "X: " + nf(vertex.x, 0, 2) + " Y: " + nf(vertex.y, 0, 2) + " Z: " + nf(vertex.z, 0, 2);
  
  fill(0, 255, 255);
  textSize(20);
  textAlign(LEFT, TOP);
  text(vertexText, mouseX + 5, mouseY + 5);
  
  stroke(0, 255, 255);
  noFill();
  strokeWeight(2.0);
  circle(mouseX, mouseY, 5);
}
