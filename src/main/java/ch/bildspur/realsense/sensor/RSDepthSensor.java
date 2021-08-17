package ch.bildspur.realsense.sensor;

import org.intel.rs.sensor.Sensor;

public class RSDepthSensor extends RSSensor {
    public RSDepthSensor(Sensor sensor) {
        super(sensor);
    }

    public float getDepthScale() {
        return sensor.getDepthScale();
    }
}
