package ch.bildspur.realsense.test;


import ch.bildspur.realsense.RealSenseCamera;
import processing.core.PApplet;
import processing.opengl.PJOGL;

/**
 * Created by cansik on 21.03.17.
 */
public class RawDepthBufferTest extends PApplet {
    public final static int OUTPUT_WIDTH = 1280;
    public final static int OUTPUT_HEIGHT = 500;

    public final static int VIEW_WIDTH = 640;
    public final static int VIEW_HEIGHT = 480;

    public final static int FRAME_RATE = 30;

    RealSenseCamera camera = new RealSenseCamera(this);

    public static void main(String... args) {
        RawDepthBufferTest sketch = new RawDepthBufferTest();
        sketch.runSketch();
    }

    public void settings() {
        size(OUTPUT_WIDTH, OUTPUT_HEIGHT, FX2D);
        PJOGL.profile = 1;
    }

    public void setup() {
        frameRate(FRAME_RATE);

        if(camera.isDeviceAvailable()) {
            println("Camera found!");
        }
        else {
            println("No camera available!");
            exit();
        }

        camera.enableDepthStream();

        camera.enableColorizer();

        camera.start();
    }

    public void draw() {
        // clear screen
        background(55);

        camera.readFrames();

        // show depth stream
        image(camera.getDepthImage(), VIEW_WIDTH, 0, VIEW_WIDTH, VIEW_HEIGHT);

        // get depth buffer and draw onto screen
        short[][] raw = camera.getDepthData();
        noStroke();
        for(int y = 0; y < raw.length; y += 10) {
            for(int x = 0; x < raw[0].length; x += 10) {
                int depth = raw[y][x];

                // map raw data
                float d = map(depth, 0,5000, 0, 255);

                fill(color(d));
                ellipse(x, y, 5, 5);
            }
        }

        fill(255, 255, 255);
        textAlign(LEFT, CENTER);
        text("Raw Depth Data", 20, VIEW_HEIGHT  + 8);
        text("Depth Stream", VIEW_WIDTH + 20, VIEW_HEIGHT  + 8);
        surface.setTitle("RealSense Processing - FPS: " + Math.round(frameRate));
    }
}
