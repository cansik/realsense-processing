import ch.bildspur.realsense.*;

RealSenseCamera cam1 = new RealSenseCamera(this);
RealSenseCamera cam2 = new RealSenseCamera(this);

void setup()
{
  size(1280, 480);
  
  if(cam1.getDeviceCount() < 2) {
    println("attach at least two realsense cameras");
    exit();
  }
  
  cam1.enableColorStream(640, 480, 30);
  cam1.start("840412060157"); // start by serial number
  
  cam2.enableColorStream(640, 480, 30);
  cam2.start("817612070819"); // start by serial number
}

void draw()
{
  background(0);
  
  // read frames
  cam1.readFrames();
  cam2.readFrames();
  
  // show color images
  image(cam1.getColorImage(), 0, 0);
  image(cam2.getColorImage(), 640, 0);
}
