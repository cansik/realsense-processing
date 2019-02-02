package ch.bildspur.realsense.test;


import ch.bildspur.realsense.RealSenseCamera;
import processing.core.PApplet;
import processing.opengl.PJOGL;

/**
 * Created by cansik on 21.03.17.
 */
public class Sketch extends PApplet {
    public final static int OUTPUT_WIDTH = 1280;
    public final static int OUTPUT_HEIGHT = 500;

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

        camera.start(640, 480, 30, true, true);
    }

    public void draw() {
        // clear screen
        background(55);

        camera.readFrames();

        // create depth image
        camera.createDepthImage(0, 1500);

        // show both streams
        image(camera.getDepthImage(), 0, 0);
        image(camera.getColorImage(), 640, 0);

        // show depth info
        if(mouseX < 640 && mouseY < 480)
        {
            // show depth info
            fill(0, 255, 0);
            text("Depth: " + camera.getDepth(mouseX, mouseY), mouseX, mouseY + 10);
        }

        fill(255, 255, 255);
        textAlign(LEFT, CENTER);
        text("Depth Stream", 20, 480  + 8);
        text("Color Stream", 640 + 20, 480  + 8);
        surface.setTitle("RealSense Processing - FPS: " + Math.round(frameRate));
    }
}
