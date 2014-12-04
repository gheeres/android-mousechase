package com.heeresonline.mousechase;

import com.heeresonline.mousechase.opengl.GLRenderer;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.MotionEvent;

public class GLRenderView extends GLSurfaceView {
  private static final String TAG = "GLRenderView";
  protected GLRenderer renderer;
  protected World world;
  
  public GLRenderView(Context context) {
    super(context);
    
    Log.d(TAG, "Creating the game world.");
    world = new World(context);

    Log.d(TAG, "Setting OpenGL to version 2.0.");
    setEGLContextClientVersion(2);

    Log.d(TAG, "Creating renderer.");
    renderer = new GLRenderer(context, world);
    setRenderer(renderer);
    
    setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
  }
  
  @Override
  public void onPause() {
    super.onPause();

    Log.v(TAG, "onPause");
    renderer.pause();
    world.pause();
  }

  @Override
  public void onResume() {
    super.onResume();

    Log.v(TAG, "onResume");
    renderer.resume();
    world.resume();
  }

  @Override
  public boolean performClick() {
    return(super.performClick());
  }
  
  @Override
  public boolean onTouchEvent(MotionEvent event) {
    int pointerIndex = event.getActionIndex();
    //int pointerId = event.getPointerId(pointerIndex);
    int action = event.getActionMasked();
    
    switch(action) {
      case MotionEvent.ACTION_DOWN:
      case MotionEvent.ACTION_POINTER_DOWN:
        world.meow();
        world.moveCatTo(event.getX(pointerIndex), renderer.getHeight() - event.getY(pointerIndex));
        break;
      
      case MotionEvent.ACTION_CANCEL:
      case MotionEvent.ACTION_UP:
      case MotionEvent.ACTION_POINTER_UP:
          performClick();
          //world.stopCat();
        break;
      case MotionEvent.ACTION_MOVE:
        for(int index = 0, events = event.getPointerCount(); index < events; index++) {
          world.moveCatTo(event.getX(pointerIndex),  renderer.getHeight() - event.getY(pointerIndex));
        }
        break;
      default:
        return(false);
    }
    return(true);
  }
}
