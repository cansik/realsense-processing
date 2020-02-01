package ch.bildspur.realsense.test;


import ch.bildspur.realsense.RealSenseCamera;
import ch.bildspur.realsense.type.ColorScheme;
import processing.core.PApplet;
import processing.opengl.PJOGL;

/**
 * Created by cansik on 21.03.17.
 */
public class DepthMeasureTest extends PApplet {
    public final static int OUTPUT_WIDTH = 1280;
    public final static int OUTPUT_HEIGHT = 500;

    public final static int VIEW_WIDTH = 640;
    public final static int VIEW_HEIGHT = 480;

    public final static int FRAME_RATE = 30;

    RealSenseCamera camera = new RealSenseCamera(this);

    public static void main(String... args) {
        DepthMeasureTest sketch = new DepthMeasureTest();
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
            exit();
        }

        camera.enableDepthStream();
        camera.enableColorStream();

        camera.enableColorizer(ColorScheme.Cold);

        camera.start();
    }

    public void draw() {
        // clear screen
        background(55);

        camera.readFrames();

        // show both streams
        image(camera.getDepthImage(), 0, 0, VIEW_WIDTH, VIEW_HEIGHT);
        image(camera.getColorImage(), VIEW_WIDTH, 0, VIEW_WIDTH, VIEW_HEIGHT);

        // show depth info
        if(mouseX < VIEW_WIDTH && mouseY < VIEW_HEIGHT)
        {
            // show depth info
            fill(255, 255, 0);
            textSize(20);
            text("Depth: " + nfp(camera.getDistance(mouseX, mouseY), 0, 2) + "m", mouseX, mouseY + 10);
        }

        fill(255, 255, 255);
        textSize(12);
        textAlign(LEFT, CENTER);
        text("Depth Stream", 20, VIEW_HEIGHT  + 8);
        text("Color Stream", VIEW_WIDTH + 20, VIEW_HEIGHT  + 8);
        surface.setTitle("RealSense Processing - FPS: " + Math.round(frameRate));
    }
}
