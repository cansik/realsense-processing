package ch.bildspur.realsense.test;


import ch.bildspur.realsense.RealSenseCamera;
import ch.bildspur.realsense.type.IRStream;
import org.intel.rs.device.Device;
import processing.core.PApplet;
import processing.opengl.PJOGL;

/**
 * Created by cansik on 21.03.17.
 */
public class GetDevicesTest extends PApplet {

    RealSenseCamera camera = new RealSenseCamera(this);

    public static void main(String... args) {
        GetDevicesTest sketch = new GetDevicesTest();
        sketch.runSketch();
    }

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        if(camera.isCameraAvailable()) {
            println("Camera found!");
        }
        else {
            println("No camera available!");
            System.exit(1);
        }

        // list all serial numbers
        Device[] devices = camera.getDevices();

        println("Cameras: " + camera.getCameraCount());

        for(Device d : devices) {
            println(d.getSerialNumber());
            d.close();
        }

        println("Cameras: " + camera.getCameraCount());

        camera.enableColorStream();

        camera.start();
    }

    public void draw() {
        // clear screen
        background(55);

        camera.readFrames();

        // show both streams
        image(camera.getColorImage(), 0, 0, 640, 480);

        fill(255, 255, 255);
        textAlign(LEFT, CENTER);
        text("Color Stream",  20, 480  + 8);
        surface.setTitle("RealSense Processing - FPS: " + Math.round(frameRate));
    }
}
