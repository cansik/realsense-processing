package ch.bildspur.realsense.processing;

import ch.bildspur.realsense.type.ColorScheme;
import org.intel.rs.processing.Colorizer;
import org.intel.rs.types.Option;

public class RSColorizer extends RSProcessingBlock<Colorizer> {

    public void init() {
        super.init(new Colorizer());
    }

    public void setColorScheme(ColorScheme scheme) {
        setOption(Option.ColorScheme, scheme.getIndex());
    }

    public ColorScheme getColorScheme() {
        return ColorScheme.values()[(int) getValue(Option.ColorScheme)];
    }
}
