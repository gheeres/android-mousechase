package com.heeresonline.justhoops;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import android.opengl.GLES20;

public class Square extends GLObject {
  private final FloatBuffer vertexBuffer;
  private final ShortBuffer drawListBuffer;
  
  // number of coordinates per vertex in this array
  static final int COORDS_PER_VERTEX = 3;
  static float squareCoords[] = {
          -0.5f,  0.5f, 0.0f,   // top left
          -0.5f, -0.5f, 0.0f,   // bottom left
           0.5f, -0.5f, 0.0f,   // bottom right
           0.5f,  0.5f, 0.0f }; // top right

  private short drawOrder[] = { 0, 1, 2, 0, 2, 3 }; // order to draw vertices

  private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex

  float color[] = { 0.2f, 0.709803922f, 0.898039216f, 1.0f };

  public Square() {
    super();

    // initialize vertex byte buffer for shape coordinates
    ByteBuffer bb = ByteBuffer.allocateDirect(// (# of coordinate values * 4 bytes per float)
                                              squareCoords.length * 4);
    bb.order(ByteOrder.nativeOrder());
    vertexBuffer = bb.asFloatBuffer();
    vertexBuffer.put(squareCoords);
    vertexBuffer.position(0);

    // initialize byte buffer for the draw list
    ByteBuffer dlb = ByteBuffer.allocateDirect(// (# of coordinate values * 2 bytes per short)
                                               drawOrder.length * 2);
    dlb.order(ByteOrder.nativeOrder());
    drawListBuffer = dlb.asShortBuffer();
    drawListBuffer.put(drawOrder);
    drawListBuffer.position(0);
  }

  @Override
  public void draw(float[] matrix) {
    // Add program to OpenGL environment
    GLES20.glUseProgram(program);

    // get handle to vertex shader's vPosition member
    int positionHandle = GLES20.glGetAttribLocation(program, "vPosition");

    // Enable a handle to the triangle vertices
    GLES20.glEnableVertexAttribArray(positionHandle);

    // Prepare the triangle coordinate data
    GLES20.glVertexAttribPointer(positionHandle, COORDS_PER_VERTEX,
                                 GLES20.GL_FLOAT, false,
                                 vertexStride, vertexBuffer);

    // get handle to fragment shader's vColor member
    int colorHandle = GLES20.glGetUniformLocation(program, "vColor");

    // Set color for drawing the triangle
    GLES20.glUniform4fv(colorHandle, 1, color, 0);

    // get handle to shape's transformation matrix
    int mvpMatrixHandle = GLES20.glGetUniformLocation(program, "uMVPMatrix");
    checkGlError("glGetUniformLocation");

    // Apply the projection and view transformation
    GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, matrix, 0);
    checkGlError("glUniformMatrix4fv");

    // Draw the square
    GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length,
                          GLES20.GL_UNSIGNED_SHORT, drawListBuffer);

    // Disable vertex array
    GLES20.glDisableVertexAttribArray(positionHandle);
  }

  @Override
  public String getVertexShaderCode() {
    return(
      // This matrix member variable provides a hook to manipulate
      // the coordinates of the objects that use this vertex shader
      "uniform mat4 uMVPMatrix;" +
      "attribute vec4 vPosition;" +
      "void main() {" +
      // The matrix must be included as a modifier of gl_Position.
      // Note that the uMVPMatrix factor *must be first* in order
      // for the matrix multiplication product to be correct.
      "  gl_Position = uMVPMatrix * vPosition;" +
      "}"
     );
  }

  @Override
  public String getFragmentShaderCode() {
    return(      
      "precision mediump float;" +
      "uniform vec4 vColor;" +
      "void main() {" +
      "  gl_FragColor = vColor;" +
      "}"
    );
  }
}