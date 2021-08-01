package ch.bildspur.realsense.processing;

import org.intel.rs.processing.ThresholdFilter;
import org.intel.rs.types.Option;

public class RSThresholdFilter extends RSFilterBlock {
    public RSThresholdFilter() {
        init(new ThresholdFilter());
    }

    public void setMinDistance(float minDistance) {
        setOption(Option.MinDistance, minDistance);
    }

    public float getMinDistance() {
        return getValue(Option.MinDistance);
    }

    public void setMaxDistance(float minDistance) {
        setOption(Option.MaxDistance, minDistance);
    }

    public float getMaxDistance() {
        return getValue(Option.MaxDistance);
    }
}
