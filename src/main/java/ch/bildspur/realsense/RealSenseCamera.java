package ch.bildspur.realsense;

import org.librealsense.Context;
import org.librealsense.Device;
import org.librealsense.DeviceList;
import org.librealsense.Native;
import processing.core.PApplet;
import processing.core.PConstants;

import java.util.List;

/**
 * Intel RealSense Camera
 */
public class RealSenseCamera implements PConstants {
    // processing
    private PApplet parent;

    // realsense
    private Context context;

    // camera
    volatile private boolean running = false;

    /**
     * Create a new Intel RealSense camera.
     * @param parent Parent processing sketch.
     */
    public RealSenseCamera(PApplet parent)
    {
        this.parent = parent;
        parent.registerMethod("stop", this);

        // load native libs
        loadNativeLibraries();

        // create context
        context = Context.create();
    }

    /**
     * Start the camera.
     */
     public void start()
     {
         if(running)
             return;

         running = true;
         setupCamera();
     }

    public void readFrames()
    {

    }

    public List<Device> getDevices() {
        DeviceList deviceList = this.context.queryDevices();
        return deviceList.getDevices();
    }

    public boolean isCameraAvailable() {
        return !getDevices().isEmpty();
    }

    /**
     * Stop the camera.
     */
    public void stop()
    {
        if(!running)
            return;

        // set states
        running = false;
    }

    private void setupCamera()
    {

    }

    private void loadNativeLibraries()
    {
        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("win")) {
            Native.loadNativeLibraries("native/windows-x64");
        } else if (os.contains("mac")) {
            Native.loadNativeLibraries("native/osx-x64");
        } else if (os.contains("nix") || os.contains("nux") || os.contains("aix")) {
            Native.loadNativeLibraries("native/linux-64");
        } else {
            // Operating System not supported!
            PApplet.println("RealSense: Load the native libraries by your own.");
        }
    }

    /**
     * Check if the camera is running.
     * @return True if the camera is running.
     */
    public boolean isRunning() {
        return running;
    }
}
