package ch.bildspur.realsense.processing;

import org.intel.rs.processing.DecimationFilter;
import org.intel.rs.processing.DisparityTransform;
import org.intel.rs.types.Option;

public class RSDisparityTransform extends RSFilterBlock {

    public RSDisparityTransform(boolean depthToDisparity) {
        init(new DisparityTransform(depthToDisparity));
    }
}
