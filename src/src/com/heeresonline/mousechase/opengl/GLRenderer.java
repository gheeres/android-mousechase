package com.heeresonline.mousechase.opengl;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import com.android.opengl.GLText;
import com.heeresonline.mousechase.Barrier;
import com.heeresonline.mousechase.Cat;
import com.heeresonline.mousechase.GameObject;
import com.heeresonline.mousechase.GameObjectChangeEvent;
import com.heeresonline.mousechase.R;
import com.heeresonline.mousechase.World;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.Matrix;
import android.util.Log;

public class GLRenderer implements Renderer {
  static final String TAG = "GLRenderer";

  private final Context context;
  private World world;

  private GLText glText;
  private GLText glInfoText;
  private GLText glHudText;
  private GLText glHudSmallText;
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
    this.world.setGameObjectChangeEventListener(new GameObjectChangeEvent() {
      @Override
      public void added(GameObject obj) {
        GLTexture texture;
        int program = GLShaderFactory.programs.get("texture2D");
        if (obj instanceof Cat) texture = GLTextureFactory.textures.get("cat");
        else if (obj instanceof Barrier) texture = GLTextureFactory.textures.get("barrier");
        else texture = GLTextureFactory.textures.get("mouse");
            
        Log.d(TAG, String.format("Creating GLRectangle with size %5.2f @ %5.2fx%5.2f with angle %5.2f using texture %d. Screen size: %5.2fx%5.2f", 
                                  obj.size, obj.position.x, obj.position.y, 
                                  obj.direction, texture.id, screenWidth, screenHeight));
        GLRectangle rectangle = new GLRectangle(obj.id, obj.position.x, obj.position.y, obj.size, obj.size, program, texture.clone());
        rectangle.rotate(360 - obj.direction);
        shapes.add(rectangle);
      }

      @Override
      public void removed(GameObject obj) {
        boolean found = false;
        for(Iterator<GLShape> shapeIterator = shapes.iterator(); (! found) && shapeIterator.hasNext(); ) {
          GLShape shape = shapeIterator.next();
          if ((shape != null) && (shape.id == obj.id)) {
            Log.d(TAG, String.format("Removing GLRectangle with id %d at %5.2fx%5.2f.", 
                                     shape.id, shape.origin.x, shape.origin.y));
            shapes.remove(shape);
          }
        }
      }

      @Override
      public void cleared() {
        Log.d(TAG, "All GLRectangles cleared / removed.");
        shapes.clear();
      }
    });
    
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
    glDebugText.load("fonts/Roboto-Regular.ttf", (int) (screenHeight * 0.015f), 2, 2);  // Create Font (Height: 24 Pixels / X+Y Padding 2 Pixels)
    glText = new GLText(assets);
    glText.load("fonts/QuartzMS.ttf", (int) (screenHeight * 0.05f), 8, 8);  // Create Font (Height: 72 Pixels / X+Y Padding 2 Pixels)
    glInfoText = new GLText(assets);
    glInfoText.load("fonts/QuartzMS.ttf", (int) (screenHeight * 0.03f), 8, 8);  // Create Font (Height: 72 Pixels / X+Y Padding 2 Pixels)
    glHudText = new GLText(assets);
    glHudText.load("fonts/MotorwerkOblique.ttf", (int) (screenHeight * 0.05f), 8, 8);  // Create Font (Height: 72 Pixels / X+Y Padding 2 Pixels)
    glHudSmallText = new GLText(assets);
    glHudSmallText.load("fonts/MotorwerkOblique.ttf", (int) (screenHeight * 0.03f), 8, 8);  // Create Font (Height: 72 Pixels / X+Y Padding 2 Pixels)
    
    // Setup our screen width and height for normal sprite translation.
    Matrix.orthoM(projectionMatrix, 0, 0f, screenWidth, 0.0f, screenHeight, 0, 50);
    // Set the camera position (View matrix)
    Matrix.setLookAtM(viewMatrix, 0, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
    // Calculate the projection and view transformation
    Matrix.multiplyMM(projectionAndViewMatrix, 0, projectionMatrix, 0, viewMatrix, 0);

    world.initialize();
    world.start();
  }

  @Override
  public void onSurfaceCreated(GL10 unused, EGLConfig config) {
    // Set the clear color to black
    GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1); 
    GLES20.glEnable(GLES20.GL_BLEND);
    GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);
    
    // Register our shaders
    Resources res = context.getResources();
    //GLShaderFactory.addProgram("solidColor", new int[] {
    //  GLShaderFactory.loadShader(GLES20.GL_VERTEX_SHADER, res, R.raw.solidcolor_vs),
    //  GLShaderFactory.loadShader(GLES20.GL_FRAGMENT_SHADER, res, R.raw.solidcolor_fs),
    //});
    //GLShaderFactory.addProgram("gradientColor", new int[] {
    //  GLShaderFactory.loadShader(GLES20.GL_VERTEX_SHADER, res, R.raw.gradientcolor_vs),
    //  GLShaderFactory.loadShader(GLES20.GL_FRAGMENT_SHADER, res, R.raw.gradientcolor_fs),
    //});
    GLShaderFactory.addProgram("texture2D", new int[] {
      GLShaderFactory.loadShader(GLES20.GL_VERTEX_SHADER, res, R.raw.image_vs),
      GLShaderFactory.loadShader(GLES20.GL_FRAGMENT_SHADER, res, R.raw.image_fs),
    });
    
    // Load our textures
    GLTextureFactory.addTexture("atlas", context.getAssets(), "textureatlas.png");
    GLTextureFactory.addTexture("barrier", context.getAssets(), "barrier.png");
    GLTextureFactory.addTexture("cat", context.getAssets(), "cat.png");
    GLTextureFactory.addTexture("mouse", context.getAssets(), "mouse.png");
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

  protected void render(float[] matrix, float deltaTime) {
    // clear Screen and Depth Buffer, we have set the clear color as black.
    GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

    switch(world.getState()) {
      case READY:
        glText.begin(1.0f, 1.0f, 1.0f, 1.0f, matrix); 
        glText.drawC("Ready?", screenWidth/2f, screenHeight/2f, 0);
        glText.end();
      break;
  
      case INITIALIZING:
        glText.begin(1.0f, 1.0f, 1.0f, 1.0f, matrix); 
        glText.drawC("Initializing...", screenWidth/2f, screenHeight/2f, 0);
        glText.end();
      break;
      
      case GAMEOVER:
        glText.begin(1.0f, 1.0f, 1.0f, 1.0f, matrix); 
        glText.drawC("GAME OVER", screenWidth/2f, screenHeight/2f, 0);
        glText.end();

        glInfoText.begin(0.8f, 0.8f, 0.8f, 1.0f, matrix); 
        glInfoText.drawC("Touch screen to restart", screenWidth/2f, screenHeight/2f - glText.getHeight(), 0);
        glInfoText.end();
  
        // Intentionally fall through to the RUNNING status so that the last positions are rendered.
        //break;
      
      case RUNNING:
        // Synchronize the game/world objects with our visual representations (i.e. GL objects).
        for(Iterator<GameObject> gameObjectIterator = world.getGameObjects().iterator(); gameObjectIterator.hasNext(); ){
          GameObject obj = gameObjectIterator.next();
          if (obj != null) {
            boolean found = false;
            for(Iterator<GLShape> shapeIterator = shapes.iterator(); (! found) && shapeIterator.hasNext(); ) {
              GLShape shape = shapeIterator.next();
              if ((shape != null) && (shape.id == obj.id)) {
                found = true;

                shape.translate(obj.position.x, obj.position.y);
                shape.rotate(360 - obj.direction);
                shape.draw(matrix);
              }
            }
          }
        }
      break;
      
      default:
      break;
    }

    renderMouseCount(matrix, world.getCount());
    renderElapsedTime(matrix, world.getElapsedTime());
    renderFPS(matrix, deltaTime / 1000);
  }
  
  /**
   * Renders the total number of mice on the screen.
   * @param matrix The matrix to use for rendering.
   * @param count The number of mice on the screen.
   */
  protected void renderMouseCount(float[] matrix, int count) {
    String label ="Mice: ";
    float width = glHudText.getLength(label);
    glHudText.begin(1.0f, 0.0f, 0.0f, 1.0f, matrix); 
    glHudText.draw(label, 0.0f, screenHeight - glHudText.getHeight(), 0.0f);
    glHudText.end();

    String value = String.format(Locale.getDefault(), "%03d", count);
    glHudText.begin(1.0f, 0.94f, 0.0f, 1.0f, matrix); 
    glHudText.draw(value, width, screenHeight - glHudText.getHeight(), 0.0f);
    glHudText.end();
}

  /**
   * Renders the elapsed time since the game started.
   * @param matrix The matrix to use for rendering.
   * @param elapsedTime The number of milliseconds elapsed since game start.
   */
  protected void renderElapsedTime(float[] matrix, float elapsedTime) {
    String elapsedMilliSeconds = String.format(Locale.getDefault(), ".%03.0f", elapsedTime % 1000);
    float width = glHudText.getLength(elapsedMilliSeconds);
    glHudSmallText.begin(1.0f, 0.94f, 0.0f, 1.0f, matrix); 
    glHudSmallText.draw(elapsedMilliSeconds, screenWidth - width, screenHeight - glHudText.getHeight() + (glHudText.getHeight() * 0.1081f), 0.0f);
    glHudSmallText.end();

    String elapsedSeconds = String.format(Locale.getDefault(), "%03.0f", elapsedTime / 1000);
    width = width + glHudText.getLength(elapsedSeconds);
    glHudText.begin(1.0f, 0.94f, 0.0f, 1.0f, matrix); 
    glHudText.draw(elapsedSeconds, screenWidth - width, screenHeight - glHudText.getHeight(), 0.0f);
    glHudText.end();

    String label = "Time: ";
    width = width + glHudText.getLength(label);
    glHudText.begin(1.0f, 0.0f, 0.0f, 1.0f, matrix); 
    glHudText.draw(label, screenWidth - width, screenHeight - glHudText.getHeight(), 0.0f);
    glHudText.end();
  }
  
  /**
   * Render the frames per second to the screen.
   * @param matrix The matrix to use for rendering.
   * @param deltaTime The time in seconds since the last update.
   */
  protected void renderFPS(float[] matrix, float deltaTime) {
    time = ((time == 0) ? 1 : time) * 0.9f + deltaTime * 0.1f; // Smooth time / fps
    String statistics = String.format(Locale.getDefault(), "fps: %8.2f [delta: %8.6f]", 1/time, deltaTime);
    glDebugText.begin(1.0f, 1.0f, 1.0f, 1.0f, matrix); 
    glDebugText.draw(statistics, 0.0f, 0.0f, 0.0f);
    glDebugText.end();
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
