package com.heeresonline.mousechase.opengl;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import com.android.opengl.GLText;
import com.heeresonline.mousechase.R;
import com.heeresonline.mousechase.World;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.Matrix;

public class GLRenderer implements Renderer {
  static final String TAG = "GLRenderer";
  private final Context context;
  private World world;

  private GLText glText;
  private GLText glDebugText;
  
  private final float[] projectionMatrix = new float[16];
  private final float[] viewMatrix = new float[16];
  private final float[] projectionAndViewMatrix = new float[16];

  private float time;
  private long lastTime;
  private float screenWidth = 1024;
  private float screenHeight = 768;
  
  private List<GLShape> shapes = new CopyOnWriteArrayList<GLShape>();
  
  public GLRenderer(Context context, World world)
  {
    this.context = context;
    this.world = world;
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
  
  @Override
  public void onSurfaceChanged(GL10 unused, int width, int height) {
    screenWidth = width;
    screenHeight = height;
    
    world.setHeight((int) screenHeight);
    world.setWidth((int) screenWidth);
    GLES20.glViewport(0, 0, (int)screenWidth, (int)screenHeight);
    clearMatrix();
      
    // Create the GLText
    // Load the font from file (set size + padding), creates the texture
    // NOTE: after a successful call to this the font is ready for rendering!
    AssetManager assets = context.getAssets();
    glDebugText = new GLText(assets);
    glDebugText.load("Roboto-Regular.ttf", (int) (screenHeight * 0.015f), 2, 2);  // Create Font (Height: 24 Pixels / X+Y Padding 2 Pixels)
    glText = new GLText(assets);
    glText.load("QuartzMS.ttf", (int) (screenHeight * 0.05f), 8, 8);  // Create Font (Height: 72 Pixels / X+Y Padding 2 Pixels)
    
    // Setup our screen width and height for normal sprite translation.
    Matrix.orthoM(projectionMatrix, 0, 0f, screenWidth, 0.0f, screenHeight, 0, 50);
    // Set the camera position (View matrix)
    Matrix.setLookAtM(viewMatrix, 0, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
    // Calculate the projection and view transformation
    Matrix.multiplyMM(projectionAndViewMatrix, 0, projectionMatrix, 0, viewMatrix, 0);

    generateRandomShapes(100);
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
    GLTextureFactory.addTexture("atlas", context.getAssets(), "textureatlas.png");
    GLTextureFactory.addTexture("cube", context.getAssets(), "cube.png");
    GLTextureFactory.addTexture("icon", context.getResources(), R.drawable.ic_launcher);
  }  

  /**
   * Clears the display, model and projection matrixes.
   */
  private void clearMatrix() {
    // Clear our matrices
    for(int index = 0; index < 16; index++) {
      projectionMatrix[index] = 0.0f;
      viewMatrix[index] = 0.0f;
      projectionAndViewMatrix[index] = 0.0f;
    }
  }

  protected void generateRandomShapes(int count) {
    Random random = new Random();
    int program = GLShaderFactory.programs.get("texture2D");
    GLTexture[] textures = GLTextureFactory.textures.values().toArray(new GLTexture[0]);
    
    float[][] atlas = new float[][] {
      new float[] {
          0.0f, 0.0f,
          0.0f, 0.5f,
          0.5f, 0.5f,
          0.5f, 0.0f  
        },
      new float[] {
        0.0f, 0.5f,
        0.0f, 1.0f,
        0.5f, 1.0f,
        0.5f, 0.5f  
      },
      new float[] {
        0.5f, 0.5f,
        0.5f, 1.0f,
        1.0f, 1.0f,
        1.0f, 0.5f  
      },
      new float[] {
        0.5f, 0.0f,
        0.5f, 0.5f,
        1.0f, 0.5f,
        1.0f, 0.0f  
      },
    };
    for(int index = 0; index < count; index++) {
      float x = random.nextFloat() * screenWidth;
      float y = random.nextFloat() * screenHeight;
      GLTexture texture = textures[random.nextInt(textures.length)];
      if (texture.id == 1) {
        texture = texture.clone(atlas[random.nextInt(atlas.length)]);
      }
      
      GLRectangle rectangle = new GLRectangle(x, y, 100f, 100f, program, texture);
      rectangle.angle = random.nextInt(360);
      rectangle.scale.x = random.nextFloat()*2;
      rectangle.scale.y = rectangle.scale.x;
      shapes.add(rectangle);
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
    
    String date = new Date().toString();
    glText.begin(1.0f, 1.0f, 1.0f, 1.0f, matrix); 
    glText.drawC(new Date().toString(), screenWidth/2f, screenHeight/2f, 0);
    glText.end();

    renderFPS(matrix, deltaTime / 1000);
  }
  
  /**
   * Render the frames per second to the screen.
   * @param matrix The matrix to use for rendering.
   * @param deltaTime The time in seconds since the last update.
   */
  protected void renderFPS(float[] matrix, float deltaTime) {
    time = ((time == 0) ? 1 : time) * 0.9f + deltaTime * 0.1f; // Smooth time / fps
    String statistics = String.format("fps: %8.2f [delta: %8.6f]", 1/time, deltaTime);
    glDebugText.begin(1.0f, 1.0f, 1.0f, 1.0f, matrix); 
    glDebugText.draw(statistics, 0.0f, 0.0f, 0.0f);
    glDebugText.end();
  }
  
  public void move(float x, float y) {
    for(Iterator<GLShape> iterator = shapes.iterator(); iterator.hasNext(); ) {
      GLShape shape = iterator.next();
      if ((shape != null) && (shape instanceof GLRectangle)) {
        //((GLRectangle) shape).move(((GLRectangle) shape).centerX() + x, ((GLRectangle) shape).centerY() + y);
      }
    }
  }
  
  public void resume() {
    lastTime = System.currentTimeMillis();
  }

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
