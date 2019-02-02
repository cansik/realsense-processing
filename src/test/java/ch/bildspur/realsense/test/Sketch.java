package ch.bildspur.realsense.test;


import ch.bildspur.realsense.RealSenseCamera;
import processing.core.PApplet;
import processing.opengl.PJOGL;

/**
 * Created by cansik on 21.03.17.
 */
public class Sketch extends PApplet {
    public final static int OUTPUT_WIDTH = 640;
    public final static int OUTPUT_HEIGHT = 480;

    public final static int FRAME_RATE = 60;

    RealSenseCamera camera = new RealSenseCamera(this);

    public void settings() {
        size(OUTPUT_WIDTH, OUTPUT_HEIGHT, FX2D);
        PJOGL.profile = 1;
    }

    public void setup() {
        frameRate(FRAME_RATE);

        if(camera.isCameraAvailable())
            println("Camera found!");
        else
            println("No camera available!");
    }

    public void draw() {
        // clear screen
        background(55);

        fill(0, 255, 0);
        text("FPS: " + frameRate, 20, 20);
    }
}
