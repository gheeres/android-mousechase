package com.heeresonline.mousechase.opengl;

import java.nio.FloatBuffer;

import android.graphics.Matrix;
import android.graphics.PointF;
import android.util.Log;

public class GLRectangle extends GLShape {
  public static final String TAG = "GLRectangle";
  
  protected static final short[] defaultIndices =  new short[] { 
    0, 1, 2, 0, 2, 3 
  };
  
  public final PointF origin = new PointF();
  public final PointF scale = new PointF(1.0f, 1.0f);
  public float width;
  public float height;
  public float angle;

  public GLRectangle(float x, float y, float width, float height, int program) {
    this(x, y, width, height, program, null);
  }

  public GLRectangle(float x, float y, float width, float height, int program, GLTexture texture) {
    super(getVertices(x, y, width, height), defaultIndices, program, texture);

    this.origin.x = x;
    this.origin.y = y;
    this.width = width;
    this.height = height;
  }

  /**
   * For the bottom, left, right and top positions calculate the verticies for a square.
   * @param bottom The bottom edge.
   * @param left The left edge
   * @param right The right edge
   * @param top The top edge
   * @return The vertexes for the rectangle
   */
  protected static float[] getVertices(float x, float y, float width, float height) {
    float bottom = y - (height / 2.0f);
    float left = x - (width / 2.0f);
    float right = x + (width / 2.0f);
    float top = y + (height / 2.0f);
    
    return(new float[] {
      left, top, 0.0f,
      left, bottom, 0.0f,
      right, bottom, 0.0f,
      right, top, 0.0f
    });
  }
  
  /**
   * Get's the left edge
   * @return
   */
  public float left() {
    return(origin.x - (width / 2));
  }

  /**
   * Get's the right edge
   * @return
   */
  public float right() {
    return(origin.x + (width / 2));
  }

  /**
   * Get's the top edge
   * @return
   */
  public float top() {
    return(origin.y + (height / 2));
  }

  /**
   * Get's the bottom edge
   * @return
   */
  public float bottom() {
    return(origin.y - (height / 2));
  }

  /**
   * Get's the height of the item.
   * @return
   */
  public float height() {
    return(height);
  }

  /**
   * Get's the width of the square.
   * @return The width of the item.
   */
  public float width() {
    return(height);
  }
  
  /**
   * Gets the origin
   * @return The origin or center.
   */
  public PointF origin() {
    return(origin);
  }

  /**
   * Get's the current angle of the item.
   * @return
   */
  public float angle() {
    return(angle);
  }
  
  /**
   * Moves the center of the item to the specified x and y coordinate.
   * @param x The x position.
   * @param y
   */
  public void translate(float x, float y) {
    origin.x = x;
    origin.y = y;
  }

  /**
   * Scales the item the specified amount.
   * @param scale The scale
   */
  public void scale(float scale) {
    scale(scale, scale);
  }

  /**
   * Scales the item the specified amount.
   * @param x The scale for the x position.
   * @param y The scale for the y position.
   */
  public void scale(float x, float y) {
    scale.x = x;
    scale.y = y;
  }

  /**
   * The angle to rotate the item.
   * @param angle The angle to rotate.
   */
  public void rotate(float angle) {
    this.angle = angle;
  }
  
  @Override
  public FloatBuffer getVertexBuffer() {
    float[] vertices2d = getMatrix2(getVertices(origin.x, origin.y, width, height));

    Matrix m = new Matrix();
    m.setScale(scale.x, scale.y);
    m.postRotate(angle, origin.x, origin.y);
    m.mapPoints(vertices2d);

    return(getFloatBuffer(getMatrix3(vertices2d, 0.0f)));
  }
}