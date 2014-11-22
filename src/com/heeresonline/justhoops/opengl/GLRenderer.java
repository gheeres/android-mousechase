package com.heeresonline.justhoops.opengl;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import com.heeresonline.justhoops.IView;
import com.heeresonline.justhoops.R;

import android.content.Context;
import android.content.res.Resources;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.Matrix;

public class GLRenderer implements Renderer, IView {
  static final String TAG = "GLRenderer";
  private final Context context;

  private final float[] projectionMatrix = new float[16];
  private final float[] viewMatrix = new float[16];
  private final float[] projectionAndViewMatrix = new float[16];

  private long lastTime;
  private float screenWidth = 1024;
  private float screenHeight = 768;
  
  private List<GLShape> shapes = new CopyOnWriteArrayList<GLShape>();
  
  public GLRenderer(Context context)
  {
    this.context = context;
    lastTime = System.currentTimeMillis();
  }  
  
  @Override
  public void onDrawFrame(GL10 unused) {
    long now = System.currentTimeMillis();
    if (lastTime <= now) {
      long deltaTime = now - lastTime;
      render(projectionAndViewMatrix, deltaTime);
      lastTime = now;
    }
  }
  
  private void clearMatrix() {
    // Clear our matrices
    for(int index = 0; index < 16; index++) {
      projectionMatrix[index] = 0.0f;
      viewMatrix[index] = 0.0f;
      projectionAndViewMatrix[index] = 0.0f;
    }
  }
  
  @Override
  public void onSurfaceChanged(GL10 gl, int width, int height) {
    screenWidth = width;
    screenHeight = height;
    
    GLES20.glViewport(0, 0, (int)screenWidth, (int)screenHeight);
    clearMatrix();
      
    // Setup our screen width and height for normal sprite translation.
    Matrix.orthoM(projectionMatrix, 0, 0f, screenWidth, 0.0f, screenHeight, 0, 50);
    // Set the camera position (View matrix)
    Matrix.setLookAtM(viewMatrix, 0, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
    // Calculate the projection and view transformation
    Matrix.multiplyMM(projectionAndViewMatrix, 0, projectionMatrix, 0, viewMatrix, 0);
  }

  @Override
  public void onSurfaceCreated(GL10 unused, EGLConfig config) {
    // Set the clear color to black
    GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1); 

    // Register our shaders
    Resources res = context.getResources();
    GLShaderFactory.addProgram("solidColor", new int[] {
      GLShaderFactory.loadShader(GLES20.GL_VERTEX_SHADER, res, R.raw.solidcolor_vs),
      GLShaderFactory.loadShader(GLES20.GL_FRAGMENT_SHADER, res, R.raw.solidcolor_fs),
    });
    GLShaderFactory.addProgram("gradientColor", new int[] {
      GLShaderFactory.loadShader(GLES20.GL_VERTEX_SHADER, res, R.raw.gradientcolor_vs),
      GLShaderFactory.loadShader(GLES20.GL_FRAGMENT_SHADER, res, R.raw.gradientcolor_fs),
    });
    GLShaderFactory.addProgram("texture2D", new int[] {
      GLShaderFactory.loadShader(GLES20.GL_VERTEX_SHADER, res, R.raw.image_vs),
      GLShaderFactory.loadShader(GLES20.GL_FRAGMENT_SHADER, res, R.raw.image_fs),
    });
    
    // Set our shader programm
    shapes.add(new Rectangle(100, 300, 400, 200, GLShaderFactory.programs.get("texture2D"), new GLTexture(context.getAssets(), "cube.png")));
    shapes.add(new Rectangle(650, 850, 950, 750, GLShaderFactory.programs.get("texture2D"), new GLTexture(context.getResources(), R.drawable.ic_launcher)));
    shapes.add(new Rectangle(300, 100, 200, 400, GLShaderFactory.programs.get("solidColor")));
    shapes.add(new Rectangle(600, 600, 700, 700, GLShaderFactory.programs.get("solidColor")));
  }  
  
  private void render(float[] matrix, float deltaTime) {
    // clear Screen and Depth Buffer, we have set the clear color as black.
    GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

    for(Iterator<GLShape> iterator = shapes.iterator(); iterator.hasNext(); ) {
      GLShape shape = iterator.next();
      if (shape != null) {
        shape.draw(matrix);
      }
    }
  }
  
  @Override
  public void resume() {
    lastTime = System.currentTimeMillis();
  }

  @Override
  public void pause() {
  }
}
