package ch.bildspur.realsense.type;

public enum PersistencyIndex {
    Disabled(0),
    ValidIn8_8 (1),
    ValidIn2_Last3(2),
    ValidIn2_Last4(3),
    ValidIn2_Last8(4),
    ValidIn1_Last2(5),
    ValidIn1_Last5(6),
    ValidIn1_Last8(7),
    PersistIndefinitely(8);

    private int index;

    PersistencyIndex(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }
}
