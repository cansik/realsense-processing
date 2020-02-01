package ch.bildspur.realsense.stream;

import org.intel.rs.types.Format;
import org.intel.rs.types.Stream;

public class DepthRealSenseStream extends VideoRealSenseStream {
    short[][] data;

    @Override
    public void init(Stream streamType, int index, int width, int height, Format format, int fps) {
        super.init(streamType, index, width, height, format, fps);

        // todo: check which format (x, y) or (y, x)
        data = new short[width][height];
    }
}
