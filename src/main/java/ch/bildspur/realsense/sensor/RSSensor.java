package ch.bildspur.realsense.sensor;

import org.intel.rs.option.CameraOption;
import org.intel.rs.sensor.Sensor;
import org.intel.rs.types.Option;

public class RSSensor {
    private Sensor sensor;

    public RSSensor(Sensor sensor) {
        this.sensor = sensor;
    }

    public float getOption(Option option) {
        CameraOption op = sensor.getSensorOptions().get(option);
        checkOptionSupported(op);
        return op.getValue();
    }

    public void setOption(Option option, float value) {
        CameraOption op = sensor.getSensorOptions().get(option);
        checkOptionSupported(op);
        op.setValue(value);
    }

    public String getOptionDescription(Option option) {
        CameraOption op = sensor.getSensorOptions().get(option);
        checkOptionSupported(op);
        return op.getDescription();
    }

    public float getOptionDefault(Option option) {
        CameraOption op = sensor.getSensorOptions().get(option);
        checkOptionSupported(op);
        return op.getDefault();
    }

    public float getOptionMin(Option option) {
        CameraOption op = sensor.getSensorOptions().get(option);
        checkOptionSupported(op);
        return op.getMin();
    }

    public float getOptionMax(Option option) {
        CameraOption op = sensor.getSensorOptions().get(option);
        checkOptionSupported(op);
        return op.getMax();
    }

    public float getOptionStep(Option option) {
        CameraOption op = sensor.getSensorOptions().get(option);
        checkOptionSupported(op);
        return op.getStep();
    }

    private void checkOptionSupported(CameraOption option) {
        if(!option.isSupported()) {
            throw new RuntimeException("Option " + option.getKey().name() + " is not supported!");
        }
    }
}
