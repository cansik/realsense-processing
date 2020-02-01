package ch.bildspur.realsense.type;

public enum ColorScheme {
    Jet(0),
    Classic(1),
    WhiteToBlack(2),
    BlackToWhite(3),
    Bio(4),
    Cold(5),
    Warm(6),
    Quantized(7),
    Pattern(8);

    private int index;

    ColorScheme(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }
}
