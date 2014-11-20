package com.heeresonline.justhoops;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;


class GLRenderer implements GLSurfaceView.Renderer {
  private static final String TAG = "GLRenderer";

  private final float[] matrixProjectionAndView = new float[16];
  private final float[] matrixProjection = new float[16];
  private final float[] matrixView = new float[16];
  private final float[] matrixRotation = new float[16];

  private float angle;
  private Triangle triangle;
  private Square square;

  @Override
  public void onSurfaceCreated(GL10 unused, EGLConfig config) {
    // Set the background frame color
    GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

    triangle = new Triangle();
    square = new Square();
  }

  @Override
  public void onDrawFrame(GL10 unused) {
    float[] scratch = new float[16];

    // Draw background color
    GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

    // Set the camera position (View matrix)
    Matrix.setLookAtM(matrixView, 0, 0, 0, -3, 0f, 0f, 0f, 0f, 1.0f, 0.0f);

    // Calculate the projection and view transformation
    Matrix.multiplyMM(matrixProjectionAndView, 0, matrixProjection, 0, matrixView, 0);

    // Draw square
    square.draw(matrixProjectionAndView);

    // Create a rotation for the triangle
    // Use the following code to generate constant rotation.
    // Leave this code out when using TouchEvents.
    // long time = SystemClock.uptimeMillis() % 4000L;
    // float angle = 0.090f * ((int) time);
    Matrix.setRotateM(matrixRotation, 0, angle, 0, 0, 1.0f);

    // Combine the rotation matrix with the projection and camera view
    // Note that the mMVPMatrix factor *must be first* in order
    // for the matrix multiplication product to be correct.
    Matrix.multiplyMM(scratch, 0, matrixProjectionAndView, 0, matrixRotation, 0);

    // Draw triangle
    triangle.draw(scratch);
  }

  @Override
  public void onSurfaceChanged(GL10 unused, int width, int height) {
    // Adjust the viewport based on geometry changes,
    // such as screen rotation
    GLES20.glViewport(0, 0, width, height);

    float ratio = (float) width / height;

    // this projection matrix is applied to object coordinates
    // in the onDrawFrame() method
    Matrix.frustumM(matrixProjection, 0, -ratio, ratio, -1, 1, 3, 7);
  }

  /**
   * Returns the rotation angle of the triangle shape (mTriangle).
   *
   * @return - A float representing the rotation angle.
   */
  public float getAngle() {
    return(angle);
  }

  /**
   * Sets the rotation angle of the triangle shape (mTriangle).
   */
  public void setAngle(float angle) {
     this.angle = angle;
  }
}
