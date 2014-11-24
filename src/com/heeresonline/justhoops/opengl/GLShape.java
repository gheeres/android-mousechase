package com.heeresonline.justhoops.opengl;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import android.opengl.GLES20;
import android.opengl.Matrix;

/**
 * A base GLObject class for common functionality.
 */
public class GLShape extends GLObject {
  public static final String TAG = "GLShape";
  
  protected float[] vertices;
  protected short[] indices;
  protected GLTexture texture;
  protected int program;
  
  public GLShape(float[] vertices, short[] indices, int program) {
    this.vertices = vertices;
    this.indices = indices;
    this.program = program;
  }

  public GLShape(float[] vertices, short[] indices, int program, GLTexture texture) {
    this(vertices, indices, program);
    this.texture = texture;
  }

  public void draw(float[] matrix) {
    GLES20.glUseProgram(program);
    if (texture != null) drawTexture(matrix);
    else drawSolid(matrix);
  }
  
  protected void drawTexture(float[] matrix) {
    // get handle to vertex shader's vPosition member
    // Enable generic vertex attribute array
    // Prepare the triangle coordinate data
    int vPosition = GLES20.glGetAttribLocation(program, "vPosition");
    GLES20.glEnableVertexAttribArray(vPosition);
    GLES20.glVertexAttribPointer(vPosition, 3, GLES20.GL_FLOAT, false, 0, getVertexBuffer());
      
    // Get handle to texture coordinates location
    // Enable generic vertex attribute array
    // Prepare the texturecoordinates
    int a_texCoord = GLES20.glGetAttribLocation(program, "a_texCoord" );
    GLES20.glEnableVertexAttribArray(a_texCoord);
    GLES20.glVertexAttribPointer(a_texCoord, 2, GLES20.GL_FLOAT, false, 0, texture.getUvsBuffer());

    // Get handle to shape's transformation matrix
    // Apply the projection and view transformation
    int uMVPMatrix = GLES20.glGetUniformLocation(program, "uMVPMatrix");
    GLES20.glUniformMatrix4fv(uMVPMatrix, 1, false, matrix, 0);
        
    // Get handle to textures locations
    // Set the sampler texture unit to 0, where we have saved the texture.
    int s_texture = GLES20.glGetUniformLocation (program, "s_texture");
    GLES20.glUniform1i(s_texture, 0);

    // Draw 
    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture.id);
    GLES20.glDrawElements(GLES20.GL_TRIANGLES, indices.length, GLES20.GL_UNSIGNED_SHORT, getIndexBuffer());

    // Disable vertex array
    GLES20.glDisableVertexAttribArray(vPosition);
    GLES20.glDisableVertexAttribArray(a_texCoord);
  }

  protected void drawSolid(float[] matrix) {
    // get handle to vertex shader's vPosition member
    // Enable generic vertex attribute array
    // Prepare the triangle coordinate data
    int vPosition = GLES20.glGetAttribLocation(program, "vPosition");
    GLES20.glEnableVertexAttribArray(vPosition);
    GLES20.glVertexAttribPointer(vPosition, 3, GLES20.GL_FLOAT, false, 0, getVertexBuffer());

    // Get handle to shape's transformation matrix
    // Apply the projection and view transformation
    int uMVPMatrix = GLES20.glGetUniformLocation(program, "uMVPMatrix");
    GLES20.glUniformMatrix4fv(uMVPMatrix, 1, false, matrix, 0);

    // Draw
    GLES20.glDrawElements(GLES20.GL_TRIANGLES, indices.length, GLES20.GL_UNSIGNED_SHORT, getIndexBuffer());

    // Disable vertex array
    GLES20.glDisableVertexAttribArray(vPosition);
  }

  public FloatBuffer getVertexBuffer() {
    return(getFloatBuffer(vertices));
  }

  public ShortBuffer getIndexBuffer() {
    return(getShortBuffer(indices));
  }
}