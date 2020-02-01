package ch.bildspur.realsense.stream;

import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.Pointer;
import org.intel.rs.frame.VideoFrame;
import org.intel.rs.types.Format;
import org.intel.rs.types.Stream;
import processing.core.PConstants;
import processing.core.PImage;

import java.nio.ByteBuffer;

import static ch.bildspur.realsense.ProcessingColorUtil.toColor;

public class VideoRSStream extends RSStream {
    private PImage image = null;

    @Override
    public void init(Stream streamType, int index, int width, int height, Format format, int fps) {
        super.init(streamType, index, width, height, format, fps);

        image = new PImage(width, height, PConstants.RGB);
    }

    public PImage getImage() {
        return image;
    }

    public void copyPixels(VideoFrame frame) {
        switch (getFormat()) {
            case Rgb8:
                copyRGB8(frame);
                break;

            case Y8:
                copyY8(frame);
                break;

            default:
                throw new RuntimeException("No copy method defined for format " + getFormat() + "!");
        }

        // update pixels
        image.updatePixels();
    }

    private void copyRGB8(VideoFrame frame) {
        frame.copyTo(image.pixels);
    }

    private void copyY8(VideoFrame frame) {
        Pointer dataPtr = frame.getDataPointer();
        int size = frame.getDataSize();

        BytePointer ptr = new BytePointer(dataPtr);
        ptr.capacity(size);

        ByteBuffer rawPixels = ptr.asBuffer();

        for (int i = 0; i < image.pixels.length; i++) {
            image.pixels[i] = toColor(rawPixels.get(i) & 0xFF);
        }
    }
}
