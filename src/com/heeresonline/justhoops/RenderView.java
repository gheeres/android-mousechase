package com.heeresonline.justhoops;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

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
import android.os.Environment;
import android.util.Log;
import android.view.View;
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
    
    bitmap = getBitmap("cube.png");
  }
  
  public Bitmap getBitmap(String filename) {
    AssetManager assets = context.getAssets();
    InputStream input = null;
    try {
      input = assets.open(filename);
      return(BitmapFactory.decodeStream(input));
    } catch (IOException e) {
      Log.e(TAG, String.format("Failed to open the requested asset '%s'. %s", filename, e.getMessage()), e);
    }
    finally {
      if (input != null) {
        try {
          input.close();
        } catch (IOException e) {
        }
      }
    }
    return(null);
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
  
  public float rotateX(float x, float y, Vec2 center, float angle) {
    // To origin
    float x1 = x - center.x;
    float y1 = y - center.y;
    return((float) (x1 * Math.cos(angle) - y1 * Math.sin(angle)) + center.x);
  }
  
  public float rotateY(float x, float y, Vec2 center, float angle) {
    // To origin
    float x1 = x - center.x;
    float y1 = y - center.y;
    return((float) (x1 * Math.sin(angle) + y1 * Math.cos(angle)) + center.y);
  }

  protected void render(Canvas canvas, float deltaTime) {
    canvas.drawRGB(0, 0, 0);
    int height = canvas.getHeight();
    int width = canvas.getWidth();
    
    Paint debugPaint = new Paint();
    debugPaint.setColor(Color.RED);
    
    int index = 0;
    Matrix matrix = new Matrix();
    for(Iterator<Body> iterator = getWorld().getBodies().iterator(); iterator.hasNext(); index++) {
      Body body = iterator.next();
      float angle = body.getAngle() * (180/(float) Math.PI);
      Bitmap sprite = bitmap; //(Bitmap) body.getUserData();
      Vec2 position = camera.worldToScreen(body.getPosition());
//position.x += sprite.getWidth()/2;
//position.y += sprite.getHeight()/2;
      Log.v(TAG, String.format("[%d] x: %5.5f, y: %5.4f, angle: %5.4f", 
                               index, position.x, height - position.y, body.getAngle() * (180/(float) Math.PI)));
      matrix.setRotate(angle);
      canvas.drawBitmap(Bitmap.createBitmap(sprite, 0, 0, sprite.getWidth(), sprite.getHeight(), matrix, true), 
                        position.x, height - position.y, paint);

      Shape shape = body.getFixtureList().getShape();
      if (shape.getType() == ShapeType.POLYGON) {
        int count = ((PolygonShape) shape).getVertexCount();
        Vec2[] verticies = ((PolygonShape) shape).getVertices();
        for(int v = 0; v < count; v++) {
          Vec2 current = getCamera().worldToScreen(verticies[v]);
          Vec2 next = getCamera().worldToScreen(verticies[(v == (count - 1)) ? 0 : v + 1]);
//Log.d(TAG, String.format("[%d/%d] %5.2f,%5.2f => %5.2f,%5.2f", index, v, current.x, current.y, next.x, next.y));
          
          float x1 = position.x + current.x;
          float y1 = ((height - position.y) + current.y);
          float x2 = position.x + next.x;
          float y2 = ((height - position.y) + next.y);
          Vec2 center = new Vec2(position.x, (height - position.y));
Log.d(TAG, String.format("[%d/%d] %5.2f,%5.2f %5.2f,%5.2f -> center: %5.2f, %5.2f", index, v, x1, y1, x2, y2, center.x, center.y));
          canvas.drawLine(rotateX(x1, y1, center, body.getAngle()),
                          rotateY(x1, y1, center, body.getAngle()),
                          rotateX(x2, y2, center, body.getAngle()),
                          rotateY(x2, y2, center, body.getAngle()),
                          debugPaint);
          
          
//          Log.d(TAG, String.format("[%d/%d] %5.5f,%5.5f => %5.5f,%5.5f", 
//                                   index, v, 
//                                   position.x + current.x, (height - position.y) + current.y, 
//                                   position.x + next.x, (height - position.y) + next.y));
//          float angle = body.getAngle() * (180/(float) Math.PI);
//          float cos = (float) Math.cos(angle);
//          float sin = (float) Math.sin(angle);
//          canvas.drawLine(((position.x + current.x) * cos) - (((height - position.y) + current.y) * sin), 
//                          ((position.x + current.x) * sin) + (((height - position.y) + current.y) * cos), 
//                          ((position.x + next.x) * cos) - (((height - position.y) + next.y) * sin), 
//                          ((position.x + next.x) * sin) + (((height - position.y) + next.y) * cos), 
//                          debugPaint);
        }
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
    
    // Output debug text for first body
    paint.setTextAlign(Align.LEFT);
    Iterator<Body> iterator = getWorld().getBodies().iterator();
    if (iterator.hasNext()) {
      Body body = null;
      while(iterator.hasNext()) {
        body = iterator.next();
      }
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
    Camera camera = getCamera();

    int pointerIndex = event.getActionIndex();
    int pointerId = event.getPointerId(pointerIndex);
    int action = event.getActionMasked();
    switch(action) {
      case MotionEvent.ACTION_DOWN:
      case MotionEvent.ACTION_POINTER_DOWN:
        Vec2 worldSize = new Vec2(bitmap.getWidth() * camera.scaleScreenToWorld.x,
                                  bitmap.getHeight() * camera.scaleScreenToWorld.y);
        Vec2 worldPosition = camera.screenToWorld(event.getX(pointerIndex), height - event.getY(pointerIndex));
        Log.i(TAG, String.format("Adding box with coordinates: SCREEN(%dx%d @ %5.1f,%5.1f), WORLD(%5.4fx%5.4f @ %5.4f,%5.4f)", 
                                 bitmap.getWidth(), bitmap.getHeight(), event.getX(pointerIndex), event.getY(pointerIndex),
                                 worldSize.x, worldSize.y, worldPosition.x, worldPosition.y));
        world.addBox(worldPosition, worldSize);
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
    return(true);
  }
}
