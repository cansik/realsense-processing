package ch.bildspur.realsense.processing;

import org.intel.rs.processing.SpatialFilter;
import org.intel.rs.types.Option;

public class RSSpatialFilter extends RSFilterBlock {

    public RSSpatialFilter() {
        init(new SpatialFilter());
    }

    public void setMagnitude(int filterMagnitude) {
        setOption(Option.FilterMagnitude, filterMagnitude);
    }

    public int getMagnitude() {
        return (int) getValue(Option.FilterMagnitude);
    }

    public void setSmoothAlpha(float smoothAlpha) {
        setOption(Option.FilterSmoothAlpha, smoothAlpha);
    }

    public float getSmoothAlpha() {
        return getValue(Option.FilterSmoothAlpha);
    }

    public void setSmoothDelta(int smoothDelta) {
        setOption(Option.FilterSmoothDelta, smoothDelta);
    }

    public int getSmoothDelta() {
        return (int) getValue(Option.FilterSmoothDelta);
    }

    public void setHoleFilling(int holeFilling) {
        setOption(Option.HolesFill, holeFilling);
    }

    public int getHoleFilling() {
        return (int) getValue(Option.HolesFill);
    }
}
