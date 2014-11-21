package com.heeresonline.justhoops.opengl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import ri.blog.opengl002.riGraphicTools;

import com.heeresonline.justhoops.IView;
import com.heeresonline.justhoops.R;
import com.heeresonline.justhoops.R.drawable;
import com.heeresonline.justhoops.R.raw;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.Log;

public class GLRenderer implements Renderer, IView {
  static final String TAG = "GLRenderer";
  private final Context context;

  private final float[] projectionMatrix = new float[16];
  private final float[] viewMatrix = new float[16];
  private final float[] projectionAndViewMatrix = new float[16];

  private long lastTime;
  private float screenWidth = 1024;
  private float screenHeight = 768;
  
  private Triangle triangle;
  private Square square;
  private GLTexture texture;

  
  
  
  public static float vertices[];
  public static short indices[];
  public static float uvs[];
  public FloatBuffer vertexBuffer;
  public ShortBuffer drawListBuffer;
  public FloatBuffer uvBuffer;
  
  public void SetupTriangle()
  {
      // We have create the vertices of our view.
    vertices = new float[]
        {  10.0f, 200f, 0.0f,
           10.0f, 100f, 0.0f,
           100f, 100f, 0.0f,
           100f, 200f, 0.0f,
        };

    indices = new short[] {0, 1, 2, 0, 2, 3}; // The order of vertexrendering.

      // The vertex buffer.
      ByteBuffer bb = ByteBuffer.allocateDirect(vertices.length * 4);
      bb.order(ByteOrder.nativeOrder());
      vertexBuffer = bb.asFloatBuffer();
      vertexBuffer.put(vertices);
      vertexBuffer.position(0);

      // initialize byte buffer for the draw list
      ByteBuffer dlb = ByteBuffer.allocateDirect(indices.length * 2);
      dlb.order(ByteOrder.nativeOrder());
      drawListBuffer = dlb.asShortBuffer();
      drawListBuffer.put(indices);
      drawListBuffer.position(0);

  }
  public void SetupImage()
  {
    // Create our UV coordinates.
    uvs = new float[] {
        0.0f, 0.0f,
        0.0f, 1.0f,
        1.0f, 1.0f,
        1.0f, 0.0f
    };
   
    // The texture buffer
    ByteBuffer bb = ByteBuffer.allocateDirect(uvs.length * 4);
    bb.order(ByteOrder.nativeOrder());
    uvBuffer = bb.asFloatBuffer();
    uvBuffer.put(uvs);
    uvBuffer.position(0);
   
    // Generate Textures, if more needed, alter these numbers.
    int[] texturenames = new int[1];
    GLES20.glGenTextures(1, texturenames, 0);
   
    // Retrieve our image from resources.
    int id = context.getResources().getIdentifier("drawable/ic_launcher", null, 
        context.getPackageName());
   
    // Temporary create a bitmap
    Bitmap bmp = BitmapFactory.decodeResource(context.getResources(), id);
   
    // Bind texture to texturename
    GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texturenames[0]);
   
    // Set filtering
    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
   
    // Set wrapping mode
    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
   
    // Load the bitmap into the bound texture.
    GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bmp, 0);
   
    // We are done using the bitmap so we should recycle it.
    bmp.recycle();
  }
  
  @Override
  public void onDrawFrame(GL10 unused) {
    
    // Get the current time
      long now = System.currentTimeMillis();
      
      // We should make sure we are valid and sane
      if (lastTime > now) return;
        
      // Get the amount of time the last frame took.
      long elapsed = now - lastTime;
    
    // Update our example
    
    // Render our example
    Render(projectionAndViewMatrix);
    
    // Save the current time to see how long it took :).
        lastTime = now;
    
  }
  
  private void Render(float[] m) {
    
    // clear Screen and Depth Buffer, we have set the clear color as black.
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        
        // get handle to vertex shader's vPosition member
      int mPositionHandle = GLES20.glGetAttribLocation(riGraphicTools.sp_Image, "vPosition");
      
      // Enable generic vertex attribute array
      GLES20.glEnableVertexAttribArray(mPositionHandle);

      // Prepare the triangle coordinate data
      GLES20.glVertexAttribPointer(mPositionHandle, 3,
                                   GLES20.GL_FLOAT, false,
                                   0, vertexBuffer);
      
      // Get handle to texture coordinates location
      int mTexCoordLoc = GLES20.glGetAttribLocation(riGraphicTools.sp_Image, "a_texCoord" );
      
      // Enable generic vertex attribute array
      GLES20.glEnableVertexAttribArray ( mTexCoordLoc );
      
      // Prepare the texturecoordinates
      GLES20.glVertexAttribPointer ( mTexCoordLoc, 2, GLES20.GL_FLOAT,
                false, 
                0, uvBuffer);
      
      // Get handle to shape's transformation matrix
        int mtrxhandle = GLES20.glGetUniformLocation(riGraphicTools.sp_Image, "uMVPMatrix");

        // Apply the projection and view transformation
        GLES20.glUniformMatrix4fv(mtrxhandle, 1, false, m, 0);
        
        // Get handle to textures locations
        int mSamplerLoc = GLES20.glGetUniformLocation (riGraphicTools.sp_Image, "s_texture" );
        
        // Set the sampler texture unit to 0, where we have saved the texture.
        GLES20.glUniform1i ( mSamplerLoc, 0);

        // Draw the triangle
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, indices.length,
                GLES20.GL_UNSIGNED_SHORT, drawListBuffer);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mTexCoordLoc);
          
  }
  

  @Override
  public void onSurfaceChanged(GL10 gl, int width, int height) {
    
    // We need to know the current width and height.
    screenWidth = width;
    screenHeight = height;
    
    // Redo the Viewport, making it fullscreen.
    GLES20.glViewport(0, 0, (int)screenWidth, (int)screenHeight);
    
    // Clear our matrices
      for(int i=0;i<16;i++)
      {
        projectionMatrix[i] = 0.0f;
        viewMatrix[i] = 0.0f;
        projectionAndViewMatrix[i] = 0.0f;
      }
      
      // Setup our screen width and height for normal sprite translation.
      Matrix.orthoM(projectionMatrix, 0, 0f, screenWidth, 0.0f, screenHeight, 0, 50);
      
      // Set the camera position (View matrix)
        Matrix.setLookAtM(viewMatrix, 0, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1.0f, 0.0f);

        // Calculate the projection and view transformation
        Matrix.multiplyMM(projectionAndViewMatrix, 0, projectionMatrix, 0, viewMatrix, 0);

  }

  @Override
  public void onSurfaceCreated(GL10 unused, EGLConfig config) {
    // Create the triangles
    SetupTriangle();
    // Create the image information
    SetupImage();
    
    // Set the clear color to black
    GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1); 

    // Create the shaders, solid color
    int vertexShader = riGraphicTools.loadShader(GLES20.GL_VERTEX_SHADER, riGraphicTools.vs_SolidColor);
    int fragmentShader = riGraphicTools.loadShader(GLES20.GL_FRAGMENT_SHADER, riGraphicTools.fs_SolidColor);

    riGraphicTools.sp_SolidColor = GLES20.glCreateProgram();             // create empty OpenGL ES Program
    GLES20.glAttachShader(riGraphicTools.sp_SolidColor, vertexShader);   // add the vertex shader to program
    GLES20.glAttachShader(riGraphicTools.sp_SolidColor, fragmentShader); // add the fragment shader to program
    GLES20.glLinkProgram(riGraphicTools.sp_SolidColor);                  // creates OpenGL ES program executables
      
    // Create the shaders, images
    vertexShader = riGraphicTools.loadShader(GLES20.GL_VERTEX_SHADER, riGraphicTools.vs_Image);
    fragmentShader = riGraphicTools.loadShader(GLES20.GL_FRAGMENT_SHADER, riGraphicTools.fs_Image);

    riGraphicTools.sp_Image = GLES20.glCreateProgram();             // create empty OpenGL ES Program
    GLES20.glAttachShader(riGraphicTools.sp_Image, vertexShader);   // add the vertex shader to program
    GLES20.glAttachShader(riGraphicTools.sp_Image, fragmentShader); // add the fragment shader to program
    GLES20.glLinkProgram(riGraphicTools.sp_Image);                  // creates OpenGL ES program executables
      
    // Set our shader programm
    GLES20.glUseProgram(riGraphicTools.sp_Image);
  }  
  
  public GLRenderer(Context context)
  {
    this.context = context;
    lastTime = System.currentTimeMillis();
    
    //triangle = new Triangle();
    //square = new Square();
    
    //texture = new GLTexture(context.getAssets(), "cube.png");
    //texture = new GLTexture(context.getResources(), R.drawable.ic_launcher);
  }
  
//  @Override
//  public void onSurfaceCreated(GL10 unused, EGLConfig config) {
//    GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1);
//
//    Resources res = context.getResources();
//    //int solidColorVertexShader = loadShader(GLES20.GL_VERTEX_SHADER, res, R.raw.solidcolor_vs);
//    //int solidColorFragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, res, R.raw.solidcolor_fs);
//    int imageVertexShader = loadShader(GLES20.GL_VERTEX_SHADER, res, R.raw.image_vs);
//    int imageFragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, res, R.raw.image_fs);
//    
//    program = GLES20.glCreateProgram();
//    //GLES20.glAttachShader(program, solidColorVertexShader);
//    //GLES20.glAttachShader(program, solidColorFragmentShader);
//    GLES20.glAttachShader(program, imageVertexShader);
//    GLES20.glAttachShader(program, imageFragmentShader);
//    GLES20.glLinkProgram(program);
//    
//    GLES20.glUseProgram(program);
//  }

//  private void clearMatrix() {
//    // Clear our matrices
//    for(int i = 0; i < 16; i++) {
//      projectionMatrix[i] = 0.0f;
//      viewMatrix[i] = 0.0f;
//      projectionAndViewMatrix[i] = 0.0f;
//    }
//  }
  
//  @Override
//  public void onSurfaceChanged(GL10 unused, int width, int height) {
//    screenWidth = width;
//    screenHeight = height;
//
//    GLES20.glViewport(0, 0, (int)screenWidth, (int)screenHeight);
//    clearMatrix();
//
//    Matrix.orthoM(projectionMatrix, 0, 0f, screenWidth, 0.0f, screenHeight, 0, 50);
//    Matrix.setLookAtM(viewMatrix, 0, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
//    Matrix.multiplyMM(projectionAndViewMatrix, 0, projectionMatrix, 0, viewMatrix, 0);
//  }

//  @Override
//  public void onDrawFrame(GL10 unused) {
//   // Get the current time
//    long now = System.currentTimeMillis();
//    if (lastTime <= now) {
//      long deltaTime = now - lastTime;
//      render(projectionAndViewMatrix, deltaTime);
//      lastTime = now;
//    }
//  }
  
//  protected void render(float[] matrix, long deltaTime) {
//    int handlePosition = GLES20.glGetAttribLocation(program, "vPosition");
//    GLES20.glEnableVertexAttribArray(handlePosition);
//    GLES20.glVertexAttribPointer(handlePosition, 3, GLES20.GL_FLOAT, false, 0, triangle.getVertexBuffer());
//
//    int handleMatrix = GLES20.glGetUniformLocation(program, "uMVPMatrix");
//    GLES20.glUniformMatrix4fv(handleMatrix, 1, false, matrix, 0);
//
//    ShortBuffer drawListBuffer = triangle.getDrawListBuffer();
//    GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawListBuffer.capacity(),
//                          GLES20.GL_UNSIGNED_SHORT, drawListBuffer);
//
//    GLES20.glDisableVertexAttribArray(handlePosition);
    
//    int handlePosition = GLES20.glGetAttribLocation(program, "vPosition");
//    GLES20.glEnableVertexAttribArray(handlePosition);
//    GLES20.glVertexAttribPointer(handlePosition, 3, GLES20.GL_FLOAT, false, 0, square.getVertexBuffer());
//    
//    int handleTexturePosition = GLES20.glGetAttribLocation(program, "a_texCoord");
//    GLES20.glEnableVertexAttribArray(handleTexturePosition);
//    GLES20.glVertexAttribPointer(handlePosition, 2, GLES20.GL_FLOAT, false, 0, texture.getVertexBuffer());
//
//    int handleMatrix = GLES20.glGetUniformLocation(program, "uMVPMatrix");
//    GLES20.glUniformMatrix4fv(handleMatrix, 1, false, matrix, 0);
//
//    int handleTexture = GLES20.glGetUniformLocation(program, "s_texture");
//    GLES20.glUniform1i(handleTexture, 0);
//    
//    ShortBuffer drawListBuffer = square.getDrawListBuffer();
//    GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawListBuffer.capacity(),
//                          GLES20.GL_UNSIGNED_SHORT, drawListBuffer);
//    
//    // Disable vertex array
//    GLES20.glDisableVertexAttribArray(handlePosition);
//    GLES20.glDisableVertexAttribArray(handleTexturePosition);
//  }

//  /**
//   * Loads the specified shader from the raw resource id.
//   * @param type The type of shader to created.
//   * @param res The resources collection.
//   * @param resourceId The id of the resource to retrieve.
//   * @return The id of the compiled shader. If equal 0, then the call failed.
//   */
//  public int loadShader(int type, Resources res, int resourceId) {
//    final InputStream inputStream = context.getResources().openRawResource(resourceId);
//    final InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
//    final BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
//    
//    String nextLine;
//    final StringBuilder body = new StringBuilder();
//    try {
//      while((nextLine = bufferedReader.readLine()) != null) {
//        body.append(nextLine);
//        body.append("\n");
//      }
//      return(loadShader(type, body.toString()));
//    }
//    catch (IOException exception) {
//      Log.e(TAG, String.format("Failed to load shader resource (%d).", resourceId));
//      return(0);
//    }
//  }
  
//  /**
//   * Loads the specified shader from the opengl source code.
//   * @param type The type of shader to created.
//   * @param source The openGL source code to compile.
//   * @return The id of the compiled shader. If equal 0, then the call failed.
//   */
//  public int loadShader(int type, String source) {
//    int shader = GLES20.glCreateShader(type);
//    
//    GLES20.glShaderSource(shader, source);
//    GLES20.glCompileShader(shader);
//    checkGlError("glCompileShader");
//    
//    return(shader);
//  }
//
//  /**
//  * Utility method for debugging OpenGL calls. Provide the name of the call
//  * just after making it:
//  *
//  * If the operation is not successful, the check throws an error.
//  *
//  * @param glOperation - Name of the OpenGL call to check.
//  */
//  public static void checkGlError(String glOperation) {
//    int error;
//    while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
//      String message = String.format("%s: glError %s", glOperation, error); 
//
//      Log.e(TAG, message);
//      throw new RuntimeException(message);
//    }
//  }
  
  @Override
  public void resume() {
    lastTime = System.currentTimeMillis();
  }

  @Override
  public void pause() {
  }
}
