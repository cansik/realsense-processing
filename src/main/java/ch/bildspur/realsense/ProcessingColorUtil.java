package ch.bildspur.realsense;

public class ProcessingColorUtil {
    public static int toColor(int gray) {
        return toColor(gray, gray, gray);
    }

    public static int toColor(int red, int green, int blue) {
        return -16777216 | red << 16 | green << 8 | blue;
    }
}
