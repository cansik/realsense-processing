package ch.bildspur.realsense.stream;

import org.intel.rs.types.Pose;

public class PoseRSStream extends RSStream {
    private Pose pose;

    public void copyPose(Pose raw) {
        pose = raw;
    }

    public Pose getPose() {
        return pose;
    }
}
