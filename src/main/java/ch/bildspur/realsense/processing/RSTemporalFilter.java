package ch.bildspur.realsense.processing;

import ch.bildspur.realsense.type.PersistencyIndex;
import org.intel.rs.processing.TemporalFilter;
import org.intel.rs.types.Option;

public class RSTemporalFilter extends RSFilterBlock {

    public RSTemporalFilter() {
        init(new TemporalFilter());
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

    public void setPersistencyIndex(PersistencyIndex persistencyIndex) {
        setOption(Option.HolesFill, persistencyIndex.getIndex());
    }

    public PersistencyIndex getPersistencyIndex() {
        return PersistencyIndex.values()[(int) getValue(Option.HolesFill)];
    }
}
