package ch.bildspur.realsense.processing;

import org.intel.rs.processing.DecimationFilter;
import org.intel.rs.types.Option;

public class RSDecimationFilter extends RSFilterBlock {

    public RSDecimationFilter() {
        init(new DecimationFilter());
    }

    public void setFilterMagnitude(int filterMagnitude) {
        setOption(Option.FilterMagnitude, (float)filterMagnitude);
    }

    public int getFilterMagnitude() {
        return (int)getValue(Option.FilterMagnitude);
    }
}
