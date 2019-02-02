package ch.bildspur.realsense;

import processing.core.PApplet;
import processing.core.PConstants;

import java.net.URL;
import java.nio.file.Paths;

class ProcessingUtils {
    static String getLibPath(PApplet sketch) {
        ProcessingUtils utils = new ProcessingUtils();
        URL url = utils.getClass().getResource(ProcessingUtils.class.getSimpleName() + ".class");
        if (url != null) {
            // Convert URL to string, taking care of spaces represented by the "%20"
            // string.
            String path = url.toString().replace("%20", " ");

            if (!path.contains(".jar"))
                return sketch.sketchPath();

            int n0 = path.indexOf('/');

            int n1 = -1;

            // read jar file name
            String fullJarPath = ProcessingUtils.class.getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .getPath();

            if (PApplet.platform == PConstants.WINDOWS) {
                // remove leading slash in windows path
                fullJarPath = fullJarPath.substring(1);
            }

            String jar = Paths.get(fullJarPath).getFileName().toString();

            n1 = path.indexOf(jar);
            if (PApplet.platform == PConstants.WINDOWS) {
                // remove leading slash in windows path
                n0++;
            }


            if ((-1 < n0) && (-1 < n1)) {
                return path.substring(n0, n1);
            } else {
                return sketch.sketchPath();
            }
        }
        return sketch.sketchPath();
    }
}
