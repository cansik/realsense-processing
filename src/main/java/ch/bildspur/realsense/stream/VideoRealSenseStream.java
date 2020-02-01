package ch.bildspur.realsense.stream;

import org.intel.rs.types.Format;
import org.intel.rs.types.Stream;
import processing.core.PConstants;
import processing.core.PImage;

public class VideoRealSenseStream extends RealSenseStream {
    private PImage image = null;

    @Override
    public void init(Stream streamType, int index, int width, int height, Format format, int fps) {
        super.init(streamType, index, width, height, format, fps);

        image = new PImage(width, height, PConstants.RGB);
    }

    public PImage getImage() {
        return image;
    }
}
