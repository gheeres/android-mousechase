package com.heeresonline.justhoops.opengl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import android.content.res.Resources;
import android.opengl.GLES20;
import android.util.Log;

public class GLShaderFactory {
  public static final String TAG = "GLShaderFactory";
  
  //public static final Map<String, Integer> shaders = new HashMap<String, Integer>();
  public static final Map<String, Integer> programs = new HashMap<String, Integer>();

  /**
   * Attach and link the shaders to the OpenGL program.
   * @param name The name of the shader.
   * @param shaders The array of shaders to attach and link.
   * @return The id of the OpenGL program.
   */
  public static int addProgram(String name, int[] shaders) {
    // create empty OpenGL ES Program
    int program = GLES20.glCreateProgram();             
    for(int index = 0, length = shaders.length; index < length; index++) {
      // add the vertex shader to program
      GLES20.glAttachShader(program, shaders[index]);
      Log.v(TAG, String.format("Attaching %d/%d shaders %d to program %s (%d).", 
                               index+1, length, shaders[index], name, program));
      checkGlError("glLinkProgram");
    }
    // creates OpenGL ES program executables
    GLES20.glLinkProgram(program);
    checkGlError("glLinkProgram");
    
    programs.put(name, program);
    Log.d(TAG, String.format("Created shader program (%d) as %s.", program, name));
    return(program);
  }
  
  /**
  * Loads the specified shader from the raw resource id.
  * @param type The type of shader to created.
  * @param res The resources collection.
  * @param resourceId The id of the resource to retrieve.
  * @return The id of the compiled shader. If equal 0, then the call failed.
  */
  public static int loadShader(int type, Resources res, int resourceId) {
    final InputStream inputStream = res.openRawResource(resourceId);
    final InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
    final BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
   
    String nextLine;
    final StringBuilder body = new StringBuilder();
    try {
      while((nextLine = bufferedReader.readLine()) != null) {
        body.append(nextLine);
        body.append("\n");
      }
      return(loadShader(type, body.toString()));
    }
    catch (IOException exception) {
      Log.e(TAG, String.format("Failed to load shader resource (%d).", resourceId));
      return(0);
    }
  }

  /**
  * Loads the specified shader from the opengl source code.
  * @param type The type of shader to created.
  * @param source The openGL source code to compile.
  * @return The id of the compiled shader. If equal 0, then the call failed.
  */
  public static int loadShader(int type, String source) {
    int shader = GLES20.glCreateShader(type);
   
    GLES20.glShaderSource(shader, source);
    GLES20.glCompileShader(shader);
    checkGlError("glCompileShader");
   
    return(shader);
  }

  /**
  * Utility method for debugging OpenGL calls. Provide the name of the call
  * just after making it:
  *
  * If the operation is not successful, the check throws an error.
  *
  * @param glOperation - Name of the OpenGL call to check.
  */
  public static void checkGlError(String glOperation) {
    int error;
    while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
      String message = String.format("%s: glError %s", glOperation, error); 
      Log.e(TAG, message);
      throw new RuntimeException(message);
    }
  }
}
