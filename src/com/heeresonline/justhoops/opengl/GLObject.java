package com.heeresonline.justhoops.opengl;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * A base GLObject class for common functionality.
 */
abstract class GLObject {
  public static final String TAG = "GLObject";
  
  protected ShortBuffer getShortBuffer(short[] array, int position) {
    ByteBuffer byteBuffer = ByteBuffer.allocateDirect(array.length * 2);
    byteBuffer.order(ByteOrder.nativeOrder());
    
    ShortBuffer buffer = byteBuffer.asShortBuffer();
    buffer.put(array);
    buffer.position(position);
    return(buffer);
  }
  
  protected ShortBuffer getShortBuffer(short[] array) {
    return(getShortBuffer(array, 0));
  }

  protected FloatBuffer getFloatBuffer(float[] array, int position) {
    ByteBuffer byteBuffer = ByteBuffer.allocateDirect(array.length * 4);
    byteBuffer.order(ByteOrder.nativeOrder());
    
    FloatBuffer buffer = byteBuffer.asFloatBuffer();
    buffer.put(array);
    buffer.position(position);
    return(buffer);
  }
  
  protected FloatBuffer getFloatBuffer(float[] array) {
    return(getFloatBuffer(array, 0));
  }
}