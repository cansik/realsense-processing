package ch.bildspur.realsense.test;


import ch.bildspur.realsense.RealSenseCamera;
import org.intel.rs.device.Device;
import processing.core.PApplet;

/**
 * Created by cansik on 21.03.17.
 */
public class MultiCamTest extends PApplet {

    RealSenseCamera cam1 = new RealSenseCamera(this);
    RealSenseCamera cam2 = new RealSenseCamera(this);

    float measuredDistance = 0;

    public static void main(String... args) {
        MultiCamTest sketch = new MultiCamTest();
        sketch.runSketch();
    }

    public void settings() {
        size(1280, 480);
    }

    public void setup() {
        if(RealSenseCamera.isDeviceAvailable()) {
            println("Camera found!");
        }
        else {
            println("No camera available!");
            System.exit(1);
        }

        // list all serial numbers
        Device[] devices = RealSenseCamera.getDevices();

        println("Cameras: " + RealSenseCamera.getDeviceCount());

        for(Device d : devices) {
            println(d.getSerialNumber());
            d.close();
        }

        cam1.enableDepthStream();
        cam2.enableDepthStream();

        cam1.enableColorizer();
        cam2.enableColorizer();

        cam1.start("037522250717");
        cam2.start("819612070681");
    }

    public void draw() {
        // clear screen
        background(55);

        cam1.readFrames();
        cam2.readFrames();

        image(cam1.getDepthImage(), 0, 0);
        image(cam2.getDepthImage(), 640, 0);

        fill(255, 255, 255);
        textAlign(LEFT, CENTER);
        text("037522250717",  20, 480  + 8);
        text("819612070681",  640 + 20, 480  + 8);

        surface.setTitle("RealSense Processing - Distance: "
                + measuredDistance
                + " - FPS: " + Math.round(frameRate));
    }

    public void mousePressed() {
        if(mouseX < 640) {
            measuredDistance = cam1.getDistance(mouseX, mouseY);
        } else {
            measuredDistance = cam2.getDistance(mouseX - 640, mouseY);
        }
    }
}
