package com.heeresonline.justhoops.opengl;

import com.heeresonline.justhoops.IView;

import android.content.Context;
import android.graphics.PointF;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.MotionEvent;

public class GLRenderView extends GLSurfaceView implements IView {
  private static final String TAG = "GLRenderView";
  protected GLRenderer renderer;
  
  public GLRenderView(Context context) {
    super(context);
    
    Log.d(TAG, "Setting OpenGL to version 2.0.");
    setEGLContextClientVersion(2);

    Log.d(TAG, "Creating renderer.");
    renderer = new GLRenderer(context);
    setRenderer(renderer);

    setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
  }
  
  @Override
  public void onPause() {
    super.onPause();
    pause();
  }

  @Override
  public void onResume() {
    super.onResume();
    resume();
  }

  @Override
  public void resume() {
    Log.d(TAG, "Resume");
    ((IView) renderer).resume();
  }

  @Override
  public void pause() {
    Log.d(TAG, "Pause");
    ((IView) renderer).pause();
  }
  
  @Override
  public boolean onTouchEvent(MotionEvent event) {

    int pointerIndex = event.getActionIndex();
    int pointerId = event.getPointerId(pointerIndex);
    int action = event.getActionMasked();
    
    float centerX = renderer.getWidth() / 2;
    float centerY = renderer.getHeight() / 2;
    
    switch(action) {
      case MotionEvent.ACTION_DOWN:
      case MotionEvent.ACTION_POINTER_DOWN:
        // [pointerId] = new PointF(event.getX(pointerIndex), event.getY(pointerIndex));
        if (event.getX(pointerIndex) < centerX) {
          renderer.move(-10, 0);
        }
        else {
          renderer.move(10, 0);
        }
        break;
      
      case MotionEvent.ACTION_CANCEL:
      case MotionEvent.ACTION_UP:
      case MotionEvent.ACTION_POINTER_UP:

        break;
      case MotionEvent.ACTION_MOVE:
//        for(int index = 0, events = event.getPointerCount(); index < events; index++) {
//          pointerId = event.getPointerId(index);
//          points[pointerId].x = event.getX(index);
//          points[pointerId].y = event.getY(index);
//        }
        break;
      default:
        return(false);
    }
    return(true);
  }
}
