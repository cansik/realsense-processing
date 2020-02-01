package ch.bildspur.realsense.test;


import ch.bildspur.realsense.type.IRStream;
import ch.bildspur.realsense.RealSenseCamera;
import processing.core.PApplet;
import processing.opengl.PJOGL;

/**
 * Created by cansik on 21.03.17.
 */
public class Sketch extends PApplet {
    public final static int OUTPUT_WIDTH = 1280;
    public final static int OUTPUT_HEIGHT = 500;

    public final static int VIEW_WIDTH = 640;
    public final static int VIEW_HEIGHT = 480;

    public final static int FRAME_RATE = 120;

    RealSenseCamera camera = new RealSenseCamera(this);

    public static void main(String... args) {
        Sketch sketch = new Sketch();
        sketch.runSketch();
    }

    public void settings() {
        size(OUTPUT_WIDTH, OUTPUT_HEIGHT, FX2D);
        PJOGL.profile = 1;
    }

    public void setup() {
        frameRate(FRAME_RATE);

        if(camera.isCameraAvailable()) {
            println("Camera found!");
        }
        else {
            println("No camera available!");
            System.exit(1);
        }

        camera.enableIRStream(640, 480, 30, IRStream.Second);
        camera.enableColorStream();

        camera.start();
    }

    public void draw() {
        // clear screen
        background(55);

        camera.readFrames();

        // show both streams
        image(camera.getIRImage(IRStream.Second), 0, 0, VIEW_WIDTH, VIEW_HEIGHT);
        image(camera.getColorImage(), VIEW_WIDTH, 0, VIEW_WIDTH, VIEW_HEIGHT);

        fill(255, 255, 255);
        textAlign(LEFT, CENTER);
        text("IR Stream", 20, VIEW_HEIGHT  + 8);
        text("Color Stream", VIEW_WIDTH + 20, VIEW_HEIGHT  + 8);
        surface.setTitle("RealSense Processing - FPS: " + Math.round(frameRate));
    }
}
