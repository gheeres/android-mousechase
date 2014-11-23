package com.heeresonline.justhoops.opengl;

import java.util.Iterator;
import java.util.List;
import java.util.Random;
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

    generateRandomShapes(30);
  }

  @Override
  public void onSurfaceCreated(GL10 unused, EGLConfig config) {
    // Set the clear color to black
    GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1); 
    GLES20.glEnable(GLES20.GL_BLEND);
    GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);
    
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
    
    // Load our textures
    GLTextureFactory.addTexture("atlas", context.getAssets(), "textureatlas.png", new float[] {
      
    });
    GLTextureFactory.addTexture("cube", context.getAssets(), "cube.png");
    GLTextureFactory.addTexture("icon", context.getResources(), R.drawable.ic_launcher);
  }  
  
  protected void generateRandomShapes(int count) {
    Random random = new Random();
    int program = GLShaderFactory.programs.get("texture2D");
    GLTexture[] textures = GLTextureFactory.textures.values().toArray(new GLTexture[0]);
    
    for(int index = 0; index < count; index++) {
      float bottom = random.nextFloat() * screenHeight;
      float left = random.nextFloat() * screenWidth;
      shapes.add(new Rectangle(bottom, left, left + 100f, bottom + 100f,
                 program, textures[random.nextInt(textures.length)]));
    }
  }

  protected void render(float[] matrix, float deltaTime) {
    // clear Screen and Depth Buffer, we have set the clear color as black.
    GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

    for(Iterator<GLShape> iterator = shapes.iterator(); iterator.hasNext(); ) {
      GLShape shape = iterator.next();
      if (shape != null) {
        shape.draw(matrix);
      }
    }
  }
  
  public void move(float x, float y) {
    for(Iterator<GLShape> iterator = shapes.iterator(); iterator.hasNext(); ) {
      GLShape shape = iterator.next();
      if ((shape != null) && (shape instanceof Rectangle)) {
        ((Rectangle) shape).move(((Rectangle) shape).centerX() + x, ((Rectangle) shape).centerY() + y);
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
  
  /**
   * Gets the screen width
   * @return The width of screen
   */
  public float getWidth() {
    return(screenWidth);
  }

  /**
   * Gets the screen height
   * @return The height of screen
   */
  public float getHeight() {
    return(screenHeight);
  }
}
