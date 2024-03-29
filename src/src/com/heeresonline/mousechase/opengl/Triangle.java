package com.heeresonline.mousechase.opengl;

/**
 * Represents a Triangle shape in OpenGL
 */
class Triangle extends GLShape {
  public Triangle(int program) {
    this(0, program);
  }
  
  public Triangle(int id, int program) {
    super(id, new float[] { 
      10.0f, 200f, 0.0f,
      10.0f, 100f, 0.0f,
      100f, 100f, 0.0f,
    }, new short[] { 
      0, 1, 2 
    }, program);
  }
}