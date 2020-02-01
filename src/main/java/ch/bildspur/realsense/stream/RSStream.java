package ch.bildspur.realsense.stream;

import org.intel.rs.types.Format;
import org.intel.rs.types.Stream;

public class RSStream {
    private boolean enabled = false;

    private Stream streamType;
    private int index;
    private int width;
    private int height;
    private Format format;
    private int fps;

    public void init(Stream streamType, int index, int width, int height, Format format, int fps) {
        this.streamType = streamType;
        this.index = index;
        this.width = width;
        this.height = height;
        this.format = format;
        this.fps = fps;

        this.enabled = true;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Stream getStreamType() {
        return streamType;
    }

    public int getIndex() {
        return index;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Format getFormat() {
        return format;
    }

    public int getFps() {
        return fps;
    }
}
