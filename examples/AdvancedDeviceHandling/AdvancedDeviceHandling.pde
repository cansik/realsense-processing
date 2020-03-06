import ch.bildspur.realsense.*;
import org.intel.rs.device.*;

RealSenseCamera rs2 = new RealSenseCamera(this);
RealSenseCamera[] cams;

void setup()
{
  size(640, 120);
  
  Device[] devices = rs2.getDevices();
  cams = new RealSenseCamera[devices.length];
  
  println("found " + devices.length + " devices!");
  
  // start all devices
  for(int i = 0; i < devices.length; i++) {
      Device device = devices[i];
    
      RealSenseCamera cam = new RealSenseCamera(this);
      cam.enableColorStream(640, 480, 30);
      cams[i] = cam;
      
      // close device to reopen it later
      device.close();
      cam.start(device);
  }
}

void draw()
{
  background(0);
  
  // read frames
  for(int i = 0; i < cams.length; i++) {
    RealSenseCamera cam = cams[i];
    cam.readFrames();
    
    image(cam.getColorImage(), i * 160, 0, 160, 120);
    
    fill(255);
    text("#" + i + " SN: " + cam.getDevice().getSerialNumber(), i * 160 + 5, 100);
  }
}
