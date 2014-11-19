package com.heeresonline.justhoops;

import java.util.Iterator;
import java.util.List;

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
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class RenderView extends SurfaceView implements Runnable {
  private static final String TAG = "RenderView";
  private static final float WORLD_WIDTH = 10.0f;
  private static final float WORLD_HEIGHT = 10.0f;
  
  private final static int MAX_FPS = 50; //desired fps   
  private final static int MAX_FRAME_SKIPS = 5; // maximum number of frames to be skipped    
  private final static int FRAME_PERIOD = 1000 / MAX_FPS; // the frame period  

  private Context context;
  private SurfaceHolder holder;
  private Thread thread;
  private volatile boolean isRunning = false;
  
  private PhysicsWorld world;
  private Camera camera;
  private float height;
  private float width;
  private Bitmap bitmap;
  
  private Paint paint;
  private Rect bounds;
  
  private float time;
  
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
      //Log.d(TAG, String.format("DeltaTime: %f", deltaTime));

      Canvas canvas = holder.lockCanvas();
      width = canvas.getWidth();
      height = canvas.getHeight();

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
        //Log.d(TAG, String.format("%d / %d frame(s) skipped. Behind by %d msec", 
        //                         framesSkipped+1, MAX_FRAME_SKIPS, sleepTime));
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
    
    Iterable<Body> bodies = getWorld().getBodies();
    synchronized(bodies) {
      for(Body body : bodies) {
        Bitmap sprite = bitmap; //(Bitmap) body.getUserData();
        Vec2 position = camera.worldToScreen(body.getPosition());
        Log.d(TAG, String.format("x: %5.5f, y: %5.4f, angle: %5.4f", 
                                   position.x, height - position.y, body.getAngle() * (180/(float) Math.PI)));
        //Matrix matrix = new Matrix();
        //matrix.setRotate(body.getAngle() * (180/(float) Math.PI));
        canvas.drawBitmap(sprite, position.x, height - position.y, paint);
      }
    }
    
    paint.setColor(Color.WHITE);
    canvas.drawLine(0, height - 50, width, height - 50, paint);
    
    paint.setTextAlign(Align.RIGHT);
    paint.setTextSize(24);
    time = ((time == 0) ? 1 : time) * 0.9f + deltaTime * 0.1f; 
    String statistics = String.format("fps: %5.2f [delta: %3.6f]", 1/time, deltaTime);
    paint.getTextBounds(statistics, 0, statistics.length()-1, bounds);
    canvas.drawText(statistics, width, height - bounds.height(), paint);
    
    paint.setTextAlign(Align.LEFT);
    Iterator<Body> iterator = bodies.iterator();
    if (iterator.hasNext()) {
      Body body = iterator.next();
      if (body != null) {
        Vec2 position = camera.worldToScreen(body.getPosition());
        canvas.drawText(String.format("pos: %5.1f,%5.1f (%5.3f)", position.x, position.y, body.getAngle() * (180/(float) Math.PI)), 
                        0, height - bounds.height(), paint);
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
  
  public PhysicsWorld getWorld() {
    if (world == null) world = new PhysicsWorld(0, 0, WORLD_WIDTH, WORLD_HEIGHT);
    return(world);
  }

  public Camera getCamera() {
    if (camera == null) camera = new Camera(new Vec2(WORLD_WIDTH, WORLD_HEIGHT), new Vec2(width, height));
    return(camera);
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    PhysicsWorld world = getWorld();
    
    int action = event.getAction() & MotionEvent.ACTION_MASK;
    int pointerIndex = (event.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
    int pointerId = event.getPointerId(pointerIndex);
    switch(action) {
      case MotionEvent.ACTION_DOWN:
      case MotionEvent.ACTION_POINTER_DOWN:
        world.addBox(getCamera().screenToWorld(event.getX(pointerIndex), height - event.getY(pointerIndex)), 
                                               getCamera().screenToWorld(bitmap.getWidth(), bitmap.getHeight()));
        //points[pointerId] = new PointF(event.getX(pointerIndex), event.getY(pointerIndex));
        break;
      case MotionEvent.ACTION_CANCEL:
      case MotionEvent.ACTION_UP:
      case MotionEvent.ACTION_POINTER_UP:
        break;
      case MotionEvent.ACTION_MOVE:
        //for(int index = 0, events = event.getPointerCount(); index < events; index++) {
        //  pointerId = event.getPointerId(index);
        //  points[pointerId].x = event.getX(index);
        //  points[pointerId].y = event.getY(index);
        //}
        break;
      
      default:
        return(false);
    }
    return super.onTouchEvent(event);
  }
}
