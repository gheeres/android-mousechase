package com.heeresonline.mousechase.opengl;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;

public class GLTextureFactory {
  public static final String TAG = "GLTextureFactory";
  //private static int[] internalTexture = new
  //public static final Map<String, Integer> shaders = new HashMap<String, Integer>();
  public static final Map<String, GLTexture> textures = new HashMap<String, GLTexture>();

  /**
   * Adds a texture with the specified name.
   * @param name The name of the texture
   * @param res The resources manager.
   * @param resourceId The id of the resource to load.
   * @return The OpenGL id of the texture.
   */
  public static int addTexture(String name, Resources res, int resourceId) {
    return(addTexture(name, res, resourceId, GLTexture.defaultUvs));
  }
  
  /**
   * Adds a texture with the specified name.
   * @param name The name of the texture
   * @param res The resources manager.
   * @param resourceId The id of the resource to load.
   * @param uvs The vectors for the texture.
   * @return The OpenGL id of the texture.
   */
  public static int addTexture(String name, Resources res, int resourceId, float[] uvs) {
    return(addTexture(name, BitmapFactory.decodeResource(res, resourceId)));
  }
  
  /**
   * Adds a texture with the specified name.
   * @param name The name of the texture
   * @param assets The asset manager to load from.
   * @param filename The path/name of the file.
   * @param uvs The vectors for the texture.
   * @return The OpenGL id of the texture.
   */
  public static int addTexture(String name, AssetManager assets, String filename) {
    return(addTexture(name, assets, filename, GLTexture.defaultUvs));
  }
  
  /**
   * Adds a texture with the specified name.
   * @param name The name of the texture
   * @param assets The asset manager to load from.
   * @param filename The path/name of the file.
   * @param uvs The vectors for the texture.
   * @return The OpenGL id of the texture.
   */
  public static int addTexture(String name, AssetManager assets, String filename, float[] uvs) {
    InputStream input = null;
    try {
      input = assets.open(filename);
      return(addTexture(name, BitmapFactory.decodeStream(input), uvs));
    } catch (IOException e) {
      Log.e(GLRenderer.TAG, String.format("Failed to open the requested asset '%s'. %s", filename, e.getMessage()), e);
      return(0);
    }
    finally {
      if (input != null) {
        try {
          input.close();
        } catch (IOException e) {
        }
      }
    }
  }

  /**
   * Adds a texture with the specified name.
   * @param name The name of the texture.
   * @param bitmap The bitmap to add.
   * @param uvs The vectors for the texture.
   * @return The OpenGL id of the texture.
   */
  public static int addTexture(String name, Bitmap bitmap) {
    return(addTexture(name, bitmap, GLTexture.defaultUvs));
  }

  /**
   * Adds a texture with the specified name.
   * @param name The name of the texture.
   * @param bitmap The bitmap to add.
   * @param uvs The vectors for the texture.
   * @return The OpenGL id of the texture.
   */
  public static int addTexture(String name, Bitmap bitmap, float[] uvs) {
    int[] texturenames = new int[1];
    GLES20.glGenTextures(1, texturenames, 0);
    Log.d(TAG, String.format("Generating %d textures. Id: %d", texturenames.length, texturenames[0]));
    
    GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texturenames[0]);
    
    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
    
    GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
    bitmap.recycle();

    textures.put(name, new GLTexture(texturenames[0]));
    return(texturenames[0]);
  }
}
