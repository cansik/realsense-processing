package ch.bildspur.realsense.processing;

import ch.bildspur.realsense.type.HoleFillingType;
import org.intel.rs.processing.HoleFillingFilter;
import org.intel.rs.types.Option;

public class RSHoleFillingFilter extends RSFilterBlock {

    public RSHoleFillingFilter() {
        init(new HoleFillingFilter());
    }

    public void setHoleFillingType(HoleFillingType fillingType) {
        setOption(Option.HolesFill, fillingType.getIndex());
    }

    public HoleFillingType getHoleFillingType() {
        return HoleFillingType.values()[(int) getValue(Option.HolesFill)];
    }
}
