import ch.bildspur.realsense.*;
import ch.bildspur.realsense.processing.*;
import ch.bildspur.realsense.stream.*;
import ch.bildspur.realsense.type.*;

import org.intel.rs.frame.Points;
import org.intel.rs.processing.PointCloud;
import org.intel.rs.processing.DecimationFilter;
import org.intel.rs.processing.ThresholdFilter;
import org.intel.rs.types.Vertex;
import org.intel.rs.frame.DepthFrame;

import peasy.PeasyCam;

RealSenseCamera camera = new RealSenseCamera(this);

// additional filters
PointCloud pointCloud = new PointCloud();
ThresholdFilter thresholdFilter = new ThresholdFilter();
DecimationFilter decimationFilter = new DecimationFilter();

PeasyCam cam;
PShape cloud;

float scale = 200;

int streamWidth = 424;
int streamHeight = 240;

void setup()
{
  size(640, 480, P3D);
  pixelDensity(2);

  // using peasycam plugin as camera
  cam = new PeasyCam(this, 400);

  // prepare cloud buffer
  cloud = createShape();
  cloud.setStroke(color(255));
  cloud.beginShape(POINTS);
  for (int i = 0; i < (streamWidth * streamHeight * 0.25); i++) {
    cloud.vertex(0, 0, 0);
  }
  cloud.endShape();

  // enable depth stream (limited to 1 meter)
  camera.enableDepthStream(streamWidth, streamHeight);
  camera.start();
}

void draw()
{
  background(0);

  // read frames
  camera.readFrames();

  // read pointcloud
  DepthFrame depthFrame = camera.getFrames().getDepthFrame();
  DepthFrame decimatedFrame = decimationFilter.process(depthFrame);

  // get points
  Points points = pointCloud.calculate(decimatedFrame);
  Vertex[] vertices = points.getVertices();

  points.release();
  decimatedFrame.release();

  // update cloud
  for (int i = 0; i < vertices.length; i++) {
    Vertex v = vertices[i];
    cloud.setVertex(i, v.getX(), v.getY(), v.getZ());
  }

  // display
  push();
  scale(scale, scale, -scale);
  shape(cloud);
  pop();

  surface.setTitle("FPS: " + nf(frameRate, 0, 2));
}
