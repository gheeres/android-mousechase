package com.heeresonline.justhoops.opengl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.FloatBuffer;

import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;

class GLTexture extends GLObject {
  protected float[] uvs = new float[] {
    0.0f, 0.0f,
    0.0f, 1.0f,
    1.0f, 1.0f,
    1.0f, 0.0f
  };
  
  public GLTexture(Resources res, int resourceId) {
    this(BitmapFactory.decodeResource(res, resourceId));
  }

  public GLTexture(AssetManager assets, String filename) {
    InputStream input = null;
    try {
      input = assets.open(filename);
      load(BitmapFactory.decodeStream(input));
    } catch (IOException e) {
       Log.e(GLRenderer.TAG, String.format("Failed to open the requested asset '%s'. %s", filename, e.getMessage()), e);
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
  
  protected GLTexture(Bitmap bitmap) {
    load(bitmap);
  }
  
  protected void load(Bitmap bitmap) {
    int[] texturenames = new int[1];
    GLES20.glGenTextures(1, texturenames, 0);
    
    GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,  texturenames[0]);
    
    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
    
    GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
    bitmap.recycle();
  }

  @Override
  public FloatBuffer getVertexBuffer() {
    return(getFloatBuffer(uvs));
  }
}