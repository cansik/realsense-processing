package ch.bildspur.realsense.processing;

import org.intel.rs.processing.UnitsTransform;
import org.intel.rs.processing.ZeroOrderInvalidationFilter;

public class RSZeroOrderInvalidationFilter extends RSFilterBlock {

    public RSZeroOrderInvalidationFilter() {
        init(new ZeroOrderInvalidationFilter());
    }
}
