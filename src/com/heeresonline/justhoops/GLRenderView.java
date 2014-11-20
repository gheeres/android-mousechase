package com.heeresonline.justhoops;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Iterator;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import org.jbox2d.collision.shapes.EdgeShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.collision.shapes.ShapeType;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.World;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class GLRenderView extends GLSurfaceView implements IView,Runnable {
  private static final String TAG = "RenderView";

  private final static int MAX_FPS = 50; //desired fps   
  private final static int MAX_FRAME_SKIPS = 5; // maximum number of frames to be skipped    
  private final static int FRAME_PERIOD = 1000 / MAX_FPS; // the frame period  

  private Context context;
  private Thread thread;
  private volatile boolean isRunning = false;
  
  private float time;
  
  public GLRenderView(Context context) {
    super(context);
    this.context = context;
    
    setEGLContextClientVersion(2);
    setPreserveEGLContextOnPause(true);
    //setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    setRenderer(new GLRenderer());
    //setRenderer(new MyGLRenderer());
  }
  
  @Override
  public void run() {
    int sleepTime = 0;
    long startTime = System.nanoTime();

    while (isRunning) {
      int framesSkipped = 0;
      float deltaTime = (System.nanoTime() - startTime) / 1000000000.0f;
      startTime = System.nanoTime();

      // Constant frame rate / speed
      sleepTime = (int) (FRAME_PERIOD - deltaTime);
      if (sleepTime > 0) {
        try {
          //Log.d(TAG, String.format("Nothing to do, sleeping for %d msec", sleepTime));
          Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
        }
      }
      while ((sleepTime < 0) && (framesSkipped < MAX_FRAME_SKIPS)) {
        Log.d(TAG, String.format("%d / %d frame(s) skipped. Behind by %d msec", 
                                 framesSkipped+1, MAX_FRAME_SKIPS, sleepTime));
        sleepTime += FRAME_PERIOD;
        framesSkipped++;
      }
    }
  }

  public void resume() {
    isRunning = true;
    thread = new Thread(this);
    thread.start();
  }
  
  public void pause() {
    isRunning = false;
    while (true) {
      try {
        thread.join();         
      }
      catch(InterruptedException e) {
      }
    }
  }
}
