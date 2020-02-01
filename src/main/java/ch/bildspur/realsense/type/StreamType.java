package ch.bildspur.realsense.type;

import org.intel.rs.types.Stream;

public enum  StreamType {
    Color(Stream.Color),
    Depth(Stream.Depth),
    Infrared(Stream.Infrared)
    ;

    private Stream stream;

    StreamType(Stream stream) {
        this.stream = stream;
    }

    public Stream getStream() {
        return stream;
    }
}
