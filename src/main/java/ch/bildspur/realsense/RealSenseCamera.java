package ch.bildspur.realsense;

import processing.core.PApplet;
import processing.core.PConstants;

/**
 * Sweep Sensor
 */
public class RealSenseCamera implements PConstants {
    private static final int DEFAULT_SPEED = 5;
    private static final int DEFAULT_SAMPLE_RATE = 500;

    private static final int THREAD_JOIN_WAIT_TIME = 5000;

    private PApplet parent;
    private Thread sweepThread;

    volatile private boolean isRunning = false;
    volatile private boolean isStarting = false;


    /**
     * Create a new Sweep sensor.
     * @param parent Parent processing sketch.
     */
    public RealSenseCamera(PApplet parent)
    {
        this.parent = parent;
        parent.registerMethod("stop", this);
    }

    /**
     * Start the sensor and listen on a specific port.
     * @param port COM Port to listen on.
     */
    public void start(String port) {
      this.start(port, DEFAULT_SPEED, DEFAULT_SAMPLE_RATE);
    }

    /**
     * Start the sensor and listen on a specific port, rotation speed and sample rate.
     * @param port COM Port to listen on.
     * @param speed rotation speed of the realsense sensor.
     * @param sampleRate sample rate of the realsense sensor.
     */
    public void start(String port, int speed, int sampleRate) {this.start(port, speed, sampleRate, false);}

    /**
     * Start the sensor and listen on a specific port.
     * @param port COM Port to listen on.
     */
    public void startAsync(String port) {
        this.startAsync(port, DEFAULT_SPEED, DEFAULT_SAMPLE_RATE);
    }

    /**
     * Start the sensor asynchronously and listen on a specific port, rotation speed and sample rate.
     * @param port COM Port to listen on.
     * @param speed rotation speed of the realsense sensor.
     * @param sampleRate sample rate of the realsense sensor.
     */
    public void startAsync(String port, int speed, int sampleRate) {this.start(port, speed, sampleRate, true);}

    /**
     * Start the sensor and listen on a specific port, rotation speed and sample rate.
     * @param port COM Port to listen on.
     * @param speed rotation speed of the realsense sensor.
     * @param sampleRate sample rate of the realsense sensor.
     * @param async indicates if method runs asynchronously.
     */
     private void start(String port, int speed, int sampleRate, boolean async)
     {
         if(isRunning)
             return;

         if(!async)
             //setupSweep();

         // create update thread
         sweepThread = new Thread(() -> {
             if(async)
                 //setupSweep();

             isRunning = true;
             PApplet.println("RealSense: running!");

             while(isRunning) {
                 //updateScans();
             }
         });

         // start update thread
         isStarting = true;
         sweepThread.start();
     }

    /**
     * Stop the sensor.
     */
    public void stop()
    {
        if(!isRunning && !isStarting)
            return;

        if(isRunning) {
            //device.stopScanning();
            //device.close();
        }

        // set states
        isRunning = false;
        isStarting = false;

        // join thread
        try {
            sweepThread.join(THREAD_JOIN_WAIT_TIME);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        PApplet.println("RealSense: stopped!");
    }

    /**
     * Check if the sensor is running.
     * @return True if the sensor is running.
     */
    public boolean isRunning() {
        return isRunning;
    }

    /**
     * Check if the sensor is starting up.
     * @return True if the sensor is starting up.
     */
    public boolean isStarting() {
        return isStarting;
    }
}
