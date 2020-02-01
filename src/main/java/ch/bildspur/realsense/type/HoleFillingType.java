package ch.bildspur.realsense.type;

public enum HoleFillingType {
    FillFromLeft(0),
    FarestFromAround(1),
    NearestFromAround(2);

    private int index;

    HoleFillingType(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }
}
