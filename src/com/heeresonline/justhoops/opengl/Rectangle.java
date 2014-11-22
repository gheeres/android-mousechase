package com.heeresonline.justhoops.opengl;

import android.graphics.Rect;

public class Rectangle extends GLShape {
  public static final String TAG = "Square";
  
  protected float left;
  protected float right;
  protected float top;
  protected float bottom;
  
  protected static final short[] defaultIndices =  new short[] { 
    0, 1, 2, 0, 2, 3 
  };
  
  public Rectangle(Rect rect, int program) {
    this(rect.bottom, rect.left, rect.right, rect.top, program);
  }
  
  public Rectangle(float bottom, float left, float right, float top, int program) {
    super(getVertices(bottom, left, right, top), defaultIndices, program);
  }

  public Rectangle(Rect rect, int program, GLTexture texture) {
    this(rect.bottom, rect.left, rect.right, rect.top, program, texture);
  }

  public Rectangle(float bottom, float left, float right, float top, int program, GLTexture texture) {
    super(getVertices(bottom, left, right, top), defaultIndices, program, texture);
  }
  
  /**
   * For the bottom, left, right and top positions calculate the verticies for a square.
   * @param bottom The bottom edge.
   * @param left The left edge
   * @param right The right edge
   * @param top The top edge
   * @return The vertexes for the rectangle
   */
  protected static float[] getVertices(float bottom, float left, float right, float top) {
    return(new float[] {
      left, top, 0.0f,
      left, bottom, 0.0f,
      right, bottom, 0.0f,
      right, top, 0.0f
    });
  }
  
  /**
   * Get's the height of the item.
   * @return
   */
  public float height() {
    return(Math.abs(top - bottom));
  }

  /**
   * Get's the width of the square.
   * @return The width of the item.
   */
  public float width() {
    return(Math.abs(right - left));
  }
  
  /**
   * Moves the center of the item to the specified x and y coordinate.
   * @param x The x position.
   * @param y
   */
  public void move(float x, float y) {
    float halfHeight = height() / 2;
    float halfWidth = width() / 2;
    
    vertices = getVertices((bottom + halfHeight) - y,
                           (left + halfWidth) - x,
                           (right + halfWidth) + x,
                           (top + halfHeight) + y);      
  }
}