package com.heeresonline.justhoops;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import android.opengl.GLES20;

public class Triangle extends GLObject {
  static final int COORDS_PER_VERTEX = 3;
  private FloatBuffer vertexBuffer;
  static float triangleCoords[] = {   // in counterclockwise order:
    0.0f,  0.622008459f, 0.0f, // top
   -0.5f, -0.311004243f, 0.0f, // bottom left
    0.5f, -0.311004243f, 0.0f  // bottom right
  };
  private final int vertexCount = triangleCoords.length / COORDS_PER_VERTEX;
  private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex
  
  // Set color with red, green, blue and alpha (opacity) values
  float color[] = { 0.63671875f, 0.76953125f, 0.22265625f, 1.0f };
  
  public Triangle() {
    super();
    
    // initialize vertex byte buffer for shape coordinates
    ByteBuffer bb = ByteBuffer.allocateDirect(
            // (number of coordinate values * 4 bytes per float)
            triangleCoords.length * 4);
    // use the device hardware's native byte order
    bb.order(ByteOrder.nativeOrder());

    // create a floating point buffer from the ByteBuffer
    vertexBuffer = bb.asFloatBuffer();
    // add the coordinates to the FloatBuffer
    vertexBuffer.put(triangleCoords);
    // set the buffer to read the first coordinate
    vertexBuffer.position(0);
  }

  @Override
  public void draw(float[] matrix) {
   // Add program to OpenGL environment
    GLES20.glUseProgram(program);

    // get handle to vertex shader's vPosition member
    int handlePosition = GLES20.glGetAttribLocation(program, "vPosition");

    // Enable a handle to the triangle vertices
    GLES20.glEnableVertexAttribArray(handlePosition);

    // Prepare the triangle coordinate data
    GLES20.glVertexAttribPointer(handlePosition, COORDS_PER_VERTEX,
                                 GLES20.GL_FLOAT, false,
                                 vertexStride, vertexBuffer);

    // get handle to fragment shader's vColor member
    int handleColor = GLES20.glGetUniformLocation(program, "vColor");

    // Set color for drawing the triangle
    GLES20.glUniform4fv(handleColor, 1, color, 0);

    // get handle to shape's transformation matrix
    int handleMatrix = GLES20.glGetUniformLocation(program, "uMVPMatrix");
    checkGlError("glGetUniformLocation");

    // Apply the projection and view transformation
    GLES20.glUniformMatrix4fv(handleMatrix, 1, false, matrix, 0);
    checkGlError("glUniformMatrix4fv");

    // Draw the triangle
    GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount);

    // Disable vertex array
    GLES20.glDisableVertexAttribArray(handlePosition);
  }

  @Override
  public String getVertexShaderCode() {
    return(
      // This matrix member variable provides a hook to manipulate
      // the coordinates of the objects that use this vertex shader
      "uniform mat4 uMVPMatrix;" +
      "attribute vec4 vPosition;" +
      "void main() {" +
      // the matrix must be included as a modifier of gl_Position
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