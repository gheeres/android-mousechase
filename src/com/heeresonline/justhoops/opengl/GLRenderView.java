package com.heeresonline.justhoops.opengl;

import com.heeresonline.justhoops.IView;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.Log;

public class GLRenderView extends GLSurfaceView implements IView {
  private static final String TAG = "GLRenderView";
  protected Renderer renderer;
  
  public GLRenderView(Context context) {
    super(context);
    
    Log.d(TAG, "Setting OpenGL to version 2.0.");
    setEGLContextClientVersion(2);

    Log.d(TAG, "Creating renderer.");
    //renderer = new ri.blog.opengl002.GLRenderer(context);
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
}
