package com.heeresonline.justhoops;

import org.jbox2d.collision.shapes.EdgeShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.World;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class RenderView extends SurfaceView implements Runnable {
  private static final String TAG = "RenderView";
  private static final float PIXELS_TO_METERS = 100f;
  private final static int MAX_FPS = 50; //desired fps   
  private final static int MAX_FRAME_SKIPS = 5; // maximum number of frames to be skipped    
  private final static int FRAME_PERIOD = 1000 / MAX_FPS; // the frame period  

  private Context context;
  private SurfaceHolder holder;
  private Thread thread;
  private volatile boolean isRunning = false;
  
  private PhysicsWorld world;
  private Bitmap bitmap;
  
  private Paint paint;
  private Rect bounds;
  
  public RenderView(Context context) {
    super(context);
    this.context = context;
    
    holder = getHolder();
    paint = new Paint();
    bounds = new Rect();
    
    bitmap = BitmapFactory.decodeResource(getResources(),  R.drawable.ic_launcher);
  }
  
  @Override
  public void run() {
    int sleepTime = 0;
    long startTime = System.nanoTime();

    while (isRunning) {
      if (! holder.getSurface().isValid()) continue;

      int framesSkipped = 0;
      float deltaTime = (System.nanoTime() - startTime) / 1000000000.0f;
      startTime = System.nanoTime();
      Log.d(TAG, String.format("DeltaTime: %f", deltaTime));

      Canvas canvas = holder.lockCanvas();
      if (world == null) world = new PhysicsWorld(canvas.getWidth(), canvas.getHeight());
      update(deltaTime);
      render(canvas, deltaTime);
      holder.unlockCanvasAndPost(canvas);
      
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
        update(sleepTime);
        sleepTime += FRAME_PERIOD;
        framesSkipped++;
      }
    }
  }

  protected void update(float deltaTime) {
    if (world != null) {
      world.step(deltaTime);
    }
  }
  
  protected void render(Canvas canvas, float deltaTime) {
    canvas.drawRGB(0, 0, 0);
    int height = canvas.getHeight();
    int width = canvas.getWidth();
    
    for(Body body : world.getBodies()) {
      Bitmap sprite = bitmap; //(Bitmap) body.getUserData();
      canvas.drawBitmap(sprite, 
          body.getPosition().x * PIXELS_TO_METERS - (sprite.getWidth() / 2), 
          body.getPosition().y * PIXELS_TO_METERS - (sprite.getHeight() / 2), paint);
    }
    
    paint.setColor(Color.WHITE);
    canvas.drawLine(0, height - 50, width, height - 50, paint);
    
    paint.setTextAlign(Align.RIGHT);
    paint.setTextSize(24);
    String statistics = String.format("%3.6f", deltaTime);
    paint.getTextBounds(statistics, 0, statistics.length()-1, bounds);
    canvas.drawText(statistics, width, height - bounds.height(), paint);
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
  
  @Override
  public boolean onTouchEvent(MotionEvent event) {
    if (world != null) {
      world = null;
    }
    return super.onTouchEvent(event);
  }
}
