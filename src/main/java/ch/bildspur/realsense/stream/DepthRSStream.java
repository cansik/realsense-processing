package ch.bildspur.realsense.stream;

import org.intel.rs.types.Format;
import org.intel.rs.types.Stream;

public class DepthRSStream extends VideoRSStream {
    short[][] data;

    @Override
    public void init(Stream streamType, int index, int width, int height, Format format, int fps) {
        super.init(streamType, index, width, height, format, fps);

        // y, x
        data = new short[height][width];
    }

    public short[][] getData() {
        return data;
    }
}
