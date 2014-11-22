package com.heeresonline.justhoops.opengl;

public class Square extends GLShape {
  public static final String TAG = "Square";
  
  protected static final short[] defaultIndices =  new short[] { 
    0, 1, 2, 0, 2, 3 
  };
  
  public Square(float[] verticies, int program) {
    super(verticies, defaultIndices, program);
  }

  public Square(float[] verticies, short[] indices, int program) {
    super(verticies, indices, program);
  }
  public Square(float[] verticies, int program, GLTexture texture) {
    this(verticies, defaultIndices, program, texture);
  }
  
  public Square(float[] verticies, short[] indices, int program, GLTexture texture) {
    super(verticies, indices, program, texture);
  }
}