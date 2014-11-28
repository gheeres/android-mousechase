package com.heeresonline.mousechase.opengl;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * A base GLObject class for common functionality.
 */
abstract class GLObject {
  public static final String TAG = "GLObject";
  
  /**
   * Get's a ShortBuffer from the specified array.
   * @param array The array to convert
   * @param position The initial position of the buffer.
   * @return A ShortBuffer
   */
  protected ShortBuffer getShortBuffer(short[] array, int position) {
    ByteBuffer byteBuffer = ByteBuffer.allocateDirect(array.length * 2);
    byteBuffer.order(ByteOrder.nativeOrder());
    
    ShortBuffer buffer = byteBuffer.asShortBuffer();
    buffer.put(array);
    buffer.position(position);
    return(buffer);
  }
  
  /**
   * Get's a ShortBuffer from the specified array and initialized the
   * position to the first item.
   * @param array The array to convert
   * @return A ShortBuffer
   */
  protected ShortBuffer getShortBuffer(short[] array) {
    return(getShortBuffer(array, 0));
  }

  /**
   * Gets a 3x3 matrix from the 4x4 matrix.
   * @param matrix4 The 4x4 matrix to retrieve.
   * @return A 3x3 matrix.
   */
  protected float[] getMatrix3(float[] matrix4) {
    float[] result = new float[matrix4.length - (matrix4.length / 4)];
    for(int index = 0, length = matrix4.length; index < length; index += 4) {
      int resultIndex = index-(index/4);
      result[resultIndex]   = matrix4[index];
      result[resultIndex+1] = matrix4[index+1];
      result[resultIndex+2] = matrix4[index+2];
    }
    return(result);
  }

  /**
   * Gets a 3x3 matrix from the 2x2 matrix setting the z access.
   * @param matrix2 The 2x2 matrix to use as a source.
   * @return A 3x3 matrix.
   */
  protected float[] getMatrix3(float[] matrix2, float z) {
    float[] result = new float[matrix2.length + (matrix2.length / 2)];
    for(int index = 0, length = matrix2.length; index < length; index+=2) {
      int resultIndex = index + (index / 2);
      result[resultIndex] = matrix2[index];
      result[resultIndex+1] = matrix2[index+1];
      result[resultIndex+2] = z;
    }
    return(result);
  }

  /**
   * Gets a 2x2 matrix from the 3x3 matrix.
   * @param matrix3 The 2x2 matrix to retrieve.
   * @return A 2x2 matrix.
   */
  protected float[] getMatrix2(float[] matrix3) {
    float[] result = new float[matrix3.length - (matrix3.length / 3)];
    for(int index = 0, length = matrix3.length; index < length; index += 3) {
      int resultIndex = index-(index/3);
      result[resultIndex]   = matrix3[index];
      result[resultIndex+1] = matrix3[index+1];
    }
    return(result);
  }

  /**
   * Converts the specified array into a FloatBuffer
   * @param array The array to convert.
   * @param position The position to set the initial buffer.
   * @return A FloatBuffer
   */
  protected FloatBuffer getFloatBuffer(float[] array, int position) {
    ByteBuffer byteBuffer = ByteBuffer.allocateDirect(array.length * 4);
    byteBuffer.order(ByteOrder.nativeOrder());
    
    FloatBuffer buffer = byteBuffer.asFloatBuffer();
    buffer.put(array);
    buffer.position(position);
    return(buffer);
  }
  
  /**
   * Converts the specified array into a FloatBuffer and sets the position 
   * to the start of the buffer.
   * @param array The array to convert.
   * @return A FloatBuffer
   */
  protected FloatBuffer getFloatBuffer(float[] array) {
    return(getFloatBuffer(array, 0));
  }
}