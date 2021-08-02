package ch.bildspur.realsense.processing;

import ch.bildspur.realsense.type.HoleFillingType;
import org.intel.rs.processing.HoleFillingFilter;
import org.intel.rs.processing.UnitsTransform;
import org.intel.rs.types.Option;

public class RSUnitsTransform extends RSFilterBlock {

    public RSUnitsTransform() {
        init(new UnitsTransform());
    }
}
