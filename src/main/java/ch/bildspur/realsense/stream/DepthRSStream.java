package ch.bildspur.realsense.stream;

import org.intel.rs.frame.DepthFrame;
import org.intel.rs.types.Format;
import org.intel.rs.types.Stream;

import java.nio.ByteBuffer;

import static ch.bildspur.realsense.ProcessingColorUtil.toColor;

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

    public void updateDepthData(DepthFrame frame) {
        if(frame == null)
            return;

        ByteBuffer raw = frame.getData();

        // todo: create test and validate
        for(int y = 0; y < getHeight(); y++) {
            for(int x = 0; x < getWidth(); x++) {
                data[y][x] = raw.getShort();
            }
        }
    }
}
