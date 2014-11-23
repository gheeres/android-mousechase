package com.heeresonline.justhoops.opengl;

import java.nio.FloatBuffer;

import android.opengl.GLES20;

class GLTexture extends GLObject {
  private static final String TAG = "GLTexture";

  protected final int id;
  public float[] uvs;
  public static final float[] defaultUvs = new float[] {
    0.0f, 0.0f,
    0.0f, 1.0f,
    1.0f, 1.0f,
    1.0f, 0.0f
  };
  
  /**
   * Creates an instance of the Texture class.
   * @param id The OpenGL id for the texture.
   */
  public GLTexture(int id) {
    this(id, defaultUvs);
  }

  /**
   * Creates an instance of the Texture class.
   * @param id The OpenGL id for the texture.
   * @param uvs The coordinates for the texture.
   */
  public GLTexture(int id, float[] uvs) {
    this.id = id;
    this.uvs = uvs;
  }
  
  public FloatBuffer getUvsBuffer() {
    return(getFloatBuffer(uvs, 0));
  }
}
