package com.heeresonline.justhoops.opengl;

class Square extends GLObject {
  public Square() {
    vertices = new float[] {  
      10.0f, 200f, 0.0f,
      10.0f, 100f, 0.0f,
      100f, 100f, 0.0f,
      100f, 200f, 0.0f,
    };
       
    indices = new short[] {0, 1, 2, 0, 2, 3};
  }
}