package ch.bildspur.realsense.test;


import ch.bildspur.realsense.RealSenseCamera;
import org.intel.rs.device.Device;
import org.intel.rs.types.Pose;
import processing.core.PApplet;

/**
 * Created by cansik on 21.03.17.
 */
public class PoseStreamTest extends PApplet {

    RealSenseCamera camera = new RealSenseCamera(this);

    public static void main(String... args) {
        PoseStreamTest sketch = new PoseStreamTest();
        sketch.runSketch();
    }

    public void settings() {
        size(640, 480);
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

        camera.enablePoseStream();

        camera.start();
    }

    public void draw() {
        // clear screen
        background(55);

        camera.readFrames();
        Pose pose = camera.getPose();

        // display pose data
        fill(255, 255, 255);
        textAlign(CENTER, CENTER);
        text(pose.getRotation()[0],  width / 2, height / 2);
        surface.setTitle("RealSense Processing - FPS: " + Math.round(frameRate));
    }
}
