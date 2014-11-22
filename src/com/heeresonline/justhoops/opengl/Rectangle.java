package com.heeresonline.justhoops.opengl;

import android.graphics.Rect;
import android.util.Log;

public class Rectangle extends GLShape {
  public static final String TAG = "Rectangle";
  
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
    initialize(bottom, left, right, top);
  }

  public Rectangle(Rect rect, int program, GLTexture texture) {
    this(rect.bottom, rect.left, rect.right, rect.top, program, texture);
  }

  public Rectangle(float bottom, float left, float right, float top, int program, GLTexture texture) {
    super(getVertices(bottom, left, right, top), defaultIndices, program, texture);
    initialize(bottom, left, right, top);
  }

  /**
   * Initialize the rectangle.
   * @param bottom The bottom
   * @param left The left
   * @param right The right
   * @param top The top
   */
  private void initialize(float bottom, float left, float right, float top) {
    this.bottom = bottom;
    this.left = left;
    this.right = right;
    this.top = top;
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
   * Gets the center X position.
   * @return The center in the X coordinate space.
   */
  public float centerX() {
    return(left - width() / 2);
  }
  
  /**
   * Gets the center Y position.
   * @return The center in the Y coordinate space.
   */
  public float centerY() {
    return(bottom + height() / 2);
  }

  /**
   * Moves the center of the item to the specified x and y coordinate.
   * @param x The x position.
   * @param y
   */
  public void move(float x, float y) {
    float movementX = x - centerX();
    float movementY = y - centerY();

    float oldBottom = bottom;
    float oldLeft = left;
    float oldRight = right;
    float oldTop = top;
    float oldX = centerX();
    float oldY = centerY();
    
    bottom += movementY;
    left += movementX;
    right += movementX;
    top += movementY;
    vertices = getVertices(bottom, left, right, top);
    
    Log.v(TAG, String.format("Moving shape from %5.2f,%5.2f to %5.2f,%5.2f. Vertex change from (%5.2f,%5.2f,%5.2f,%5.2f) to (%5.2f,%5.2f,%5.2f,%5.2f).", 
                             oldX, oldY, x, y,
                             oldBottom, oldLeft, oldRight, oldTop,
                             bottom, left, right, top));
  }
}